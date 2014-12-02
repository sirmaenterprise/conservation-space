package com.sirma.itt.semantic.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author kirq4e
 */
public class EMF {

	/** http://ittruse.ittbg.com/ontology/enterpriseManagementFramework# */
	public static final String NAMESPACE = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#";

	/**
	 * Recommended prefix for the EnterpriseManagementFramework namespace: "emf"
	 */
	public static final String PREFIX = "emf";

	/**
	 * An immutable {@link Namespace} constant that represents the Inteligent Document namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	public static final URI DATA_CONTEXT;

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	/**
	 * Project
	 */
	public static final URI PROJECT;

	/**
	 * Case
	 */
	public static final URI CASE;

	/**
	 * Case section
	 */
	public static final URI CASE_SECTION;

	/**
	 * Document
	 */
	public static final URI DOCUMENT;

	/**
	 * Object
	 */
	public static final URI DOMAIN_OBJECT;

	/**
	 * Task
	 */
	public static final URI TASK;

	/** Workflow task. */
	public static final URI BUSINESS_PROCESS_TASK;

	/**
	 * Workflow
	 */
	public static final URI BUSINESS_PROCESS;

	/**
	 * Comment class
	 */
	public static final URI COMMENT;

	/**
	 * Topic class
	 */
	public static final URI TOPIC;

	/**
	 * Section class
	 */
	public static final URI SECTION;

	/** Generic folder class. */
	public static final URI FOLDER;
	/**
	 * Image annotation class
	 */
	public static final URI IMAGE_ANNOTATION;

	/**
	 * Relation class
	 */
	public static final URI RELATION;

	/** Media document class */
	public static final URI MEDIA;

	/** Image document class. */
	public static final URI IMAGE;

	/** Video document class. */
	public static final URI VIDEO;

	/** Intelligent document class (idoc). */
	public static final URI INTELLIGENT_DOCUMENT;

	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	/**
	 * hasText property
	 */
	public static final URI CONTENT;
	/**
	 * commentsOn property
	 */
	public static final URI COMMENTS_ON;
	/**
	 * hasAgent property
	 */
	public static final URI HAS_AGENT;
	/**
	 * isRelatedTo property
	 */
	public static final URI IS_RELATED_TO;
	/**
	 * replyTo property
	 */
	public static final URI REPLY_TO;
	/**
	 * viewBox property
	 */
	public static final URI VIEW_BOX;
	/**
	 * zoomLevel property
	 */
	public static final URI ZOOM_LEVEL;
	/**
	 * svgValue property
	 */
	public static final URI SVG_VALUE;
	/**
	 * externalID property
	 */
	public static final URI EXTERNAL_ID;
	/**
	 * createdOn property
	 */
	public static final URI CREATED_ON;
	/**
	 * hasImageAnnotation property
	 */
	public static final URI HAS_IMAGE_ANNOTATION;
	/**
	 * status property
	 */
	public static final URI STATUS;
	/**
	 * Type property
	 */
	public static final URI TYPE;
	/**
	 * status property
	 */
	public static final URI MODIFIED_ON;

	/**
	 * relation type property
	 */
	public static final URI RELATION_TYPE;

	/**
	 * relation is active property
	 */
	public static final URI IS_ACTIVE;

	/** The inverse relation of a complex relation */
	public static final URI INVERSE_RELATION;

	/**
	 * Business type property
	 */
	public static final URI BUSINESS_TYPE;

	/**
	 * Is deleted flag
	 */
	public static final URI IS_DELETED;

	/**
	 * Created by
	 */
	public static final URI CREATED_BY;

	/**
	 * URI of a instance entity <br>
	 * Predicate used to store the instance URI in order to be indexed in Solr. <br>
	 * <B>TODO:</B> remove on subsequent Ontotext Forest Solr connector release
	 */
	public static final URI URI;

	/** Relation property to identify the source of the relation */
	public static final URI SOURCE;

	/** Relation property to identify the target of the relation */
	public static final URI DESTINATION;

	public static final URI HAS_ASSIGNEE;

	/**
	 * Member variables of EmfInstance class TODO : to be removed
	 */
	public static final URI ID;
	public static final URI DMS_ID;
	public static final URI CONTENT_MGT_ID;
	public static final URI IDENTIFIER;
	public static final URI REVISION;
	public static final URI CONTAINER;
	public static final URI PARENT;
	public static final URI PARENT_TYPE;
	public static final URI DEFINITION_ID;
	public static final URI PURPOSE;

	// end of EmfInstance member variables

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		// init context URIs
		DATA_CONTEXT = factory.createURI("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework");

		// init Class URIs
		PROJECT = factory.createURI(NAMESPACE, "Project");
		CASE = factory.createURI(NAMESPACE, "Case");
		CASE_SECTION = factory.createURI(NAMESPACE, "CaseSection");
		DOCUMENT = factory.createURI(NAMESPACE, "Document");
		DOMAIN_OBJECT = factory.createURI(NAMESPACE, "DomainObject");
		TASK = factory.createURI(NAMESPACE, "Task");
		BUSINESS_PROCESS_TASK = factory.createURI(NAMESPACE, "BusinessProcessTask");
		BUSINESS_PROCESS = factory.createURI(NAMESPACE, "BusinessProcess");
		COMMENT = factory.createURI(NAMESPACE, "Comment");
		TOPIC = factory.createURI(NAMESPACE, "Topic");
		SECTION = factory.createURI(NAMESPACE, "Section");
		FOLDER = factory.createURI(NAMESPACE, "Folder");
		IMAGE_ANNOTATION = factory.createURI(NAMESPACE, "ImageAnnotation");
		RELATION = factory.createURI(NAMESPACE, "Relation");
		MEDIA = factory.createURI(NAMESPACE, "Media");
		IMAGE = factory.createURI(NAMESPACE, "Image");
		VIDEO = factory.createURI(NAMESPACE, "Video");
		INTELLIGENT_DOCUMENT = factory.createURI(NAMESPACE, "IntelligentDocument");

		// init property URIs
		CONTENT = factory.createURI(NAMESPACE, "content");
		COMMENTS_ON = factory.createURI(NAMESPACE, "commentsOn");
		HAS_AGENT = factory.createURI(NAMESPACE, "hasAgent");
		IS_RELATED_TO = factory.createURI(NAMESPACE, "isRelatedTo");
		REPLY_TO = factory.createURI(NAMESPACE, "replyTo");
		VIEW_BOX = factory.createURI(NAMESPACE, "viewBox");
		ZOOM_LEVEL = factory.createURI(NAMESPACE, "zoomLevel");
		SVG_VALUE = factory.createURI(NAMESPACE, "svgValue");
		EXTERNAL_ID = factory.createURI(NAMESPACE, "externalID");
		HAS_IMAGE_ANNOTATION = factory.createURI(NAMESPACE, "hasImageAnnotation");
		CREATED_ON = factory.createURI(NAMESPACE, "createdOn");
		STATUS = factory.createURI(NAMESPACE, "status");
		TYPE = factory.createURI(NAMESPACE, "type");
		MODIFIED_ON = factory.createURI(NAMESPACE, "modifiedOn");
		RELATION_TYPE = factory.createURI(NAMESPACE, "relationType");
		IS_ACTIVE = factory.createURI(NAMESPACE, "isActive");
		INVERSE_RELATION = factory.createURI(NAMESPACE, "inverseRelation");
		BUSINESS_TYPE = factory.createURI(NAMESPACE, "businessType");
		IS_DELETED = factory.createURI(NAMESPACE, "isDeleted");
		CREATED_BY = factory.createURI(NAMESPACE, "createdBy");
		URI = factory.createURI(NAMESPACE, "uri");
		SOURCE = factory.createURI(NAMESPACE, "source");
		DESTINATION = factory.createURI(NAMESPACE, "destination");

		HAS_ASSIGNEE = factory.createURI(NAMESPACE, "hasAssignee");

		// Member variables of EmfInstance class
		// TODO : to be removed
		ID = factory.createURI(NAMESPACE, "id");
		DMS_ID = factory.createURI(NAMESPACE, "dmsId");
		CONTENT_MGT_ID = factory.createURI(NAMESPACE, "contentManagementId");
		IDENTIFIER = factory.createURI(NAMESPACE, "identifier");
		REVISION = factory.createURI(NAMESPACE, "revision");
		CONTAINER = factory.createURI(NAMESPACE, "container");
		PARENT = factory.createURI(NAMESPACE, "parent");
		PARENT_TYPE = factory.createURI(NAMESPACE, "parentType");
		DEFINITION_ID = factory.createURI(NAMESPACE, "definitionId");
		PURPOSE = factory.createURI(NAMESPACE, "purpose");
		// end of EmfInstance member variables
	}

}
