package io.pivotal.cfapp.domain;

import org.apache.commons.lang3.StringUtils;

import com.sanctionco.jmail.JMail;

public class EmailValidator {

    public static boolean isValid(String email) {

		if (StringUtils.isBlank(email)) {
			return false;
		}
		return JMail.isValid(email);
	}

}
