package com.sirma.itt.menu.main;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.web.menu.main.MainMenuRight;
import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Registered right main menu item plugins.
 * 
 * @author siliev
 *
 */
public class CSMainMenuRigthExtension extends MainMenuRight {

	/**
	 * Main menu upload button.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = false, order = 700, priority = 10)
	public static class MainMenuUpload extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "main-menu-upload";

		private static final String UPLOAD_TEMPLATE_PATH = "header/mainmenu-right/upload/" + FRAGMENT_NAME;

		/**
		 * Empty get path.
		 * 
		 * @return null
		 */
		@Override
		public String getPath() {
			return null;
		}

		/**
		 * Page fragment.
		 * 
		 * @return the page fragment
		 */
		@Override
		public String getPageFragment() {
			return buildTemplate(createModel(FRAGMENT_NAME, null, "emf.main.menu.upload", null, null, null),
					UPLOAD_TEMPLATE_PATH);
		}
	}
	
	
	/**
	 * The Class create new button - teleport to UI-2.
	 */
	@ApplicationScoped
	@Extension(target = "main.menu.right", enabled = true, order = 600, priority = 5)
	public static class CreateNewButton extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "create-new";

		private static final String CREATE_NEW_TEMPLATE_PATH = "header/mainmenu-right/" + FRAGMENT_NAME + "/"
				+ FRAGMENT_NAME;

		@Override
		public String getPath() {
			return null;
		}

		@Override
		public String getPageFragment() {
			return buildTemplate(createModel(FRAGMENT_NAME, null, "ui2.create.new.btn", null, null, null),
					CREATE_NEW_TEMPLATE_PATH);
		}
		
	}

}
