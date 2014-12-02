/**
 * Copyright (c) 2014 05.02.2014 , Sirma ITT. /* /**
 */
package com.sirma.itt.idoc.web.util.sanitize;

import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.sirma.itt.emf.util.sanitize.ContentSanitizer;

/**
 * Sanitizes HTML content
 * 
 * @author Adrian Mitev
 */
@ApplicationScoped
public class IdocSanitizer implements ContentSanitizer {
	
	private static final String NON_EXISTENT_DOMAIN = "http://non.existent.domain.com/";

	private static final Pattern ID_ATTRIBUTE_PATTERN = Pattern.compile(" id=\".+?\"");

	private static final Pattern EMPTY_ATTRIBUTE_PATTERN = Pattern.compile("[a-z]+=\"\\s*\"");

	private static final String IDOC_VISUAL_CLASSES = "idoc-visual-";

	private static final Pattern IDOC_VISUAL_PATTERN = Pattern.compile(IDOC_VISUAL_CLASSES
			+ "[a-zA-Z0-9\\-_]+");

	private static final String NG_CLASSES = "ng-";

	private static final Pattern NG_CLASSES_PATTERN = Pattern.compile(NG_CLASSES
			+ "[a-zA-Z0-9\\-_]+");

	private static final Pattern TRIM_TAGS_PATTERN = Pattern
			.compile("<([a-zA-Z-0-9]+?)(\\s[a-zA-Z-0-9\\-]+?\\=\"[^\"]+\")*\\s+>");

	private final Whitelist documentWhiteList;

	/**
	 * Initializes whitelist.
	 */
	public IdocSanitizer() {

		documentWhiteList = Whitelist.relaxed();
		// add widget rules
		documentWhiteList.addAttributes(":all", "id", "class");
		documentWhiteList.addAttributes(":all", "data-widget", "data-name", "data-config",
				"data-value", "widget", "name", "config", "value");

		// This is needed because of tinymce formatting otherwise it's broken after save/edit
		documentWhiteList.addAttributes("br", "data-mce-bogus");
		documentWhiteList.addAttributes("div", "style");
		documentWhiteList.addAttributes("span", "style");
		documentWhiteList.addAttributes("p", "style");

		// This is needed for internal links/thumbnails to be shown
		// http://stackoverflow.com/questions/22444156/how-to-make-a-jsoup-whitelist-to-accept-certain-attribute-content
		documentWhiteList.addProtocols("img", "src", "data");
		documentWhiteList.addAttributes("a", "class", "rel", "data-instance-type",
				"data-instance-id");

		documentWhiteList.preserveRelativeLinks(true);
	}
	
	@Override
	public String sanitize(String content) {
		return sanitize(content, IdocSanitizer.NON_EXISTENT_DOMAIN);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public String sanitize(String content, String origin) {

		Document document = sanitizeInternal(content, origin);
		String result = document.toString();
		result = removeEmptyAttributes(result);
		result = trimTagWhitespaces(result);
		return result;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public String sanitizeTemplate(String content, String origin) {
		Document document = sanitizeInternal(content, origin);

		// remove widget values
		removeAttribute(document, ".widget", "data-value");

		String result = document.toString();

		// remove id attributes
		result = ID_ATTRIBUTE_PATTERN.matcher(result).replaceAll("");

		// remove empty tags
		result = removeEmptyAttributes(result);

		result = trimTagWhitespaces(result);

		return result;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public String sanitizeBeforeClone(String content, String origin) {

		String result = null;
		// remove id attributes
		result = ID_ATTRIBUTE_PATTERN.matcher(content).replaceAll("");
		result = sanitize(result, origin);

		return result;
	}

	/**
	 * Performs internal sanitizing by executing all steps on a Document.
	 * 
	 * @param content
	 *            content to sanitize.
	 * @param origin
	 *            the origin
	 * @return sanitized document.
	 */
	private Document sanitizeInternal(String content, String origin) {
		Document document = Jsoup.parse(content);

		// Because tinymce is an a**hole, that's why
		document.select(".widget").removeClass("edit-mode").removeClass("preview-mode")
				.html("<br data-mce-bogus=\"1\">");

		removeStyleClassByPrefix(document, IDOC_VISUAL_CLASSES, IDOC_VISUAL_PATTERN);

		removeStyleClassByPrefix(document, NG_CLASSES, NG_CLASSES_PATTERN);

		// clean is at last position because it breaks the widget content
		document = clean(document, origin);

		removeWrappingHtmlAndBody(document);

		return document;
	}

	/**
	 * Cleans up the document using the document list.
	 * 
	 * @param document
	 *            document object
	 * @param origin
	 *            the origin
	 * @return cleaned up document object.
	 */
	private Document clean(Document document, String origin) {
		String local = origin;
		if (local == null) {
			local = "";
		}
		return Jsoup.parse(Jsoup.clean(document.toString(), local, documentWhiteList));
	}

	/**
	 * Removes a class from a widget.
	 * 
	 * @param document
	 *            document object
	 * @param className
	 *            class to remove
	 * @param regexPattern
	 *            regex pattern finding the exact class
	 */
	private void removeStyleClassByPrefix(Document document, String className, Pattern regexPattern) {
		Elements elements = document.select("*[class*=" + className + "]");
		for (Element element : elements) {
			String _class = element.attr("class");
			_class = regexPattern.matcher(_class).replaceAll("").trim();
			element.attr("class", _class);
		}
	}

	/**
	 * Removes an attribute
	 * 
	 * @param document
	 *            document object
	 * @param selector
	 *            CSS query selector.
	 * @param attribute
	 *            attribute to remove.
	 */
	private void removeAttribute(Document document, String selector, String attribute) {
		document.select(selector).removeAttr(attribute);
	}

	/**
	 * Unwraps html, head and body tags.
	 * 
	 * @param document
	 *            document object
	 */
	private void removeWrappingHtmlAndBody(Document document) {
		document.getElementsByTag("html").unwrap();
		document.getElementsByTag("head").remove();
		document.getElementsByTag("body").unwrap();
	}

	/**
	 * Removes empty attributes from html.
	 * 
	 * @param html
	 *            html which empty attributes to remove.
	 * @return html with empty attributes removed.
	 */
	private String removeEmptyAttributes(String html) {
		return EMPTY_ATTRIBUTE_PATTERN.matcher(html).replaceAll("");
	}

	/**
	 * Trims ending whatspaces from tags.
	 * 
	 * @param content
	 *            content to trim.
	 * @return trimmed content.
	 */
	private String trimTagWhitespaces(String content) {
		return TRIM_TAGS_PATTERN.matcher(content).replaceAll("<$1$2>");
	}
}