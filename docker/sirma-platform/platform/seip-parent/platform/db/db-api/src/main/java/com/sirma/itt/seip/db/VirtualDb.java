package com.sirma.itt.seip.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier used to annotate services that use a virtual in memory database operations. This could be used for some
 * integration that does not require actual database integrations. When using {@link VirtualDb} annotation the user
 * could select that the virutal db is a secondary database to some other primary database. This could be specified by
 * value parameter of the annotation.
 *
 * @author BBonev
 * @see DbType
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface VirtualDb {

	/**
	 * The type of other {@link DbDao} implementation that should be combined with the virtual db. The virtual db will
	 * be secondary db for the injected {@link DbDao}
	 * 
	 * @return the dao type
	 */
	DbType value() default DbType.NONE;

	/**
	 * Database type to inject along with the {@link VirtualDb}.
	 *
	 * @author BBonev
	 */
	enum DbType {

		/** A {@link DbDao} implementation that corresponds to {@link RelationalDb} */
		RELATIONAL,
		/** A {@link DbDao} implementation that corresponds to {@link SemanticDb} */
		SEMANTIC,
		/**
		 * No other {@link DbDao} implementation will be added. The default value. This relates directly to
		 * the {@link VirtualDb} implementation
		 */
		NONE;
	}
}
