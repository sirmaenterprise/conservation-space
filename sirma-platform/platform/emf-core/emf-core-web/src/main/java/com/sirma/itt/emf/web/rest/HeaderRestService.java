package com.sirma.itt.emf.web.rest;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.web.menu.main.MainMenu;
import com.sirma.itt.emf.web.menu.main.MainMenuRight;
import com.sirma.itt.emf.web.plugin.PageModel;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.time.TimeTracker;

import freemarker.template.TemplateException;

/**
 * Handles requests for loading of menu items in different model types.
 *
 * @author hlungov
 */
@Path("/header")
@ApplicationScoped
public class HeaderRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(HeaderRestService.class);
	private static final String TEMPLATE_HEADER = "header/header";
	private static final String TEMPLATE_MAIN_MENU = "header/mainmenu/mainmenu";
	private static final String TEMPLATE_MAIN_MENU_RIGHT = "header/mainmenu-right/mainmenu-right";

	@Inject
	@ExtensionPoint(value = MainMenu.EXTENSION_POINT)
	private Iterable<Plugin> mainMenuExtensionPoints;

	@Inject
	@ExtensionPoint(value = MainMenuRight.EXTENSION_POINT)
	private Iterable<Plugin> mainMenuRightExtensionPoints;

	@Inject
	private FreemarkerProvider freemarkerProvider;

	@Inject
	private UserPreferences userPreferences;

	@Inject
	private LibraryProvider libraryProvider;

	@Inject
	private CodelistService codelistService;

	@Inject
	private DictionaryService dictionaryService;

	/**
	 * Builds a model for libraries header menus.
	 *
	 * @param library
	 *            The library type
	 * @return json model
	 * @throws JSONException
	 */
	@GET
	@Path("/library")
	@Produces(Versions.V2_JSON)
	public String getLibrary(@QueryParam("library") String library) throws JSONException {
		JSONArray items = new JSONArray();
		buildLibraryModel(library, items);
		return items.toString();
	}

	private void buildLibraryModel(String library, JSONArray items) throws JSONException {
		String libraryType = library + "-library";
		String language = userPreferences.getLanguage();
		List<ClassInstance> classInstances = sortLabels(
				libraryProvider.getAllowedLibraries(libraryType, ActionTypeConstants.VIEW_DETAILS), language);
		for (ClassInstance classInstance : classInstances) {
			String label;
			Serializable objectType;
			if (LibraryProvider.OBJECT_LIBRARY.equals(libraryType)) {
				label = classInstance.getLabel(language);
				objectType = classInstance.getId();
			} else {
				objectType = classInstance.getLabel();
				label = classInstance.getLabel();
				label = codelistService.getDescription(getCodelistNumber(label), label, language);
				if (label == null) {
					label = classInstance.getLabel();
				}
			}
			String href = "/search/basic-search.jsf?" + joinParameters(libraryType, label, objectType);
			Serializable defaultHeader = classInstance.getProperties().get(DefaultProperties.HEADER_DEFAULT);
			JSONObject item = getItem(objectType, label, href, defaultHeader);
			items.put(item);
		}
	}

	private Integer getCodelistNumber(String definitionId) {
		DefinitionModel definition = dictionaryService.find(definitionId);
		return definition.getField(DefaultProperties.TYPE).map(PropertyDefinition::getCodelist).orElse(null);
	}

	private static JSONObject getItem(Serializable serializable, String label, String href, Serializable header) throws JSONException {
		JSONObject item = new JSONObject();
		item.put("name", serializable);
		item.put("label", label);
		item.put("href", href);
		item.put(DefaultProperties.HEADER_DEFAULT, header);
		return item;
	}

	private static String joinParameters(String libraryType, String label, Serializable objectType) {
		StringBuilder joined = new StringBuilder();
		joined.append(EmfQueryParameters.LIBRARY).append("=").append(libraryType).append("&");
		joined.append(EmfQueryParameters.LIBRARY_TITLE).append("=").append(label).append("&");
		joined.append("objectType[]").append("=").append(objectType);
		return joined.toString();
	}

	static List<ClassInstance> sortLabels(List<ClassInstance> items, String language) {
		Collator collator = Collator.getInstance(new Locale(language));
		return items
				.stream()
					.sorted((item1, item2) -> collator.compare(item1.getLabel(language), item2.getLabel(language)))
					.collect(Collectors.toCollection(() -> new ArrayList<>(items.size())));
	}

	/**
	 * Retrieve values.
	 *
	 * @return the response
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response buildMenu() {

		Map<String, Object> model = CollectionUtils.createLinkedHashMap(4);
		try {
			TimeTracker tracker = null;
			if (LOGGER.isTraceEnabled()) {
				tracker = TimeTracker.createAndStart();
			}
			// prepare main menu
			Iterator<Plugin> iterator = mainMenuExtensionPoints.iterator();
			model.put("menus", loadHtmlPageFragments(iterator));
			String mainMenuHtml = freemarkerProvider.processTemplateByFullPath(model, TEMPLATE_MAIN_MENU);

			// prepare main menu right
			model.clear();
			iterator = mainMenuRightExtensionPoints.iterator();
			model.put("menus", loadHtmlPageFragments(iterator));
			String mainMenuRighthtml = freemarkerProvider.processTemplateByFullPath(model, TEMPLATE_MAIN_MENU_RIGHT);

			model.clear();
			model.put("mainMenu", mainMenuHtml);
			model.put("mainMenuRight", mainMenuRighthtml);
			String responseBody = freemarkerProvider.processTemplateByFullPath(model, TEMPLATE_HEADER);

			if (tracker != null) {
				LOGGER.trace("Header building took {} ms", tracker.stop());
			}
			return prepareResponse(Status.OK, responseBody);
		} catch (TemplateException e) {
			LOGGER.debug(e.getMessage(), e);
			return prepareResponse(Status.BAD_REQUEST, e.getMessage());
		}
	}

	/**
	 * Prepare response.
	 *
	 * @param status
	 *            the status of response.
	 * @param responseBody
	 *            the response body
	 * @return the response
	 */
	private static Response prepareResponse(Status status, String responseBody) {
		return Response.status(status).entity(responseBody).build();
	}

	/**
	 * Iterates over <code>iterator's</code> element and collect their htmls.
	 *
	 * @param iterator
	 *            the iterator
	 * @return the map
	 */
	private static List<String> loadHtmlPageFragments(Iterator<? extends Plugin> iterator) {
		List<String> list = new ArrayList<>();
		while (iterator.hasNext()) {
			Plugin nextElement = iterator.next();
			if (nextElement instanceof PageModel) {
				list.add(((PageModel) nextElement).getPageFragment());
			}
		}
		return list;
	}
}
