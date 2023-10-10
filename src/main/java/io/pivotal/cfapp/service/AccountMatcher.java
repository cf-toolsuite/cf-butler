package io.pivotal.cfapp.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.pivotal.cfapp.config.PasSettings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccountMatcher {

    private final Pattern pattern;
    private final PasSettings settings;

    @Autowired
    public AccountMatcher(PasSettings settings) {
        this.settings = settings;
        this.pattern = Pattern.compile(settings.getAccountRegex());
    }

    public boolean matches(final String candidate) {
        Matcher matcher = pattern.matcher(candidate);
        boolean result = matcher.matches();
        log.trace("Does account {} match account regex pattern {}? {}", candidate, settings.getAccountRegex(), String.valueOf(result));
        return result;
    }
}
