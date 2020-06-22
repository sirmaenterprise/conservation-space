package com.sirma.itt.seip.configuration;

/**
 * Configuration properties for runtime configuration use.
 *
 * @author BBonev
 */
public interface RuntimeConfigurationProperties {

	/** Tells the properties service to save properties without definition. */
	String SAVE_PROPERTIES_WITHOUT_DEFINITION = "SAVE_PROPERTIES_WITHOUT_DEFINITION";

	/**
	 * Use the add only properties option when saving properties of instances.<br>
	 * NOTE: this will not remove any properties but will only add new properties and update modified.
	 */
	String ADD_ONLY_PROPERTIES = "ADD_ONLY_PROPERTIES";

	/** The use container filtering when searching for properties and definitions. */
	String DO_NOT_USE_CONTAINER_FILTERING = "USE_CONTAINER_FILTERING";

	/**
	 * The called method that supports this should only update the internal cache but not the database if connected.
	 */
	String CACHE_ONLY_OPERATION = "CACHE_ONLY_OPERATION";

	/** Provides current language code needed in a non session context. */
	String CURRENT_LANGUAGE_CODE = "CURRENT_LANGUAGE_CODE";

	/**
	 * The Constant DO_NO_CALL_DMS. If this constant is present in the current context then the current executing
	 * operation will not call DMS sub system
	 */
	String DO_NO_CALL_DMS = "DO_NO_CALL_DMS";

	/**
	 * The use recursive conversion. When executing type conversion if the converted object has supports a nested
	 * conversion (like tree elements) if the converter implementation should process the child elements or not. The
	 * default behavior is not to process child elements so this should be explicitly set.
	 */
	String USE_RECURSIVE_CONVERSION = "USE_RECURSIVE_CONVERSION";

	/** Disables firing of the instance persist event */
	String DO_NOT_FIRE_PERSIST_EVENT = "DO_NOT_FIRE_PERSIST_EVENT";

	/** The Constant SERIALIZATION_ENGINE used for thread local store of the default engines. */
	String SERIALIZATION_ENGINE = "$SERIALIZATION_ENGINE$";

	/**
	 * When saving complex objects sometimes if the default behavior is not to save the child object cascade. To
	 * activate the cascade persist the option could be used.
	 */
	String SAVE_CHILDREN = "SAVE_CHILDREN";

	/** The do not load children override. */
	String DO_NOT_LOAD_CHILDREN_OVERRIDE = "DO_NOT_LOAD_CHILDREN_OVERRIDE";

	/** The currently executed operation by the current user if any. */
	String CURRENT_OPERATION = "CURRENT_OPERATION";

	/** The disable all automatic links creation. */
	String DISABLE_AUTOMATIC_LINKS = "DISABLE_AUTOMATIC_LINKS";

	/** The disable automatic parent to child links created when creating new instances. */
	String DISABLE_AUTOMATIC_PARENT_CHILD_LINKS = "DISABLE_AUTOMATIC_PARENT_CHILD_LINKS";

	/** The disable automatic context to child links created when creating new instances. */
	String DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS = "DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS";

	/** Audit disabled is configuration to stop automatic update of modified/modifier properties. */
	String AUDIT_MODIFICATION_DISABLED = "AUDIT_MODIFICATION_DISABLED";

	/**
	 * The load link properties. Forces the link service to load all link properties. Properties loading is disabled by
	 * default for performance optimization. The service will load the state and the creator of the link but nothing
	 * else. This applies only for semantic service for now.
	 */
	String LOAD_LINK_PROPERTIES = "LOAD_LINK_PROPERTIES";

	/**
	 * The generate random link id when creating new relations This overrides the default behavior of the service to
	 * generate id based on the source, destination and link type. This is useful when need to create more than one
	 * relation of the same type between the same instances like logging work on task.
	 */
	String GENERATE_RANDOM_LINK_ID = "GENERATE_RANDOM_LINK_ID";

	/**
	 * This will disable the checks on every instance that will be persisted for stale data modifications. This will
	 * allow to save an instance that is with modify date different from the one in the database. This should be used
	 * only for external system integrations and in {@link #OVERRIDE_MODIFIER_INFO} option.
	 */
	String DISABLE_STALE_DATA_CHECKS = "DISABLE_STALE_DATA_CHECKS";

	/**
	 * Enabling of the option will disable setting modifier and data of modification when saving instance. Note that if
	 * the caller does not set these fields they will remain the same or will not be set at all if empty. This could be
	 * used if have to perform save without marking the instances as modified or external integration. To work this
	 * properly make sure to use the {@link #DISABLE_STALE_DATA_CHECKS} if needed.
	 */
	String OVERRIDE_MODIFIER_INFO = "OVERRIDE_MODIFIER_INFO";

	/** The upload session folder. The folder containing files that are going to be uploaded to DMS. */
	String UPLOAD_SESSION_FOLDER = "UPLOAD_SESSION_FOLDER";

	/**
	 * The disable audit log operation. This should prompt the audit module not to add the current operation to the
	 * audit log. For security purposes it's advisable at least to print in the log that operation is skipped for
	 * instance.
	 */
	String DISABLE_AUDIT_LOG = "DISABLE_AUDIT_LOG";

	/** The do not persist in any database. */
	String DO_NOT_PERSIST = "DO_NOT_PERSIST";

	/** The do not persist in relational database. */
	String DO_NOT_PERSIST_IN_RD = "DO_NOT_PERSIST_IN_RD";

	/** The do not persist in semantic database. */
	String DO_NOT_PERSIST_IN_SD = "DO_NOT_PERSIST_IN_SD";

	/**
	 * Setting this property will cause current operation to be added in custom owlim graph. Don't forget to disable it
	 * after operation finished.
	 */
	String IMPORT_CUSTOM_GRAPH = "IMPORT_CUSTOM_GRAPH";

	/**
	 * Setting this will force permissions to be overloaded in semantic. Don't forget to disable it after operation
	 * finished.
	 */
	String FORCE_PERMISSIONS = "FORCE_PERMISSIONS";

	/**
	 * The allow loading of deleted instances. By default deleted instances are not returned by the general queries, but
	 * because the instances are marked as deleted in advance and need to be queries later again this requires this
	 * option.
	 */
	String ALLOW_LOADING_OF_DELETED_INSTANCES = "ALLOW_LOADING_OF_DELETED_INSTANCES";

	/**
	 * Setting this will stop rules from executing. Don't forget to disable it after operation finished.
	 */
	String DISABLE_RULES = "DISABLE_RULES";

	/**
	 * The disable automatic id generation on instance creation. Every instance before persisted for the first time have
	 * their ids generated if defined. This option could disable this behavior. This is useful for example when creating
	 * copies of instances like for publishing.
	 */
	String DISABLE_AUTOMATIC_ID_GENERATION = "DISABLE_AUTOMATIC_ID_GENERATION";

	/** The disable thumbnail loading when returning instance. */
	String DISABLE_THUMBNAIL_LOADING = "DISABLE_THUMBNAIL_LOADING";

	/**
	 * The disable instance post loaders for loaded instances. This may include thumbnails, user favorites etc. This can
	 * be used when executing background operations that does not require this information to be loaded.
	 */
	String DISABLE_POST_INSTANCE_LOAD_DECORATION = "DISABLE_POST_INSTANCE_LOAD_DECORATION";

	/**
	 * Prevents instance's save after attach operation. Helpful when we have saved the instance before that and want to
	 * avoid second save.
	 */
	String PREVENT_SAVE_AFTER_ATTACH = "PREVENT_SAVE_AFTER_ATTACH";
}
