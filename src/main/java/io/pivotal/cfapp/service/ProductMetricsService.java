package io.pivotal.cfapp.service;

import static org.mockito.ArgumentMatchers.anyIterableOf;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import io.pivotal.cfapp.client.OpsmanClient;
import io.pivotal.cfapp.domain.product.PivnetCache;
import io.pivotal.cfapp.domain.product.ProductMetric;
import io.pivotal.cfapp.domain.product.ProductMetrics;
import io.pivotal.cfapp.domain.product.ProductType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@ConditionalOnExpression(
    "${om.enabled:false} and ${pivnet.enabled:false}"
)
public class ProductMetricsService {

    private static final String[] PRODUCT_EXCLUSIONS = { "p-bosh" };

    private final PivnetCache pivnetCache;
    private final OpsmanClient opsmanClient;

    @Autowired
    public ProductMetricsService(
        PivnetCache pivnetCache,
        OpsmanClient opsmanClient
    ) {
        this.pivnetCache = pivnetCache;
        this.opsmanClient = opsmanClient;
    }

    public Mono<ProductMetrics> getProductMetrics() {
        return opsmanClient
                .getDeployedProducts()
                .flatMapMany(deployedProducts -> Flux.fromIterable(deployedProducts))
                .map(deployedProduct ->
                    ProductMetric
                        .builder()
                        .name(refineType(deployedProduct.getType()))
                        .currentlyInstalledVersion(deployedProduct.getProductVersion())
                        .currentlyInstalledReleaseDate(
                            pivnetCache.findProductReleaseBySlugAndVersion(
                                refineType(deployedProduct.getType()), deployedProduct.getProductVersion()
                            )
                            .getReleaseDate()
                        )
                        .latestAvailableVersion(
                            pivnetCache.findLatestProductReleaseBySlug(
                                refineType(deployedProduct.getType())
                            )
                            .getVersion()
                        )
                        .latestAvailableReleaseDate(
                            pivnetCache.findLatestProductReleaseBySlug(
                                refineType(deployedProduct.getType())
                            )
                            .getReleaseDate()
                        )
                        .type(ProductType.from(refineType(deployedProduct.getType())))
                        .endOfSupportDate(
                            pivnetCache.findProductReleaseBySlugAndVersion(
                                refineType(deployedProduct.getType()), deployedProduct.getProductVersion()
                            )
                            .getEndOfSupportDate()
                        )
                        .build()
                )
                .filter(productExclusions())
                .collect(Collectors.toSet())
                .map(metrics ->
                    ProductMetrics
                        .builder()
                        .productMetrics(metrics)
                        .build()
                );
    }

    private static String refineType(String value) {
        String normalizedValue = value.replaceAll("_", "-");
        if (normalizedValue.startsWith("apm")) {
            return "apm";
        } else if (normalizedValue.startsWith("cf")) {
            return "elastic-runtime";
        }
        return normalizedValue;
    }

    private static Predicate<ProductMetric> productExclusions() {
        return productMetric -> !Arrays.asList(PRODUCT_EXCLUSIONS).contains(productMetric.getName());
    }

}