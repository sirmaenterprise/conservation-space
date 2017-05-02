package com.sirma.itt.objects.web.menu;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.cmf.web.menu.main.CmfMenuItem;
import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.PageModel;
import com.sirma.itt.emf.web.plugin.Plugable;
import com.sirma.itt.emf.web.rest.EmfQueryParameters;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.security.UserPreferences;

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
	public static class ObjectsLibraryMenuExtension extends AbstractPageModel implements PageFragment, Plugable {

		/** The Constant FRAGMENT_NAME. */
		private static final String FRAGMENT_NAME = "objects-library";

		/** The Constant OBJECTS_LIBRARY_TEMPLATE_PATH. */
		private static final String OBJECTS_LIBRARY_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH + FRAGMENT_NAME + "/"
				+ FRAGMENT_NAME;

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
			// as of CMF-20403 this is not needed.
			return "";
		}
	}

	//
	// SUBMENUS
	//

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

		@Inject
		private UserPreferences userPreferences;

		@Inject
		private LibraryProvider libraryProvider;

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
			List<ClassInstance> objectLibraryMenuItems = sortLabels(libraryProvider.getAllowedLibraries(
					LibraryProvider.OBJECT_LIBRARY, ActionTypeConstants.VIEW_DETAILS), userPreferences.getLanguage());
			// add menu separator
			Map<String, Object> separator = new HashMap<>();
			processedTemplated.append(buildTemplate(separator, MENU_SEPARATOR_TEMPLATE));

			// build object libraries list
			for (ClassInstance objectLibraryMenuItem : objectLibraryMenuItems) {
				Map<String, Object> model = new HashMap<>();
				String objectType = objectLibraryMenuItem.getId().toString().replace(":", "_");
				model.put("name", objectType);
				model.put("id", "library_" + objectType);
				model.put("type", objectLibraryMenuItem.getId());
				model.put("label", objectLibraryMenuItem.getLabel(userPreferences.getLanguage()));
				// #{request.contextPath}/search/basic-search.jsf?#{library.joinParameters()}
				// /emf/search/basic-search.jsf?library=object&objectType=chd%3ABook
				model.put("href", getHRef() + joinParameters(objectLibraryMenuItem));

				processedTemplated.append(buildTemplate(model, OBJECTS_LIBRARY_LIST_TEMPLATE_PATH));
			}

			return processedTemplated.toString();
		}

		private String joinParameters(ClassInstance libraryElement) {
			StringBuilder joined = new StringBuilder();
			joined.append(EmfQueryParameters.LIBRARY).append("=object-library&");
			joined.append(EmfQueryParameters.LIBRARY_TITLE).append("=").append(
					libraryElement.getLabel(userPreferences.getLanguage())).append("&");
			joined.append("objectType[]").append("=").append(libraryElement.getId());

			return joined.toString();
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

	static List<ClassInstance> sortLabels(List<ClassInstance> items, String language) {
		Collator collator = Collator.getInstance(new Locale(language));
		return items
				.stream()
					.sorted((item1, item2) -> collator.compare(item1.getLabel(language), item2.getLabel(language)))
					.collect(Collectors.toCollection(() -> new ArrayList<>(items.size())));
	}
}
