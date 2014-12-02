package com.sirma.itt.emf.cache;

import com.sirma.itt.emf.util.Documentation;

/**
 * The cache eviction configuration
 *
 * @author BBonev
 */
public @interface Eviction {

	/**
	 * Sets the cache eviction strategy. Available options are 'UNORDERED', 'FIFO', 'LRU', 'LIRS'
	 * and 'NONE' (to disable eviction).
	 */
	@Documentation("Sets the cache eviction strategy. Available options are 'UNORDERED', 'FIFO', 'LRU', 'LIRS' "
			+ "and 'NONE' (to disable eviction).")
	String strategy() default "NONE";

	/**
	 * Maximum number of entries in a cache instance. If selected value is not a power of two the
	 * actual value will default to the least power of two larger than selected value. -1 means no
	 * limit.
	 */
	@Documentation("Maximum number of entries in a cache instance. "
			+ "If selected value is not a power of two the actual value will default to the least power of two "
			+ "larger than selected value. -1 means no limit.")
	int maxEntries() default 10000;
}
