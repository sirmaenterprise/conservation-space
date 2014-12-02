package com.sirma.itt.emf.properties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.dao.PersistentPropertiesExtension;

/**
 * The list of properties that need to be persisted without definition: properties for check boxes,
 * action buttons and link description properties
 * 
 * @author BBonev
 */
@Extension(target = PersistentPropertiesExtension.TARGET_NAME, order = 10)
public class DefaultPersistentProperties implements PersistentPropertiesExtension {

	/** The allowed no definition fields. */
	private static final Set<String> ALLOWED_NO_DEFINITION_FIELDS = new HashSet<String>(
			Arrays.asList(DefaultProperties.CHECK_BOX_VALUE,
					DefaultProperties.CHECK_BOX_MODIFIED_FROM,
					DefaultProperties.CHECK_BOX_MODIFIED_ON,
					DefaultProperties.ACTION_BUTTON_EXECUTED_ON,
					DefaultProperties.ACTION_BUTTON_EXECUTED,
					DefaultProperties.ACTION_BUTTON_EXECUTED_FROM, DefaultProperties.THUMBNAIL_IMAGE));
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getPersistentProperties() {
		return ALLOWED_NO_DEFINITION_FIELDS;
	}

}
