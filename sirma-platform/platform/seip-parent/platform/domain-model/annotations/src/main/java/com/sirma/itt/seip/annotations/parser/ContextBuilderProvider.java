package com.sirma.itt.seip.annotations.parser;

import java.util.Map;

/**
 * Context extension provider interface. Context providers have to implement this interface in order to be instantiated
 * at platform startup deploy phase.
 *
 * @see {@link com.sirma.itt.seip.annotations.parser.CDIContextBuilderExtension}
 * @author tdossev
 */
@FunctionalInterface
public interface ContextBuilderProvider {

	/**
	 * Context getter method to provide context data used in context initialization. 
	 * Example for namespace:
	 *
	 * <pre>
	 *"emf": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#"
	 * </pre>
	 *
	 * @param <K>
	 *            namespace prefix
	 * @param <V>
	 *            actual namespace
	 * @return specific context map
	 */
	<K, V> Map<K, V> getSpecificContext();
}
