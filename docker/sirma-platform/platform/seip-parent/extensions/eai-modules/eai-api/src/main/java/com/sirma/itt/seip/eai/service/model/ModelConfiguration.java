package com.sirma.itt.seip.eai.service.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.SealedModel;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityRelation;
import com.sirma.itt.seip.eai.model.mapping.EntityType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

/**
 * Full model wrapper holding tenant and system specific mapping. To be runtime usable the wrapper should be sealed by
 * invoking {@link #seal()}
 *
 * @author bbanchev
 */
public class ModelConfiguration extends SealedModel {

	@JsonIgnore
	private Map<String, EntityType> definitionIdToEntityType = new HashMap<>();
	@JsonIgnore
	private Set<EntityType> entities = new HashSet<>();
	@JsonIgnore
	private Map<String, EntityProperty> externalNameToProperty = new HashMap<>();
	@JsonIgnore
	private Map<String, Set<EntityProperty>> internalNameToProperty = new HashMap<>();
	@JsonIgnore
	private Set<EntityProperty> allProperties = new HashSet<>();
	private Set<String> namespaces = new TreeSet<>();

	/**
	 * Adds the model {@link EntityType}. Throws {@link EAIRuntimeException} on duplicate {@link EntityType}
	 *
	 * @param entity the entity to append. null values are skipped
	 * @return the model configuration.
	 */
	public ModelConfiguration addEntityType(EntityType entity) {
		if (entity != null && !isSealed()) {
			boolean added = entities.add(entity);
			if (!added) {
				throw new EAIRuntimeException("Duplicate entity: " + entity + "! Check model!");
			}
			definitionIdToEntityType.put(entity.getIdentifier().toUpperCase(), entity);
		}
		return this;
	}

	/**
	 * Gets the set of entity types.
	 *
	 * @return the types model
	 */
	@JsonProperty("entities")
	public Set<EntityType> getEntityTypes() {
		return entities;
	}

	/**
	 * Gets the property by external name.
	 *
	 * @param name the name
	 * @return the property by external name
	 */
	public EntityProperty getPropertyByExternalName(String name) {
		if (name == null) {
			return null;
		}
		return externalNameToProperty.get(name);
	}

	/**
	 * Gets the relation by external name.
	 *
	 * @param definitionId is the entity definition
	 * @param name the name of relation as external id
	 * @return the relation by external name or null if not found
	 */
	public EntityRelation getRelationByExternalName(String definitionId, String name) {
		EntityType entityType;
		if (definitionId == null || (entityType = definitionIdToEntityType.get(definitionId.toUpperCase())) == null) {
			return null;
		}
		return entityType
				.getRelations()
				.stream()
				.filter(e -> e.hasMapping(name))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Gets the property by external name - {@link EntityProperty#getDataMapping()}.
	 *
	 * @param definitionId the definition id of the type to search in
	 * @param key the name to search
	 * @return the property by external name or null if not found for that definition or if the definition is not found
	 */
	public EntityProperty getPropertyByExternalName(String definitionId, String key) {
		return filterPropertyInType(definitionId,
				property -> property.getDataMapping() != null && key.equalsIgnoreCase(property.getDataMapping()));
	}

	/**
	 * Gets the property by custom filter in specific type
	 *
	 * @param definitionId the definition id of the type to search in
	 * @param filter the predicate
	 * @return the property filtered by the predicate or null if not found for that definition or if the definition is
	 * not found
	 */
	public EntityProperty getPropertyByFilter(String definitionId, Predicate<EntityProperty> filter) {
		return filterPropertyInType(definitionId, filter);
	}

	/**
	 * Gets the property by custom filter
	 *
	 * @param filter the predicate to filter
	 * @return the property filtered by the predicate or null if not found for that definition or if the definition is
	 * not found
	 */
	public EntityProperty getPropertyByFilter(Predicate<EntityProperty> filter) {
		if (filter == null) {
			return null;
		}
		return allProperties.stream().filter(filter).findFirst().orElse(null);
	}

	/**
	 * Gets the type by external name from the {@link EntityType#getMappings()}.
	 *
	 * @param name the name to match
	 * @return the type by external name or null if not found
	 */
	public EntityType getTypeByExternalName(String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}
		return getFilteredType(entity -> entity.hasMapping(name));
	}

	/**
	 * Gets the type by definition id - the {@link EntityType#getIdentifier()}.
	 *
	 * @param definitionId the type identifier
	 * @return the type by definition id
	 */
	public EntityType getTypeByDefinitionId(String definitionId) {
		EntityType entityType;
		if (definitionId == null || (entityType = definitionIdToEntityType.get(definitionId.toUpperCase())) == null) {
			return null;
		}
		return entityType;
	}

	private EntityType getFilteredType(Predicate<? super EntityType> filter) {
		Optional<EntityType> found = entities.stream().filter(filter).findFirst();
		return found.orElse(null);
	}

	/**
	 * Gets the property by internal name.
	 *
	 * @param name the name
	 * @return the property by internal name
	 */
	public Set<EntityProperty> getPropertyByInternalName(String name) {
		if (name == null) {
			return Collections.emptySet();
		}
		return internalNameToProperty.get(name);
	}

	/**
	 * Gets the property by internal uri.
	 *
	 * @param definitionId the definition id. Might be null
	 * @param key the uri to search. Value should be a valid uri, otherwise null is returned
	 * @return the property by internal name or null if not found for that definition or if the definition is not found
	 */
	public EntityProperty getPropertyByInternalName(String definitionId, String key) {
		if (definitionId == null) {
			Set<EntityProperty> propertyByInternalName = getPropertyByInternalName(key);
			if (propertyByInternalName == null || propertyByInternalName.size() != 1) {
				return null;
			}
			return propertyByInternalName.iterator().next();
		}
		return filterPropertyInType(definitionId, e -> e.getUri() != null && key.equalsIgnoreCase(e.getUri()));
	}

	private EntityProperty filterPropertyInType(String definitionId, Predicate<EntityProperty> filter) {
		EntityType entityType;
		if (definitionId == null || (entityType = definitionIdToEntityType.get(definitionId.toUpperCase())) == null) {
			return null;
		}
		return entityType.getProperties().stream().filter(filter).findFirst().orElse(null);
	}

	@Override
	public void seal() {
		if (isSealed()) {
			return;
		}
		// update mapping and seal
		entities = Collections.unmodifiableSet(entities);
		fillMappingModel();
		for (EntityType entityType : entities) {
			entityType.seal();
		}
		definitionIdToEntityType = Collections.unmodifiableMap(definitionIdToEntityType);
		externalNameToProperty = Collections.unmodifiableMap(externalNameToProperty);
		internalNameToProperty = Collections.unmodifiableMap(internalNameToProperty);
		allProperties = Collections.unmodifiableSet(allProperties);
		namespaces = Collections.unmodifiableSet(namespaces);
		super.seal();
	}

	@Override
	@JsonIgnore
	public boolean isSealed() {
		return super.isSealed();
	}

	private void fillMappingModel() {
		for (EntityType entityType : entities) {
			definitionIdToEntityType.put(entityType.getIdentifier(), entityType);
			Stream<EntityProperty> mappings = entityType
					.getProperties()
					.stream()
					.filter(e -> StringUtils.isNotBlank(e.getDataMapping()));

			Map<String, EntityProperty> mappingsByData = mappings
					.collect(CollectionUtils.toIdentityMap(EntityProperty::getDataMapping));
			mappingsByData.forEach((key, value) -> {
				EntityProperty existing = externalNameToProperty.put(key, value);
				if (existing != null && !existing.equals(value)) {
					throw new EAIRuntimeException("Duplicated property with name " + key + "! Existing value '" +
							existing + "', new value '" + value + "'. This happened for definition: " + entityType
							.getIdentifier() + ".");
				}
			});
			allProperties.addAll(entityType.getProperties());
			entityType.getProperties().stream().filter(e -> e.getUri() != null).forEach(e -> {
				internalNameToProperty.putIfAbsent(e.getUri(), new HashSet<>());
				internalNameToProperty.get(e.getUri()).add(e);
			});

		}

	}

	/**
	 * Checks for namespace in the set of registered namespaces.
	 *
	 * @param namespace the namespace to search
	 * @return true, if it has
	 */
	public boolean hasNamespace(String namespace) {
		return namespaces.contains(namespace.toUpperCase());
	}

	/**
	 * Register namespace to the set of namespaces for this model.
	 *
	 * @param namespace the namespace to add
	 */
	public void registerNamespace(String namespace) {
		namespaces.add(namespace.toUpperCase());
	}

}
