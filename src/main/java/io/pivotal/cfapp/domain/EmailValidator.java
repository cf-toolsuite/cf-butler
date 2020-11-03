package io.pivotal.cfapp.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class EmailValidator {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*" +  // local-part
                    "@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";	// domain part

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValid(String email) {

		if (StringUtils.isBlank(email)) {
			return false;
		}

		Matcher matcher = EMAIL_PATTERN.matcher(email);
		return matcher.matches();
	}

}
