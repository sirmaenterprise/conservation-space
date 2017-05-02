package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.domain.definition.DataTypeDefinition.TEXT;
import static com.sirma.itt.seip.domain.definition.DataTypeDefinition.URI;
import static com.sirma.itt.seip.domain.definition.PropertyDefinition.hasControl;
import static com.sirma.itt.seip.domain.definition.PropertyDefinition.hasType;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Utility class to accommodate common logic when implementing the semantic properties converters.
 *
 * @author BBonev
 */
public class BasePropertiesConverter {

	private static final String INSTANCE = "INSTANCE";
	/** The namespace registry service. */
	@Inject
	protected NamespaceRegistryService namespaceRegistryService;

	/**
	 * Checks if the given field definition is to a field that should be converted to URI
	 *
	 * @param field
	 *            the field
	 * @return true, if is uri field
	 */
	public static boolean isUriField(PropertyDefinition field) {
		return hasType(URI).or(hasControl("USER")).or(hasControl(INSTANCE).and(hasType(TEXT))).test(field);
	}

	/**
	 * Checks if is sub instance field.
	 *
	 * @param field
	 *            the field
	 * @return true, if is sub instance field
	 */
	public static boolean isSubInstanceField(PropertyDefinition field) {
		return hasControl(INSTANCE).and(hasType(DataTypeDefinition.INSTANCE)).test(field);
	}

}
