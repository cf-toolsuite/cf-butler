package io.pivotal.cfapp.domain;

import com.sanctionco.jmail.JMail;

public class EmailValidator {

    public static boolean isValid(String email) {
	return JMail.isValid(email);
    }
}
