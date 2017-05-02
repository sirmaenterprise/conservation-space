package com.sirma.itt.cmf.cache;

import com.sirma.itt.seip.annotation.Documentation;

/**
 * Initialize the Infinispan configuration.
 *
 * @author BBonev
 */
@Documentation("Infinispan specific configuration")
public interface InfinispanConfig {

	/** The cache infinispan jndi. */
	@Documentation("Infinispan cache configuration. Defines the base JNDI suffix name under which all other caches are searched. If more then one is defined separate them with comma (,). Cache configurations will be searched in order of definition as JNDI resource like java:jboss/infinispan/<first_value>. <b>Default value is : cmf</b>")
	String CACHE_INFINISPAN_JNDI = "cache.infinispan.jndi";

	/** The cache infinispan default cache. */
	@Documentation("Infinispan cache configuration. The name of the default cache region. The name of the default cache configuration. The cache is used when the searched any of the searched caches is not found then this cache instance will be returned. This is the minimum infinispan cache configuration. <b>Default value is : DEFAULT_CACHE_REGION</b>")
	String CACHE_INFINISPAN_DEFAULT_CACHE = "cache.infinispan.defaultCache";

}
