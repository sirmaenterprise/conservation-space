package com.sirma.itt.seip.configuration;

/**
 * The listener interface for receiving configurationChange events. The class that is interested in processing a
 * configurationChange event implements this interface, and the object created with that class is registered with a
 * component using the component's <code>addConfigurationChangeListener</code> method. When the configurationChange
 * event occurs, that object's appropriate method is invoked.
 *
 * @author BBonev
 * @param <T>
 *            the generic type
 */
@FunctionalInterface
public interface ConfigurationChangeListener<T> {

	/**
	 * On configuration change.
	 *
	 * @param changedProperty
	 *            the changed property
	 */
	void onConfigurationChange(ConfigurationProperty<T> changedProperty);
}
