package com.sirma.itt.emf.properties;

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
	/** The check box value. */
	String CHECK_BOX_VALUE = "checkBoxValue";
	/** The check box modified on. */
	String CHECK_BOX_MODIFIED_ON = "checkBoxModifiedOn";
	/** The check box modified from. */
	String CHECK_BOX_MODIFIED_FROM = "checkBoxModifiedFrom";

	/*
	 * Default fields for action button definition control
	 */
	/** The action button executed. */
	String ACTION_BUTTON_EXECUTED = "actionButtonExecuted";
	/** The action button executed from. */
	String ACTION_BUTTON_EXECUTED_FROM = "actionButtonExecutedFrom";
	/** The action button executed on. */
	String ACTION_BUTTON_EXECUTED_ON = "actionButtonExecutedOn";

	/*
	 * Default/common fields between all instance objects
	 */
	/** The unique identifier. Maps to: http://purl.org/dc/terms/identifier */
	String UNIQUE_IDENTIFIER = "identifier";
	/**
	 * The type property. This property should be used to describe the current definition
	 * identifier. Maps to: http://purl.org/dc/terms/type
	 */
	String TYPE = "type";
	/** The status. */
	String STATUS = "status";
	/** The title. Maps to: http://purl.org/dc/terms/title */
	String TITLE = "title";
	/** The name. */
	String NAME = "name";
	/** The description. Maps to http://purl.org/dc/terms/description */
	String DESCRIPTION = "description";
	/** The system creator. The user in the system who created the object. */
	String CREATED_BY = "createdBy";
	/** The system created. The date in which the object was created in the system. */
	String CREATED_ON = "createdOn";
	/** The system modifier. The date in which the object was modified in the system. */
	String MODIFIED_BY = "modifiedBy";
	/** The system modified. The user in the system who last modified the object. */
	String MODIFIED_ON = "modifiedOn";
	/** The is deleted. Property that defines if the instance is marked for deleted or not. */
	String IS_DELETED = "emf:isDeleted";
	/**
	 * The rights. Maps to: http://purl.org/dc/terms/rights => RightsStatement (Description of
	 * rights)
	 */
	String RIGHTS = "rights";
	/** The description of change. Maps to: http://purl.org/dc/terms/provenance */
	String DESCRIPTION_OF_CHANGE = "provenance";
	/** The language. Maps to: http://purl.org/dc/terms/language */
	String LANGUAGE = "language";
	/** The mimetype. Maps to: http://purl.org/dc/terms/format */
	String MIMETYPE = "mimetype";
	/** The priority. */
	String PRIORITY = "priority";

	/*
	 * Lock related constants
	 */
	/** The user that locked the document. */
	public static final String LOCKED_BY = "lockedBy";
	/**
	 * true/false value depending on wheather or not the currently logged in user has locked the
	 * document.
	 */
	String IS_LOCKED_BY_ME = "isLockedByMe";
	/**
	 * Message to show to the user, indicating who has locked the document.
	 */
	String LOCKED_BY_MESSAGE = "lockedByMessage";

	/*
	 * Header fields for all instance objects.
	 */
	/** The header default. */
	String HEADER_DEFAULT = "default_header";
	/** The header compact. */
	String HEADER_COMPACT = "compact_header";
	/** The header breadcrumb. */
	String HEADER_BREADCRUMB = "breadcrumb_header";
	/** The header tooltip. */
	String HEADER_TOOLTIP = "tooltip_header";
	/** The headers. */
	Set<String> HEADERS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			HEADER_DEFAULT, HEADER_COMPACT, HEADER_BREADCRUMB)));

	/*
	 * Instance time properties
	 */
	/** The planned start date. */
	String PLANNED_START_DATE = "plannedStartDate";
	/** The actual start date. */
	String ACTUAL_START_DATE = "startTime";
	/** The planned end date. */
	String PLANNED_END_DATE = "plannedEndDate";
	/** The actual end date. */
	String ACTUAL_END_DATE = "endTime";
	/** The estimated effort. */
	String ESTIMATED_EFFORT = "estimatedEffort";
	/** The accumulated estimated effort. */
	String ACCUMULATED_ESTIMATED_EFFORT = "accumulatedEstimatedEffort";
	/** The actual effort. */
	String ACTUAL_EFFORT = "actualEffort";
	/** The accumulated actual effort. */
	String ACCUMULATED_ACTUAL_EFFORT = "accumulatedActualEffort";

	/*
	 * Tree properties
	 */
	/** The id. */
	String ID = "Id";
	/** The leaf. */
	String LEAF = "leaf";
	/** The parent id. */
	String PARENT_ID = "parentId";

	/*
	 * Not cloneable properties
	 */
	String DEFAULT_VIEW_LOCATION = "defaultViewLocation";
	String DEFAULT_VIEW = "defaultView";
	String VERSION = "version";
	String URI = "uri";

	/**
	 * Default fields set.
	 */
	Set<String> DEFAULT_FIELDS = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays
			.asList(UNIQUE_IDENTIFIER, TYPE, STATUS, TITLE, DESCRIPTION, CREATED_BY, CREATED_ON,
					MODIFIED_BY, MODIFIED_ON, /* CREATOR, CREATED, MODIFIER, MODIFIED, OWNER, */
					RIGHTS, DESCRIPTION_OF_CHANGE, LANGUAGE, MIMETYPE)));

	/** The not clonable properties. */
	Set<String> NOT_CLONABLE_PROPERTIES = Collections.unmodifiableSet(new LinkedHashSet<String>(
			Arrays.asList(UNIQUE_IDENTIFIER, STATUS, CREATED_BY, CREATED_ON, MODIFIED_BY,
					MODIFIED_ON, RIGHTS, HEADER_BREADCRUMB, HEADER_COMPACT, HEADER_DEFAULT,
					DEFAULT_VIEW, DEFAULT_VIEW_LOCATION, VERSION, STATUS, URI)));

	/** The default headers. */
	Set<String> DEFAULT_HEADERS = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays
			.asList(DefaultProperties.HEADER_TOOLTIP, DefaultProperties.HEADER_DEFAULT,
					DefaultProperties.HEADER_COMPACT, DefaultProperties.HEADER_BREADCRUMB)));

	/** Value for fields and properties that represent it's not used or not applicable */
	String NOT_USED_PROPERTY_VALUE = "FORBIDDEN";

	/** The object thumbnail. */
	String THUMBNAIL_IMAGE = "thumbnailImage";

	/**
	 * The primary image. <br />
	 * <i>Note: Functionality for primary image is not implemented.</i>
	 */
	String PRIMARY_IMAGE = "emf:primaryImage";

	/**
	 * The transient instance. If set in an instance the instance should not be persisted in
	 * semantic database.
	 */
	String TRANSIENT_SEMANTIC_INSTANCE = "$TRANSIENT_SEMANTIC_INSTANCE$";

	/** The evaluated actions for the instance */
	String EVALUATED_ACTIONS = "$evaluatedActions$";

	/** The content property. */
	String CONTENT = "content";

	/** The is imported. */
	String IS_IMPORTED = "emf:imported";

	/** The purpose. */
	String PURPOSE = "purpose";
}
