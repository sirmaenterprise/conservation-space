package com.sirma.itt.emf.template;

import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Default properties for template instances.
 *
 * @author BBonev
 */
public interface TemplateProperties extends DefaultProperties {

	/** The group id to witch a template belongs. */
	String GROUP_ID = TYPE;

	/** The flag if the template is primary. */
	String PRIMARY = "primary";

	/** The flag if the template is public. */
	String PUBLIC = "public";

	/** The default group name. */
	String DEFAULT_GROUP = "default_group";

	/**
	 * The content digest. Hash of the content used to detect changes in the content without loading
	 * one of the files
	 */
	String CONTENT_DIGEST = "contentDigest";

	/** The is content loaded. */
	String IS_CONTENT_LOADED = "isContentLoaded";

}
