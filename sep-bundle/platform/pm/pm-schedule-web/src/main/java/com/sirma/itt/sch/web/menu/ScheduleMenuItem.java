package com.sirma.itt.sch.web.menu;

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

/**
 * Main menu extension point to add project schedule menus.
 */
public class ScheduleMenuItem extends CmfMenuItem {

	/**
	 * Tasks allocation main menu
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 600, priority = 1)
	public static class ResourceAllocationMenuItem extends AbstractPageModel implements
			PageFragment {

		private static final String FRAGMENT_NAME = "resource-allocation";

		private static final String RESOURCE_ALLOCATION_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		public static final String EXTENSION_POINT = "resourceAllocation";

		@Inject
		@ExtensionPoint(value = EXTENSION_POINT)
		private Iterable<PageModel> extension;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/main/resource-allocation.xhtml";
		}

		@Override
		public String getPageFragment() {
			Iterator<PageModel> iterator = extension.iterator();
			List<String> loadHtmlPageFragments = loadHtmlPageFragments(iterator);
			return buildTemplate(
					createModel(FRAGMENT_NAME, "", "cmf.main.menu.taskallocation", null,
							loadHtmlPageFragments, Boolean.TRUE), RESOURCE_ALLOCATION_TEMPLATE_PATH);
		}
	}

	/**
	 * MyDashboard extension.
	 */
	@ApplicationScoped
	@Extension(target = ResourceAllocationMenuItem.EXTENSION_POINT, enabled = true, order = 1, priority = 1)
	public static class PMTaskAllocationAllUsers extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "resource-allocation-all-users";

		private static final String RESOURCE_ALLOCATION_ALL_USERS_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ ResourceAllocationMenuItem.FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		@Override
		public String getPath() {
			return "";
		}

		@Override
		public String getPageFragment() {
			return buildTemplate(
					createModel(FRAGMENT_NAME, "/project/project-resource-allocation.jsf",
							"pm.taskallocation.allusers", null, null, null),
					RESOURCE_ALLOCATION_ALL_USERS_TEMPLATE_PATH);
		}
	}

	/**
	 * MyDashboard extension.
	 */
	@ApplicationScoped
	@Extension(target = ResourceAllocationMenuItem.EXTENSION_POINT, enabled = true, order = 2, priority = 1)
	public static class PMTaskAllocationSelectedUsers extends AbstractPageModel implements
			PageFragment {

		private static final String FRAGMENT_NAME = "resource-allocation-selected-users";

		private static final String RESOURCE_ALLOCATION_SELECTED_USERS_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ ResourceAllocationMenuItem.FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		@Override
		public String getPath() {
			return "";
		}

		@Override
		public String getPageFragment() {
			return buildTemplate(
					createModel(FRAGMENT_NAME, "/project/project-resource-allocation.jsf",
							"pm.taskallocation.selectedusers", null, null, null),
					RESOURCE_ALLOCATION_SELECTED_USERS_TEMPLATE_PATH);
		}
	}

}
