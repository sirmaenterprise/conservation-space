package com.sirma.itt.seip.eai.model.error;

import static com.sirma.itt.seip.eai.service.model.EAIBaseConstants.NEW_LINE;

import java.util.Objects;

/**
 * The ErrorBuilderProvider facilities building a composite errors.
 *
 * @author bbanchev
 */
public class ErrorBuilderProvider {

	private StringBuilder builder = null;

	/**
	 * Gets the current builder. Might instantiate a new with default capacity of 16 characters
	 *
	 * @return the default wrapped {@link StringBuilder}
	 */
	public synchronized StringBuilder get() {
		return getOrCreate(getInitialLength());
	}

	/**
	 * Gets the initial error builder size
	 * 
	 * @return 16 as default
	 */
	@SuppressWarnings("static-method")
	protected int getInitialLength() {
		return 16;// NOSONAR
	}

	/**
	 * Gets the builder and if not yet initialized initialize it the provided capacity.
	 *
	 * @param capacity
	 *            the capacity
	 * @return wrapped {@link StringBuilder} with custom capacity
	 */
	public synchronized StringBuilder get(int capacity) {
		return getOrCreate(capacity);
	}

	private StringBuilder getOrCreate(int capacity) {
		if (builder == null) {
			builder = new StringBuilder(capacity);
		}
		return builder;
	}

	/**
	 * Checks if there is any recorded message.
	 *
	 * @return true, if there is
	 */
	public synchronized boolean hasErrors() {
		return builder != null && builder.length() > 0;
	}

	/**
	 * Append a new message to the builder
	 *
	 * @param data
	 *            the data to append
	 * @return the error builder as chain
	 */
	public synchronized ErrorBuilderProvider append(Object data) {
		get().append(data);
		return this;
	}

	/**
	 * Append a new line to the message
	 *
	 * @return the error builder as chain
	 */
	public synchronized ErrorBuilderProvider separator() {
		if (hasErrors()) {
			return append(NEW_LINE);
		}
		return this;
	}

	/**
	 * Builds the composite error from all appended messages
	 * 
	 * @return the builder as string
	 */
	public synchronized String build() {
		return Objects.toString(builder, "");
	}

	@Override
	public String toString() {
		return build();
	}

}
