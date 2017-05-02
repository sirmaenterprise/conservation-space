package com.sirma.itt.seip.instance.version;

/**
 * Instance properties primary used in the instance versioning.
 *
 * @author A. Kunchev
 */
public interface VersionProperties {

	/**
	 * Key For widget queries that should be executed for versions, that are send with the instance model from the web.
	 */
	String DYNAMIC_QUERIES_JSON_KEY = "dynamicQueries";

	/**
	 * Used as key for temporary storing of widgets queries in instance. The assigned value is passed json model of the
	 * search conditions.
	 */
	String DYNAMIC_QUERIES = "$dynamicQueries$";

	/**
	 * Key for storing the content id of the queries results. After the queries are executed, the result of them is
	 * stored as content (json file) of the instance.
	 */
	String QUERIES_RESULT_CONTENT_ID = "emf:queriesResultContentId";

	/**
	 * Key for the date when version is created. Needed as marker when loading specific version. Not persisted with the
	 * instance. The instance is used as temporary store for it.
	 */
	String VERSION_CREATED_ON = "$versionCreatedOn$";

	/**
	 * Used as key for the date when the version is created, when serializing version.
	 */
	String VERSION_CREATION_DATE = "versionCreationDate";

	/**
	 * Used to mark version instances, when they are returned for the instance version service. This properties will not
	 * be persisted.
	 */
	String IS_VERSION = "isVersion";

	/**
	 * Shows if the version instance has view content or not. <br>
	 * <b>Will be removed when the patch for version content restoring is done.</b>
	 */
	String HAS_VIEW_CONTENT = "hasViewContent";

	/**
	 * Key for storing original instance id, when the version instance is loaded.
	 */
	String ORIGINAL_INSTANCE_ID = "originalInstanceId";

	/**
	 * Used to store the results from the search queries for the version instances. After the correct version ids are
	 * found for the results they are returned as instance property under this key.
	 */
	// TODO remove when queries results contents are migrated
	String QUERIES_RESULTS = "queriesResults";

	/**
	 * Used to store transformed instance ids for selected instances in widgets. The ids are get and then transformed to
	 * version instances ids, so that the correct instance version is loaded, when specific version is opened.
	 */
	// TODO remove when queries results contents are migrated
	String MANUALLY_SELECTED = "manuallySelected";

	/**
	 * Key for the {@link java.util.Date}, when the version is created, used in handlers context for Idoc content node
	 * handlers.
	 */
	String HANDLERS_CONTEXT_VERSION_DATE_KEY = "versionCreationDate";

	/**
	 * Key for the property that shows what definition is mapped to the given version instance. Primary used when
	 * reverting versions because the loaded versions does not contain this property and when the version properties
	 * replace the properties from the original one this property is removed from the model, which causes problems
	 * afterwards.<br>
	 * <b>NOTE!</b> Probably will be removed or resolved by other means in the newer version of the application so do
	 * not relay on it.
	 */
	String DEFINITION_ID = "emf:definitionId";

}
