package com.sirma.itt.seip.convert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Marker interface to allow injecting {@link TypeConverter} implementation that is serializable into web pages.
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
public @interface SerializableConverter {

}
