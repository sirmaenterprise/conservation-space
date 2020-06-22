package com.sirma.itt.seip.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * Specifies that a method is capable of converting/producing configuration values. The annotated method should have at
 * least one argument of type {@link ConverterContext} or {@link GroupConverterContext} and non void return. If the
 * method have any other arguments than these arguments will be injected if possible. If not an exception will be thrown
 * and the conversion will fail.
 * <p>
 * The produced type will be inferred from the return method value type definition not from the actual return type.
 * <p>
 * If the annotation is placed on a non static method that method should be in a valid CDI bean.
 * <p>
 * <b>Note:</b> If the converter is defined in the same class that injects the value produced by the converter the
 * method should be <code>static</code>!
 *
 * @author BBonev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface ConfigurationConverter {

	/**
	 * Filter configuration name. If specified the converter will be called when converting this exact configuration.
	 * 
	 * @return configuration name
	 */
	String value() default "";
}
