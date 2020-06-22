package com.sirma.sep.resources.definitions;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.CommonDefinitionAccessor;
import com.sirma.itt.seip.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;

/**
 * Handles generic resource instances like users and groups and their definitions.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class ResourcesDefinitionAccessor extends CommonDefinitionAccessor<GenericDefinition> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Set<Class<?>> SUPPORTED_OBJECTS = new HashSet<>(
			Arrays.asList(Resource.class, EmfResource.class, EmfUser.class, EmfGroup.class));

	// reusing generic definitions cache
	private static final String GENERIC_DEFINITION_CACHE = "GENERIC_DEFINITION_CACHE";
	private static final String GENERIC_DEFINITION_MAX_REVISION_CACHE = "GENERIC_DEFINITION_MAX_REVISION_CACHE";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "user.definition.default", defaultValue = "userDefinition", label = "The default definition to be used for users handling")
	private ConfigurationProperty<String> defaultUserDefinition;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "group.definition.default", defaultValue = "groupDefinition", label = "The default definition to be used for groups handling")
	private ConfigurationProperty<String> defaultGroupDefinition;

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	@PostConstruct
	public void initinializeCache() {
		super.initinializeCache();
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(Instance instance) {
		String identifier = instance.getIdentifier();
		String definitionId = identifier != null ? identifier : resolveResourceId(instance);
		if (definitionId == null) {
			LOGGER.warn("Did not find any definition identifier for instance: {}", instance.getId());
			return null;
		}

		return getDefinition(definitionId);
	}

	private String resolveResourceId(Instance target) {
		if (target instanceof Resource) {
			return resolveResourceDefinition(target);
		}

		InstanceType type = target.type();
		if (type == null) {
			return null;
		}

		if (type.is(ObjectTypes.USER)) {
			return defaultUserDefinition.get();
		} else if (type.is(ObjectTypes.GROUP)) {
			return defaultGroupDefinition.get();
		}

		return null;
	}

	private String resolveResourceDefinition(Instance instance) {
		ResourceType type = ((Resource) instance).getType();
		if (type == null) {
			return instance.getIdentifier();
		}

		switch (type) {
			case USER:
				return defaultUserDefinition.get();
			case GROUP:
				return defaultGroupDefinition.get();
			default:
				return resolveResourceByClass(instance);
		}
	}

	private String resolveResourceByClass(Instance instance) {
		if (instance instanceof Group) {
			return defaultGroupDefinition.get();
		} else if (instance instanceof User) {
			return defaultUserDefinition.get();
		}

		return null;
	}

	@Override
	public String getDefaultDefinitionId(Object target) {
		if (target instanceof Instance) {
			return resolveResourceId((Instance) target);
		}

		return null;
	}

	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return super.saveDefinition(definition, this);
	}

	@Override
	protected Class<GenericDefinition> getTargetDefinition() {
		return GenericDefinition.class;
	}

	@Override
	protected String getBaseCacheName() {
		return GENERIC_DEFINITION_CACHE;
	}

	@Override
	protected String getMaxRevisionCacheName() {
		return GENERIC_DEFINITION_MAX_REVISION_CACHE;
	}
}