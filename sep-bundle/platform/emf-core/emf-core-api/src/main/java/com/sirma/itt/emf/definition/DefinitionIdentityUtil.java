package com.sirma.itt.emf.definition;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.ReferenceDefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.TenantAware;

/**
 * Utility class for extracting and constructing complex key from the definition's id/parent id and
 * container.
 * 
 * @author BBonev
 */
public class DefinitionIdentityUtil {

	/** The Constant SEPARATOR. */
	public static final String SEPARATOR = "@";

	/**
	 * Gets the definition id.
	 * 
	 * @param definitionId
	 *            the definition id
	 * @param container
	 *            the container
	 * @return the definition id
	 */
	public static String createDefinitionId(String definitionId, String container) {
		String defId = definitionId;
		if (com.sirma.itt.commons.utils.string.StringUtils.isNotNullOrEmpty(container)) {
			defId += SEPARATOR + container;
		}
		return defId;
	}

	/**
	 * Gets the definition id.
	 * 
	 * @param definition
	 *            the definition
	 * @return the definition id
	 */
	public static String createDefinitionId(TopLevelDefinition definition) {
		String defId = definition.getIdentifier();
		if (com.sirma.itt.commons.utils.string.StringUtils.isNotNullOrEmpty(definition
				.getContainer())) {
			defId += SEPARATOR + definition.getContainer();
		}
		return defId;
	}

	/**
	 * Creates the parent definition id.
	 * 
	 * @param definition
	 *            the definition
	 * @return the string
	 */
	public static String createParentDefinitionId(TopLevelDefinition definition) {
		String defId = definition.getParentDefinitionId();
		if (com.sirma.itt.commons.utils.string.StringUtils.isNotNullOrEmpty(definition
				.getContainer())) {
			defId += SEPARATOR + definition.getContainer();
		}
		return defId;
	}

	/**
	 * Creates the reference definition id.
	 * 
	 * @param definition
	 *            the definition
	 * @return the string
	 */
	public static String createReferenceDefinitionId(ReferenceDefinitionModel definition) {
		String defId = definition.getReferenceId();
		if ((definition instanceof TenantAware)
				&& com.sirma.itt.commons.utils.string.StringUtils
						.isNotNullOrEmpty(((TenantAware) definition).getContainer())) {
			defId += SEPARATOR + ((TenantAware) definition).getContainer();
		}
		return defId;
	}

	/**
	 * Gets the definition id from the unique definition id plus container id.
	 * 
	 * @param definitionId
	 *            the definition id
	 * @return the definition id
	 */
	public static String getDefinitionId(String definitionId) {
		if (com.sirma.itt.commons.utils.string.StringUtils.isNullOrEmpty(definitionId)) {
			return null;
		}
		int indexOf = definitionId.lastIndexOf(SEPARATOR);
		if (indexOf < 0) {
			return definitionId;
		}
		return definitionId.substring(0, indexOf);
	}

	/**
	 * Gets the definition id from the unique definition id plus container id.
	 * 
	 * @param definitionId
	 *            the definition id
	 * @return the definition id
	 */
	public static String getContainerId(String definitionId) {
		if (com.sirma.itt.commons.utils.string.StringUtils.isNullOrEmpty(definitionId)) {
			return null;
		}
		int indexOf = definitionId.lastIndexOf(SEPARATOR);
		if (indexOf < 0) {
			return null;
		}
		return definitionId.substring(indexOf + 1);
	}

	/**
	 * Parses the definition id.
	 * 
	 * @param definitionId
	 *            the definition id
	 * @return the pair
	 */
	public static Pair<String, String> parseDefinitionId(String definitionId) {
		if (definitionId == null) {
			return null;
		}
		return new Pair<String, String>(getDefinitionId(definitionId), getContainerId(definitionId));
	}

	/**
	 * Creates the definition pair.
	 * 
	 * @param definition
	 *            the definition
	 * @return the pair
	 */
	public static Pair<String, String> createDefinitionPair(TopLevelDefinition definition) {
		if (definition == null) {
			return null;
		}
		String container = null;
		if (StringUtils.isNotNullOrEmpty(definition.getContainer())) {
			container = definition.getContainer();
		}
		return new Pair<String, String>(definition.getIdentifier(), container);
	}

}
