package com.sirma.itt.seip.search;

import java.io.Serializable;

/**
 * Represents a name value pair for a single value in a {@link ResultItem}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/06/2017
 */
public interface ResultValue extends Serializable {

	/**
	 * The result value name.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * The actual value represented by the current instance
	 *
	 * @return the value, never {@code null}
	 */
	Serializable getValue();

	/**
	 * Creates a simple {@link ResultValue} instance
	 *
	 * @param name the value name
	 * @param value the actual value
	 * @return new result value instance
	 */
	static ResultValue create(String name, Serializable value) {
		return new SimpleResultValue(name, value);
	}

	/**
	 * Simple result value implementation. The implementation provides a {@link #equals(Object)} and
	 * {@link #hashCode()} implementations that take into account the name and the value.
	 */
	class SimpleResultValue implements ResultValue {

		private final String name;
		private final Serializable value;

		/**
		 * Instantiate new result value instance
		 *
		 * @param name thr value name
		 * @param value the actual value
		 */
		public SimpleResultValue(String name, Serializable value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Serializable getValue() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof ResultValue)) {
				return false;
			}

			ResultValue that = (ResultValue) o;

			if (name != null ? !name.equals(that.getName()) : that.getName() != null) {
				return false;
			}
			return value != null ? value.equals(that.getValue()) : that.getValue() == null;
		}

		@Override
		public int hashCode() {
			int result = name != null ? name.hashCode() : 0;
			result = 31 * result + (value != null ? value.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return new StringBuilder(60).append("{").append(name).append('=').append(value).append('}').toString();
		}
	}
}
