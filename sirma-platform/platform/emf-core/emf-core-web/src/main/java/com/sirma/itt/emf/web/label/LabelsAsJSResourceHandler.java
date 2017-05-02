package com.sirma.itt.emf.web.label;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.common.hash.Hashing;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.web.resources.WebResource;
import com.sirma.itt.emf.web.resources.WebResourceHandler;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Serves all labels provided by {@link LabelProvider} as a javascript file to used at the client side.
 *
 * @author Adrian Mitev
 */
@Extension(target = WebResourceHandler.TARGET_NAME, order = 0.2)
public class LabelsAsJSResourceHandler implements WebResourceHandler {

	private static final String PREFIX = "labels_";

	@Inject
	private LabelProvider labelProvider;

	@Override
	public boolean canHandle(String path, HttpServletRequest request, ServletContext servletContext) {
		return path.startsWith(PREFIX);
	}

	@Override
	public WebResource handle(String path, HttpServletRequest request, ServletContext servletContext) {
		String language = getLanguageFromPath(path);

		Map<String, String> labels = new TreeMap<>();

		Iterable<ResourceBundle> bundles = labelProvider.getBundles(language);

		for (ResourceBundle bundle : bundles) {
			Enumeration<String> keys = bundle.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				labels.put(key, bundle.getString(key));
			}
		}

		StringBuilder builder = new StringBuilder(2048).append("var _emfLabels = [];").append(StringUtils.NEW_LINE);
		for (Entry<String, String> label : labels.entrySet()) {
			builder
					.append(StringUtils.NEW_LINE)
						.append("_emfLabels['")
						.append(label.getKey())
						.append("'] = ")
						.append("'")
						.append(StringEscapeUtils.escapeJavaScript(label.getValue()))
						.append("';");
		}

		WebResource resource = new WebResource();

		resource.setName(path);
		resource.setContentType("text/javascript");

		String content = builder.toString();

		resource.setCachable(false);
		resource.setHash(Hashing.sha1().hashString(content + language).toString());
		resource.setContent(content.getBytes(StandardCharsets.UTF_8));

		return resource;
	}

	/**
	 * Fetches the language from the requested path. It should be in the following format - "labels_<language>.js".
	 *
	 * @param path
	 *            path to process.
	 * @return extracted language.
	 */
	String getLanguageFromPath(String path) {
		return path.substring(PREFIX.length(), path.lastIndexOf("."));
	}
}
