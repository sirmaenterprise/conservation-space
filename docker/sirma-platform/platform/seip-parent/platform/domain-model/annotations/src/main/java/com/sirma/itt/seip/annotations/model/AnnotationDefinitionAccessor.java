package com.sirma.itt.seip.annotations.model;

import java.lang.invoke.MethodHandles;
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
import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Implementation of the interface {@link DefinitionAccessor} that handles the {@link GenericDefinition} definitions and
 * annotation instances.
 *
 * @author BBonev
 */
@ApplicationScoped
class AnnotationDefinitionAccessor extends CommonDefinitionAccessor<GenericDefinition> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * The set of supported objects that are returned by the method {@link #getSupportedObjects()}.
	 */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;
	// reuse the caches, no need to define new for now
	private static final String GENERIC_DEFINITION_CACHE = "GENERIC_DEFINITION_CACHE";
	private static final String GENERIC_DEFINITION_MAX_REVISION_CACHE = "GENERIC_DEFINITION_MAX_REVISION_CACHE";

	/*
	 * DEFAULT DEFINITIONS
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "annotation.definition.default", defaultValue = "annotation", label = "The default definition to be used for annotations handling")
	private ConfigurationProperty<String> defaultAnnotationDefinition;

	static {
		SUPPORTED_OBJECTS = new HashSet<>();
		SUPPORTED_OBJECTS.add(Annotation.class);
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

	@Override
	public <D extends DefinitionModel> D getDefinition(Instance instance) {
		String definitionId = getDefaultDefinitionId(instance);

		if (definitionId == null) {
			LOGGER.warn("Failed to find generic definition for the instance with class: {}",
					instance.getClass().getSimpleName());
			return null;
		}
		return getDefinition(definitionId);
	}

	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return super.saveDefinition(definition, this);
	}

	@Override
	public String getDefaultDefinitionId(Object target) {
		if (target instanceof Annotation) {
			return defaultAnnotationDefinition.get();
		}
		return null;
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
