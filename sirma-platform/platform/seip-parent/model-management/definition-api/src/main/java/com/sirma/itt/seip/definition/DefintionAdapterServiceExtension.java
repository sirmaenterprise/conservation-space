package com.sirma.itt.seip.definition;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * Extension for the {@link DefintionAdapterService} that provides means for loading different definition types via
 * that service.
 *
 * @author BBonev
 */
@Documentation("Extension for the {@link DefintionAdapterService} that provides means for loading different definition types via that service.")
public interface DefintionAdapterServiceExtension extends SupportablePlugin<Class> {

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
