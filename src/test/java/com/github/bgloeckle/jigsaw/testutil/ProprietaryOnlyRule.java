/**
 * jigsaw: Solve image jigsaws.
 *
 * Copyright (C) 2016, 2017 Bastian Gloeckle
 *
 * This file is part of jigsaw.
 *
 * diqube is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.bgloeckle.jigsaw.testutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MethodRule} to only execute a test method if {@link #EXECUTE_PROPRIETARY_SYSTEM_PROPERTY} system property is
 * specified.
 *
 * @author Bastian Gloeckle
 */
public class ProprietaryOnlyRule implements MethodRule {
    private static final Logger logger = LoggerFactory.getLogger(ProprietaryOnlyRule.class);

    public static final String EXECUTE_PROPRIETARY_SYSTEM_PROPERTY = "executeProprietaryTests";
    private static final boolean executeTests;

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        if (method.getAnnotation(ProprietaryOnly.class) == null || executeTests) {
            return base;
        }

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                logger.warn("Ignoring execution of the following test, since '{}' system property was not specified: {}",
                                EXECUTE_PROPRIETARY_SYSTEM_PROPERTY, method);
                throw new AssumptionViolatedException("Test ignored, since '" + EXECUTE_PROPRIETARY_SYSTEM_PROPERTY
                                + "' system property was not set.");
            }
        };
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ProprietaryOnly {

    }

    static {
        executeTests = System.getProperty(EXECUTE_PROPRIETARY_SYSTEM_PROPERTY) != null;
    }
}
