package com.sirma.itt.emf.configuration;

/**
 * Interface that represents a single updateable configuration property. The configuration value
 * will be automatically updated if value has been changed. Also the property user could trigger a
 * manual property value change check.
 * 
 * @param <E>
 *            the configuration property type
 * @author BBonev
 */
public interface ConfigurationProperty<E> {

	/**
	 * Gets the configuration value in the proper type. The method should return <code>null</code>
	 * if:
	 * <ul>
	 * <li>the method {@link #isNull()} returns <code>true</code>
	 * <li>the method {@link #isConfigurationSet()} returns <code>true</code> and default value has
	 * not been set. {@link #getDefaultValue()} will also returns <code>null</code>.
	 * <li>after refresh (manual or automatic) the configuration has been removed and default value
	 * has not been set.
	 * </ul>
	 * The method will return the default value, if any, if the method {@link #isConfigurationSet()}
	 * returns <code>false</code>.
	 * 
	 * @return the value
	 */
	E getValue();

	/**
	 * Gets the default value if any
	 * 
	 * @return the default value
	 */
	E getDefaultValue();

	/**
	 * Checks if the value is <code>null</code>
	 * 
	 * @return true, if is <code>null</code>
	 */
	boolean isNull();

	/**
	 * Checks if is configuration has been set via any means.
	 * 
	 * @return <code>true</code>, if the configuration has been set.
	 */
	boolean isConfigurationSet();

	/**
	 * Triggers a manual property refresh. A call to this method should not trigger full application
	 * property update.
	 * 
	 * @return <code>true</code>, if the value of the property has been changed during the refresh.
	 */
	boolean refresh();

	/**
	 * Gets the value type class
	 * 
	 * @return the value class
	 */
	Class<E> getValueClass();

	/**
	 * Gets the key that is represented by the current configuration property.
	 * 
	 * @return the key
	 */
	String getKey();

	/**
	 * Gets the configuration factory.
	 * 
	 * @return the configuration factory
	 */
	SystemConfiguration getConfigurationFactory();
}
