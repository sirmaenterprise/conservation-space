package com.sirma.itt.pm.domain;

import java.util.Map;

import com.sirma.itt.emf.definition.dao.AllowedChildTypeMappingExtension;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Default type mappings for PMF classes as project.
 * 
 * @author BBonev
 */
@Extension(target = AllowedChildTypeMappingExtension.TARGET_NAME, order = 50)
public class PmfAllowedChildTypeMappingExtension implements AllowedChildTypeMappingExtension {

	/** The Constant definitionMapping. */
	private static final Map<String, Class<? extends DefinitionModel>> definitionMapping;
	/** The Constant instanceMapping. */
	private static final Map<String, Class<? extends Instance>> instanceMapping;
	/** The Constant typeMapping. */
	private static final Map<String, String> typeMapping;

	static {
		definitionMapping = CollectionUtils.createHashMap(3);
		definitionMapping.put(ObjectTypesPm.PROJECT, ProjectDefinition.class);

		instanceMapping = CollectionUtils.createHashMap(3);
		instanceMapping.put(ObjectTypesPm.PROJECT, ProjectInstance.class);

		typeMapping = CollectionUtils.createHashMap(3);
		typeMapping.put(ObjectTypesPm.PROJECT, "projectInstance");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends DefinitionModel>> getDefinitionMapping() {
		return definitionMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends Instance>> getInstanceMapping() {
		return instanceMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTypeMapping() {
		return typeMapping;
	}

}
