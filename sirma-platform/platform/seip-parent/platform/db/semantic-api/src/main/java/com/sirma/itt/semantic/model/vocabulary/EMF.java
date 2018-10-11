package com.sirma.itt.semantic.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	// /////////////////////////////////////////
	// Context
	// /////////////////////////////////////////

	public static final IRI DATA_CONTEXT;

	public static final IRI DEFINITIONS_CONTEXT;

	public static final IRI ANNOTATIONS_CONTEXT;

	public static final IRI CLASS_DESCRIPTION_CONTEXT;

	public static final IRI DEFAULT_RELATION_CONTEXT;

	public static final IRI REMOVE_GRAPH;

	// /////////////////////////////////////////
	// CLASSES
	// /////////////////////////////////////////

	/**
	 * Project
	 */
	public static final IRI PROJECT;

	/**
	 * Case
	 */
	public static final IRI CASE;

	/**
	 * Case section
	 */
	public static final IRI CASE_SECTION;

	/**
	 * Document
	 */
	public static final IRI DOCUMENT;


	public static final IRI RECORD_SPACE;

	/**
	 * Object
	 */
	public static final IRI DOMAIN_OBJECT;

	/**
	 * Task
	 */
	public static final IRI TASK;

	/** Workflow task. */
	public static final IRI BUSINESS_PROCESS_TASK;

	/**
	 * Workflow
	 */
	public static final IRI BUSINESS_PROCESS;

	/**
	 * Comment class
	 */
	public static final IRI COMMENT;

	/**
	 * Topic class
	 */
	public static final IRI TOPIC;

	/**
	 * Section class
	 */
	public static final IRI SECTION;

	/** Generic folder class. */
	public static final IRI FOLDER;
	/**
	 * Image annotation class
	 */
	public static final IRI IMAGE_ANNOTATION;

	/**
	 * Relation class
	 */
	public static final IRI RELATION;

	/** Media document class */
	public static final IRI MEDIA;

	/** Image document class. */
	public static final IRI IMAGE;

	/** Video document class. */
	public static final IRI VIDEO;

	/** Audio document class. */
	public static final IRI AUDIO;

	/** Intelligent document class (idoc). */
	public static final IRI INTELLIGENT_DOCUMENT;

	public static final IRI USER;

	public static final IRI GROUP;

	public static final IRI CLASS_DESCRIPTION;

	/**
	 * Filter used to save search criteria of basic or advanced searches that can be loaded later.
	 */
	public static final IRI SAVED_SEARCH;

	/**
	 * Class representing tags in the system which can be used to tag different instances.
	 */
	public static final IRI TAG;

	/**
	 * Class representing help objects in the system which will be displayed when the user requests help.
	 */
	public static final IRI HELP;
	/**
	 * Definition class.
	 */
	public static final IRI DEFINITION;

	/** Generic property that is described in a definition and should be loaded for an instance. */
	public static final IRI DEFINITION_PROPERTY;

	/** Literal property that is described in a definition and should be loaded for an instance. */
	public static final IRI DEFINITION_DATA_PROPERTY;

	/** Object property that is described in a definition and should be loaded for an instance. */
	public static final IRI DEFINITION_OBJECT_PROPERTY;

	/** Base class for activities like projects, tasks etc */
	public static final IRI ACTIVITY;

	/**
	 * Class representing Project Library elements
	 */
	public static final IRI LIBRARY;

	/**
	 * Class identifier for instance templates
	 */
	public static final IRI TEMPLATE;
	// /////////////////////////////////////////
	// PROPERTIES
	// /////////////////////////////////////////

	/*
	 * Model properties
	 */
	/** If the semantic class could be searched via the user interface or not. */
	public static final IRI IS_SEARCHABLE;

	/** If an instance of the given class could be created by the user via the user interface. */
	public static final IRI IS_CREATEABLE;

	/** If the user could upload a file of the given class via the user interface. */
	public static final IRI IS_UPLOADABLE;

	/** If an instance of the given class is versionable or rather that its content is versionable. */
	public static final IRI IS_VERSIONABLE;

	/** If an instance of the given class support mailbox. */
	public static final IRI IS_MAILBOX_SUPPORTABLE;

	/** Indicates the ontology that a class belongs to **/
	public static final IRI PART_OF_ONTOLOGY;

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
	public static final IRI ACCEPT_DATATYPE_PATTERN;

	/**
	 * Alternative instance title used for indexing and sorting.
	 */
	public static final IRI ALT_TITLE;

	/** Class property that represents the class category: case, project, document, etc. */
	public static final IRI CLASS_CATEGORY;

	public static final IRI VERSION;

	/**
	 * hasText property
	 */
	public static final IRI CONTENT;
	/**
	 * hasText property for the instance view
	 */
	public static final IRI VIEW;

	/**
	 * Text property where contents fo widget will be save.
	 */
	public static final IRI VIEW_WIDGETS_CONTENT;

	/**
	 * Text property where OCR content will be save.
	 */
	public static final IRI OCR_CONTENT;
	/**
	 * commentsOn property
	 */
	public static final IRI COMMENTS_ON;
	/**
	 * hasAgent property
	 */
	public static final IRI HAS_AGENT;
	/**
	 * isRelatedTo property
	 */
	public static final IRI IS_RELATED_TO;
	/**
	 * replyTo property
	 */
	public static final IRI REPLY_TO;
	/**
	 * viewBox property
	 */
	public static final IRI VIEW_BOX;
	/**
	 * zoomLevel property
	 */
	public static final IRI ZOOM_LEVEL;
	/**
	 * svgValue property
	 */
	public static final IRI SVG_VALUE;
	/**
	 * externalID property
	 */
	public static final IRI EXTERNAL_ID;
	/**
	 * createdOn property
	 */
	public static final IRI CREATED_ON;
	/**
	 * hasImageAnnotation property
	 */
	public static final IRI HAS_IMAGE_ANNOTATION;
	/**
	 * status property
	 */
	public static final IRI STATUS;
	/**
	 * Type property
	 */
	public static final IRI TYPE;
	/**
	 * Modified on property
	 */
	public static final IRI MODIFIED_ON;
	/**
	 * Modified by property
	 */
	public static final IRI MODIFIED_BY;
	/**
	 * relation type property
	 */
	public static final IRI RELATION_TYPE;

	/**
	 * relation is active property
	 */
	public static final IRI IS_ACTIVE;

	/** The inverse relation of a complex relation */
	public static final IRI INVERSE_RELATION;

	/**
	 * Business type property
	 */
	public static final IRI BUSINESS_TYPE;

	/**
	 * Is deleted flag
	 */
	public static final IRI IS_DELETED;

	/**
	 * System property marker
	 */
	public static final IRI IS_SYSTEM_PROPERTY;

	/**
	 * Created by
	 */
	public static final IRI CREATED_BY;

	/** Deleted on */
	public static final IRI DELETED_ON;

	/** Deleted by */
	public static final IRI DELETED_BY;

	/** Relation property to identify the source of the relation */
	public static final IRI SOURCE;

	/** Relation property to identify the target of the relation */
	public static final IRI DESTINATION;

	public static final IRI HAS_ASSIGNEE;

	/**
	 * Defines a relation between 2 instances and indicates that the second instance is attached to the first one.
	 * Inverse of {@link #IS_ATTACHED_TO}
	 */
	public static final IRI HAS_ATTACHMENT;
	/**
	 * Defines a relation between 2 instances and indicates that the first instance is attached to the second one.
	 * Inverse of {@link #HAS_ATTACHMENT}
	 */
	public static final IRI IS_ATTACHED_TO;

	/**
	 * Predicate for revision instances to determine the revision type of the current instance. Possible object values
	 * are: {@link #TYPE_CURRENT} and {@link #TYPE_REVISION}. The predicate can have more that one object at a time.
	 */
	public static final IRI REVISION_TYPE;

	/**
	 * Revision type that identifies an instance that is a revision of an instance.<br>
	 * Instance marked with this type should be considered as archive revision.
	 */
	public static final IRI TYPE_REVISION;

	/**
	 * Revision type that identifies an instance that should be considered as the current draft or the instance that is
	 * most up to date. Instance marked with this type is not archived revision.
	 */
	public static final IRI TYPE_CURRENT;

	public static final IRI DEFAULT_HEADER;
	public static final IRI COMPACT_HEADER;

	public static final IRI INSTANCE_TYPE;

	/**
	 * Identifies that an instance is tagged with {@link #TAG}.
	 */
	public static final IRI HAS_TAG;

	/**
	 * Identifies the target of the help.
	 */
	public static final IRI HELP_TARGET;

	/**
	 * Default relation that shows if there is an exsisting relation between the two instances
	 */
	public static final IRI HAS_RELATION;
	/**
	 * Member variables of EmfInstance class TODO : to be removed
	 */
	public static final IRI ID;
	public static final IRI DMS_ID;
	public static final IRI CONTENT_MGT_ID;
	public static final IRI IDENTIFIER;
	public static final IRI DEFINITION_REVISION;
	public static final IRI CONTAINER;
	public static final IRI PARENT;
	public static final IRI PARENT_TYPE;
	/**
	 * This field is replaced by {@link EMF#TYPE} and is no longer used for now
	 */
	public static final IRI DEFINITION_ID;
	public static final IRI PURPOSE;

	public static final IRI REPLY;

	public static final IRI ANNOTATION;

	public static final IRI MENTIONED_USERS;

	public static final IRI HAS_MODEL;

	public static final IRI DEFAULT_TEMPLATE;

	public static final IRI IS_MEMBER_OF;

	// end of EMF member variables

	/*
	 * Relation properties for audit configuration
	 */
	/**
	 * Relation property that acts as inverse mapping of created relation to executed action
	 */
	public static final IRI AUDIT_EVENT;

	/**
	 * System value for removing all statements for the given subject or subject and predicate .
	 */
	public static final IRI REMOVE_ALL;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		// init context IRIs
		DATA_CONTEXT = factory.createIRI("http://ittruse.ittbg.com/data/enterpriseManagementFramework");
		DEFINITIONS_CONTEXT = factory.createIRI("http://www.sirma.com/data/definitions");
		ANNOTATIONS_CONTEXT = factory.createIRI("http://www.sirma.com/data/annotations");
		CLASS_DESCRIPTION_CONTEXT = factory
				.createIRI("http://ittruse.ittbg.com/enterpriseManagementFramework/ontology/description");
		DEFAULT_RELATION_CONTEXT = factory
				.createIRI("http://ittruse.ittbg.com/enterpriseManagementFramework/ontology/defaultRelation");
		REMOVE_GRAPH = factory.createIRI("http://www.sirma.com/system/removeStatements");

		// init Class IRIs
		PROJECT = factory.createIRI(NAMESPACE, "Project");
		CASE = factory.createIRI(NAMESPACE, "Case");
		CASE_SECTION = factory.createIRI(NAMESPACE, "CaseSection");
		DOCUMENT = factory.createIRI(NAMESPACE, "Document");
		RECORD_SPACE = factory.createIRI(NAMESPACE, "RecordSpace");
		DOMAIN_OBJECT = factory.createIRI(NAMESPACE, "DomainObject");
		TASK = factory.createIRI(NAMESPACE, "Task");
		BUSINESS_PROCESS_TASK = factory.createIRI(NAMESPACE, "BusinessProcessTask");
		BUSINESS_PROCESS = factory.createIRI(NAMESPACE, "BusinessProcess");
		COMMENT = factory.createIRI(NAMESPACE, "Comment");
		TOPIC = factory.createIRI(NAMESPACE, "Topic");
		SECTION = factory.createIRI(NAMESPACE, "Section");
		FOLDER = factory.createIRI(NAMESPACE, "Folder");
		IMAGE_ANNOTATION = factory.createIRI(NAMESPACE, "ImageAnnotation");
		RELATION = factory.createIRI(NAMESPACE, "Relation");
		MEDIA = factory.createIRI(NAMESPACE, "Media");
		IMAGE = factory.createIRI(NAMESPACE, "Image");
		VIDEO = factory.createIRI(NAMESPACE, "Video");
		AUDIO = factory.createIRI(NAMESPACE, "Audio");
		INTELLIGENT_DOCUMENT = factory.createIRI(NAMESPACE, "IntelligentDocument");
		USER = factory.createIRI(NAMESPACE, "User");
		GROUP = factory.createIRI(NAMESPACE, "Group");
		CLASS_DESCRIPTION = factory.createIRI(NAMESPACE, "ClassDescription");
		SAVED_SEARCH = factory.createIRI(NAMESPACE, "SavedSearch");
		TAG = factory.createIRI(NAMESPACE, "Tag");
		HELP = factory.createIRI(NAMESPACE, "Help");
		DEFINITION = factory.createIRI(NAMESPACE, "Definition");
		DEFINITION_PROPERTY = factory.createIRI(NAMESPACE, "DefinitionProperty");
		DEFINITION_DATA_PROPERTY = factory.createIRI(NAMESPACE, "DefinitionDataProperty");
		DEFINITION_OBJECT_PROPERTY = factory.createIRI(NAMESPACE, "DefinitionObjectProperty");
		LIBRARY = factory.createIRI(NAMESPACE, "Library");
		TEMPLATE = factory.createIRI(NAMESPACE, "Template");
		ACTIVITY = factory.createIRI(NAMESPACE, "Activity");

		// init property IRIs
		IS_SEARCHABLE = factory.createIRI(NAMESPACE, "isSearchable");
		IS_CREATEABLE = factory.createIRI(NAMESPACE, "isCreateable");
		IS_UPLOADABLE = factory.createIRI(NAMESPACE, "isUploadable");
		IS_VERSIONABLE = factory.createIRI(NAMESPACE, "isVersionable");
		IS_MAILBOX_SUPPORTABLE = factory.createIRI(NAMESPACE, "isMailboxSupportable");
		ACCEPT_DATATYPE_PATTERN = factory.createIRI(NAMESPACE, "acceptDataTypePattern");
		ALT_TITLE = factory.createIRI(NAMESPACE, "altTitle");
		CLASS_CATEGORY = factory.createIRI(NAMESPACE, "classCategory");
		VERSION = factory.createIRI(NAMESPACE, "version");
		PART_OF_ONTOLOGY = factory.createIRI(NAMESPACE, "partOfOntology");

		CONTENT = factory.createIRI(NAMESPACE, "content");
		VIEW = factory.createIRI(NAMESPACE, "viewContent");
		VIEW_WIDGETS_CONTENT = factory.createIRI(NAMESPACE, "viewWidgetsContent");
		OCR_CONTENT = factory.createIRI(NAMESPACE, "ocrContent");
		COMMENTS_ON = factory.createIRI(NAMESPACE, "commentsOn");
		HAS_AGENT = factory.createIRI(NAMESPACE, "hasAgent");
		IS_RELATED_TO = factory.createIRI(NAMESPACE, "isRelatedTo");
		REPLY_TO = factory.createIRI(NAMESPACE, "replyTo");
		VIEW_BOX = factory.createIRI(NAMESPACE, "viewBox");
		ZOOM_LEVEL = factory.createIRI(NAMESPACE, "zoomLevel");
		SVG_VALUE = factory.createIRI(NAMESPACE, "svgValue");
		EXTERNAL_ID = factory.createIRI(NAMESPACE, "externalID");
		HAS_IMAGE_ANNOTATION = factory.createIRI(NAMESPACE, "hasImageAnnotation");
		CREATED_ON = factory.createIRI(NAMESPACE, "createdOn");
		STATUS = factory.createIRI(NAMESPACE, "status");
		TYPE = factory.createIRI(NAMESPACE, "type");
		MODIFIED_ON = factory.createIRI(NAMESPACE, "modifiedOn");
		MODIFIED_BY = factory.createIRI(NAMESPACE, "modifiedBy");
		RELATION_TYPE = factory.createIRI(NAMESPACE, "relationType");
		IS_ACTIVE = factory.createIRI(NAMESPACE, "isActive");
		INVERSE_RELATION = factory.createIRI(NAMESPACE, "inverseRelation");
		BUSINESS_TYPE = factory.createIRI(NAMESPACE, "businessType");
		IS_DELETED = factory.createIRI(NAMESPACE, "isDeleted");
		IS_SYSTEM_PROPERTY = factory.createIRI(NAMESPACE, "isSystemProperty");
		CREATED_BY = factory.createIRI(NAMESPACE, "createdBy");
		DELETED_ON = factory.createIRI(NAMESPACE, "deletedOn");
		DELETED_BY = factory.createIRI(NAMESPACE, "deletedBy");
		SOURCE = factory.createIRI(NAMESPACE, "source");
		DESTINATION = factory.createIRI(NAMESPACE, "destination");

		HAS_ASSIGNEE = factory.createIRI(NAMESPACE, "hasAssignee");

		HAS_ATTACHMENT = factory.createIRI(NAMESPACE, "hasAttachment");
		IS_ATTACHED_TO = factory.createIRI(NAMESPACE, "isAttachedTo");

		HAS_TAG = factory.createIRI(NAMESPACE, "hasTag");
		HELP_TARGET = factory.createIRI(NAMESPACE, "helpTarget");
		HAS_RELATION = factory.createIRI(NAMESPACE, "hasRelation");
		// revisions
		REVISION_TYPE = factory.createIRI(NAMESPACE, "revisionType");
		TYPE_CURRENT = factory.createIRI(NAMESPACE, "current");
		TYPE_REVISION = factory.createIRI(NAMESPACE, "revision");
		DEFAULT_HEADER = factory.createIRI(NAMESPACE, "default_header");
		COMPACT_HEADER = factory.createIRI(NAMESPACE, "compact_header");
		INSTANCE_TYPE = factory.createIRI(NAMESPACE, "instanceType");

		// Member variables of EmfInstance class
		// TODO : to be removed
		ID = factory.createIRI(NAMESPACE, "id");
		DMS_ID = factory.createIRI(NAMESPACE, "dmsId");
		CONTENT_MGT_ID = factory.createIRI(NAMESPACE, "contentManagementId");
		IDENTIFIER = factory.createIRI(NAMESPACE, "identifier");
		DEFINITION_REVISION = factory.createIRI(NAMESPACE, "revision");
		CONTAINER = factory.createIRI(NAMESPACE, "container");
		PARENT = factory.createIRI(NAMESPACE, "parent");
		PARENT_TYPE = factory.createIRI(NAMESPACE, "parentType");
		DEFINITION_ID = factory.createIRI(NAMESPACE, "definitionId");
		PURPOSE = factory.createIRI(NAMESPACE, "purpose");
		// end of EmfInstance member variables

		// annotation properties
		ANNOTATION = factory.createIRI(NAMESPACE, "annotation");
		REPLY = factory.createIRI(NAMESPACE, "reply");
		HAS_MODEL = factory.createIRI(NAMESPACE, "hasModel");
		MENTIONED_USERS = factory.createIRI(NAMESPACE, "mentionedUsers");
		DEFAULT_TEMPLATE = factory.createIRI(NAMESPACE, "defaultTemplate");

		IS_MEMBER_OF = factory.createIRI(NAMESPACE, "isMemberOf");

		AUDIT_EVENT = factory.createIRI(NAMESPACE, "auditEvent");

		REMOVE_ALL = factory.createIRI(NAMESPACE, "removeAll");
	}

	/**
	 * This class is only for constants and it should not be instantiated
	 */
	private EMF() {
		// utility class
	}
}