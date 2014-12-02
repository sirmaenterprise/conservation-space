/**
 *
 */
package com.sirma.itt.emf.configuration;

import java.util.Properties;

/**
 * The Interface ConfigProvider should be implemented by all classes providing system properties.
 * Depending on order properties from all implementing classes are joined. The higher order means
 * override of property already contained.
 * 
 * @author bbanchev
 */
public interface ConfigProvider {

	/**
	 * Gets the properties for current provider.
	 * 
	 * @return the properties
	 */
	Properties getProperties();

	/**
	 * Gets the order.
	 * 
	 * @return the order
	 */
	Integer getOrder();
}
