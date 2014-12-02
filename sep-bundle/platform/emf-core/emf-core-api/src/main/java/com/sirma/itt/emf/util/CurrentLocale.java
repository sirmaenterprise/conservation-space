package com.sirma.itt.emf.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Used to qualify the current locale.
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
public @interface CurrentLocale {

}
