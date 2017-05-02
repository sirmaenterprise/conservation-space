package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;

/**
 * {@link InstanceTypes} implementation that loads the types from the {@link ClassInstance}s provided by the
 * {@link SemanticDefinitionService}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SemanticInstanceTypes implements InstanceTypes {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private javax.enterprise.inject.Instance<InstanceTypeResolver> instanceTypeResolver;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private TypeMappingProvider typeMapping;

	@Override
	public boolean is(InstanceReference reference, Serializable type) {
		Optional<InstanceType> currentType = from(reference);
		Optional<InstanceType> referenceType = from(type);
		if (referenceType.isPresent()) {
			return currentType.filter(aType -> aType.instanceOf(referenceType.get())).isPresent();
		}
		return false;
	}

	@Override
	public boolean hasTrait(InstanceReference reference, Serializable trait) {
		String traitAsString = typeConverter.tryConvert(String.class, trait);
		if (StringUtils.isNullOrEmpty(traitAsString)) {
			return false;
		}
		return from(reference).filter(type -> type.hasTrait(traitAsString)).isPresent();
	}

	@Override
	public Collection<InstanceType> forCategory(String category) {
		if (StringUtils.isNullOrEmpty(category)) {
			return Collections.emptyList();
		}
		String expandedName = typeMapping.getDataTypeName(category);
		return semanticDefinitionService
				.getClasses()
					.stream()
					.map(ClassInstance::type)
					.filter(filterByCategory(category, expandedName))
					.collect(Collectors.toSet());
	}

	private static Predicate<InstanceType> filterByCategory(String category, String expandedName) {
		return type -> {
			String current = type.getCategory();
			return nullSafeEquals(current, category) || nullSafeEquals(current, expandedName);
		};
	}

	@Override
	public Optional<InstanceType> from(Serializable source) {
		if (source == null) {
			return Optional.empty();
		}
		String type = typeConverter.tryConvert(String.class, source);
		return resolveType(type);

	}

	private Optional<InstanceType> resolveType(String source) {
		ClassInstance classInstance = semanticDefinitionService.getClassInstance(source);
		if (classInstance == null) {
			LOGGER.trace("Resolving instance type via instance loading for id: {}" + source);
			// if the given id is not a class id check it if it's an instance id
			return instanceTypeResolver.get().resolve(source);
		}
		return Optional.of(classInstance.type());
	}

	@Override
	public Optional<InstanceType> from(Instance source) {
		if (source == null) {
			return Optional.empty();
		}
		Optional<InstanceType> resolvedType = Optional.empty();
		if (source.type() != null) {
			// fetch new copy of the type
			resolvedType = from(source.type().getId());
		}
		if (!resolvedType.isPresent()) {
			String type = source.getAsString(SEMANTIC_TYPE);
			if (type == null) {
				// if the instance does not have a type and semantic type we better try to load the instance reference
				// and
				// fetch the type from there
				resolvedType = from(source.getId());
			} else {
				resolvedType = from(type);
			}
		}
		source.setType(resolvedType.orElse(null));
		return resolvedType;
	}

	@Override
	public Optional<InstanceType> from(InstanceReference source) {
		if (source == null) {
			return Optional.empty();
		}
		Optional<InstanceType> resolvedType = Optional.empty();
		if (source.getType() != null) {
			// fetch new copy of the type
			resolvedType = from(source.getType().getId());
		}
		if (!resolvedType.isPresent()) {
			resolvedType = from(source.getIdentifier());
		}
		source.setType(resolvedType.orElse(null));
		return resolvedType;
	}

	@Override
	public Optional<InstanceType> from(DefinitionModel model) {
		if (model == null) {
			return Optional.empty();
		}
		return model.getField(SEMANTIC_TYPE).map(PropertyDefinition::getDefaultValue).flatMap(this::from);
	}

	@Override
	public void resolveTypes(Collection<? extends Instance> instances) {
		if (isEmpty(instances)) {
			return;
		}
		instances.stream().filter(hasSemanticType()).forEach(this::from);

		Set<Serializable> toResolve = instances
				.stream()
					.filter(hasSemanticType().negate())
					.map(Instance::getId)
					.collect(Collectors.toSet());

		Map<Serializable, InstanceType> resolved = instanceTypeResolver.get().resolve(toResolve);

		for (Instance toUpdate : instances) {
			InstanceType type = resolved.get(toUpdate.getId());
			if (type != null) {
				toUpdate.setType(type);
			}
		}
	}

	private static Predicate<Instance> hasSemanticType() {
		return instance -> instance.type() != null || instance.isValueNotNull(SEMANTIC_TYPE);
	}
}
