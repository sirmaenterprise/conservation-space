package com.sirma.itt.semantic.search;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.util.List;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.io.ResourceLoadUtil;

/**
 * Enumeration for semantic queries. The query is as String and a list of possible parameters TODO Change ?id to
 * ?instance and ?type with corresponding rdf:type class
 *
 * @author kirq4e
 */
public enum SemanticQueries {

	QUERY_CLASSES_TYPES_FOR_SEARCH("QUERY_CLASSES_TYPES_FOR_SEARCH", "queryClassMetadata.sparql", null),

	QUERY_CLASSES_TYPES("QUERY_CLASSES_TYPES", "queryClassDataForTypesXmlGeneration.sparql", null),

	QUERY_DATA_PROPERTIES("QUERY_DATA_PROPERTIES", "queryDataProperties.sparql", null),

	QUERY_RELATION_PROPERTIES("QUERY_RELATION_PROPERTIES", "queryRelationProperties.sparql", null),

	QUERY_PROPERTY_BY_NAME("QUERY_PROPERTY_BY_NAME",
			"select ?instance (owl:DatatypeProperty as ?instanceType) where {\n" + "{\n"
					+ "?instance a owl:DatatypeProperty .\n" + "bind(STRAFTER(str(?instance), \"#\") as ?label) .\n"
					+ "filter(regex(?label, concat(\"^\", ?labelValue), 'i')).\n" + "}\n" + "union {\n"
					+ "?instance a owl:AnnotationProperty . \n" + "filter(contains(str(?instance), \"dc/term\")). \n"
					+ "filter(STRENDS(str(?instance), ?labelValue)). \n" + "} }",
			null),

	QUERY_CLASS_BY_NAME("QUERY_CLASS_BY_NAME",
			"select ?instance (owl:Class as ?instanceType) where {\n" + "?instance a owl:Class .\n"
					+ "bind(STRAFTER(str(?instance), \"#\") as ?label) .\n"
					+ "filter(regex(?label, ?labelValue, 'i')).\n" + "}\n",
			null),

	QUERY_DEFINITIONS_FOR_CLASS("QUERY_DEFINITIONS_FOR_CLASS",
			"select ?instance (emf:ClassDescription as ?instanceType) ?definitionId where {\n" +
			"   ?instance a owl:Class .\n" +
			"   ?instance emf:hasModel ?definition .\n"	+
			"   ?instance rdfs:label ?label . \n" +
			"   ?definition a emf:Definition . \n"	+
			"   ?definition emf:definitionId ?definitionId .\n" +
			"}",
			null),

	QUERY_GET_LIBRARIES("QUERY_GET_LIBRARIES",
			"SELECT DISTINCT ?instance (emf:ClassDescription as ?instanceType) ?title (\"en\" as ?titleLanguage)"
					+ "(\"description\" as ?description) (\"APPROVED\" as ?status) (\"classDefinition\" as ?definitionId) "
					+ "(\"ontologyClassTemplate\" as ?defaultTemplate) (\"classDefinition\" as ?type) (\"false\"^^xsd:boolean as ?isDeleted) "
					+ "(emf:system as ?creator) where {" + "?instance a emf:Library ;" + " emf:definitionId ?title ."
					+ "}",
			null),

	/** The query libraries as objects sorted by title for passed language in the binding {@code lang} */
	QUERY_LIBRARIES_AS_OBJECTS("QUERY_LIBRARIES_AS_OBJECTS", "queryLibraries.sparql", null),

	QUERY_CLASS_DESCRIPTION("QUERY_CLASS_DESCRIPTION", "queryClassDescription.sparql", null),

	/**
	 * Query instance hierarchy by returning the parent instances and their parents
	 */
	QUERY_INSTANCE_HIERARCHY(
			"QUERY_INSTANCE_HIERARCHY", "select * where {\n" +
			" ?initial ptop:partOf ?parent .\n" +
			" optional { ?parent ptop:partOf ?parentOfParent . }\n" +
			"}", null),

	QUERY_GET_SECURITY_ROLES_FOR_INSTANCE("QUERY_GET_SECURITY_ROLES_FOR_INSTANCE", "querySecurityRolesForInstance.spalrq", null),

	QUERY_GET_ROLES_MAPPING("QUERY_GET_ROLES_MAPPING", "queryGetSecurityRolesMapping.sparql", null)
	;

	private final String name;
	private final String query;
	private final List<StringPair> parameters;

	/**
	 * Initialization constructor
	 *
	 * @param name
	 *            Query name
	 * @param query
	 *            Query string
	 * @param parameters
	 *            Query parameters
	 */
	private SemanticQueries(String name, String query, List<StringPair> parameters) {
		this.name = name;
		// try to load the query as file if not successful use the argument as is
		this.query = getOrDefault(ResourceLoadUtil.loadResource(SemanticQueries.class, query), query);
		this.parameters = parameters;
	}

	/**
	 * Returns value by name
	 *
	 * @param name
	 *            The name of the element
	 * @return value by name or null if there is no value that corresponds to the name
	 */
	public static SemanticQueries getValueByName(String name) {
		for (SemanticQueries query : values()) {
			if (query.getName().equals(name)) {
				return query;
			}
		}
		return null;
	}

	/**
	 * Getter method for query.
	 *
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Getter method for parameters.
	 *
	 * @return the parameters
	 */
	public List<StringPair> getParameters() {
		return parameters;
	}

	/**
	 * Getter method for name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
