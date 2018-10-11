package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles the processing of object properties, when creating instance version. This is necessary, because the object
 * properties are actually instances, which also have versions and we want to store them in the current version so that
 * when it is previewed, the links(instance headers) to point to the specific version.<br>
 * The process consists in few things, first all object properties are extracted for the instance by using the current
 * definition model to retrieve, which properties are object. Then the value of those properties are retrieved from the
 * instance. After that the extracted value is process(transformed to version id) and the old value for the that
 * property is replaced by the transformed. <br>
 * The logic is executed over the version instance that will be persisted in order to avoid making any changes to the
 * target instance, because it is used as result from the whole save process.
 * <p>
 * Note that:
 * <ul>
 * <li>only object properties that currently have value will be transformed</li>
 * <li>object property with name {@link DefaultProperties#SEMANTIC_TYPE} will be filtered, because it does not point to
 * actual instance</li>
 * <li>all object property that don't start with valid {@code emf} prefix will be preserved without trying to process
 * them. Such properties are {@link DefaultProperties#REVISION_TYPE}, {@link DefaultProperties#SEMANTIC_TYPE}, etc.</li>
 * </ul>
 * </p>
 *
 * @author A. Kunchev
 */
@Extension(target = VersionStep.TARGET_NAME, enabled = true, order = 14)
public class ProcessObjectPropertiesVersionStep implements VersionStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DefinitionService definitionService;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private VersionDao versionDao;

	@Inject
	private InstanceService instanceService;

	@Override
	public String getName() {
		return "processObjectPropertiesVersion";
	}

	@Override
	public void execute(VersionContext context) {
		if (!context.isObjectPropertiesVersioningEnabled()) {
			LOGGER.trace("Object properties versioning is disabled for instance - {}", context.getTargetInstanceId());
			return;
		}

		Instance version = context.getVersionInstance().orElseThrow(
				() -> new EmfRuntimeException("Version instance should be available at this point."));
		definitionService
				.getInstanceObjectProperties(version)
					.filter(PropertyDefinition.hasName(SEMANTIC_TYPE).negate().and(
							property -> version.isValueNotNull(property.getName())))
					.collect(toMap(PropertyDefinition::getName, transform(version, context.getCreationDate())))
					.forEach(version::add);
	}

	/**
	 * Transforms object properties values, which are actually instance ids to version ids. Supports transformation for
	 * multi and single value properties.
	 */
	private Function<PropertyDefinition, Serializable> transform(Instance instance, Date date) {
		return property -> {
			String name = property.getName();
			LOGGER.trace("Transforming the value/s of object property - [{}]", name);
			Serializable value = instance.get(name);
			return property.isMultiValued() ? handleAsMultivalue(value, date) : handleAsSingleValue(value, date);
		};
	}

	@SuppressWarnings("unchecked")
	private Serializable handleAsMultivalue(Serializable value, Date date) {
		if (value instanceof Collection<?>) {
			Collection<String> ids = (Collection<String>) value;
			if (ids.isEmpty()) {
				LOGGER.trace("No need to process, the value is empty. It will be kept as it is.");
				return value;
			}

			Collection<Serializable> convertedIds = convertToShortUris(ids);
			return versionPropertyValues(convertedIds, ids, date);
		}

		// instance data validations tho -> this should not be plausible
		LOGGER.warn("There is multivalue field, which value isn't collection, it will be handled as single"
				+ " value property. Check the defined model and fix it!");
		return handleAsSingleValue(value, date);
	}

	private Collection<Serializable> convertToShortUris(Collection<String> ids) {
		return typeConverter.convert(ShortUri.class, ids).stream().map(ShortUri::toString).collect(Collectors.toList());
	}

	private Serializable versionPropertyValues(Collection<Serializable> converted, Collection<String> ids, Date date) {
		Map<Serializable, Serializable> versionsMap = versionIds(converted, date);
		if (ids.size() != versionsMap.size()) {
			ids.forEach(id -> versionsMap.computeIfAbsent(id, Function.identity()));
		}

		return new ArrayList<>(versionsMap.values());
	}

	private Map<Serializable, Serializable> versionIds(Collection<Serializable> ids, Date date) {
		Collection<Serializable> filtered = instanceService.exist(ids).get();
		return versionDao.findVersionIdsByTargetIdAndDate(filtered, date);
	}

	private Serializable handleAsSingleValue(Serializable value, Date date) {
		Collection<Serializable> converted = convertToShortUris(Collections.singleton(value.toString()));
		Map<Serializable, Serializable> versionsMap = versionIds(converted, date);
		if (versionsMap.isEmpty()) {
			return value;
		}

		return versionsMap.values().iterator().next();
	}
}