package com.sirma.itt.objects.web.menu;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * Menu item extension point to add objects library main menu.
 * 
 * @author svelikov
 */
public class ObjectsMenuItem extends CmfMenuItem {

	/**
	 * ObjectsLibraryMenuExtension.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 700, priority = 1)
	public static class ObjectsLibraryMenuExtension extends AbstractPageModel implements
			PageFragment, Plugable {

		/** The Constant FRAGMENT_NAME. */
		private static final String FRAGMENT_NAME = "objects-library";

		/** The Constant OBJECTS_LIBRARY_TEMPLATE_PATH. */
		private static final String OBJECTS_LIBRARY_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		/** The Constant EXTENSION_POINT. */
		public static final String EXTENSION_POINT = "objectsLibrary";

		/** The extension. */
		@Inject
		@ExtensionPoint(value = EXTENSION_POINT)
		private Iterable<PageModel> extension;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/main/objects-library.xhtml";
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getExtensionPoint() {
			return EXTENSION_POINT;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPageFragment() {
			Iterator<PageModel> iterator = extension.iterator();
			List<String> loadHtmlPageFragments = loadHtmlPageFragments(iterator);
			return buildTemplate(
					createModel(FRAGMENT_NAME, null, "menu.main.objectsLibrary", null,
							loadHtmlPageFragments, Boolean.TRUE), OBJECTS_LIBRARY_TEMPLATE_PATH);
		}
	}

	//
	// SUBMENUS
	//

	/**
	 * CreateObjectMenuItem.
	 */
	@ApplicationScoped
	@Extension(target = ObjectsLibraryMenuExtension.EXTENSION_POINT, enabled = true, order = 1, priority = 1)
	public static class CreateObjectMenuItem extends AbstractPageModel implements PageFragment {

		/** The Constant FRAGMENT_NAME. */
		private static final String FRAGMENT_NAME = "create-object";

		/** The Constant NEW_OBJECT_TEMPLATE_PATH. */
		private static final String NEW_OBJECT_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ ObjectsLibraryMenuExtension.FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "";
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPageFragment() {
			return buildTemplate(
					createModel(FRAGMENT_NAME, "/object/object.jsf",
							getLabelProvider().getValue("operation.object.create"), null, null,
							null), NEW_OBJECT_TEMPLATE_PATH);
		}
	}

	/**
	 * ObjectsLibraryList provides a list with configured object libraries for current user.
	 */
	@ApplicationScoped
	@Extension(target = ObjectsLibraryMenuExtension.EXTENSION_POINT, enabled = true, order = 2, priority = 1)
	public static class ObjectsLibraryList extends AbstractPageModel implements PageFragment {

		/** The Constant FRAGMENT_NAME. */
		private static final String FRAGMENT_NAME = "list-libraries";

		/** The Constant OBJECTS_LIBRARY_LIST_TEMPLATE_PATH. */
		private static final String OBJECTS_LIBRARY_LIST_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ ObjectsLibraryMenuExtension.FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		/** The semantic object libraries menu provider. */
		@Inject
		private ObjectLibrariesMenuProvider semanticObjectLibrariesMenuProvider;

		/** The href. */
		private String href;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "";
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPageFragment() {
			StringBuilder processedTemplated = new StringBuilder();
			List<ObjectLibraryMenuItem> objectLibraryMenuItems = semanticObjectLibrariesMenuProvider
					.getObjectLibraryMenuItems();

			// add menu separator
			Map<String, Object> separator = new HashMap<String, Object>();
			processedTemplated.append(buildTemplate(separator, MENU_SEPARATOR_TEMPLATE));

			// build object libraries list
			for (ObjectLibraryMenuItem objectLibraryMenuItem : objectLibraryMenuItems) {
				Map<String, Object> model = new HashMap<String, Object>();
				String objectType = objectLibraryMenuItem.getObjectType().replace(":", "_");
				model.put("name", objectType);
				model.put("id", "library_" + objectType);
				model.put("type", objectLibraryMenuItem.getObjectType());
				model.put("label", objectLibraryMenuItem.getLabel());
				// #{request.contextPath}/search/basic-search.jsf?#{library.joinParameters()}
				// /emf/search/basic-search.jsf?library=object&objectType=chd%3ABook
				model.put("href", getHRef() + objectLibraryMenuItem.joinParameters());

				processedTemplated.append(buildTemplate(model, OBJECTS_LIBRARY_LIST_TEMPLATE_PATH));
			}

			return processedTemplated.toString();
		}

		/**
		 * Gets the href.
		 * 
		 * @return the href
		 */
		private String getHRef() {
			if (href == null) {
				href = getServerBaseAndContext() + "/search/basic-search.jsf?";
			}
			return href;
		}
	}
}
