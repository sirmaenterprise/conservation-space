package com.sirma.itt.semantic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Can be used to annotate a injected {@link org.eclipse.rdf4j.repository.RepositoryConnection} to specify that the
 * given connection will only perform read operations and will never do any modifications to the underlining repository.
 *
 * This may act as performance improvement for cases where transactions are not needed.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/03/2018
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface ReadOnly {
	// not needed
}
