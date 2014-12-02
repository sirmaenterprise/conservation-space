package com.sirma.itt.emf.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Annotation marker to disable a bean but not actually disable it.
 * <p>
 * <b>NOTE:</b> If the annotation is used for bean injection then the converter
 * injected will be empty and will not be able to handle the requests.
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
public @interface EmptyConverter {

}
