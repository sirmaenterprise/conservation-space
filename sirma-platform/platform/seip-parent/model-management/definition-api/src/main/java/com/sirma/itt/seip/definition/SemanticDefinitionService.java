package com.sirma.itt.seip.definition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.event.Observes;

import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;

/**
 * Interface for Semantic definitions service that will provide methods for extraction of the hierarchy of classes,
 * properties and relations from the semantic
 *
 * @author kirq4e
 */
public interface SemanticDefinitionService {

	/**
	 * Returns all classes from the semantics and their hierarchy
	 *
	 * @return All classes from the semantics and their hierarchy
	 */
	List<ClassInstance> getClasses();

	/**
	 * Searches the cache for a {@link ClassInstance} with the specified identifier.
	 *
	 * @param identifier
	 *            Class instance identifier - short or long uri.
	 * @return {@link ClassInstance} or {@code null} if no class with that identifier is found in the cache.
	 */
	ClassInstance getClassInstance(String identifier);

	/**
	 * Returns all classes that can be used when searching from the semantics and their hierarchy
	 *
	 * @return All classes from the semantics and their hierarchy
	 */
	List<ClassInstance> getSearchableClasses();

	/**
	 * Returns all classes belonging to the given ontology.
	 * 
	 * @param ontologyId
	 *            is the ID of the ontology
	 * @return the list of all {@link ClassInstance} belonnging to that ontology
	 */
	List<ClassInstance> getClassesForOntology(String ontologyId);

	/**
	 * Returns all primitive data types from the semantics
	 *
	 * @return All primitive data types from the semantics
	 */
	List<ClassInstance> getDataTypes();

	/**
	 * Searches the cache for a specific data type with the specified identifier.
	 *
	 * @param identifier
	 *            Data type identifier - short or long uri.
	 * @return {@link ClassInstance} or {@code null} if no data type with that identifier is found in the cache.
	 */
	ClassInstance getDataType(String identifier);

	/**
	 * Returns all literal properties from the semantics
	 *
	 * @return All literal properties from the semantics
	 */
	List<PropertyInstance> getProperties();

	/**
	 * Returns all literal properties from the semantics as a map. The key is the property <strong>short</strong> uri.
	 *
	 * @return Literal properties map.
	 */
	Map<String, PropertyInstance> getPropertiesMap();

	/**
	 * Using {@link #getPropertiesMap()} retrieves a literal property by it's uri. The method accepts both short and
	 * full uris.
	 *
	 * @param uri
	 *            Property URI.
	 * @return the mapped property instance or null if nothing is mapped to this uri.
	 */
	PropertyInstance getProperty(String uri);

	/**
	 * Returns all properties for a given class from the semantics and his parents
	 *
	 * @param classType
	 *            URI of the Class that owns the properties
	 * @return List with all properties for a given class from the semantics and his parents
	 */
	List<PropertyInstance> getProperties(String classType);

	/**
	 * Returns *only* the own properties for a class.
	 *
	 * @param classType
	 *            URI of the Class that owns the properties.
	 * @return List with all properties for a given class.
	 */
	List<PropertyInstance> getOwnProperties(String classType);

	/**
	 * Returns all relations from the semantics
	 *
	 * @return All relations from the semantics
	 */
	List<PropertyInstance> getRelations();

	/**
	 * Returns all searchable relations from the semantics
	 *
	 * @return All searchable relations from the semantics
	 */
	List<PropertyInstance> getSearchableRelations();

	/**
	 * Returns relation data by relation URI. This method accepts full URI of the relation
	 *
	 * @param relationUri
	 *            The Full URI of the relation
	 * @return Relation data - labels, definitions
	 */
	PropertyInstance getRelation(String relationUri);

	/**
	 * Gets the inverse relation provider instance
	 *
	 * @return the inverse relation provider
	 */
	InverseRelationProvider getInverseRelationProvider();

	/**
	 * Checks whether a given relation is defined as system
	 *
	 * @param relationUri
	 *            the relation short URI
	 * @return true if given relation is defined as system
	 */
	Boolean isSystemRelation(String relationUri);

	/**
	 * Returns all relations that can be made between the classes passed as parameters. The parameters can be 'null' and
	 * this means that the filter by this parameter will be skipped. if <code>fromClass</code> is null then this service
	 * will return all classes that can be created to <code>toClass</code>
	 *
	 * @param fromClass
	 *            URI of the Class that is Starting point of the relation
	 * @param toClass
	 *            URI of the Class that is Ending point of the relation
	 * @return List with relations that can be created between these classes
	 */
	List<PropertyInstance> getRelations(String fromClass, String toClass);

	/**
	 * Gets the relations map.
	 *
	 * @return the relations map
	 */
	Map<String, PropertyInstance> getRelationsMap();

	/**
	 * Constructs a hierarchy list that contains all ancestors of the provided type.
	 *
	 * @param classType
	 *            type (URI) which hierarchy to provide.
	 * @return list with the ancestors.
	 */
	List<String> getHierarchy(String classType);

	/**
	 * Observes for event for reloading of the cache of classes and properties. Reloads cache for classes, properties
	 * and relations
	 *
	 * @param event
	 *            The event
	 */
	void observeReloadDefinitionEvent(@Observes LoadSemanticDefinitions event);

	/**
	 * Gets classes that are part of the passed library ID Library
	 *
	 * @param libraryId
	 *            ID of the library
	 * @return List of classes part of the passed library ID Library
	 */
	List<ClassInstance> getLibrary(String libraryId);

	/**
	 * Gets the top level types. These are types that can be instantiated.
	 *
	 * @return the top level types
	 */
	Supplier<Collection<String>> getTopLevelTypes();

	/**
	 * Gets the root class for other classes in the system.
	 *
	 * @return the root class
	 */
	ClassInstance getRootClass();

	/**
	 * Filters relations between the passed as arguments collections with class URI-s
	 *
	 * @param fromClasses
	 *            The collection with domain classes
	 * @param toClasses
	 *            The collection with the range classes
	 * @return List of relations which domain and range match the passed collections
	 */
	List<PropertyInstance> getRelations(Collection<String> fromClasses, Collection<String> toClasses);

	/**
	 * Gets the most concrete class for the given class hierarchy. The classes in the collection must belong to a single
	 * hierarchy and the method should return the class that is as the lowest level in the hierarchy
	 *
	 * @param collection
	 *            the collection of classes to reduce to a single element
	 * @return the most concrete class
	 */
	String getMostConcreteClass(Collection<String> collection);
	
	/**
	 * Recursively collects the subclasses of the provided semantic class.
	 * This method doesn't do any filtration on the classes.
	 * 
	 * @param id
	 *            Full or short uri of the class for which to retrieve
	 *            subclasses.
	 * @return A {@link Set} containing all subclasses.
	 */
	Set<ClassInstance> collectSubclasses(String id);

	/**
	 * Notify the interested parties that the model has been updated in the external data store and they should reload
	 * their model.
	 */
	default void modelUpdated() {}
}
