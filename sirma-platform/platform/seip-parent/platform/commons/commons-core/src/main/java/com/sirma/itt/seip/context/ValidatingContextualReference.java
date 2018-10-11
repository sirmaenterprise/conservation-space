/**
 *
 */
package com.sirma.itt.seip.context;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.enterprise.inject.Vetoed;

/**
 * Defines a contextual references that can validate the initial values via provided validator.
 *
 * @author BBonev
 * @param <T>
 *            the generic type
 */
@Vetoed
public class ValidatingContextualReference<T> extends ContextualReference<T> {

	private final Consumer<T> validator;

	/**
	 * Instantiates a new validating contextual reference with a single context and non null validation
	 */
	public ValidatingContextualReference() {
		this(CONTEXT_ID_SUPPLIER, Objects::requireNonNull);
	}

	/**
	 * Instantiates a new validating contextual reference.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param validator
	 *            the validator
	 */
	public ValidatingContextualReference(Supplier<String> contextIdSupplier, Consumer<T> validator) {
		super(contextIdSupplier);
		this.validator = validator;
	}

	/**
	 * Instantiates a new validating contextual reference.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param initialValue
	 *            the initial value
	 * @param validator
	 *            the validator
	 */
	public ValidatingContextualReference(Supplier<String> contextIdSupplier, Supplier<T> initialValue,
			Consumer<T> validator) {
		super(contextIdSupplier, initialValue);
		this.validator = validator;
	}

	@Override
	protected void validateInitialValue(T initialValue) {
		if (validator != null) {
			validator.accept(initialValue);
		}
	}

}
