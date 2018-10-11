package com.sirma.itt.seip.instance.properties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.instance.version.VersionProperties;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The list of properties that need to be persisted without definition: properties for check boxes, action buttons and
 * link description properties
 *
 * @author BBonev
 */
@Extension(target = PersistentPropertiesExtension.TARGET_NAME, order = 10)
public class DefaultPersistentProperties implements PersistentPropertiesExtension {

	/** The allowed no definition fields. */
	private static final Set<String> ALLOWED_NO_DEFINITION_FIELDS = new HashSet<>(
			Arrays.asList(DefaultProperties.CHECK_BOX_VALUE, DefaultProperties.CHECK_BOX_MODIFIED_FROM,
					DefaultProperties.CHECK_BOX_MODIFIED_ON, DefaultProperties.ACTION_BUTTON_EXECUTED_ON,
					DefaultProperties.ACTION_BUTTON_EXECUTED, DefaultProperties.ACTION_BUTTON_EXECUTED_FROM,
					// TODO: REMOVE thumbnail from here...
					DefaultProperties.THUMBNAIL_IMAGE, DefaultProperties.PURPOSE, DefaultProperties.EMF_PURPOSE,
					DefaultProperties.STANDALONE, DefaultProperties.IS_ACTIVE,
					DefaultProperties.PRIMARY_CONTENT_ID, DefaultProperties.VERSION,
					VersionProperties.QUERIES_RESULT_CONTENT_ID));

	@Override
	public Set<String> getPersistentProperties() {
		return ALLOWED_NO_DEFINITION_FIELDS;
	}

}
