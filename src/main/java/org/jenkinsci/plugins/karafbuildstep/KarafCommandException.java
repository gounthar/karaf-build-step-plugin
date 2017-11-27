/*
 * Copyright 2017 by Brisa Inovação e Tecnologia S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Brisa, SA ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Brisa.
 */
package org.jenkinsci.plugins.karafbuildstep;


/**
 * KarafCommandException
 *
 */
public class KarafCommandException extends Exception {

    private static final long serialVersionUID = 1L;

    public KarafCommandException() {
        // Do nothing
    }

    public KarafCommandException(String message) {
        super(message);
    }

    public KarafCommandException(Throwable ex) {
        super(ex);
    }
}
