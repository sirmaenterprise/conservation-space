package com.sirma.itt.seip.search;

import java.io.Serializable;

/**
 * Represents a transforming function that accepts a single {@link ResultItem} and other desired type. Used for
 * mapping the raw search results to actual desired data.
 *
 * @param <R> the transformer result type
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/06/2017
 */
@FunctionalInterface
public interface ResultItemTransformer<R> {

	/**
	 * Transform the given {@link ResultItem} to the desired output
	 *
	 * @param resultItem the item to process
	 * @return the transformer result
	 */
	R transform(ResultItem resultItem);

	/**
	 * Returns a result transformer that does not perform any transforming by rather provide pass through functionality
	 *
	 * @return pass through transformer instance
	 */
	static ResultItemTransformer<ResultItem> asIs() {
		return NoResultTransformer.INSTANCE;
	}

	/**
	 * Returns a result transformer that returns a single property identified by the given projection property name.
	 * The value will be fetched by calling the {@link ResultItem#getResultValue(String)} method with the given
	 * property name.
	 *
	 * @param valueName the projection property name to return.
	 * @return a result item to single value transformer
	 */
	static ResultItemTransformer<Serializable> asSingleValue(String valueName) {
		return new SingleValueTransformer(valueName);
	}

	/**
	 * Result transformer instance that return the argument as output
	 *
	 * @author BBonev
	 */
	class NoResultTransformer implements ResultItemTransformer<ResultItem> {
		public static final ResultItemTransformer<ResultItem> INSTANCE = new NoResultTransformer();

		@Override
		public ResultItem transform(ResultItem resultItem) {
			return resultItem;
		}
	}

	/**
	 * Result transformer that returns a single property
	 *
	 * @author BBonev
	 */
	class SingleValueTransformer implements ResultItemTransformer<Serializable> {

		private final String valueName;

		/**
		 * Instantiate transformer
		 *
		 * @param valueName the property name to return
		 */
		public SingleValueTransformer(String valueName) {
			this.valueName = valueName;
		}

		@Override
		public Serializable transform(ResultItem resultItem) {
			return resultItem.getResultValue(valueName);
		}
	}
}
