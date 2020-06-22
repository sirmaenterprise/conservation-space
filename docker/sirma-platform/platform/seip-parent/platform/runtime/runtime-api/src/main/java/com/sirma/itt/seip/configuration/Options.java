/*
 *
 */
package com.sirma.itt.seip.configuration;

import com.sirma.itt.seip.context.Config;
import com.sirma.itt.seip.context.Option;
import com.sirma.itt.seip.context.RuntimeContext;

/**
 * This is helper class for {@link RuntimeContext} and {@link RuntimeConfigurationProperties}.
 * <p>
 * Application thread local configurations. The configurations provided here are valid only for the current thread that
 * runs the code. The configurations are transfered to the additional threads scheduled for execution from the current
 * thread with active options when called via (@code com.sirma.itt.emf.concurrent.GenericAsyncTask).
 * <p>
 * <b>NOTE</b>: the user should disable each enabled option when the scope is completed. Example of use:
 * <p>
 *
 * <pre>
 * <code>
 * Options.DO_NOT_CALL_DMS.enable();
 * try {
 * 	// do some work here
 * } finally {
 * 	Options.DO_NOT_CALL_DMS.disable();
 * }</code>
 * </pre>
 *
 * @author BBonev
 */
public class Options {

	/**
	 * Instantiates a new options.
	 */
	private Options() {
		// utility class
	}

	/**
	 * No operation option. Option that does not activate or deactivate anything. Used for NULL pattern.
	 */
	public static final Option NO_OP = RuntimeContext.createOption("NO_OP");

	/* OPTIONS */

	/**
	 * The do not persist in relational database. When this is activated the all operations that must store data in the
	 * relational database will do nothing.
	 */
	public static final Option DO_NOT_PERSIST_IN_RD = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DO_NOT_PERSIST_IN_RD);
	/**
	 * The do not persist in semantic database.When this is activated the all operations that must store data in the
	 * semantic database will do nothing.
	 */
	public static final Option DO_NOT_PERSIST_IN_SD = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DO_NOT_PERSIST_IN_SD);
	/**
	 * The do not persist in any database. When this is activated the all operations that must store data will do
	 * nothing.
	 */
	public static final Option DO_NOT_PERSIST = RuntimeContext
			.createGroup(RuntimeConfigurationProperties.DO_NOT_PERSIST, DO_NOT_PERSIST_IN_RD, DO_NOT_PERSIST_IN_SD);
	/**
	 * If enabled the current executing operation should not call DMS sub system.
	 */
	public static final Option DO_NOT_CALL_DMS = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DO_NO_CALL_DMS);

	/**
	 * Use the add only properties option when saving properties of instances.<br>
	 * NOTE: this will not remove any properties but will only add new properties and update modified.
	 */
	public static final Option ADD_ONLY_PROPERTIES = RuntimeContext
			.createOption(RuntimeConfigurationProperties.ADD_ONLY_PROPERTIES);

	/**
	 * When saving complex objects sometimes if the default behavior is not to save the child object cascade. To
	 * activate the cascade persist the option could be used.
	 */
	public static final Option SAVE_CHILDREN = RuntimeContext
			.createOption(RuntimeConfigurationProperties.SAVE_CHILDREN);

	/** Audit disabled is configuration to stop automatic update of modified/modifier properties. */
	public static final Option AUDIT_MODIFICATION_DISABLED = RuntimeContext
			.createOption(RuntimeConfigurationProperties.AUDIT_MODIFICATION_DISABLED);

	/**
	 * The called method that supports this should only update the internal cache but not the database if connected.
	 */
	public static final Option CACHE_ONLY_OPERATION = RuntimeContext
			.createOption(RuntimeConfigurationProperties.CACHE_ONLY_OPERATION);

	/**
	 * The disable audit log operation. This should prompt the audit module not to add the current operation to the
	 * audit log. For security purposes it's advisable at least to print in the log that operation is skipped for
	 * instance.
	 */
	public static final Option DISABLE_AUDIT_LOG = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DISABLE_AUDIT_LOG);

	/** The disable automatic context to child links created when creating new instances. */
	public static final Option DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS);

	/** The disable all automatic links creation. */
	public static final Option DISABLE_AUTOMATIC_LINKS = RuntimeContext.createGroup(
			RuntimeConfigurationProperties.DISABLE_AUTOMATIC_LINKS, DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS);

	/**
	 * This will disable the checks on every instance that will be persisted for stale data modifications. This will
	 * allow to save an instance that is with modify date different from the one in the database. This should be used
	 * only for external system integrations and in {@link #OVERRIDE_MODIFIER_INFO} option.
	 */
	public static final Option DISABLE_STALE_DATA_CHECKS = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DISABLE_STALE_DATA_CHECKS);

	/** Disables firing of the instance persist event */
	public static final Option DO_NOT_FIRE_PERSIST_EVENT = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DO_NOT_FIRE_PERSIST_EVENT);

	/** The do not load children override. */
	public static final Option DO_NOT_LOAD_CHILDREN_OVERRIDE = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DO_NOT_LOAD_CHILDREN_OVERRIDE);
	/** The use container filtering when searching for properties and definitions. */
	public static final Option DO_NOT_USE_CONTAINER_FILTERING = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DO_NOT_USE_CONTAINER_FILTERING);

	/**
	 * The generate random link id when creating new relations This overrides the default behavior of the service to
	 * generate id based on the source, destination and link type. This is useful when need to create more than one
	 * relation of the same type between the same instances like logging work on task.
	 */
	public static final Option GENERATE_RANDOM_LINK_ID = RuntimeContext
			.createOption(RuntimeConfigurationProperties.GENERATE_RANDOM_LINK_ID);

	/**
	 * The load link properties. Forces the link service to load all link properties. Properties loading is disabled by
	 * default for performance optimization. The service will load the state and the creator of the link but nothing
	 * else. This applies only for semantic service for now.
	 */
	public static final Option LOAD_LINK_PROPERTIES = RuntimeContext
			.createOption(RuntimeConfigurationProperties.LOAD_LINK_PROPERTIES);

	/**
	 * Enabling of the option will disable setting modifier and data of modification when saving instance. Note that if
	 * the caller does not set these fields they will remain the same or will not be set at all if empty. This could be
	 * used if have to perform save without marking the instances as modified or external integration. To work this
	 * properly make sure to use the {@link #DISABLE_STALE_DATA_CHECKS} if needed.
	 */
	public static final Option OVERRIDE_MODIFIER_INFO = RuntimeContext
			.createOption(RuntimeConfigurationProperties.OVERRIDE_MODIFIER_INFO);

	/** Tells the properties service to save properties without definition. */
	public static final Option SAVE_PROPERTIES_WITHOUT_DEFINITION = RuntimeContext
			.createOption(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);

	/**
	 * The allow loading of deleted instances. By default deleted instances are not returned by the general queries, but
	 * because the instances are marked as deleted in advance and need to be queries later again this requires this
	 * option.
	 */
	public static final Option ALLOW_LOADING_OF_DELETED_INSTANCES = RuntimeContext
			.createOption(RuntimeConfigurationProperties.ALLOW_LOADING_OF_DELETED_INSTANCES);

	/**
	 * Setting this will stop rules from executing. Don't forget to disable it after operation finished.
	 */
	public static final Option DISABLE_RULES = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DISABLE_RULES);

	/**
	 * The disable automatic id generation on instance creation. Every instance before persisted for the first time have
	 * their ids generated if defined. This option could disable this behavior. This is useful for example when creating
	 * copies of instances like for publishing.
	 */
	public static final Option DISABLE_AUTOMATIC_ID_GENERATION = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DISABLE_AUTOMATIC_ID_GENERATION);

	/**
	 * The disable instance post loaders for loaded instances. This may include thumbnails, user favorites etc. This can
	 * be used when executing background operations that does not require this information to be loaded.
	 */
	public static final Option DISABLE_POST_INSTANCE_LOAD_DECORATION = RuntimeContext
			.createOption(RuntimeConfigurationProperties.DISABLE_POST_INSTANCE_LOAD_DECORATION);

	/**
	 * Prevents instance's save after attach operation when enabled. Helpful when we have saved the instance before that
	 * and want to avoid second save.
	 */
	public static final Option PREVENT_SAVE_AFTER_ATTACH = RuntimeContext
			.createOption(RuntimeConfigurationProperties.PREVENT_SAVE_AFTER_ATTACH);

	/* CONFIGURATIONS */

	/**
	 * The use recursive conversion. When executing type conversion if the converted object has supports a nested
	 * conversion (like tree elements) if the converter implementation should process the child elements or not. The
	 * default behavior is not to process child elements so this should be explicitly set.
	 */
	public static final Option USE_RECURSIVE_CONVERSION = RuntimeContext
			.createOption(RuntimeConfigurationProperties.USE_RECURSIVE_CONVERSION);

	/** The currently executed operation by the current user if any. */
	public static final Config CURRENT_OPERATION = RuntimeContext
			.createConfig(RuntimeConfigurationProperties.CURRENT_OPERATION, true);

	/**
	 * The upload session folder. The folder containing files that are going to be uploaded to DMS.
	 */
	public static final Config UPLOAD_SESSION_FOLDER = RuntimeContext
			.createConfig(RuntimeConfigurationProperties.UPLOAD_SESSION_FOLDER, true);

	/**
	 * Setting this property will cause current operation to be added in custom owlim graph. Don't forget to disable it
	 * after operation finished.
	 */
	public static final Config USE_CUSTOM_GRAPH = RuntimeContext
			.createConfig(RuntimeConfigurationProperties.IMPORT_CUSTOM_GRAPH, true);

	/**
	 * Setting this will force permissions to be overloaded in semantic. Don't forget to disable it after operation
	 * finished.
	 */
	public static final Config FORCE_PERMISSIONS = RuntimeContext
			.createConfig(RuntimeConfigurationProperties.FORCE_PERMISSIONS, true);
}
