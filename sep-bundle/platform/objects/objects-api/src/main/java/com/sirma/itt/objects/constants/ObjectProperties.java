package com.sirma.itt.objects.constants;

import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Object specific properties.
 *
 * @author BBonev
 */
public interface ObjectProperties extends DefaultProperties {

	/** The default template. */
	String DEFAULT_TEMPLATE = "defaultTemplate";

	/** The default view. */
	String DEFAULT_VIEW = "defaultView";

	/** The default view location. */
	String DEFAULT_VIEW_LOCATION = "defaultViewLocation";

	/** The object view definition. */
	String OBJECT_VIEW_DEFINITION = "viewDefinition";

	/** The object view. */
	String OBJECT_VIEW = "objectview";

	/** The version. */
	String VERSION = "version";

	/** The primary image. */
	String PRIMARY_IMAGE = "primaryImage";

	String CONTENT = "emf:content";
}
