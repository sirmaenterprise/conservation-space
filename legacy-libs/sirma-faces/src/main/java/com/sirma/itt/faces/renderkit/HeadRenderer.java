package com.sirma.itt.faces.renderkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.richfaces.resource.ResourceKey;
import org.richfaces.resource.ResourceLibrary;
import org.richfaces.resource.ResourceLibraryFactoryImpl;

import com.google.common.hash.Hashing;
import com.sirma.itt.faces.resources.ResourceId;
import com.sirma.itt.faces.resources.ResourceServlet;

/**
 * Renders head content based on the following order: <br/>
 * Begin facet<br/>
 * Richfaces resources (.ecss and .reslib)<br/>
 * CSS resources (resources that end with .css)<br/>
 * Middle facet<br/>
 * JavaScript resources (resources that end with .js)<br/>
 * h:head content encoded by {@link Renderer#encodeChildren(FacesContext, UIComponent)}<br/>
 * End facet<br/>
 * Usage - add the following in faces-config.xml:
 * <renderer><component-family>javax.faces.Output</component-family>
 * <renderer-type>javax.faces.Head</renderer-type>
 * <renderer-class>com.sirma.itt.faces.renderkit.HeadRenderer</renderer-class> </renderer>
 */
public class HeadRenderer extends Renderer {

	/**
	 * Time of the class loading time, used as "salt" to the resource hash to ensure that a new
	 * resource hash is generated on each server restart.
	 */
	private static final long START_TIME = System.currentTimeMillis();

	@Override
	@SuppressWarnings("unchecked")
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		// init resource lists
		List<UIComponent> richfacesStyles = new ArrayList<UIComponent>();
		List<UIComponent> richfacesReslibs = new ArrayList<UIComponent>();

		List<UIComponent> styles = new ArrayList<UIComponent>();
		List<UIComponent> scripts = new ArrayList<UIComponent>();
		List<UIComponent> others = new ArrayList<UIComponent>();

		List<UIComponent> endStyles = new ArrayList<UIComponent>();
		List<UIComponent> endScripts = new ArrayList<UIComponent>();

		// Split resources managed by JSF and encode them
		for (UIComponent current : context.getViewRoot().getComponentResources(context, "head")) {
			String name = getName(current);

			if (name != null) {
				boolean positionInTheEnd = "end".equals(current.getAttributes().get("position"));

				// ecss = richfaces css
				if (name.endsWith(".css")) {
					if (positionInTheEnd) {
						endStyles.add(current);
					} else {
						styles.add(current);
					}
				} else if (name.endsWith(".js")) {
					if (positionInTheEnd) {
						endScripts.add(current);
					} else {
						scripts.add(current);
					}
				} else if (name.endsWith(ResourceServlet.RICHFACES_CSS)) {
					richfacesStyles.add(current);
				} else if (name.endsWith(ResourceServlet.RICHFACES_RESLIB)) {
					richfacesReslibs.add(current);
				} else {
					others.add(current);
				}
			}
		}

		// resource combining is enabled when the project stage is production
		// and the combine resources init param is true
		boolean combineResourcesEnabled = (Boolean.valueOf(context.getExternalContext()
				.getInitParameter(ResourceServlet.COMBINE_RESOURCES_INIT_PARAM)));

		// if the project stage is production, generate one single css and one
		// single javascript file
		String styleHash = null;
		String scriptHash = null;
		if (combineResourcesEnabled) {
			List<ResourceId> stylesList = getResourceIndetifiers(context, styles, endStyles);

			// if there are any styles
			if (!stylesList.isEmpty()) {
				styleHash = Hashing.md5().hashString(stylesList.toString() + START_TIME).toString();
				// if the has is not already presented, put it there
				if (!context.getExternalContext().getApplicationMap().containsKey(styleHash)) {
					context.getExternalContext().getApplicationMap().put(styleHash, stylesList);
					setJsfPath(context);
				}
			}

			List<ResourceId> scriptsList = getResourceIndetifiers(context, scripts, endScripts);
			List<ResourceId> richfacesReslibResources = getRichFacesResourceLibraryResources(richfacesReslibs);
			// richfaces resources should be first
			scriptsList.addAll(0, richfacesReslibResources);

			// if there are any styles
			if (!scriptsList.isEmpty()) {
				scriptHash = Hashing.md5().hashString(scriptsList.toString() + START_TIME)
						.toString();
				// if the has is not already presented, put it there
				if (!context.getExternalContext().getApplicationMap().containsKey(scriptHash)) {
					context.getExternalContext().getApplicationMap().put(scriptHash, scriptsList);
				}
			}

		}

		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("head", component);

		// "begin" facet
		UIComponent beginFacet = component.getFacet("begin");
		if (beginFacet != null) {
			beginFacet.encodeAll(context);
		}

		// Non JavaScript or css sources
		for (UIComponent other : others) {
			other.encodeAll(context);
		}

		// Styles
		if (!combineResourcesEnabled) {
			// RichFaces styles go first
			for (UIComponent style : richfacesStyles) {
				style.encodeAll(context);
			}

			for (UIComponent style : styles) {
				style.encodeAll(context);
			}

			// encode end styles
			for (UIComponent style : endStyles) {
				style.encodeAll(context);
			}
		} else {
			for (UIComponent style : richfacesStyles) {
				style.encodeAll(context);
			}

			// if there are styles create a link element with the styles hash
			if (styleHash != null) {
				writer.startElement("link", null);
				writer.writeAttribute("rel", "stylesheet", null);
				writer.writeAttribute("type", "text/css", null);
				writer.writeAttribute("href", context.getExternalContext().getRequestContextPath()
						+ ResourceServlet.SF_SERVLET_PATH + "?resource=" + styleHash + "&type=css",
						null);
				writer.endElement("link");
			}
		}

		// "middle" facet
		UIComponent middle = component.getFacet("middle");
		if (middle != null) {
			middle.encodeAll(context);
		}

		// Scripts
		if (!combineResourcesEnabled) {
			// Richfaces resource libraries (javascripts only for now)
			for (UIComponent script : richfacesReslibs) {
				script.encodeAll(context);
			}

			for (UIComponent script : scripts) {
				script.encodeAll(context);
			}

			// encode end scripts
			for (UIComponent script : endScripts) {
				script.encodeAll(context);
			}
		} else {
			// if there are scripts create a script element with the script hash
			if (scriptHash != null) {
				writer.startElement("script", null);
				writer.writeAttribute("type", "text/javascript", null);
				writer.writeAttribute("src", context.getExternalContext().getRequestContextPath()
						+ ResourceServlet.SF_SERVLET_PATH + "?resource=" + scriptHash + "&type=js",
						null);
				writer.endElement("script");
			}
		}
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		ResponseWriter writer = context.getResponseWriter();

		// "end" facet
		UIComponent endFacet = component.getFacet("end");
		if (endFacet != null) {
			endFacet.encodeAll(context);
		}

		writer.endElement("head");
	}

	/**
	 * Constructs a list with {@link ResourceId} from lists of UIComponents.
	 * 
	 * @param resources
	 *            resources lists.
	 * @return constructed list with resource identifiers.
	 */
	private List<ResourceId> getResourceIndetifiers(FacesContext context,
			List<UIComponent>... resources) {
		List<ResourceId> stylesList = new ArrayList<ResourceId>();

		for (List<UIComponent> styles : resources) {
			for (UIComponent resource : styles) {
				String minified = (String) resource.getAttributes().get("minified");
				String name = getName(resource);

				if (minified != null
						&& context.getApplication().getProjectStage()
								.equals(ProjectStage.Production)) {
					if (Boolean.valueOf(minified)) {
						name = new StringBuilder(name).insert(name.lastIndexOf("."), ".min")
								.toString();
					} else {
						// if minified is not true, but still not null use it's value as resource
						// name
						name = minified;
					}
				}

				stylesList.add(new ResourceId(getLibrary(resource), name));
			}
		}

		return stylesList;
	}

	/**
	 * Gets the resource path for jsf resources by using a dummy file.
	 * 
	 * @param context
	 *            current faces context.
	 */
	private void setJsfPath(FacesContext context) {
		if (!context.getExternalContext().getApplicationMap()
				.containsKey(ResourceServlet.JSF_RESOURCE_PATH)) {
			String dummyResourcePath = context.getApplication().evaluateExpressionGet(context,
					"#{resource['" + ResourceServlet.DUMMY_SF_FILE + "']}", String.class);
			String resourcePath = dummyResourcePath.replace(ResourceServlet.DUMMY_SF_FILE, "%s");
			context.getExternalContext().getApplicationMap()
					.put(ResourceServlet.JSF_RESOURCE_PATH, resourcePath);
		}
	}

	/**
	 * Iterates all RichFaces resource libraries and extract their resources (javascripts only for
	 * now) storing them in an order list and keeping resource uniqueness.
	 * 
	 * @param reslibs
	 *            list with richfaces resource libs.
	 * @return list containing the extracted resources.
	 */
	private List<ResourceId> getRichFacesResourceLibraryResources(List<UIComponent> reslibs) {
		List<ResourceId> result = new ArrayList<ResourceId>();
		if (!reslibs.isEmpty()) {
			ResourceLibraryFactoryImpl resourceFactory = new ResourceLibraryFactoryImpl();

			for (UIComponent reslib : reslibs) {
				// remove .reslib extension
				String name = getName(reslib).replace(ResourceServlet.RICHFACES_RESLIB, "");
				String library = getLibrary(reslib);

				ResourceLibrary resourceLibrary = resourceFactory.getResourceLibrary(name, library);
				for (ResourceKey resourceKey : resourceLibrary.getResources()) {
					// don't process nested .reslib resources
					if (!resourceKey.getResourceName().endsWith(ResourceServlet.RICHFACES_RESLIB)) {
						ResourceId resourceId = new ResourceId(resourceKey.getLibraryName(),
								resourceKey.getResourceName());
						// only add the resource if not already presented to
						// achieve uniqueness
						if (!result.contains(resourceId)) {
							result.add(resourceId);
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets name attribute of a resource component.
	 * 
	 * @param component
	 *            resource component
	 * @return resource name
	 */
	private String getName(UIComponent component) {
		return (String) component.getAttributes().get("name");
	}

	/**
	 * Gets library attribute of a resource component.
	 * 
	 * @param component
	 *            resource component
	 * @return resource library
	 */
	private String getLibrary(UIComponent component) {
		return (String) component.getAttributes().get("library");
	}

}
