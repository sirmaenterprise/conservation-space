package com.sirma.itt.cmf.services.impl.dao;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.CommonDefinitionAccessor;
import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;

/**
 * Implementation of the interface {@link DefinitionAccessor} that handles the case definitions and case instances.
 *
 * @author BBonev
 */
@ApplicationScoped
public class GenericDefinitionAccessor extends CommonDefinitionAccessor<GenericDefinition> {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3703684091702522270L;
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericDefinitionAccessor.class);
	/**
	 * The set of supported objects that are returned by the method {@link #getSupportedObjects()}.
	 */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;
	/** The Constant GENERIC_DEFINITION_CACHE. */
	@CacheConfiguration(eviction = @Eviction(maxEntries = 250) , doc = @Documentation(""
			+ "Cache used to contain the generic definitions by definition id and revision and container. "
			+ "The cache will have an entry for every distinct definition of a loaded instance that depends on a definition and is unique for every active container(tenant) and different definition versions."
			+ "Example value expression: tenants * genericDefinitions * 10. Here 10 is the number of the different versions of a single case definition. "
			+ "If the definitions does not change that much the number could be smaller like 2-5. "
			+ "<br>Minimal value expression: tenants * genericDefinitions * 10") )
	private static final String GENERIC_DEFINITION_CACHE = "GENERIC_DEFINITION_CACHE";
	/** The Constant GENERIC_DEFINITION_MAX_REVISION_CACHE. */
	@CacheConfiguration(eviction = @Eviction(maxEntries = 100) , doc = @Documentation(""
			+ "Cache used to contain the latest generic definitions. The cache will have at most an entry for every different generic definition per active tenant. "
			+ "<br>Minimal value expression: tenants * genericDefinitions") )
	private static final String GENERIC_DEFINITION_MAX_REVISION_CACHE = "GENERIC_DEFINITION_MAX_REVISION_CACHE";

	/*
	 * DEFAULT DEFINITIONS
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "user.definition.default", defaultValue = "userDefinition", label = "The default definition to be used for users handling")
	private ConfigurationProperty<String> defaultUserDefinition;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "group.definition.default", defaultValue = "groupDefinition", label = "The default definition to be used for groups handling")
	private ConfigurationProperty<String> defaultGroupDefinition;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "link.definition.default", defaultValue = "linkDefinition", label = "The default definition to be used for relations handling")
	private ConfigurationProperty<String> defaultRelationDefinition;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "classInstance.definition.default", defaultValue = "classDefinition", label = "The default definition to be used for class instance handling")
	private ConfigurationProperty<String> defaultClassInstanceDefinition;

	static {
		SUPPORTED_OBJECTS = new HashSet<>();
		SUPPORTED_OBJECTS.add(GenericDefinition.class);
		SUPPORTED_OBJECTS.add(GenericDefinitionImpl.class);
		SUPPORTED_OBJECTS.add(Resource.class);
		SUPPORTED_OBJECTS.add(EmfUser.class);
		SUPPORTED_OBJECTS.add(EmfGroup.class);
		SUPPORTED_OBJECTS.add(EmfResource.class);
		SUPPORTED_OBJECTS.add(LinkInstance.class);
		SUPPORTED_OBJECTS.add(LinkReference.class);
		SUPPORTED_OBJECTS.add(ClassInstance.class);
		SUPPORTED_OBJECTS.add(ObjectInstance.class);
	}

	@Override
	@PostConstruct
	public void initinializeCache() {
		super.initinializeCache();
	}

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <D extends DefinitionModel> D getDefinition(Instance instance) {
		String definitionId = extractDefinitionId(instance);

		if (definitionId == null) {
			LOGGER.warn("Did not find any generic definition for the instance with class: {}",
					instance.getClass().getSimpleName());
			return null;
		}
		return getDefinition(definitionId);
	}

	/**
	 * Extract definition id.
	 *
	 * @param instance
	 *            the instance
	 * @return the string
	 */
	private String extractDefinitionId(Instance instance) {
		String definitionId = instance.getIdentifier();
		if (definitionId != null) {
			return definitionId;
		}
		return resolveDefinitionId(instance);
	}

	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private String resolveDefinitionId(Instance instance) {
		String definitionId = null;
		InstanceType type = instance.type();
		if (instance instanceof Resource) {
			definitionId = resolveResourceDefinition(instance);
		} else if (type != null && type.is(ObjectTypes.USER)) {
			definitionId = defaultUserDefinition.get();
		} else if (type != null && type.is(ObjectTypes.GROUP)) {
			definitionId = defaultGroupDefinition.get();
		} else if (instance instanceof LinkReference || instance instanceof LinkInstance) {
			definitionId = defaultRelationDefinition.get();
		} else if (instance instanceof ClassInstance) {
			definitionId = defaultClassInstanceDefinition.get();
		}
		return definitionId;
	}

	private String resolveResourceDefinition(Instance instance) {
		String definitionId = instance.getIdentifier();
		ResourceType type = ((Resource) instance).getType();
		if (type == null) {
			return definitionId;
		}
		switch (type) {
			case USER:
				definitionId = defaultUserDefinition.get();
				break;
			case GROUP:
				definitionId = defaultGroupDefinition.get();
				break;
			default:
				definitionId = resolveResourceByClass(instance);
				break;
		}
		return definitionId;
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
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return super.saveDefinition(definition, this);
	}

	@Override
	public String getDefaultDefinitionId(Object target) {
		if (target instanceof Instance) {
			return resolveDefinitionId((Instance) target);
		}
		return null;
	}

	@Override
	protected void updateCache(DefinitionModel definition, boolean propertiesOnly, boolean isMaxRevision) {
		super.updateCache(definition, propertiesOnly, isMaxRevision);
		GenericDefinition genericDefinition = (GenericDefinition) definition;
		for (GenericDefinition child : genericDefinition.getSubDefinitions()) {
			injectLabelProvider(child);
		}
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
