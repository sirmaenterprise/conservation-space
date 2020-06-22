package com.sirma.itt.emf.label.retrieve;

/**
 * Annotation used to inject field related functionality.
 */
public final class FieldId {

	private FieldId() {
		// nothing to do utility class
	}

	/** The user display name by username. */
	public static final String USERNAME = "username";

	/** The user display name by short uri. */
	public static final String USERNAME_BY_URI = "usernamebyuri";

	/** The Constant OBJECTTYPE. */
	public static final String OBJECT_TYPE = "objecttype";

	/** The Constant OBJECTSUBTYPE. */
	public static final String OBJECT_SUBTYPE = "objectsubtype";

	/** The Constant OBJECT. */
	public static final String OBJECT = "object";

	/** The Constant ACTIONID. */
	public static final String ACTION_ID = "actionid";

	/** The Constant OBJECTSTATE. */
	public static final String OBJECT_STATE = "objectstate";

	public static final String CODE_LIST = "codelist";

	public static final String SOLR = "solr";

	/** Instance header for audit visualization. */
	public static final String HEADER = "header";

}
