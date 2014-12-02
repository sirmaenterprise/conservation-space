package com.sirma.itt.sch.web.menu;

import com.sirma.cmf.web.menu.NavigationMenuPlugin;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Menu item extension point to add schedule navigation menu.
 * 
 * @author svelikov
 */
public class ScheduleNavigationMenuFragment implements NavigationMenuPlugin {

	/**
	 * The Class ProjectScheduleNavigationMenu.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 25, priority = 1)
	public static class ProjectScheduleNavigationMenu implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/navigation/schedule-navigation-menu.xhtml";
		}
	}
	
	/**
	 * The Class ProjectResourceAllocationNavigationMenu.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 26, priority = 1)
	public static class ProjectResourceAllocationNavigationMenu implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/navigation/resource-allocation-navigation-menu.xhtml";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}
}
