package com.sirma.itt.semantic.search;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.domain.StringPair;
import com.sirma.itt.emf.search.NamedQueries;

/**
 * Enumeration for semantic queries. The query is as String and a list of possible parameters TODO
 * Change ?id to ?instance and ?type with corresponding rdf:type class
 *
 * @author kirq4e
 */
public enum SemanticQueries {

	QUERY_CLASSES_TYPES_FOR_SEARCH(
			"QUERY_CLASSES_TYPES_FOR_SEARCH",
			"SELECT distinct ?instance (owl:Class as ?instanceType) ?title (if(lang(?title)= \"\", \"en\", lang(?title)) as ?titleLanguage) ?definition ?superClass ((if(!bound(?isSearchable), \"false\"^^xsd:boolean , ?isSearchable)) as ?searchable) ((if(!bound(?isPartOfObjectLibrary), \"false\"^^xsd:boolean , ?isPartOfObjectLibrary)) as ?partOfObjectLibrary) where {\n"
					+ "{\n"
					+ "?instance a owl:Class ;\n"
					+ "rdfs:label ?title ;\n"
					+ "skos:definition|rdfs:comment ?definition .\n"
					+ "OPTIONAL{?instance emf:isSearchable ?isSearchable }\n"
					+ "OPTIONAL{?instance emf:isPartOfObjectLibrary ?isPartOfObjectLibrary }\n"
					+ "OPTIONAL{?instance rdfs:subClassOf ?superClass . }\n"
					+ "FILTER (!isBlank(?superClass)) .\n"
					+ "} union { \n"
					+ "bind(ptop:Entity as ?instance)\n"
					+ "?instance a owl:Class ;\n"
					+ "rdfs:label ?title ;\n"
					+ "skos:definition|rdfs:comment ?definition .\n"
					+ "OPTIONAL{?instance emf:isSearchable ?isSearchable }\n"
					+ "OPTIONAL{?instance emf:isPartOfObjectLibrary ?isPartOfObjectLibrary }\n"
					+ "OPTIONAL{?instance rdfs:subClassOf ?superClass . }\n" + "}\n" + "}\n", null),

	QUERY_DATA_PROPERTIES(
			"QUERY_DATA_PROPERTIES",
			"SELECT distinct ?instance (owl:DatatypeProperty as ?instanceType) ?title ?definition ((if (bound(?domainClass1), ?domainClass1, ptop:Entity)) as ?domainClass) ((if (bound(?rangeClass1), ?rangeClass1, xsd:string)) as ?rangeClass) where {"
					+ "?property a owl:DatatypeProperty ; "
					+ "rdfs:comment|skos:definition ?definition ;"
					+ "rdfs:label ?title . "
					+ "bind (str(?property) as ?instance) . "
					+ "optional {?property rdfs:domain ?domainClass1 . } "
					+ "optional {?property rdfs:range ?rangeClass1 . } " + "}", null),

	QUERY_RELATION_PROPERTIES(
			"QUERY_RELATION_PROPERTIES",
			"SELECT distinct ?instance (emf:Relation as ?instanceType) ?title ?definition ((if (bound(?domainClass1), ?domainClass1, ptop:Entity)) as ?domainClass) ?rangeClass ?inverseRelation where {\n"
					+ "?instance a owl:ObjectProperty ;\n "
					+ "emf:isSearchable \"true\"^^xsd:boolean ; \n "
					+ "rdfs:comment|skos:definition ?definition ;\n"
					+ "rdfs:label ?title .\n"
					+ "optional {?instance rdfs:domain ?domainClass1 . }\n"
					+ "optional {?instance rdfs:range ?rangeClass . }\n"
					+ "{\n"
					+ "optional {?instance owl:inverseOf ?inverseRelation . }\n"
					+ "}\n"
					+ "}\n", null),

	QUERY_PROPERTY_BY_NAME("QUERY_PROPERTY_BY_NAME",
			"select ?instance (owl:DatatypeProperty as ?instanceType) where {\n" + "{\n"
					+ "?instance a owl:DatatypeProperty .\n"
					+ "bind(STRAFTER(str(?instance), \"#\") as ?label) .\n"
					+ "filter(regex(?label, concat(\"^\", ?labelValue), 'i')).\n" + "}\n"
					+ "union {\n" + "?instance a owl:AnnotationProperty . \n"
					+ "filter(contains(str(?instance), \"dc/term\")). \n"
					+ "filter(STRENDS(str(?instance), ?labelValue)). \n" + "} }", null),

	QUERY_CLASS_BY_NAME("QUERY_CLASS_BY_NAME",
			"select ?instance (owl:Class as ?instanceType) where {\n" + "?instance a owl:Class .\n"
					+ "bind(STRAFTER(str(?instance), \"#\") as ?label) .\n"
					+ "filter(regex(?label, ?labelValue, 'i')).\n" + "}\n", null),

	QUERY_GET_TOPICS_BY_OBJECT_ID(
			"QUERY_GET_TOPICS_BY_OBJECT_ID",
			"SELECT distinct ?instance ?instanceType ?type ?status ?object ?objectType ?objectSection ?title ?content ?createdOn ?modifiedOn ?createdBy ?tags ?imageAnnotation ?svgValue ?zoomLevel ?viewBox  WHERE { " // (count(
																																																						// distinct
																																																						// ?reply)
																																																						// as
																																																						// ?comments)
					+ "?instance a emf:Topic. "
					+ "?instance emf:commentsOn ?object . "
					+ "?instance emf:commentsOnSection ?objectSection . "
					+ "?instance emf:status ?status ;"
					+ "emf:createdBy ?createdBy ; "
					+ "emf:createdOn ?createdOn . "
					+ "optional {?instance dc:title ?title .} "
					+ "optional {?instance emf:modifiedOn ?modifiedOn .} "
					+ " {category} "
					+ "optional {?instance emf:content ?content .} " // this should not be optional
																		// but for the old comments
																		// to work it is - until db
																		// fix is implemented
					+ " {objectTypeParameter} "
					+ "?object a ?ot. ?ot emf:definitionId ?objectType . "
					+ " {imageAnnotation} "
					+ "?instance a ?it. ?it emf:definitionId ?instanceType . "// this one could be omitted??
					// + "?reply emf:replyTo ?instance. "
					+ " {tags} "
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean . "
					+ " {datesFilter} " + "} "
					// +
					// "group by ?instance ?instanceType ?type ?status ?object ?objectType ?objectSection ?title ?content ?createdOn ?createdBy ?tags ?parent ?imageAnnotation ?svgValue ?zoomLevel ?viewBox "
					+ "",
			Arrays.asList(
					new StringPair("datesFilter", ""),
					new StringPair("filter", "createdOn"),
					new StringPair("tags", "optional {?instance emf:tag ?tags .}"),
					new StringPair("category", "optional {?instance emf:type ?type .}"),
					new StringPair("objectTypeParameter", ""),
					new StringPair(
							"imageAnnotation",
							"optional { ?instance emf:hasImageAnnotation ?imageAnnotation . ?imageAnnotation emf:svgValue ?svgValue ; emf:zoomLevel ?zoomLevel ; emf:viewBox ?viewBox. }"),
					new StringPair("categoryFilter", ""), // adds the regex for category filtering
					new StringPair("tagFilter", ""))), // adds the regex for tags filtering

	QUERY_GET_COMMENT_BY_ID("QUERY_GET_COMMENT_BY_ID",
			"SELECT distinct ?instance ?instanceType ?content ?createdOn ?createdBy ?parent WHERE {"
					+ "?instance a emf:Comment . " + "?instance emf:content ?content ; "
					+ "emf:createdBy ?createdBy ;" + "emf:createdOn ?createdOn . "
					+ "?parent a emf:Topic .  " + "?instance emf:replyTo ?parent. "
					+ "?instance sesame:directType ?instanceType . "
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) . "
					+ "} order by ASC(?createdOn)", null),

	QUERY_GET_COMMENT_FOR_TOPIC_ID("QUERY_GET_COMMENT_FOR_TOPIC_ID",
			"SELECT distinct ?instance ?instanceType ?content ?createdOn ?createdBy ?parent WHERE {"
					+ "?parent a emf:Topic .  " + "?instance emf:replyTo ?parent. "
					+ "?instance emf:createdOn ?createdOn . " + " {filterBy} "
					+ "?instance a emf:Comment . " + "?instance emf:content ?content ; "
					+ "emf:createdBy ?createdBy ." + "?instance sesame:directType ?instanceType . "
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) . "
					+ "} order by ASC(?createdOn)", Arrays.asList(new StringPair("filterBy", ""))),

	@Deprecated
	QUERY_GET_COMMENT_BY_OBJECT_ID(
			"QUERY_GET_COMMENT_BY_OBJECT_ID",
			"SELECT distinct ?instance ?instanceType ?type ?status ?object ?objectType ?objectSection ?title ?content ?createdOn ?createdBy ?tags ?parent ?imageAnnotation ?svgValue ?zoomLevel ?viewBox WHERE {"
					+ "{ ?instance a emf:Topic. "
					+ "?instance emf:commentsOn ?object ; "
					+ "dc:title ?title ; "
					+ "emf:type ?type ; "
					+ "emf:status ?status ."
					+ "optional {?instance emf:tag ?tags .} "
					+ "?object sesame:directType ?objectType . "
					+ "?instance emf:commentsOnSection ?objectSection . "
					+ "optional { ?instance emf:hasImageAnnotation ?imageAnnotation . "
					+ "?imageAnnotation emf:svgValue ?svgValue ; "
					+ "emf:zoomLevel ?zoomLevel ; "
					+ "emf:viewBox ?viewBox. } "
					+ " } union {?instance sesame:directType emf:Comment ; emf:content ?content . ?instance emf:replyTo ?parent. ?parent a emf:Topic . ?parent emf:commentsOn ?object . ?parent emf:commentsOnSection ?objectSection .} .  "
					+ "?instance sesame:directType ?instanceType . "
					+ "?instance emf:createdBy ?createdBy ; "
					+ "emf:createdOn ?createdOn . "
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) . "
					+ "} order by ASC(?createdOn)", null),

	QUERY_GET_USER_BY_USERNAME("QUERY_GET_USER_BY_USERNAME", "", null),

	/**
	 * Retrieve all topics in which the bound user is involved by following criteria <LI>all Topics,
	 * created by given user <LI>all Topics, on which the current user has commented <LI>all Topics,
	 * on instances the given user has created
	 */
	@Deprecated
	QUERY_GET_USER_COMMENTS(
			"QUERY_GET_USER_COMMENTS",
			"SELECT distinct ?instance ?instanceType ?type ?status ?object ?objectType ?objectSection ?title ?content ?createdOn ?createdBy ?parent WHERE {"
					+ "{"
					+ "?instance a emf:Topic ."
					+ "{"
					+ "?instance emf:createdBy ?owner ."
					+ "} UNION"
					+ "{"
					+ "?comment emf:replyTo ?instance ."
					+ "?comment emf:createdBy ?owner ."
					+ "} UNION"
					+ "{"
					+ "?object emf:createdBy ?owner ."
					+ "}"
					+ "?instance emf:commentsOn ?object ;"
					+ "dc:title ?title ;"
					+ "emf:type ?type ;"
					+ "emf:status ?status ."
					+ "?instance emf:commentsOnSection ?objectSection ."
					+ "}"
					+ "UNION {"
					+ "{ "
					+ "	?topic emf:createdBy ?owner . "
					+ "	} UNION  { "
					+ "		?comment emf:replyTo ?topic . "
					+ "		?comment emf:createdBy ?owner . "
					+ "	} UNION { "
					+ "		?object emf:createdBy ?owner . "
					+ "	}"
					+ "?instance a emf:Comment ;"
					+ "emf:content ?content ."
					+ "?instance emf:replyTo ?topic."
					+ "?topic a emf:Topic ."
					+ "?topic emf:commentsOn ?object ."
					+ "?instance emf:replyTo ?parent . "
					+ "} ."
					+ "?instance emf:createdOn ?createdOn ."
					+ "?object sesame:directType ?objectType ."
					+ "?instance sesame:directType ?instanceType ."
					+ "?instance emf:createdBy ?createdBy ."
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean . "
					+ "?object emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) . "
					// + "filter ( NOT EXISTS { ?object emf:isDeleted \"true\"^^xsd:boolean } ) . "
					+ "}", null),
	/**
	 * Retrieve all topics in which the bound user is involved by following criteria <LI>all Topics,
	 * created by given user <LI>all Topics, on which the current user has commented <LI>all Topics,
	 * on instances the given user has created
	 */
	QUERY_GET_USER_TOPICS(
			"QUERY_GET_USER_TOPICS",
			"SELECT distinct ?instance ?instanceType ?type ?status ?object ?objectType ?objectSection ?title ?content ?createdOn ?modifiedOn ?createdBy ?tags WHERE {"
					+ " ?instance a emf:Topic ."
					+ " {"
					+ " ?instance emf:createdBy ?owner ."
					+ " } UNION"
					+ " {"
					+ " ?comment emf:replyTo ?instance ."
					+ " ?comment emf:createdBy ?owner ."
					+ " } UNION"
					+ " {"
					+ " ?object emf:createdBy ?owner ."
					+ " }"
					+ " ?instance emf:commentsOn ?object ;"
					+ " emf:status ?status ; "
					+ " emf:createdOn ?createdOn ;"
					+ " emf:createdBy ?createdBy  "
					+ " optional {?instance dc:title ?title .}"
					+ " optional {?instance emf:content ?content .}"
					+ " optional {?instance emf:modifiedOn ?modifiedOn .} "
					+ "	{category} "
					+ " ?instance emf:commentsOnSection ?objectSection ."
					+ " {objectTypeParameter} "
					+ " ?object a ?ot. ?ot emf:definitionId ?objectType ."
					+ " ?instance a ?it. ?it emf:definitionId ?instanceType ."
					+ " {tags} "
					+ " {datesFilter} "
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean . "
					+ "?object emf:isDeleted \"false\"^^xsd:boolean . " + " }", Arrays.asList(
					new StringPair("datesFilter", ""), new StringPair("filter", "createdOn"),
					new StringPair("objectTypeParameter", " ?object a ?objectTypeParameter . "),
					new StringPair("tags", "optional {?instance emf:tag ?tags .}"), new StringPair(
							"category", "optional {?instance emf:type ?type . }"), new StringPair(
							"categoryFilter", ""), new StringPair("tagFilter", ""))),

	/**
	 * Retrieve all topic on bound instance and and all topics on its children
	 */
	@Deprecated
	QUERY_GET_INSTANCE_SUCCESSORS_COMMENTS(
			"QUERY_GET_INSTANCE_SUCCESSORS_COMMENTS",
			"SELECT distinct ?instance ?instanceType ?type ?status ?object ?objectType ?objectSection ?title ?createdOn ?createdBy ?content ?parent WHERE { "
					+ "{ "
					+ "	{ "
					+ "		?instance emf:commentsOn ?objectParameter . "
					+ "	} "
					+ "	UNION "
					+ "	{ "
					+ "	?instance emf:commentsOn ?childInstance . "
					+ "	?childInstance ptop:partOf ?objectParameter . "
					+ "	} "
					+ "	?instance a emf:Topic ; "
					+ "		dc:title ?title ; "
					+ "		emf:type ?type ; "
					+ "		emf:status ?status . "
					+ "	?instance emf:commentsOn ?object . "
					+ "?object emf:isDeleted \"false\"^^xsd:boolean . "
					// + "filter ( NOT EXISTS { ?object emf:isDeleted \"true\"^^xsd:boolean } ) . "
					+ "?instance emf:commentsOnSection ?objectSection .  "
					+ "?object sesame:directType ?objectType . "
					+ "} "
					+ "UNION { "
					+ "	?instance a emf:Comment ;  "
					+ "	emf:content ?content . "
					+ "	?instance emf:replyTo ?topic. "
					+ "	?topic a emf:Topic . "
					+ "	{ ?topic emf:commentsOn ?objectParameter . } "
					+ "		UNION "
					+ "	{ "
					+ "	?topic emf:commentsOn ?childInstance . "
					+ "	?childInstance ptop:partOf ?objectParameter . "
					+ "?childInstance emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?childInstance emf:isDeleted \"true\"^^xsd:boolean } ) . "
					+ "	} "
					+ "	?instance emf:replyTo ?parent .  "
					+ "} . "
					+ "?instance emf:createdOn ?createdOn . "
					+ "?instance emf:createdBy ?createdBy . "
					+ "?instance sesame:directType ?instanceType . "
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) .  "
					+ "} ", null),
	/**
	 * Retrieve all topic on bound instance and and all topics on its children
	 */
	QUERY_GET_INSTANCE_SUCCESSORS_TOPICS(
			"QUERY_GET_INSTANCE_SUCCESSORS_TOPICS",
			"SELECT distinct ?instance ?instanceType ?type ?status ?object ?objectType ?objectSection ?title ?createdOn ?modifiedOn ?createdBy ?content ?tags WHERE { "
					+ " 	{ "
					+ " 		?instance emf:commentsOn ?objectParameter . "
					+ " 	} "
					+ " 	UNION "
					+ " 	{ "
					+ "		?instance emf:commentsOn ?childInstance . "
					+ "		?childInstance ptop:partOf ?objectParameter . "
					+ "		?childInstance a ?objectTypeParameter . "
					+ " 	} "
					+ " ?instance a emf:Topic ; "
					+ "	emf:status ?status ; "
					+ "	emf:createdOn ?createdOn . "
					+ "	?instance emf:createdBy ?createdBy . "
					+ " ?instance emf:commentsOn ?object . "
					+ " ?instance emf:commentsOnSection ?objectSection .  "
					+ " ?object a ?ot. ?ot emf:definitionId ?objectType . "
					+ "	{category} "
					+ " optional {?instance dc:title ?title .}"
					+ " optional {?instance emf:modifiedOn ?modifiedOn .} "
					+ " optional {?instance emf:content ?content .}"
					+ " ?instance a ?it. ?it emf:definitionId ?instanceType . "
					+ " {tags} "
					+ " {datesFilter} "
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean . "
					+ "?object emf:isDeleted \"false\"^^xsd:boolean . "
					// + " filter ( NOT EXISTS { ?object emf:isDeleted \"true\"^^xsd:boolean } ) . "
					// +
					// " filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) .  "
					+ " } ", Arrays.asList(new StringPair("datesFilter", ""), new StringPair(
					"filter", "createdOn"), new StringPair("category",
					"optional {?instance emf:type ?type . }"), new StringPair("tags",
					"optional {?instance emf:tag ?tags .}"), new StringPair("categoryFilter", ""),
					new StringPair("tagFilter", ""))),

	/**
	 * Query all workflows for User, Project or Case. </br>For Project and Case pass the parameter
	 * ?object </br> For Users pass the parameter ?user
	 */
	QUERY_ALL_WORKFLOWS_FOR_OBJECT(
			NamedQueries.QUERY_ALL_WORKFLOWS_FOR_OBJECT,
			"SELECT DISTINCT ?instance ?instanceType ?default_header ?compact_header ?breadcrumb_header ?createdBy ?status ?container ?revision (if(bound(?identifier), ?identifier, ?type) as ?definitionId)  WHERE { "
					+ "?instance a emf:BusinessProcess ."
					+ "?instance sesame:directType ?instanceType ."
					+ "?instance ptop:partOf ?object ."
					+ "?instance emf:createdOn ?createdOn ."
					+ "?instance emf:default_header ?default_header . "
					+ "?instance emf:compact_header ?compact_header . "
					+ "?instance emf:breadcrumb_header ?breadcrumb_header . "
					+ "?instance emf:createdBy ?createdBy ."
					+ "?instance emf:status ?status ."
					+ "?instance emf:container ?container ."
					+ "?instance emf:revision ?revision ."
					+ "OPTIONAL {?instance emf:definitionId ?identifier .} "
					+ "?instance emf:type ?type ."
					+ "{"
					+ "{ ?instance emf:createdBy ?user .}"
					+ "UNION { ?task ptop:partOf ?instance ."
					+ "?task a emf:BusinessProcessTask ."
					+ "?task emf:hasAssignee ?user ."
					+ "} }"
					+ " ?instance emf:isDeleted \"false\"^^xsd:boolean . "
					+ " ?object emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) .  "
					// + "filter ( NOT EXISTS { ?object emf:isDeleted \"true\"^^xsd:boolean } ) ."
					+ "} ORDER BY DESC(?createdOn)", null),

	/**
	 * Not Completed workflows - all workflows which are not in end state (Completed or Cancelled)
	 * For Project and Case pass the parameter ?object </br> For Users pass the parameter ?user
	 * </br> Returns all Business process that are in states IN_PROGRESS, ON_HOLD and APPROVED
	 */
	QUERY_NOT_COMPLETED_WORKFLOWS_FOR_OBJECT(
			NamedQueries.QUERY_NOT_COMPLETED_WORKFLOWS_FOR_OBJECT,
			"SELECT DISTINCT ?instance ?instanceType ?default_header ?compact_header ?breadcrumb_header ?createdBy ?status ?container ?revision (if(bound(?identifier), ?identifier, ?type) as ?definitionId)  WHERE { "
					+ "?instance a emf:BusinessProcess ."
					+ "?instance sesame:directType ?instanceType ."
					+ "?instance ptop:partOf ?object ."
					+ "{{?instance emf:status \"IN_PROGRESS\"} UNION {?instance emf:status \"ON_HOLD\"} UNION {?instance emf:status \"APPROVED\"} }"
					+ "?instance emf:createdOn ?createdOn . "
					+ "?instance emf:default_header ?default_header . "
					+ "?instance emf:compact_header ?compact_header . "
					+ "?instance emf:breadcrumb_header ?breadcrumb_header . "
					+ "?instance emf:createdBy ?createdBy ."
					+ "?instance emf:status ?status ."
					+ "?instance emf:container ?container ."
					+ "?instance emf:revision ?revision ."
					+ "OPTIONAL {?instance emf:definitionId ?identifier .} "
					+ "?instance emf:type ?type ."
					+ "{"
					+ "{ ?instance emf:createdBy ?user .}"
					+ "UNION { ?task ptop:partOf ?instance ."
					+ "?task a emf:BusinessProcessTask ."
					+ "?task emf:hasAssignee ?user ."
					+ "} }"
					+ " ?instance emf:isDeleted \"false\"^^xsd:boolean . "
					+ " ?object emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) .  "
					// + "filter ( NOT EXISTS { ?object emf:isDeleted \"true\"^^xsd:boolean } ) . "
					+ "} ORDER BY DESC(?createdOn)", null),

	/**
	 * High Priority workflows - Not Completed workflows, which are only with High Priority </br>
	 * For Project and Case pass the parameter ?object </br> For Users pass the parameter ?user
	 * </br> Returns all Business process that are in states IN_PROGRESS, ON_HOLD and APPROVED
	 */
	QUERY_NOT_COMPLETED_WORKFLOWS_FOR_OBJECT_WITH_HIGH_PRIORITY(
			NamedQueries.QUERY_NOT_COMPLETED_WORKFLOWS_FOR_OBJECT_WITH_HIGH_PRIORITY,
			"SELECT DISTINCT ?instance ?instanceType ?default_header ?compact_header ?breadcrumb_header ?createdBy ?status ?container ?revision (if(bound(?identifier), ?identifier, ?type) as ?definitionId) WHERE { "
					+ "?instance a emf:BusinessProcess ."
					+ "?instance sesame:directType ?instanceType ."
					+ "?instance ptop:partOf ?object ."
					+ "?instance emf:priority \"0006-000085\"."
					+ "{{?instance emf:status \"IN_PROGRESS\"} UNION {?instance emf:status \"ON_HOLD\"} UNION {?instance emf:status \"APPROVED\"} } ."
					+ "?instance emf:createdOn ?createdOn . "
					+ "?instance emf:default_header ?default_header . "
					+ "?instance emf:compact_header ?compact_header . "
					+ "?instance emf:breadcrumb_header ?breadcrumb_header . "
					+ "?instance emf:createdBy ?createdBy ."
					+ "?instance emf:status ?status ."
					+ "?instance emf:container ?container ."
					+ "?instance emf:revision ?revision ."
					+ "OPTIONAL {?instance emf:definitionId ?identifier .} "
					+ "?instance emf:type ?type ."
					+ "{"
					+ "{ ?instance emf:createdBy ?user .}"
					+ "UNION { ?task ptop:partOf ?instance ."
					+ "?task a emf:BusinessProcessTask ."
					+ "?task emf:hasAssignee ?user ."
					+ "} }"
					+ " ?instance emf:isDeleted \"false\"^^xsd:boolean . "
					+ " ?object emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) .  "
					// + "filter ( NOT EXISTS { ?object emf:isDeleted \"true\"^^xsd:boolean } ) . "
					+ "} ORDER BY DESC(?createdOn)", null),

	/**
	 * *Overdue workflows - workflows which end date is before today and the workflows are still not
	 * completed.</br> For Project and Case pass the parameter ?object </br> For Users pass the
	 * parameter ?user </br> Returns all Business process that are in states IN_PROGRESS, ON_HOLD
	 * and APPROVED
	 */
	QUERY_NOT_COMPLETED_OVERDUE_WORKFLOWS_FOR_OBJECT(
			NamedQueries.QUERY_NOT_COMPLETED_OVERDUE_WORKFLOWS_FOR_OBJECT,
			"SELECT DISTINCT ?instance ?instanceType ?default_header ?compact_header ?breadcrumb_header ?createdBy ?status ?container ?revision (if(bound(?identifier), ?identifier, ?type) as ?definitionId)  WHERE { "
					+ "?instance a emf:BusinessProcess ."
					+ "?instance sesame:directType ?instanceType ."
					+ "?instance ptop:partOf ?object ."
					+ "?instance  emf:plannedEndDate ?planedEndDate ."
					+ "{{?instance emf:status \"IN_PROGRESS\"} UNION {?instance emf:status \"ON_HOLD\"} UNION {?instance emf:status \"APPROVED\"} } ."
					+ "?instance emf:createdOn ?createdOn . "
					+ "?instance emf:default_header ?default_header . "
					+ "?instance emf:compact_header ?compact_header . "
					+ "?instance emf:breadcrumb_header ?breadcrumb_header . "
					+ "?instance emf:createdBy ?createdBy ."
					+ "?instance emf:status ?status ."
					+ "?instance emf:container ?container ."
					+ "?instance emf:revision ?revision ."
					+ "OPTIONAL {?instance emf:definitionId ?identifier .} "
					+ "?instance emf:type ?type ."
					+ "{"
					+ "{ ?instance emf:createdBy ?user .}"
					+ "UNION { ?task ptop:partOf ?instance ."
					+ "?task a emf:BusinessProcessTask ."
					+ "?task emf:hasAssignee ?user ."
					+ "} }"
					+ " ?instance emf:isDeleted \"false\"^^xsd:boolean . "
					+ " ?object emf:isDeleted \"false\"^^xsd:boolean . "
					// +
					// "filter ( NOT EXISTS { ?instance emf:isDeleted \"true\"^^xsd:boolean } ) .  "
					// + "filter ( NOT EXISTS { ?object emf:isDeleted \"true\"^^xsd:boolean } ) . "
					+ "filter (?planedEndDate <= now() ) . " + "} ORDER BY DESC(?createdOn)", null),

	/**
	 * Query all cases and projects to which the given event entity has any kind of relation. Need
	 * to pass ?objectId as parameter
	 */
	QUERY_CONTEXT_FOR_AUDIT_ENTRY(
			NamedQueries.QUERY_CONTEXT_FOR_AUDIT_ENTRY,
			"SELECT DISTINCT  ?instance ?instanceType where {"
					+ "{{?objectId ?relation ?instance . ?relation a emf:Relation .} UNION {?objectId ptop:partOf ?instance}}."
					+ "?instance sesame:directType ?instanceType ."
					+ "{{?instance a emf:Case} UNION {?instance a emf:Project}} ." + "}", null),

	/**
	 * Query all classes that has the flag isPartOfObjectLibrary
	 */
	@Deprecated
	QUERY_CLASSES_PART_OF_OBJECT_LIBRARY(NamedQueries.QUERY_CLASSES_PART_OF_OBJECT_LIBRARY,
			"select ?instance (owl:Class as ?instanceType) ?title ?language where {"
					+ "?instance a rdfs:Class ."
					+ "?instance emf:isPartOfObjectLibrary \"true\"^^xsd:boolean ."
					+ "?instance rdfs:label ?title ."
					+ "bind(if(lang(?title)= \"\", \"en\", lang(?title)) as ?language)"
					+ "} order by ?title", null),

	/**
	 * Executes solr query through sparql query
	 */
	QUERY_SOLR_SEARCH(NamedQueries.QUERY_SOLR_SEARCH,
			"SELECT DISTINCT ?instance ?instanceType WHERE {" + "?instance a ?rdftype."
					+ "?rdftype emf:isSearchable \"true\"^^xsd:boolean ."
					+ "?instance sesame:directType ?instanceType ."
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean ."
					+ "?instance {orderBy} ?orderBy ." + "?search a solr:{core} ; "
					+ "solr:query ?query ; " + "solr:entities ?instance . } "
					+ "ORDER BY {orderDirection}( ?orderBy ) LIMIT {limit} OFFSET {offset}", Arrays
					.asList(new StringPair("core", "ftsearch"), new StringPair("orderBy",
							"emf:modifiedOn"), new StringPair("orderDirection", "desc"),
							new StringPair("limit", "25"), new StringPair("offset", "0"))),

	/**
	 * Executes solr query through sparql query and return total number of results
	 */
	QUERY_SOLR_SEARCH_TOTAL_RESULTS(
			NamedQueries.QUERY_SOLR_SEARCH_TOTAL_RESULTS,
			"SELECT (count(distinct ?instance) as ?count) WHERE { SELECT DISTINCT ?instance ?instanceType WHERE {"
					+ "?instance a ?rdftype."
					+ "?rdftype emf:isSearchable \"true\"^^xsd:boolean ."
					+ "?instance sesame:directType ?instanceType ."
					+ "?instance emf:isDeleted \"false\"^^xsd:boolean ."
					+ "?search a solr:{core} ; "
					+ "solr:query ?query ; "
					+ "solr:entities ?instance . } LIMIT 1000 }", Arrays.asList(new StringPair(
					"core", "ftsearch")));

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
		this.query = query;
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
