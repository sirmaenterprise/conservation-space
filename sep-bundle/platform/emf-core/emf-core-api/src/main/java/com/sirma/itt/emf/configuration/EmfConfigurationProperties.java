/*
 *
 */
package com.sirma.itt.emf.configuration;

import com.sirma.itt.emf.annotation.Optional;
import com.sirma.itt.emf.util.Documentation;

/**
 * Emf configuration properties.
 * 
 * @author BBonev
 */
@Documentation("Base EMF configuration properties.")
public interface EmfConfigurationProperties extends Configuration {
	/** The system language. */
	@Documentation("The default system language. If nothing other is specified or for operations where there is no logged in user.<b> Default value is: bg</b>.")
	String SYSTEM_LANGUAGE = "system.language";

	/** The system user display name. */
	@Documentation("Display name of for the system user. <b>Default value is: System</b>")
	String SYSTEM_USER_DISPLAY_NAME = "system.displayname";

	/** Path to external properties file. */
	@Documentation("Used to configure external source of labels - properties file on the file system")
	String SYSTEM_LABEL_EXTERNAL_PATH = "system.label.externalPath";

	/** The temporary directory. */
	@Documentation("Temporary directory path. If not specified or points to invalid folder or folder with no write access then the server will use a directory as %java.io.tmpdir%/EMF")
	String TEMP_DIR = "temp.dir";
	/** The temporary directory protect hours. */
	@Documentation("The number of hours to keep the files into the system temporary folder. <b>Default value is: 24</b>.")
	String TEMP_DIR_PROTECT_HOURS = "temp.dir.protectHours";

	/** The admin user name. */
	@Documentation("The admin user name. This user is used to connect to DMS when no other authenticated user is logged in.")
	String ADMIN_USERNAME = "admin.username";

	/** The admin password. */
	@Documentation("The admin user password. If SSO/SAML is enabled this property is not used.")
	String ADMIN_PASSWORD = "admin.password";

	/** The exclude definitions. */
	@Documentation("A list of definitions to be excluded from the list of allowed definitions for creation. This is valid for all definitions no matter the type. If more then one value they should be separeted by comma. Ex: def1, def2, ...")
	String EXCLUDE_DEFINITIONS = "exclude.definitions";

	/** The default container. */
	@Documentation("Defines the default container/tenant ID. "
			+ "The container id is the containing DMS site id, "
			+ "where the emf/cmf files are stored. If the tenant id of the logged user is not "
			+ "provided then this is used for tenant separation.")
	String DEFAULT_CONTAINER = "default.container";

	/**
	 * Forces the definition compiler to initialize only definitions from the default container.
	 */
	@Documentation("Forces the definition compiler to initialize only definitions from the default container defined by the property "
			+ DEFAULT_CONTAINER + ". <b>Default value is: false</b>.")
	String INITIALIZE_DEFAULT_CONTAINER_ONLY = "compiler.definition.initializeDefaultContainerOnly";

	/** The disable definition compiler. */
	@Documentation("Configuration that disables the definition synchronization. Turning this value to true will speed the startup which is very useful for development purposes. <b>Default value: false</b>")
	String DISABLE_DEFINITION_COMPILER = "compiler.definition.disable";

	/** max sizes for returned results. */
	@Documentation("Number of results to be fetched from the database. <b>Default value: 1000</b>")
	@Optional
	String SEARCH_RESULT_MAXSIZE = "search.result.maxsize";

	/** max sizes for uploaded files. */
	@Documentation("Max file size allowed for upload in bytes. <b>Default value: 10000000</b>")
	@Optional
	String FILE_UPLOAD_MAXSIZE = "file.upload.maxsize";

	/**
	 * The default date format to be used in the application. The format is used in expressions.
	 */
	@Documentation("The default date format to be used in the application. The format is used in expressions.<p>Since v1.0.4.")
	String DEFAULT_DATE_FORMAT = "date.format.default";
	/**
	 * The short date format. The format used for short date formatting like year, month and day of
	 * the month.
	 */
	@Documentation("The short date format. The format used for short date formatting like year, month and day of the month.<p>Since v1.0.4.")
	String SHORT_DATE_FORMAT = "date.format.short";
	/**
	 * The full date format. The format used for short date formatting like year, month and day of
	 * the month and hour to seconds.
	 */
	@Documentation("The full date format. The format used for short date formatting like year, month and day of the month and hour to seconds.<p>Since v1.0.4.")
	String FULL_DATE_FORMAT = "date.format.full";

	/**
	 * The delay between check for new tasks of the timed scheduler executor in seconds. <b>Default
	 * value: 60</b>
	 */
	@Documentation("The delay between check for new tasks of the timed scheduler executor in seconds. <b>Default value: 60</b>")
	String TIME_SCHEDULER_EXECUTOR_CHECK_INTERVAL = "scheduler.timer.checkInterval";

	/**
	 * The delay between retries to execute failed task in seconds. The time is multiplied by the
	 * number of retries. Example: if not set and error on the 3rd retry the wait time will be 3 *
	 * 60 seconds = 3 minutes. <b>Default value: 60</b>
	 */
	@Documentation("The delay between retries to execute failed task in seconds. The time is multiplied by the"
			+ "number of retries. Example: if not set and error on the 3rd retry the wait time will be 3 *"
			+ " 60 seconds = 3 minutes. <b>Default value: 60</b>")
	String TIME_SCHEDULER_EXECUTOR_RETRY_INTERVAL = "scheduler.timer.retryInterval";

	/** The release info file location. */
	@Documentation("Info file to read the build info from. Should be properties like. Default value is current: manifest.mf.")
	@Optional
	String RELEASE_INFO_FILE_LOCATION = "info.release.file.location";

	/** The admin group name. */
	@Documentation("The admin group name.The default is null, so no group is considered as admin.")
	@Optional
	String ADMIN_GROUP_NAME = "admin.groupname";

	/**
	 * The definition download pool size. Defines how many definitions to download/load
	 * simultaneously. The maximum value is 5 times the number of the cores. If 0 then 1 processor
	 * will be used. Default value: -1 (the number of processor cores).
	 */
	@Documentation("The definition download pool size. "
			+ "Defines how many definitions to download/load simultaneously. "
			+ "The maximum value is 5 times the number of the cores. "
			+ "If 0 then 1 processor will be used. Default value (the number of processor cores): -1")
	String DEFINITION_DOWNLOAD_POOL_SIZE = "compiler.definition.processingPoolSize";

	/**
	 * If definitions should be loaded and compiled in parallel or sequentially If set to true then
	 * the different definitions of type template or root will be loaded in parallel but sharing
	 * common thread pool for file downloading otherwise they will run one after another. This is
	 * disabled by default due to unknown deployment errors in the test environment. This
	 * optimization could speed up the startup up to 50%. <b>Default value: false</b>
	 */
	@Documentation("If definitions should be loaded and compiled in parallel or sequentially. "
			+ "If set to true then the different definitions of type template or root will be loaded in parallel but "
			+ "sharing common thread pool for file downloading otherwise they will run one after another. "
			+ "This is disabled by default due to unknown deployment errors in the test environment. "
			+ "This optimization could speed up the startup up to 50%. <b>Default value: false</b>")
	String PARALLEL_DEFINITION_COMPILING = "compiler.definition.asynchronousLoading";

	/** The cache provider class. */
	@Documentation("Cache provider class. "
			+ "<br>Default cache provider of no other is specified. Other possible option is "
			+ "<ul><li> com.sirma.itt.emf.cache.NullCacheProvider - No cache"
			+ "<li> com.sirma.itt.emf.cache.InMemoryCacheProvider - Default value if nothing is specified. This is non tansactional, no limit memory cache. If used for a long time the could lead to memory leaks and OutOfMemory exceptions"
			+ "<li> com.sirma.itt.cmf.cache.InfinispanCacheProvider - transactional, clustered and configurable cache. This needs additional module: cmf-cache-infinispan</ul><br>Default value: com.sirma.itt.cmf.cache.InfinispanCacheProvider")
	String CACHE_PROVIDER_CLASS = "cache.provider.class";

	/** The asynchronous task pool size. */
	@Documentation("Defines the pool size for parallel tasks. The pool is shared between all users.<br>Minimum value should not be less then the per user configuration.<br>Max pool size is 100. Values greater than this will be set to 100.<br> Default value is: 50")
	String ASYNCHRONOUS_TASK_POOL_SIZE = "asynchronous.task.poolSize";

	/** The asynchronous task per user pool size. */
	@Documentation("Defines the number of parallel tasks allowed per user.<br>Maximum value: 10. Values greater than this will be set to 10.<br>Default value: 5")
	String ASYNCHRONOUS_TASK_PER_USER_POOL_SIZE = "asynchronous.task.perUser.poolSize";

	/** The application mode development. */
	@Documentation("If application is in development mode or not. Default value: false")
	String APPLICATION_MODE_DEVELOPEMENT = "application.mode.development";

	/** The notifications templates storage directory. */
	@Documentation("Where are stored the templates for notifications?")
	@Optional
	String NOTIFICATIONS_TEMPLATES_STORAGE_DIRECTORY = "notifications.templates.storage.dir";

	/** The notifications enabled. */
	@Documentation("When false, no automatic mail notifications are sent. Default value: true")
	@Optional
	String NOTIFICATIONS_ENABLED = "notifications.enabled";

	/** The system default host name. */
	@Documentation("The default ip address/hostname ")
	@Optional
	String SYSTEM_DEFAULT_HOST_NAME = "system.default.host.name";

	/** The system default host port. */
	@Documentation("The default socket port ")
	@Optional
	String SYSTEM_DEFAULT_HOST_PORT = "system.default.host.port";

	/** The system default host protocol. */
	@Documentation("The default socket protocol ")
	@Optional
	String SYSTEM_DEFAULT_HOST_PROTOCOL = "system.default.host.protocol";

	/**
	 * Specifies the full path the wkhtmltopdf library, used to convert html documents to PDF.
	 */
	@Documentation("Specifies the full path the wkhtmltopdf library, used to convert html documents to PDF.")
	@Optional
	String WKHTMLTOPDF_LOCATION = "wkhtmltopdf.location";

	/**
	 * This is the filesystem path to a javascript file which will be executed when exporting
	 * documents. The file has to escape correctly the right symbols for the different operation
	 * systems (i.e. in DOS % has to be escaped like this: %%).
	 */
	@Optional
	@Documentation("Specifies the full path the javascript that's executed after page export page has "
			+ "loaded and before wkhtmltopdf starts exporting. The script cuts the emf menus.")
	String POST_WEB_KIT_JAVASCRIPTS = "export.js.file.location";

	/**
	 * Script engine to be used for executing custom server side scripts. <b>Default value:
	 * javascript</b>
	 */
	@Documentation("Script engine to be used for executing custom server side scripts. <b>Default value: javascript</b>")
	String DEFAULT_SCRIPT_ENGINE = "script.engine";

	/** The export username. */
	@Documentation("Username to be used for authentication when running export")
	String EXPORT_USERNAME = "export.username";

	/** The export username password. */
	@Documentation("Password for the username used for authentication when running export")
	String EXPORT_USERNAME_PASSWORD = "export.username.password";

	/** The session timeout period. */
	@Documentation("Default user session timeout time in minutes. <b>Default value is: 30</b>")
	@Optional
	String SESSION_TIMEOUT_PERIOD = "session.timeout.period";

	/**
	 * Property to define max retries for thumbnail download. <b>Default value: 5</b>
	 */
	@Documentation("Property to define max retries for thumbnail download. <b>Default value: 5</b>")
	String THUMBNAIL_MAX_DOWNLOAD_RETRIES = "thumbnail.maxDownloadRerties";

	/**
	 * Property to define thumbnail loader threads. The threads used are from the default EJB thread
	 * pool so make sure there enough threads for running the application and thumbnail loading.
	 * <b>Default value: 5</b>
	 */
	@Documentation("Property to define thumbnail loader threads. "
			+ "The threads used are from the default EJB thread pool so make sure "
			+ "there enough threads for running the application and thumbnail loading. <b>Default value: 5</b>")
	String THUMBNAIL_LOADER_THREADS = "thumbnail.loder.threads";

	/**
	 * Property to define the maximum elements for loading that will trigger parallel loading. All
	 * elements under that number will be handled over one thread. <b>Default value: 10</b>
	 */
	@Documentation("Property to define the maximum elements for loading that will trigger parallel loading. "
			+ "All elements under that number will be handled over one thread. <b>Default value: 10</b>")
	String THUMBNAIL_LOADER_PARALLEL_THRESHOLD = "thumbnail.loader.parallel.threshold";

	/**
	 * Property to define the maximum elements to be loaded by each parallel thread. This value will
	 * default the maximum elements processed in a single activation: threads x maxElements. If no
	 * limit is required -1 could be set to disable the limit. <b>Default value: 50</b>
	 */
	@Documentation("Property to define the maximum elements to be loaded by each parallel thread. This value will "
			+ "default the maximum elements processed in a single activation: threads x maxElements. If no"
			+ " limit is required -1 could be set to disable the limit. <b>Default value: 50</b>")
	String THUMBNAIL_LOADER_MAX_PARALLEL_DATA_SIZE = "thumbnail.loader.parallel.maxDataSize";

	/**
	 * Defines the minutes expression when thumbnails should be checked for download. The service
	 * will run then to collect non synchronized thumbnails. By default will run every 30 seconds.
	 * <b>Default value: *</b>
	 */
	@Documentation("Defines the minutes expression when thumbnails should be checked for download. "
			+ "The service will run then to collect non synchronized thumbnails. By default will run every 30 seconds. <b>Default value: *</b>")
	String THUMBNAIL_SCHEDULE_EXPRESSION_MINUTES = "thumbnail.schedule.expression.minues";

	/**
	 * Defines the seconds expression when thumbnails should be checked for download. The service
	 * will run then to collect non synchronized thumbnails. By default will run every 30 seconds.
	 * <b>Default value: *&#47;30</b>
	 */
	@Documentation("Defines the seconds expression when thumbnails should be checked for download. "
			+ "The service will run then to collect non synchronized thumbnails. By default will run every 30 seconds. <b>Default value: */30</b>")
	String THUMBNAIL_SCHEDULE_EXPRESSION_SECONDS = "thumbnail.schedule.expression.seconds";

	/** Date format pattern that is used in DateConverter. Formats the date as 21.11.2012 */
	@Documentation("Date format pattern that is used in DateConverter. Formats the date as 21.11.2012")
	String CONVERTER_DATE_FORMAT = "date.converter.format";
	/**
	 * Date time format pattern that is used in DateConverter. Formats the date as 21.11.2012, 12:30
	 */
	@Documentation("Date time format pattern that is used in DateConverter. Formats the date as 21.11.2012, 12:30")
	String CONVERTER_DATETIME_FORMAT = "datetime.converter.format";
}
