package com.sirma.itt.semantic.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * EMF Ontology model
 *
 * @author kirq4e
 */
public final class EMF {

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

	public static final URI DEFINITIONS_CONTEXT;

	public static final URI ANNOTATIONS_CONTEXT;
	
	public static final URI CLASS_DESCRIPTION_CONTEXT;

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

	/** Audio document class. */
	public static final URI AUDIO;

	/** Intelligent document class (idoc). */
	public static final URI INTELLIGENT_DOCUMENT;

	public static final URI USER;

	public static final URI GROUP;

	public static final URI CLASS_DESCRIPTION;

	/**
	 * Filter used to save search criteria of basic or advanced searches that can be loaded later.
	 */
	public static final URI SAVED_SEARCH;

	/**
	 * Class representing tags in the system which can be used to tag different instances.
	 */
	public static final URI TAG;

	/**
	 * Class representing help objects in the system which will be displayed when the user requests help.
	 */
	public static final URI HELP;
	/**
	 * Definition class.
	 */
	public static final URI DEFINITION;

	/** Generic property that is described in a definition and should be loaded for an instance. */
	public static final URI DEFINITION_PROPERTY;

	/** Literal property that is described in a definition and should be loaded for an instance. */
	public static final URI DEFINITION_DATA_PROPERTY;

	/** Object property that is described in a definition and should be loaded for an instance. */
	public static final URI DEFINITION_OBJECT_PROPERTY;

	/** Base class for activities like projects, tasks etc */
	public static final URI ACTIVITY;

	/**
	 * Class representing Project Library elements
	 */
	public static final URI LIBRARY;
	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	/*
	 * Model properties
	 */
	/** If the semantic class could be searched via the user interface or not. */
	public static final URI IS_SEARCHABLE;

	/** If an instance of the given class could be created by the user via the user interface. */
	public static final URI IS_CREATEABLE;

	/** If the user could upload a file of the given class via the user interface. */
	public static final URI IS_UPLOADABLE;

	/** If an instance of the given class is versionable or rather that its content is versionable. */
	public static final URI IS_VERSIONABLE;

	/**
	 * If present specifies the pattern to be used for validating the allowed uploadable files if they could be of
	 * particular type.
	 * <p>
	 * For example:<br>
	 * Given:
	 *
	 * <pre>
	 * <code>emf:IntelligentDocument emf:acceptDataTypePattern "text/x?html|application/xml".</code>
	 * </pre>
	 *
	 * Will allow only xhtml/html or xml files to be allowed for upload with type for emf:IntelligentDocument
	 */
	public static final URI ACCEPT_DATATYPE_PATTERN;

	/** Class property that represents the class category: case, project, document, etc. */
	public static final URI CLASS_CATEGORY;

	/**
	 * hasText property
	 */
	public static final URI CONTENT;
	/**
	 * hasText property for the instance view
	 */
	public static final URI VIEW;

	/**
	 * Text property where contents fo widget will be save.
	 */
	public static final URI VIEW_WIDGETS_CONTENT;
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
	 * Modified on property
	 */
	public static final URI MODIFIED_ON;
	/**
	 * Modified by property
	 */
	public static final URI MODIFIED_BY;
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
	 * System property marker
	 */
	public static final URI IS_SYSTEM_PROPERTY;

	/**
	 * Created by
	 */
	public static final URI CREATED_BY;

	/** Deleted on */
	public static final URI DELETED_ON;

	/** Deleted by */
	public static final URI DELETED_BY;

	/** Relation property to identify the source of the relation */
	public static final URI SOURCE;

	/** Relation property to identify the target of the relation */
	public static final URI DESTINATION;

	public static final URI HAS_ASSIGNEE;

	/**
	 * Defines a relation between 2 instances and indicates that the second instance is attached to the first one.
	 * Inverse of {@link #IS_ATTACHED_TO}
	 */
	public static final URI HAS_ATTACHMENT;
	/**
	 * Defines a relation between 2 instances and indicates that the first instance is attached to the second one.
	 * Inverse of {@link #HAS_ATTACHMENT}
	 */
	public static final URI IS_ATTACHED_TO;

	/**
	 * Predicated indicating the actual instance that the current instance represents. Used in the revisioning context
	 * to indicate that an object is a proxy to other object.
	 */
	public static final URI ACTUAL_OF;

	/**
	 * Predicate for revision instances to determine the revision type of the current instance. Possible object values
	 * are: {@link #TYPE_CURRENT}, {@link #TYPE_REVISION} and {@link #TYPE_LATEST_REVISION}. The predicate can have more
	 * that one object at a time.
	 */
	public static final URI REVISION_TYPE;

	/**
	 * Revision type that identifies an instance that is a revision of an instance.<br>
	 * Instance marked with this type should be considered as archive revision. <br>
	 * If combined with {@link #TYPE_LATEST_REVISION} will mean that will be considered for the latest approved
	 * revision.
	 */
	public static final URI TYPE_REVISION;

	/**
	 * Revision type that identifies an instance that should be considered as dynamic latest approved revision. <br>
	 * This could be combined with {@link #TYPE_REVISION} and {@link #TYPE_CURRENT}.
	 */
	public static final URI TYPE_LATEST_REVISION;

	/**
	 * Revision type that identifies an instance that should be considered as the current draft or the instance that is
	 * most up to date. Instance marked with this type is not archived revision.<br>
	 * It could be combined with {@link #TYPE_LATEST_REVISION} to identify that the instance is the latest draft and
	 * there are no revisions, yet. This could be used when no additional instance for {@link #TYPE_LATEST_REVISION} is
	 * required.
	 */
	public static final URI TYPE_CURRENT;

	public static final URI DEFAULT_HEADER;
	public static final URI COMPACT_HEADER;

	public static final URI INSTANCE_TYPE;

	/**
	 * Identifies that an instance is tagged with {@link #TAG}.
	 */
	public static final URI HAS_TAG;

	/**
	 * Identifies the target of the help.
	 */
	public static final URI HELP_TARGET;
	/**
	 * Member variables of EmfInstance class TODO : to be removed
	 */
	public static final URI ID;
	public static final URI DMS_ID;
	public static final URI CONTENT_MGT_ID;
	public static final URI IDENTIFIER;
	public static final URI DEFINITION_REVISION;
	public static final URI CONTAINER;
	public static final URI PARENT;
	public static final URI PARENT_TYPE;
	/**
	 * This field is replaced by {@link EMF#TYPE} and is no longer used for now
	 */
	public static final URI DEFINITION_ID;
	public static final URI PURPOSE;

	public static final URI REPLY;

	public static final URI ANNOTATION;

	public static final URI MENTIONED_USERS;

	public static final URI HAS_MODEL;
	
	public static final URI DEFAULT_TEMPLATE;

	// end of EMF member variables

	/*
	 * Relation properties for audit configuration
	 */
	/**
	 * Relation property that acts as inverse mapping of created relation to executed action
	 */
	public static final URI AUDIT_EVENT;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		// init context URIs
		DATA_CONTEXT = factory.createURI("http://ittruse.ittbg.com/data/enterpriseManagementFramework");
		DEFINITIONS_CONTEXT = factory.createURI("http://www.sirma.com/data/definitions");
		ANNOTATIONS_CONTEXT = factory.createURI("http://www.sirma.com/data/annotations");
		CLASS_DESCRIPTION_CONTEXT = factory.createURI("http://ittruse.ittbg.com/enterpriseManagementFramework/ontology/description");

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
		AUDIO = factory.createURI(NAMESPACE, "Audio");
		INTELLIGENT_DOCUMENT = factory.createURI(NAMESPACE, "IntelligentDocument");
		USER = factory.createURI(NAMESPACE, "User");
		GROUP = factory.createURI(NAMESPACE, "Group");
		CLASS_DESCRIPTION = factory.createURI(NAMESPACE, "ClassDescription");
		SAVED_SEARCH = factory.createURI(NAMESPACE, "SavedSearch");
		TAG = factory.createURI(NAMESPACE, "Tag");
		HELP = factory.createURI(NAMESPACE, "Help");
		DEFINITION = factory.createURI(NAMESPACE, "Definition");
		DEFINITION_PROPERTY = factory.createURI(NAMESPACE, "DefinitionProperty");
		DEFINITION_DATA_PROPERTY = factory.createURI(NAMESPACE, "DefinitionDataProperty");
		DEFINITION_OBJECT_PROPERTY = factory.createURI(NAMESPACE, "DefinitionObjectProperty");
		LIBRARY = factory.createURI(NAMESPACE, "Library");
		ACTIVITY = factory.createURI(NAMESPACE, "Activity");

		// init property URIs
		IS_SEARCHABLE = factory.createURI(NAMESPACE, "isSearchable");
		IS_CREATEABLE = factory.createURI(NAMESPACE, "isCreateable");
		IS_UPLOADABLE = factory.createURI(NAMESPACE, "isUploadable");
		IS_VERSIONABLE = factory.createURI(NAMESPACE, "isVersionable");
		ACCEPT_DATATYPE_PATTERN = factory.createURI(NAMESPACE, "acceptDataTypePattern");
		CLASS_CATEGORY = factory.createURI(NAMESPACE, "classCategory");

		CONTENT = factory.createURI(NAMESPACE, "content");
		VIEW = factory.createURI(NAMESPACE, "viewContent");
		VIEW_WIDGETS_CONTENT = factory.createURI(NAMESPACE, "viewWidgetsContent");
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
		MODIFIED_BY = factory.createURI(NAMESPACE, "modifiedBy");
		RELATION_TYPE = factory.createURI(NAMESPACE, "relationType");
		IS_ACTIVE = factory.createURI(NAMESPACE, "isActive");
		INVERSE_RELATION = factory.createURI(NAMESPACE, "inverseRelation");
		BUSINESS_TYPE = factory.createURI(NAMESPACE, "businessType");
		IS_DELETED = factory.createURI(NAMESPACE, "isDeleted");
		IS_SYSTEM_PROPERTY = factory.createURI(NAMESPACE, "isSystemProperty");
		CREATED_BY = factory.createURI(NAMESPACE, "createdBy");
		DELETED_ON = factory.createURI(NAMESPACE, "deletedOn");
		DELETED_BY = factory.createURI(NAMESPACE, "deletedBy");
		SOURCE = factory.createURI(NAMESPACE, "source");
		DESTINATION = factory.createURI(NAMESPACE, "destination");

		HAS_ASSIGNEE = factory.createURI(NAMESPACE, "hasAssignee");

		HAS_ATTACHMENT = factory.createURI(NAMESPACE, "hasAttachment");
		IS_ATTACHED_TO = factory.createURI(NAMESPACE, "isAttachedTo");

		HAS_TAG = factory.createURI(NAMESPACE, "hasTag");
		HELP_TARGET = factory.createURI(NAMESPACE, "helpTarget");
		// revisions
		ACTUAL_OF = factory.createURI(NAMESPACE, "actualOf");
		REVISION_TYPE = factory.createURI(NAMESPACE, "revisionType");
		TYPE_CURRENT = factory.createURI(NAMESPACE, "current");
		TYPE_REVISION = factory.createURI(NAMESPACE, "revision");
		TYPE_LATEST_REVISION = factory.createURI(NAMESPACE, "latestRevision");
		DEFAULT_HEADER = factory.createURI(NAMESPACE, "default_header");
		COMPACT_HEADER = factory.createURI(NAMESPACE, "compact_header");
		INSTANCE_TYPE = factory.createURI(NAMESPACE, "instanceType");

		// Member variables of EmfInstance class
		// TODO : to be removed
		ID = factory.createURI(NAMESPACE, "id");
		DMS_ID = factory.createURI(NAMESPACE, "dmsId");
		CONTENT_MGT_ID = factory.createURI(NAMESPACE, "contentManagementId");
		IDENTIFIER = factory.createURI(NAMESPACE, "identifier");
		DEFINITION_REVISION = factory.createURI(NAMESPACE, "revision");
		CONTAINER = factory.createURI(NAMESPACE, "container");
		PARENT = factory.createURI(NAMESPACE, "parent");
		PARENT_TYPE = factory.createURI(NAMESPACE, "parentType");
		DEFINITION_ID = factory.createURI(NAMESPACE, "definitionId");
		PURPOSE = factory.createURI(NAMESPACE, "purpose");
		// end of EmfInstance member variables

		// annotation properties
		ANNOTATION = factory.createURI(NAMESPACE, "annotation");
		REPLY = factory.createURI(NAMESPACE, "reply");
		HAS_MODEL = factory.createURI(NAMESPACE, "hasModel");
		MENTIONED_USERS = factory.createURI(NAMESPACE, "mentionedUsers");
		DEFAULT_TEMPLATE = factory.createURI(NAMESPACE, "defaultTemplate");

		AUDIT_EVENT = factory.createURI(NAMESPACE, "auditEvent");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private EMF() {
		// utility class
	}

}
