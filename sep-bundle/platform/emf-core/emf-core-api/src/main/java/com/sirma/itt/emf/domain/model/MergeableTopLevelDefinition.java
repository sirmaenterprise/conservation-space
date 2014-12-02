package com.sirma.itt.emf.domain.model;


/**
 * Interface marker for both {@link TopLevelDefinition} and {@link Mergeable}
 *
 * @param <E>
 *            the element type
 * @author BBonev
 */
public interface MergeableTopLevelDefinition<E extends MergeableTopLevelDefinition<?>> extends
		TopLevelDefinition, Mergeable<E> {
}
