package com.sirma.itt.seip.search;

/**
 * All named queries that can be executed as filters
 *
 * @author kirq4e
 */
public interface NamedQueries {

	String SELECT_BY_IDS = "SELECT_BY_IDS";
	String LOAD_PROPERTIES = "LOAD_PROPERTIES_BY_IDS";
	String SELECT_DATA_PROPERTIES_BY_IDS = "SELECT_DATA_PROPERTIES_BY_IDS";

	/**
	 * The check existing instance query - checks if the passed URIs as parameter exist in the repository and the
	 * objects aren`t deleted.
	 */
	String CHECK_EXISTING_INSTANCE = "CHECK_EXISTING_INSTANCE";

	/**
	 * Query that selects an instance by property and multiple values for that property.
	 * <p>
	 * The property name must be passed as configuration with name: {@link Params#PROPERTY_ID}.<br>
	 * The collection values must be provided with configuration {@link Params#URIS}.<br>
	 * There is optional parameter indicating the type of the collection values. By default values are treated as
	 * literals but if they are uries then the parameter {@link Params#IS_URI}{@code =true} must be provided.<br>
	 * The result of the query is an instance and a property {@link Projections#PROPERTY_VALUE} containing the matched
	 * property value for the return instance.
	 */
	String SELECT_BY_CUSTOM_ID = "SELECT_BY_CUSTOM_ID";

	/**
	 * Query that selects an instance by external passed filters only.
	 */
	String DYNAMIC_QUERY = "DYNAMIC_QUERY";

	/** Batch load annotations by annoation id */
	String LOAD_ANNOTATIONS = "LOAD_ANNOTATIONS";

	/**
	 * Collection of parameter names for semantic for queries registered in the {@link NamedQueries} class.
	 *
	 * @author BBonev
	 */
	class Params {

		/**
		 * Parameter name for the uries that will be passed when building query for multiple instances
		 */
		public static final String URIS = "URIS";

		/**
		 * The named of the properties to search for.
		 * <p>
		 * Example: if searching for property emf:customID then passing this configuration {@link #PROPERTY_ID}=
		 * {@code emf:customID} <br>
		 * Will result in search statement
		 *
		 * <pre>
		 * <code>{
		 * 	?instance emf:customID ?propertyValue.
		 *	?instance emf:customID "propertyValueToMatch".
		 * }</code>
		 * </pre>
		 */
		public static final String PROPERTY_ID = "propertyId";

		/** If missing or with value {@code false} then the accepted values will be treated as literals */
		public static final String IS_URI = "isUri";

		/**
		 * Used when checking if specific instance exits in the system. Specifies if the check should include the
		 * deleted instances.
		 */
		public static final String INCLUDE_DELETED = "includeDeleted";

		private Params() {
			// utility class
		}
	}

	/**
	 * Collection of property names used in semantic query projections
	 *
	 * @author BBonev
	 */
	class Projections {

		public static final String URI = "uri";
		public static final String PROPERTY_NAME = "propertyName";
		public static final String PROPERTY_VALUE = "propertyValue";
		public static final String PARENT = "parent";
		public static final String PARENT_TYPE = "parentType";

		/**
		 * Instantiates a new projections.
		 */
		private Projections() {
			// utility class
		}
	}

	/**
	 * Collection of names of predefined filters.
	 *
	 * @author BBonev
	 */
	class Filters {

		/** Select only deleted items */
		public static final String IS_DELETED = "isDeleted";

		/** Select not deleted items */
		public static final String IS_NOT_DELETED = "isNotDeleted";

		/** Select only revisions */
		public static final String IS_REVISION = "isRevision";

		/** Filter out revisions */
		public static final String IS_NOT_REVISION = "isNotRevision";

		/** When filtering should be skipped. */
		public static final String SKIP = "skip";

		private Filters() {
			// utility class
		}
	}
}
