package com.sirma.itt.emf.web.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.web.menu.main.MainMenu;
import com.sirma.itt.emf.web.menu.main.MainMenuRight;
import com.sirma.itt.emf.web.plugin.PageModel;

import freemarker.template.TemplateException;

/**
 * The Class MainMenuRestService.
 *
 * @author hlungov
 */
@Path("/header")
@ApplicationScoped
@Produces(MediaType.TEXT_HTML)
public class HeaderRestService {

	/** The Constant TEMPLATE_HEADER. */
	private static final String TEMPLATE_HEADER = "header/header";

	/** The Constant TEMPLATE_MAIN_MENU. */
	private static final String TEMPLATE_MAIN_MENU = "header/mainmenu/mainmenu";

	/** The Constant TEMPLATE_MAIN_MENU_RIGHT. */
	private static final String TEMPLATE_MAIN_MENU_RIGHT = "header/mainmenu-right/mainmenu-right";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(HeaderRestService.class);

	/** The main menu extension points. */
	@Inject
	@ExtensionPoint(value = MainMenu.EXTENSION_POINT)
	private Iterable<Plugin> mainMenuExtensionPoints;

	/** The main menu right extension points. */
	@Inject
	@ExtensionPoint(value = MainMenuRight.EXTENSION_POINT)
	private Iterable<Plugin> mainMenuRightExtensionPoints;

	/** The freemarker provider. */
	@Inject
	private FreemarkerProvider freemarkerProvider;

	/**
	 * Retrieve values.
	 * 
	 * @return the response
	 */
	@GET
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
			iterator =  mainMenuRightExtensionPoints.iterator();
			model.put("menus", loadHtmlPageFragments(iterator));
			String mainMenuRighthtml = freemarkerProvider.processTemplateByFullPath(model, TEMPLATE_MAIN_MENU_RIGHT);

			model.clear();
			model.put("mainMenu", mainMenuHtml);
			model.put("mainMenuRight", mainMenuRighthtml);
			String responseBody = freemarkerProvider.processTemplateByFullPath(model, TEMPLATE_HEADER);

			if (tracker != null) {
				LOGGER.trace("Header building took {} s", tracker.stopInSeconds());
			}
			return prepareResponse(Status.OK, responseBody);
		} catch (IOException | TemplateException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(e.getMessage(), e);
			}
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
	private Response prepareResponse(Status status, String responseBody) {
		return Response.status(status).entity(responseBody).build();
	}

	/**
	 * Iterates over <code>iterator's</code> element and collect their htmls.
	 *
	 * @param iterator
	 *            the iterator
	 * @return the map
	 */
	public static List<String> loadHtmlPageFragments(Iterator<? extends Plugin> iterator) {
		List<String> list = new ArrayList<String>();
		while (iterator.hasNext()) {
			Plugin nextElement = iterator.next();
			if (nextElement instanceof PageModel) {
				list.add(((PageModel) nextElement).getPageFragment());
			}
		}
		return list;
	}
}
