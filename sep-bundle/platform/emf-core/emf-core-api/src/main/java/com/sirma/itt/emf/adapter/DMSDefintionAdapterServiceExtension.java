package com.sirma.itt.emf.adapter;

import com.sirma.itt.emf.plugin.SupportablePlugin;
import com.sirma.itt.emf.util.Documentation;

/**
 * Extension for the {@link DMSDefintionAdapterService} that provides means for loading different
 * definition types via that service.
 * 
 * @author BBonev
 */
@Documentation("Extension for the {@link DMSDefintionAdapterService} that provides means for loading different definition types via that service.")
public interface DMSDefintionAdapterServiceExtension extends SupportablePlugin {

	/** The target name. */
	String TARGET_NAME = "dMSDefintionAdapterServiceExtension";

	/**
	 * Gets the search path that can be found the needed definitions.
	 * 
	 * @param definitionClass
	 *            the definition class
	 * @return the search path
	 */
	String getSearchPath(Class<?> definitionClass);
}
