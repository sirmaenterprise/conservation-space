package com.sirma.itt.seip.permissions.config;

import java.io.Serializable;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Interface that will be used to get different permission configurations that are available.
 * 
 * 
 * @author siliev
 *
 */
public interface PermissionsConfiguration extends Serializable {

	/**
	 * Returns the set configuration.
	 * 
	 * @return the set configuration.
	 */
	 ConfigurationProperty<String> getDynamicPermissionThresholdAction();
}
