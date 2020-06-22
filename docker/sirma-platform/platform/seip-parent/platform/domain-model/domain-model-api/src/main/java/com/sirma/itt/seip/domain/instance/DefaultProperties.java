package com.sirma.itt.seip.domain.instance;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Default system properties for object instances.
 *
 * @author BBonev
 */
public interface DefaultProperties {

	/*
	 * Default fields for check box definition control.
	 */
	String CHECK_BOX_VALUE = "checkBoxValue";
	String CHECK_BOX_MODIFIED_ON = "checkBoxModifiedOn";
	String CHECK_BOX_MODIFIED_FROM = "checkBoxModifiedFrom";

	/*
	 * Default fields for action button definition control
	 */
	String ACTION_BUTTON_EXECUTED = "actionButtonExecuted";
	String ACTION_BUTTON_EXECUTED_FROM = "actionButtonExecutedFrom";
	String ACTION_BUTTON_EXECUTED_ON = "actionButtonExecutedOn";

	/*
	 * Default/common fields between all instance objects
	 */
	String ENTITY_IDENTIFIER = "uri";

	/** The unique identifier. Maps to: http://purl.org/dc/terms/identifier */
	String UNIQUE_IDENTIFIER = "identifier";

	/**
	 * The type property. This property should be used to describe the current definition identifier. Maps to:
	 * http://purl.org/dc/terms/type
	 */
	String TYPE = "type";
	String STATUS = "status";

	/** The title. Maps to: http://purl.org/dc/terms/title */
	String TITLE = "title";
	String NAME = "name";

	/** The description. Maps to http://purl.org/dc/terms/description */
	String DESCRIPTION = "description";

	/** The system creator. The user in the system who created the object. */
	String CREATED_BY = "createdBy";

	/**
	 * The system created. The date in which the object was created in the system.
	 */
	String CREATED_ON = "createdOn";

	/**
	 * The system modifier. The date in which the object was modified in the system.
	 */
	String MODIFIED_BY = "modifiedBy";

	/**
	 * The system modified. The user in the system who last modified the object.
	 */
	String MODIFIED_ON = "modifiedOn";

	/**
	 * The is deleted. Property that defines if the instance is marked for deleted or not.
	 */
	String IS_DELETED = "emf:isDeleted";

	/** The is active. Property that defines if the instance is in active state or not. */
	String IS_ACTIVE = "emf:isActive";

	/**
	 * The rights. Maps to: http://purl.org/dc/terms/rights =&gt; RightsStatement (Description of rights)
	 */
	String RIGHTS = "rights";

	/**
	 * The description of change. Maps to: http://purl.org/dc/terms/provenance
	 */
	String DESCRIPTION_OF_CHANGE = "provenance";

	/** The language. Maps to: http://purl.org/dc/terms/language */
	String LANGUAGE = "language";

	/** The mimetype. Maps to: http://purl.org/dc/terms/format */
	String MIMETYPE = "mimetype";

	/*
	 * Lock related constants
	 */
	/** The user that locked the document. */
	String LOCKED_BY = "lockedBy";

	/** The info object for locked status. */
	String LOCKED_INFO = "lockedInfo";

	/**
	 * true/false value depending on wheather or not the currently logged in user has locked the document.
	 */
	String IS_LOCKED_BY_ME = "isLockedByMe";

	/**
	 * Message to show to the user, indicating who has locked the document.
	 */
	String LOCKED_BY_MESSAGE = "lockedByMessage";

	/*
	 * Header fields for all instance objects.
	 */
	String HEADER_DEFAULT = "default_header";
	String HEADER_COMPACT = "compact_header";
	String HEADER_BREADCRUMB = "breadcrumb_header";
	String HEADER_TOOLTIP = "tooltip_header";
	String HEADER_LABEL = "emf:altTitle";
	Set<String> HEADERS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(HEADER_DEFAULT, HEADER_COMPACT, HEADER_BREADCRUMB)));

	/*
	 * Instance time properties
	 */
	String PLANNED_END_DATE = "plannedEndDate";

	/**
	 * Predicate for revision instances to determine the revision type of the current instance.
	 */
	String REVISION_TYPE = "emf:revisionType";
	String REVISION_NUMBER = "emf:revisionNumber";
	String PUBLISHED_ON = "emf:publishedOn";
	String PUBLISHED_BY = "emf:publishedBy";
	String SEMANTIC_TYPE = "rdf:type";
	String DELETED_ON = "emf:deletedOn";
	String STANDALONE = "emf:standalone";

	/*
	 * Not cloneable properties
	 */
	String VERSION = "emf:version";
	String URI = "id";
	String INSTANCE_TYPE = "instanceType";

	Set<String> DEFAULT_FIELDS = Collections
			.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(UNIQUE_IDENTIFIER, TYPE, STATUS, TITLE, DESCRIPTION,
					CREATED_BY, CREATED_ON, MODIFIED_BY, MODIFIED_ON, /* CREATOR, CREATED, MODIFIER, MODIFIED, OWNER, */
					RIGHTS, DESCRIPTION_OF_CHANGE, LANGUAGE, MIMETYPE)));

	Set<String> NOT_CLONABLE_PROPERTIES = Collections
			.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(UNIQUE_IDENTIFIER, STATUS, CREATED_BY, CREATED_ON,
					MODIFIED_BY, MODIFIED_ON, RIGHTS, HEADER_BREADCRUMB, HEADER_COMPACT, HEADER_DEFAULT, VERSION, URI,
					REVISION_NUMBER, PUBLISHED_ON, PUBLISHED_BY, REVISION_TYPE)));

	Set<String> DEFAULT_HEADERS = Collections.unmodifiableSet(
			new LinkedHashSet<>(Arrays.asList(DefaultProperties.HEADER_TOOLTIP, DefaultProperties.HEADER_DEFAULT,
					DefaultProperties.HEADER_COMPACT, DefaultProperties.HEADER_BREADCRUMB)));

	Set<String> NON_REPRESENTABLE_FIELDS = Collections
			.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(DefaultProperties.HEADER_TOOLTIP,
					DefaultProperties.HEADER_DEFAULT, DefaultProperties.HEADER_COMPACT,
					DefaultProperties.HEADER_BREADCRUMB, DefaultProperties.IS_DELETED)));

	/** Value for fields and properties that represent it's not used or not applicable. */
	String NOT_USED_PROPERTY_VALUE = "FORBIDDEN";

	String THUMBNAIL_IMAGE = "thumbnailImage";

	String CONTENT = "content";

	/** Property used for transferring the instance view on instance save. Should not be used for other purposes */
	String TEMP_CONTENT_VIEW = "$viewContent$";

	/** The content length in bytes. */
	String CONTENT_LENGTH = "size";

	String IS_IMPORTED = "emf:imported";

	String EMF_PURPOSE = "emf:purpose";

	/** This field is used in solr. */
	String PURPOSE = "purpose";

	/** The version of the template applied to a particular object. */
	String TEMPLATE_VERSION = "emf:templateVersion";

	/**
	 * Temporary field used by marked for download functionality to indicate that an instance is marked for download by
	 * the current user. Should not be persisted.
	 */
	String MARKED_FOR_DOWNLOAD = "markedForDownload";

	/**
	 * Temporary field used by has favorites functionality to indicate that an instance is marked as a favorite by the
	 * current user. Should not be persisted.
	 */
	String HAS_FAVOURITE = "hasFavourite";

	/** The location of the file in the DMS. */
	String ATTACHMENT_LOCATION = "documentLocation";

	/** The content id that represents the primary content of the instance. */
	String PRIMARY_CONTENT_ID = "emf:contentId";

	/** The document purpose IDOC. */
	String PURPOSE_IDOC = "iDoc";

	String SEMANTIC_HIERARCHY = "semanticHierarchy";

	/** The status of annotation */
	String EMF_STATUS = "emf:status";

	/**
	 * Maps to: <b>http://www.sirma.com/ontologies/2014/11/security#allowInheritParentPermissions</b> <br>
	 * Indicates if the instance has to inherit permissions from its parent
	 **/
	String INHERIT_PARENT_PERMISSIONS = "allowInheritParentPermissions";

	/**
	 * Maps to <b>http://www.sirma.com/ontologies/2014/11/security#allowInheritLibraryPermissions</b> <br>
	 * Indicates if the instance has to inherit permissions from the library it belongs to
	 **/
	String INHERIT_LIBRARY_PERMISSIONS = "allowInheritLibraryPermissions";

	/**
	 * Used as key for temporary storing of instance read permission for the current user. The value mapped to this
	 * property is of type {@link Boolean} and in most cases will be <code>true</code>. This property is calculated
	 * dynamically and it should not be persisted in any data base.
	 */
	String READ_ALLOWED = "$readAllowed$";

	/**
	 * Used as key for temporary storing of instance write permission for the current user. The value mapped to this
	 * property is of type {@link Boolean}. This property is calculated dynamically and it should not be persisted in
	 * any data base.
	 */
	String WRITE_ALLOWED = "$writeAllowed$";

	/**
	 * A field that contains a list with resources (users and/or groups). Those resources will be notified by email on
	 * workflow complete or some transitions.
	 */
	String EMAIL_DISTRIBUTION_LIST = "emailDistributionList";

	/** Denotes whether assignees notification is enabled */
	String ASSIGNEES_NOTIFICATION_ENABLED = "assigneesNotificationEnabled";

	/**
	 * Name of field used for restriction whether instance can exist in context.
	 * The field is part of configuration tag of definition. Example of configuration:
	 * <pre>
	 *     &#60;configuration&#62;
	 *         &#60;fields&#62;
	 *            &#60;field name="<b>existingInContext</b>" type="an..50" codelist="CL26"&#62;
	 *                &#60;value&#62;WITHOUT_CONTEXT&#60;/value&#62;
	 *            &#60;/field&#62;
	 *          &#60;/fields&#62;
	 *        &#60;/configuration&#62;
	 * </pre>
	 */
	String EXISTING_IN_CONTEXT = "existingInContext";
}
