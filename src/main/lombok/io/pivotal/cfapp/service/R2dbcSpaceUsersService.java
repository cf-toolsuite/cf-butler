package io.pivotal.cfapp.service;

import static io.pivotal.cfapp.config.PasSettings.SYSTEM_ORG;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pivotal.cfapp.domain.SpaceUsers;
import io.pivotal.cfapp.domain.UserAccounts;
import io.pivotal.cfapp.repository.R2dbcSpaceUsersRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class R2dbcSpaceUsersService implements SpaceUsersService {

    private final R2dbcSpaceUsersRepository repo;
    private final AccountMatcher matcher;

    @Autowired
    public R2dbcSpaceUsersService(
            R2dbcSpaceUsersRepository repo,
            AccountMatcher matcher) {
        this.repo = repo;
        this.matcher = matcher;
    }

    private Map<String, Set<String>> addOrgUsers(Map<String, Set<String>> map, String org, Set<String> usernames) {
        if (map.get(org) != null) {
            Set<String> u = map.get(org);
            u.addAll(usernames);
            map.put(org, u);
        } else {
            map.put(org, usernames);
        }
        return map;
    }

    @Override
    public Mono<Map<String, Integer>> countByOrganization() {
        // a single user may belong to multiple orgs/spaces
        // iterate spaces collecting unique usernames, ignore assigned roles
        final Map<String, Set<String>> usernames = new HashMap<>();
        return repo
                .findAll()
                .map(su -> addOrgUsers(usernames, su.getOrganization(), su.getUsers()))
                .then(Mono.just(usernames))
                .map(this::tallyOrgUsers);

    }

    @Override
    @Transactional
    public Mono<Void> deleteAll() {
        return repo.deleteAll();
    }

    @Override
    public Flux<SpaceUsers> findAll() {
        return repo.findAll();
    }

    @Override
    public Flux<SpaceUsers> findByAccountName(String name) {
        return repo
                .findAll()
                .filter(su -> su.getUsers().contains(name));

    }

    @Override
    public Mono<SpaceUsers> findByOrganizationAndSpace(String organization, String space) {
        return repo.findByOrganizationAndSpace(organization, space);
    }

    @Override
    public Flux<String> obtainAccountNames() {
        return Flux.concat(obtainUserAccountNames(), obtainServiceAccountNames());
    }

    @Override
    public Flux<String> obtainServiceAccountNames() {
        return
                repo
                .findAll()
                .flatMap(su -> Flux.fromIterable(su.getUsers()))
                .collect(Collectors.toCollection(TreeSet::new))
                .flatMapMany(Flux::fromIterable)
                .filter(m -> !matcher.matches(m));
    }

    @Override
    public Flux<String> obtainUserAccountNames() {
        // a single user may belong to multiple orgs/spaces
        // iterate spaces collecting unique usernames
        return
                repo
                .findAll()
                .flatMap(su -> Flux.fromIterable(su.getUsers()))
                .collect(Collectors.toCollection(TreeSet::new))
                .flatMapMany(Flux::fromIterable)
                .filter(m -> matcher.matches(m));
    }

    @Override
    public Flux<UserAccounts> obtainUserAccounts() {
        return
                repo
                .findAll()
                .filter(ua -> !ua.getOrganization().equalsIgnoreCase(SYSTEM_ORG))
                .map(su -> UserAccounts
                        .builder()
                        .organization(su.getOrganization())
                        .space(su.getSpace())
                        .accounts(su.getUsers()
                                .stream()
                                .filter(u -> matcher.matches(u))
                                .collect(Collectors.toSet()))
                        .build());
    }

    @Override
    @Transactional
    public Mono<SpaceUsers> save(SpaceUsers entity) {
        return repo
                .save(entity)
                .onErrorContinue(
                        (ex, data) -> log.error(String.format("Problem saving space user %s.", entity), ex));
    }

    private Map<String, Integer> tallyOrgUsers(Map<String, Set<String>> map) {
        final Map<String, Integer> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k, v.size()));
        return result;
    }

    @Override
    public Mono<Long> totalServiceAccounts() {
        return obtainServiceAccountNames().count();
    }

    @Override
    public Mono<Long> totalUserAccounts() {
        return obtainUserAccountNames().count();
    }

}
