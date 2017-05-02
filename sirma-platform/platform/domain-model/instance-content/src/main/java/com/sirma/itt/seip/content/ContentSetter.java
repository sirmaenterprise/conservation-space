package com.sirma.itt.seip.content;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Plugin interface providing the contract for setting instance content.
 * 
 * @author yasko
 * @deprecated this should not be needed at the first place and should be removed.
 */
@Deprecated
public interface ContentSetter extends Plugin {

	/** Plugin target name for extensions to connect to **/
	String TARGET_NAME = "ContentSetter";

	/**
	 * Is this setter capable of handling a given instance.
	 * @param instance Instance to check.
	 * @return {@code true} if this setter can handle the given instance.
	 */
	boolean isApplicable(Instance instance);

	/**
	 * Loads the default content for an instance.
	 * @param instance Instance for which to load default content.
	 * @return Default content for the instance.
	 */
	default String loadDefaultContent(Instance instance) {
		return "<div><section data-title=\"Details\"></section></div>";
	}

	/**
	 * Sets the provided content to an instance.
	 * @param instance Instance to which to set the content.
	 * @param content Content to set to the instance.
	 */
	void setContent(Instance instance, String content);
}
