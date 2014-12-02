package com.sirma.itt.pm.web.menu;

import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.cmf.web.menu.main.CmfMenuItem;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.PageModel;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Menu item extension point to add projects menu.
 * 
 * @author svelikov
 */
public class PMMenuItem extends CmfMenuItem {

	/**
	 * The Class MyProjectsMenuItem.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 500, priority = 1)
	public static class MyProjectsMenuItem extends AbstractPageModel implements PageFragment,
			Plugable {

		private static final String FRAGMENT_NAME = "my-projects";

		private static final String MY_PROJECTS_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		public static final String EXTENSION_POINT = "myProjects";

		@Inject
		@ExtensionPoint(value = EXTENSION_POINT)
		private Iterable<PageModel> extension;

		@Override
		public String getPath() {
			return "/menu/main/my-projects.xhtml";
		}

		@Override
		public String getExtensionPoint() {
			return EXTENSION_POINT;
		}

		@Override
		public String getPageFragment() {
			Iterator<PageModel> iterator = extension.iterator();
			List<String> loadHtmlPageFragments = loadHtmlPageFragments(iterator);
			return buildTemplate(
					createModel(FRAGMENT_NAME, null, "pm.menu.my-projects", null,
							loadHtmlPageFragments, Boolean.TRUE), MY_PROJECTS_TEMPLATE_PATH);
		}
	}

	/**
	 * The Class NewProject.
	 */
	@ApplicationScoped
	@Extension(target = MyProjectsMenuItem.EXTENSION_POINT, enabled = true, order = 1, priority = 1)
	public static class NewProject extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "new-project";

		private static final String NEW_PROJECT_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ MyProjectsMenuItem.FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		@Override
		public String getPath() {
			return "";
		}

		@Override
		public String getPageFragment() {
			return buildTemplate(
					createModel(FRAGMENT_NAME, "/project/project.jsf?actionId=createProject",
							"pm.menu.new-project", null, null, null), NEW_PROJECT_TEMPLATE_PATH);
		}
	}

	/**
	 * The Class ListProjects.
	 */
	@ApplicationScoped
	@Extension(target = MyProjectsMenuItem.EXTENSION_POINT, enabled = true, order = 2, priority = 1)
	public static class ListProjects extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "list-projects";

		private static final String LIST_PROJECTS_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ MyProjectsMenuItem.FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		@Override
		public String getPath() {
			return "";
		}

		@Override
		public String getPageFragment() {
			return buildTemplate(
					createModel(FRAGMENT_NAME, "/project/project-list.jsf?searchAll=true",
							"pm.menu.list-projects", null, null, null), LIST_PROJECTS_TEMPLATE_PATH);
		}
	}
}
