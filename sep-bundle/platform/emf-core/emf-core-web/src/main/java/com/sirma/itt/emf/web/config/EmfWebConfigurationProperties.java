package com.sirma.itt.emf.web.config;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Configuration properties for Emf web module.
 *
 * @author Adrian Mitev
 */
@Documentation("Configuration properties for Emf web module.")
public interface EmfWebConfigurationProperties extends Configuration {

	/** The dms link. */
	@Documentation("Link to DMS server profile to navigate from CMF when user clicks an operation that need to be exectuetd in DMS system.")
	String DMS_LINK = "dms.link";

	/** The application logo image path. */
	@Documentation("Path to an image to be used as logo in CMF")
	String APPLICATION_LOGO_IMAGE_PATH = "application.logo.image.path";

	/** The application favicon image path. */
	@Documentation("Path to an image to be used as favicon. <b>Default value: images\\:favicon.png</b>")
	String APPLICATION_FAVICON_IMAGE_PATH = "application.favicon.image.path";

	/** The ui page size. */
	@Documentation("Datascroller configuration properties. Number of rows to be visible in the underlying list. <b>Default value: 25</b>")
	String SEARCH_RESULT_PAGER_PAGESIZE = "search.result.pager.pagesize";

	/** The ui pageing pager maxpages. */
	@Documentation("Number of pages to be visible in datascroller. <b>Default value: 5</b>")
	String SEARCH_RESULT_PAGER_MAXPAGES = "search.result.pager.maxpages";

	@Documentation("Maximum elements to be displayed in user dashlet. <b>Default value is: 25</b>")
	String DASHLET_SEARCH_RESULT_PAGESIZE = "dashlet.search.result.pager.pagesize";

	@Documentation("How much navigation point entries to be stored in navigation history. <b>Default value is: 20</b>")
	String NAVIGATION_HISTORY_MAX_COUNT = "navigation.history.max.count";

	/** The clientside rnc debug mode. */
	@Documentation("To enable client site debug mode of RnC. <b>Default value is: false</b>")
	String CLIENTSIDE_RNC_DEBUG_MODE = "clientside.rnc.debug.mode";

	/** The application footer enable. */
	@Documentation("If a footer should be visible on page. <b>Default value is: false</b>")
	String APPLICATION_FOOTER_ENABLE = "application.footer.enable";

	/** The application module schedule disable. */
	@Documentation("If PM schedule module should be inaccessible trough the application. <b>Default value is: false</b>")
	String APPLICATION_MODULE_SCHEDULE_DISABLE = "application.module.schedule.disable";

	@Documentation("If hot redeploy functionality should be enabled.")
	String HOT_REDEPLOY_ENABLED = "hot.redeploy.enabled";

	/** The datepicker first week day. */
	@Documentation("Whether the datepicker control should start the week from the Sunday=0 or Monday=1. <b>Default value is: 1</b>")
	String DATEPICKER_FIRST_WEEK_DAY = "datepicker.first.week.day";

	/** Date format pattern that is used in jQuery datepicker. Formats the date as 21.11.2012 */
	@Documentation("Date format pattern that is used in jQuery datepicker. Formats the date as 21.11.2012")
	String DATE_FORMAT = "date.format";

	/** Date format pattern that is used in extJS datepicker. Formats the date as 21.11.2012 */
	@Documentation("Date format pattern that is used in extJS datepicker. Formats the date as 21.11.2012")
	String DATE_EXTJS_FORMAT = "date.extjs.format";

	/**
	 * Register year range for date/time pickers. First value describe number of years before
	 * current and the second after current year.
	 */
	@Documentation("Register year range based on current. The default is <b>-120:+3</b>.")
	String DATEPICKER_YEAR_RANGE = "datepicker.years.range";

	/**
	 * User help module access link.
	 */
	@Documentation("User help module access link.")
	String HELP_MODULE_LINK = "help.module.link";

	@Documentation("The minimum number of results that must be initially populated in order to keep the search field in autocomplete fields visible.")
	String UI_AUTOCOMPLETE_MINIMUMITEMS = "ui.autocomplete.minimumitems";

	@Documentation("Enables javascript debug mode where available. Default: false")
	String JAVSCRIPT_DEBUG = "javascript.debug";

	/** Whether or not to install logging PhaseListener. */
	@Documentation("If true a special phase tracking PhaseListener is installed that logs the time taken by each jsf phase")
	String UI_JSF_PHASETRACKER = "ui.jsf.phasetracker";

	/** Whether or not to activate css and js merge. */
	@Documentation("When true the css and javascript resources are merged in a single file for faster page loading")
	String RESOURCES_MERGE = "ui.resources.merge";

	/** Whether or not to activate css and js minification. */
	@Documentation("When true the css and javascript resources will be minifed after merging")
	String RESOURCES_MINIFY = "ui.resources.minify";

}
