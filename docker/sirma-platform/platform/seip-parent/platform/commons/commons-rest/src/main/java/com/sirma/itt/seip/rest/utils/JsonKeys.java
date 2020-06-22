package com.sirma.itt.seip.rest.utils;

/**
 * JSON key constants.
 *
 * @author yasko
 */
public final class JsonKeys {

	public static final String KEY = "key";

	/** JSON key for the property holding the JWT token in authentication response */
	public static final String TOKEN = "token";

	/** Message key used in error responses **/
	public static final String MESSAGE = "message";

	/** used for instance id field **/
	public static final String ID = "id";

	/** Used as key for multiple instance ids. Usually when passing array with ids. */
	public static final String INSTANCE_IDS = "instanceIds";

	public static final String TYPE = "type";

	/** used for instance definition id field **/
	public static final String DEFINITION_ID = "definitionId";

	/** used for instance properties object wrapper **/
	public static final String PROPERTIES = "properties";

	/** used for instance parent id **/
	public static final String PARENT_ID = "parentId";

	/** instance content field **/
	public static final String CONTENT = "content";

	/** file size **/
	public static final String FILE_SIZE = "fileSize";

	/** The creatable attribute of the class instances **/
	public static final String CREATABLE = "creatable";

	/** The uploadable attribute of the class instances **/
	public static final String UPLOADABLE = "uploadable";

	/** The versionable attribute of the class instances **/
	public static final String VERSIONABLE = "versionable";

	/** The checksum of a content. */
	public static final String CONTENT_CHECKSUM = "checksum";

	/** The mailboxSupportable attribute of the class instances **/
	public static final String MAILBOX_SUPPORTABLE = "mailboxSupportable";

	public static final String PROPERTY_NAME = "propertyName";
	public static final String PROPERTY_VALUE = "propertyValue";
	public static final String BINDINGS = "bindings";
	public static final String TARGET = "target";
	public static final String SOURCE = "source";

	/**
	 * For template instances the groupId corresponds to the definition id for which this template could be used.
	 */
	public static final String GROUP_ID = "groupId";

	public static final String TARGET_INSTANCE = "targetInstance";

	public static final String INSTANCE_HEADERS = "headers";

	public static final String INSTANCE_RELATIONS = "relations";

	// ********* Actions json keys ******************

	public static final String USER_OPERATION = "userOperation";

	public static final String TARGET_ID = "targetId";

	public static final String OPERATION = "operation";

	public static final String PLACEHOLDER = "placeholder";

	public static final String CONTEXT_PATH = "contextPath";

	// search
	public static final String CONDITION = "condition";
	public static final String RULES = "rules";
	public static final String VALUE = "value";
	public static final String OPERATOR = "operator";
	public static final String FIELD = "field";
	public static final String RESULT_SIZE = "resultSize";
	public static final String PAGE = "pageNumber"; // same as the request parameter
	public static final String HIGHLIGHT = "highlight";
	public static final String VALUES = "values";
	public static final String AGGREGATED = "aggregated";
	public static final String LIMIT = "limit";
	public static final String OFFSET = "offset";
	public static final String CURRENT_INSTANCE_ID = "currentInstanceId";

	// date range
	public static final String DATE_RANGE = "dateRange";
	public static final String START = "start";
	public static final String END = "end";

	// Changing password
	public static final String USERNAME = "username";
	public static final String OLD_PASSWORD = "oldPassword"; //NOSONAR
	public static final String NEW_PASSWORD = "newPassword"; //NOSONAR

	public static final String TIMESTAMP = "timestamp";
	public static final String THUMBNAIL = "thumbnail";
	public static final String NAME = "name";
	public static final String TEXT = "text";
	public static final String USER = "user";

	// permissions
	public static final String READ_ALLOWED = "readAllowed";
	public static final String WRITE_ALLOWED = "writeAllowed";

	// template
	public static final String TEMPLATE_INSTANCE_ID = "templateInstanceId";

	// object properties serialization
	public static final String TOTAL = "total";
	public static final String RESULTS = "results";

	private JsonKeys() {
		// utility
	}
}