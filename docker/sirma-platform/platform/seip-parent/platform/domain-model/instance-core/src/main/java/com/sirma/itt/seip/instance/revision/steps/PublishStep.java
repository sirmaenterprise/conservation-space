package com.sirma.itt.seip.instance.revision.steps;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * When instance is published there are multiple steps that should be executed depending on the instance type and scopes
 * of the application. This class represents a single step to be executed on an instance that is being published. <br>
 * The steps should not be stateful.
 * <p>
 * Typical steps are:
 * <ul>
 * <li>Clone uploaded document
 * <li>Print published Idoc tabs as PDF
 * <li>Clone Idoc - generate new ids for widgets and tabs
 * <li>Replaced published tabs with Content Viewer for published PDF
 * <li>Run widgets queries and replace the selected instance with instance versions
 * <li>etc
 * </ul>
 *
 * @author BBonev
 */
public interface PublishStep extends Plugin, Named {

	String EXTENSION_NAME = "publishStep";

	/**
	 * Define step names that could be executed over each published instance.
	 *
	 * @author BBonev
	 */
	enum Steps implements Named {
		/**
		 * Initial step that should initialize the context for future steps (load and build Idoc instance)
		 */
		INITIAL("initialStep"),
		/**
		 * Export the marked tabs as PDF. This step is applicable when export to PDF is selected
		 */
		EXPORT_TABS_AS_PDF("exportTabsAsPdf"),
		/**
		 * Clone uploaded content as content to the new revision. This step is applicable for instances with uploaded
		 * content.
		 */
		CLONE_UPLOADED_CONTENT("cloneUploadedContent"),
		/**
		 * Replace tabs that are marked for publish with a single tab with a content viewer. Applicable for publish as
		 * PDF action for Idocs.
		 */
		REPLACE_EXPORTED_TABS("replaceExportedTabs"),
		/**
		 * Remove tabs that are marked for non publishable
		 */
		REMOVE_NON_PUBLISHED_TABS("removeNonPublishedTabs"),
		/**
		 * Lock widget data by running their queries and converting the manually and automatically selected instance ids
		 * to versions
		 */
		LOCK_WIDGET_DATA("lockWidgetData"),
		/**
		 * Export uploaded content to PDF. Applicable for instances with uploaded content
		 */
		EXPORT_CONTENT_TO_PDF("exportContentToPdf"),
		/**
		 * Create relations for properties that are displayed in the published tabs. Also copy required/mandatory object
		 * properties from the original instance to the new revision
		 */
		COPY_RELATIONS("copyRelations"),
		/**
		 * Finalization step that persist the revision view.
		 */
		SAVE_REVISION_VIEW("saveRevisionView"),
		/**
		 * Copy permissions from the original instance to the revision. All special and inherited permissions should be
		 * copied to the revision.
		 */
		COPY_PERMISSIONS("copyPermissions"),
		/**
		 * Transfer the thumbnail of the original instance to the new revision. Applicable for all instances
		 */
		SYNC_THUMBNAIL("syncThumbnail"),
		/**
		 * Sets uploaded content as primary content to the new revision. This step is applicable for instances with created
		 *  and uploaded content, where the revision is new uploaded content.
		 */
		SET_PRIMARY_CONTENT("setUploadAsContent");
		private final String name;

		Steps(String stepName) {
			name = stepName;
		}

		/**
		 * Get step name
		 */
		@Override
		public String getName() {
			return name;
		}
	}

	/**
	 * The actual step execution
	 *
	 * @param publishContext
	 *            the current publish context that contains information about the new revision
	 */
	void execute(PublishContext publishContext);

	/**
	 * Step name. The steps are called by name
	 */
	@Override
	String getName();
}
