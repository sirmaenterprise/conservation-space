package com.sirma.itt.seip.db.discovery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines one or more persistence unit associations of particular entity. The annotated entity will be bound to all
 * described persistence units. If such persistence unit is deployed then the class will be able to access the database
 * described in it.
 * This annotation should be placed on:<ul>
 * <li>Entity class annotated with {@link javax.persistence.Entity}</li>
 * <li>Entity class annotated with {@link javax.persistence.MappedSuperclass}</li>
 * <li>Entity class annotated with {@link javax.persistence.Embeddable}</li>
 * </ul>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/10/2017
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistenceUnitBinding {

	/**
	 * List one or more persistence unit names where the annotated class should be bound to.
	 *
	 * @return persistence unit names.
	 */
	String[] value();
}
