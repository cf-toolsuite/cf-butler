package io.pivotal.cfapp.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions.StoreConversions;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;

import io.pivotal.cfapp.domain.AppDetailReadConverter;
import io.pivotal.cfapp.domain.AppDetailWriteConverter;
import io.pivotal.cfapp.domain.ServiceInstanceDetailReadConverter;
import io.pivotal.cfapp.domain.ServiceInstanceDetailWriteConverter;
import io.pivotal.cfapp.domain.SpaceUsersReadConverter;
import io.pivotal.cfapp.domain.SpaceUsersWriteConverter;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
public class R2dbcConfig {

	
	@Bean
	public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory factory	) {
		return new R2dbcCustomConversions(getStoreConversions(factory), getCustomConverters());
	}

	protected List<Object> getCustomConverters() {
		List<Object> converterList = new ArrayList<>();
	    converterList.add(new AppDetailReadConverter());
	    converterList.add(new AppDetailWriteConverter());
	    converterList.add(new ServiceInstanceDetailReadConverter());
	    converterList.add(new ServiceInstanceDetailWriteConverter());
	    converterList.add(new SpaceUsersReadConverter());
	    converterList.add(new SpaceUsersWriteConverter());
	    return converterList;
	}

	protected StoreConversions getStoreConversions(ConnectionFactory factory) {

		R2dbcDialect dialect = getDialect(factory);

		List<Object> converters = new ArrayList<>(dialect.getConverters());
		converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS);

		return StoreConversions.of(dialect.getSimpleTypeHolder(), converters);
	}
	
	protected R2dbcDialect getDialect(ConnectionFactory connectionFactory) {
		return DialectResolver.getDialect(connectionFactory);
	}
}
