package com.sirma.itt.emf.definition;

import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;

import com.sirma.itt.emf.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.instance.model.PropertyInstance;

/**
 * Interface for Semantic definitions service that will provide methods for extraction of the
 * hierarchy of classes, properties and relations from the semantic
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
	 *            Class instance identifier.
	 * @return {@link ClassInstance} or {@code null} if no class with that identifier is found in
	 *         the cache.
	 */
	ClassInstance getClassInstance(String identifier);

	/**
	 * Returns all classes that can be used when searching from the semantics and their hierarchy
	 *
	 * @return All classes from the semantics and their hierarchy
	 */
	List<ClassInstance> getSearchableClasses();

	/**
	 * Returns all literal properties from the semantics
	 *
	 * @return All literal properties from the semantics
	 */
	List<PropertyInstance> getProperties();

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
	 * Returns relation data by relation URI. This method accepts full URI of the relation
	 *
	 * @param relationUri
	 *            The Full URI of the relation
	 * @return Relation data - labels, definitions
	 */
	PropertyInstance getRelation(String relationUri);

	/**
	 * Returns all relations that can be made between the classes passed as parameters. The
	 * parameters can be 'null' and this means that the filter by this parameter will be skipped. if
	 * <code>fromClass</code> is null then this service will return all classes that can be created
	 * to <code>toClass</code>
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
	 * Observes for event for reloading of the cache of classes and properties. Reloads cache for
	 * classes, properties and relations
	 *
	 * @param event
	 *            The event
	 */
	void observeReloadDefinitionEvent(@Observes LoadSemanticDefinitions event);

	/**
	 * Gets classes that are part of Object Library
	 *
	 * @return List of classes part of Object Library
	 */
	List<ClassInstance> getObjectLibrary();

}
