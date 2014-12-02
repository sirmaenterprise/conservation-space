package com.sirma.cmf.web.actionsmanager;

import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.plugin.Plugin;

/**
 * Extension point that allows other modules to provide additional non default action types. Non
 * default actions are those that allow some custom button xhtml (see action-button-template.xhtml)
 * and event handler implementation.
 * 
 * @author svelikov
 */
public interface NonDefaultActionButtonExtensionPoint extends Plugin {

	/**
	 * Extension point key path.
	 */
	public static final String EXTENSION_POINT = "non.default.action.button.extension.point";

	/**
	 * Gets the actions that are not default and should have specific implementation.
	 * 
	 * @return math with actions and supported elements(instance types)
	 */
	Map<String, List<String>> getActions();

}
