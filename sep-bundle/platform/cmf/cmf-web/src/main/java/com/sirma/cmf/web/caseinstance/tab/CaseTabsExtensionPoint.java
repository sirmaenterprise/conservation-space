package com.sirma.cmf.web.caseinstance.tab;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Extension point for case tabs.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
@Documentation("Extension point for case tabs.")
public class CaseTabsExtensionPoint implements Plugable {

	public static final String EXTENSION_POINT = "case:tabs";

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * CaseDashboardTab extension.
	 */
	@Extension(target = EXTENSION_POINT, order = 10, enabled = true)
	public static class CaseDashboardTab implements PageFragment {

		@Override
		public String getPath() {
			return "/case/tab/dashboard.xhtml";
		}

	}

	/**
	 * CaseDetailsTab extension.
	 */
	@Extension(target = EXTENSION_POINT, order = 20, enabled = true)
	public static class CaseDetailsTab implements PageFragment {

		@Override
		public String getPath() {
			return "/case/tab/case-details.xhtml";
		}

	}

	/**
	 * CaseDocumentsTab extension.
	 */
	@Extension(target = EXTENSION_POINT, order = 30, enabled = true)
	public static class CaseDocumentsTab implements PageFragment {

		@Override
		public String getPath() {
			return "/case/tab/documents.xhtml";
		}

	}

	/**
	 * CaseWorkflowListTab extension.
	 */
	@Extension(target = EXTENSION_POINT, order = 45, enabled = true)
	public static class CaseWorkflowListTab implements PageFragment {

		@Override
		public String getPath() {
			return "/case/tab/workflow-list.xhtml";
		}

	}

	/**
	 * CaseDiscussionsTab extension.
	 */
	@Extension(target = EXTENSION_POINT, order = 60, enabled = false)
	public static class CaseDiscussionsTab implements PageFragment {

		@Override
		public String getPath() {
			return "/case/tab/discussions.xhtml";
		}

	}

	/**
	 * CaseHistoryTab extension.
	 */
	@Extension(target = EXTENSION_POINT, order = 70, enabled = false)
	public static class CaseHistoryTab implements PageFragment {

		@Override
		public String getPath() {
			return "/case/tab/history.xhtml";
		}

	}

}
