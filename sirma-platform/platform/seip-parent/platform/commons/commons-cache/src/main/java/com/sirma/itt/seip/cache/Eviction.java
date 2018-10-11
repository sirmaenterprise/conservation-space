package com.sirma.itt.seip.cache;

import com.sirma.itt.seip.annotation.Documentation;

/**
 * The cache eviction configuration
 *
 * @author BBonev
 */
public @interface Eviction {

	/**
	 * Sets the cache eviction strategy. Available options are 'UNORDERED', 'FIFO', 'LRU', 'LIRS' and 'NONE' (to disable
	 * eviction).
	 * 
	 * @return the eviction strategy name
	 * @see EvictionStrategy
	 */
	@Documentation("Sets the cache eviction strategy. Available options are 'UNORDERED', 'FIFO', 'LRU', 'LIRS' "
			+ "and 'NONE' (to disable eviction).")
	String strategy() default EvictionStrategy.NONE;

	/**
	 * Maximum number of entries in a cache instance. If selected value is not a power of two the actual value will
	 * default to the least power of two larger than selected value. -1 means no limit.
	 * 
	 * @return the max retries
	 */
	@Documentation("Maximum number of entries in a cache instance. "
			+ "If selected value is not a power of two the actual value will default to the least power of two "
			+ "larger than selected value. -1 means no limit.")
	int maxEntries() default 10000;

	/**
	 * Defines the supported eviction strategies
	 *
	 * @author BBonev
	 */
	class EvictionStrategy {
		public static final String UNORDERED = "UNORDERED";
		public static final String FIFO = "FIFO";
		public static final String LRU = "LRU";
		public static final String LIRS = "LIRS";
		public static final String NONE = "NONE";

		private EvictionStrategy() {
			// utility class
		}
	}
}
