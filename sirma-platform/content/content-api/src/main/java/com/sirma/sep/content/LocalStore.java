/**
 *
 */
package com.sirma.sep.content;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier for local content management. {@link ContentStore} annotated with this annotation will be considered for
 * the default local store and should be returned by {@link ContentStoreProvider#getLocalStore()}
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
public @interface LocalStore {
	/**
	 * The name of the store that should be returned by the implementation that is annotated by the current annotation.
	 */
	String NAME = "localStore";
}
