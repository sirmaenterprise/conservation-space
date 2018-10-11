package com.sirma.sep.content.idoc.sanitizer;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

/**
 * Sanitizes HTML content. This includes operations like: <br/>
 * - removing unknown tags and attributes.<br/>
 * - removing script tags.<br/>
 * - removing ui framework specific tags, attributes and style-classes.<br/>
 * - remove style classes.<br/>
 * - generates missing ui tabs identifiers.<br/>
 *
 * @author Adrian Mitev
 */
// TODO: we should re-design this class a lot of parsing, serializing and then parsing again is performed, rename
@Singleton
public class IdocSanitizer {

	private static final String SEPARATOR = ";";
	private static final String STYLE = "style";
	private static final String CLASS = "class";
	private static final String DATA_ID_ATTR = "data-id";
	private static final String SECTION_TAB_NAME = "section";
	private static final String WIDGET_SELECTOR = ".widget";

	private static final String NON_EXISTENT_DOMAIN = "http://non.existent.domain.com/";

	private static final Pattern EMPTY_ATTRIBUTE_PATTERN = Pattern.compile("[a-z]+=\"\\s*\"");

	private static final String IDOC_VISUAL_CLASSES = "idoc-visual-";

	private static final String TEMP_TAGS = "div[data-cke-temp]";

	private static final Pattern IDOC_VISUAL_PATTERN = Pattern.compile(IDOC_VISUAL_CLASSES + "[a-zA-Z0-9\\-_]+");

	private static final String NG_CLASSES = "ng-";

	private static final Pattern NG_CLASSES_PATTERN = Pattern.compile("(^|\\s)" + NG_CLASSES + "[a-zA-Z0-9\\-_]+");

	private static final Pattern TRIM_TAGS_PATTERN = Pattern
			.compile("<([a-zA-Z-0-9]+?)(\\s[a-zA-Z-0-9\\-]+?\\=\"[^\"]+\")*\\s+>");

	/**
	 * Sections css selector used when uploading a document. It's used for retrieving all tab sections from the uploaded
	 * document's template that do not have a set id.
	 */
	private static final String SECTIONS_SELECTOR = "section:not([data-id])[data-show-comments]";

	/**
	 * Remove all id attributes without those where have widget class.
	 */
	private static final String ID_SELECTOR = "*[id]:not(.widget)";

	private static final String WIDGET_WRAPPER_CLASS = "cke_widget_wrapper";

	private static final String WIDGET_DRAG_HANDLER_CLASS = "cke_widget_drag_handler_container";

	private final Whitelist documentWhiteList;

	/**
	 * Initializes whitelist.
	 */
	public IdocSanitizer() {
		documentWhiteList = Whitelist.relaxed();

		documentWhiteList.addTags(SECTION_TAB_NAME);
		documentWhiteList.addTags("s");
		documentWhiteList.addTags("hr");

		// add widget rules
		documentWhiteList.addAttributes(SECTION_TAB_NAME, DATA_ID_ATTR, "data-title", "data-default",
				"data-show-navigation", "data-user-defined", "data-show-comments", "data-revision", "data-locked");
		documentWhiteList.addAttributes(":all", "data-tabs-counter", "data-draft-data");
		documentWhiteList.addAttributes(":all", "id", CLASS);
		documentWhiteList.addAttributes(":all", "data-widget", "data-name", "data-config", "data-value",
				"data-widget-data", "widget-data", "widget", "name", "config", "value", "context", "data-embedded-id");
		documentWhiteList.addAttributes(":all", "dir", "lang");

		// This is needed because of tinymce formatting otherwise it's broken after save/edit
		documentWhiteList.addAttributes("br", "data-mce-bogus");
		documentWhiteList.addAttributes("div", STYLE);
		documentWhiteList.addAttributes("span", STYLE);
		documentWhiteList.addAttributes("p", STYLE);
		documentWhiteList.addAttributes("img", STYLE);
		documentWhiteList.addAttributes("table", STYLE, "cellspacing", "cellpadding", "border", "align");
		documentWhiteList.addAttributes("td", STYLE);
		documentWhiteList.addAttributes("th", STYLE);
		documentWhiteList.addAttributes("li", STYLE);

		// This is needed for internal links/thumbnails to be shown
		// http://stackoverflow.com/questions/22444156/how-to-make-a-jsoup-whitelist-to-accept-certain-attribute-content
		documentWhiteList.addProtocols("img", "src", "data");
		documentWhiteList.addAttributes("a", CLASS, "rel", "data-instance-type", "data-instance-id");

		documentWhiteList.preserveRelativeLinks(true);
	}

	/**
	 * Sanitizes document html.
	 *
	 * @param content
	 *            html content to sanitize.
	 * @return sanitized html
	 */
	public String sanitize(String content) {
		return sanitize(content, IdocSanitizer.NON_EXISTENT_DOMAIN);
	}

	/**
	 * Sanitizes document html.
	 *
	 * @param content
	 *            html content to sanitize.
	 * @param origin
	 *            the origin to use for link sanitizing
	 * @return sanitized html
	 */
	public String sanitize(String content, String origin) {
		if (StringUtils.isBlank(content)) {
			throw new IllegalArgumentException("Could not sanitize content with [" + content + "] value.");
		}

		String result = sanitizeInternal(Jsoup.parse(content), origin);
		result = removeEmptyAttributes(result);
		result = trimTagWhitespaces(result);
		return result;
	}

	/**
	 * Sanitize the given html document by updating or creating new document.
	 *
	 * @param source
	 *            the source document to update
	 * @param origin
	 *            the origin uri if any
	 * @return the document instance that contains the sanitized content
	 */
	public Document sanitize(Document source, String origin) {
		String originUrl = NON_EXISTENT_DOMAIN;
		if (origin != null) {
			originUrl = origin;
		}
		String result = sanitizeInternal(source, originUrl);
		result = removeEmptyAttributes(result);
		result = trimTagWhitespaces(result);
		Document sanitizedDocument = Jsoup.parse(result);
		sanitizedDocument = sanitizeWidgetElements(sanitizedDocument);
		return removeWrappingHtmlAndBody(sanitizedDocument);
	}

	/**
	 * Sanitizes template html. Just like document sanitize but also removes "id" attribute from elements and "value"
	 * attribute from all widgets.
	 *
	 * @param content
	 *            html content to sanitize.
	 * @param origin
	 *            the origin to use for link sanitizing
	 * @return sanitized html
	 */
	public String sanitizeTemplate(String content, String origin) {
		String sanitized = sanitizeInternal(Jsoup.parse(content), origin);
		Document document = removeWrappingHtmlAndBody(Jsoup.parse(sanitized));

		// remove widget values
		removeAttribute(document, WIDGET_SELECTOR, "data-value");

		// remove id attributes of all elements without widgets
		removeAttribute(document, ID_SELECTOR, "id");
		String result = document.toString();

		// remove empty tags
		result = removeEmptyAttributes(result);

		result = trimTagWhitespaces(result);

		return result;
	}

	/**
	 * Checks if layouts are tainted with invalid content between the <b>layoutmanager</b> and <b>layout-container</b>.
	 * Used for sanitizing before save and before export to word. Check CMF-28469.
	 * @param document
	 * 				content to sanitize
	 * @return content between <b>layoutmanager</b> and <b>layout-container</b> is moved above <b>layoutmanager</b>
	 */
	public static Document sanitizeLayouts(Document document) {
		document.select(".layoutmanager").forEach(layout -> {
			// if layoutmanager does not have a single child, the layout is tainted
			if (layout.children().size() > 1) {
				StringBuilder invalidChildren = new StringBuilder();
				layout.children().forEach(layoutChild -> {
					if (!layoutChild.hasClass("layout-container")) {
						invalidChildren.append(layoutChild.outerHtml());
						layoutChild.remove();
					}
				});
				layout.before(invalidChildren.toString());
			}
		});
		return document;
	}

	/**
	 * Performs internal sanitizing by executing all steps on a Document.
	 *
	 * @param document
	 *            content to sanitize.
	 * @param origin
	 *            the origin
	 * @return sanitized document.
	 */
	private String sanitizeInternal(Document document, String origin) {

		removeWidgetContent(document);

		removeTempTags(document);

		removeStyleClassByPrefix(document, IDOC_VISUAL_CLASSES, IDOC_VISUAL_PATTERN);

		removeStyleClassByPrefix(document, NG_CLASSES, NG_CLASSES_PATTERN);

		addDataIdToTabs(document);

		removeOpacityFromImageStyles(document);

		removeMagicLineCKEditor(document);

		sanitizeLayouts(document);
		// clean is at last position because it breaks the widget content
		return clean(document, origin);
	}

	/**
	 * Removes widget content.
	 *
	 * @param document
	 *            content to sanitize
	 */
	private static void removeWidgetContent(Document document) {
		document.select(WIDGET_SELECTOR).html("");
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
	private String clean(Document document, String origin) {
		String local = origin;
		if (local == null) {
			local = "";
		}
		return Jsoup.clean(document.toString(), local, documentWhiteList);
	}

	/**
	 * Removes the opacity from the img elements styles. It is set by the lazyload of the images in the UI but is
	 * redundant and hides parts of the image.
	 */
	private static void removeOpacityFromImageStyles(Document document) {
		document.select("img[style~=opacity]").forEach(element -> {
			String[] styles = element.attr(STYLE).split(SEPARATOR);
			String style = Arrays.stream(styles).filter(currentStyle -> !currentStyle.contains("opacity")).collect(
					Collectors.joining(SEPARATOR));

			element.attr(STYLE, style + SEPARATOR);
		});
	}

	private static void removeMagicLineCKEditor(Document document) {
		document.select("span[style~=magicline]").stream().map(Element::parent).forEach(Element::remove);
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
	private static void removeStyleClassByPrefix(Document document, String className, Pattern regexPattern) {
		Elements elements = document.select("*[class*=" + className + "]");
		for (Element element : elements) {
			String classAttr = element.attr(CLASS);
			classAttr = regexPattern.matcher(classAttr).replaceAll("").trim();
			element.attr(CLASS, classAttr);
		}
	}

	/**
	 * Append a generated data-id to all sections that match the sections selector.
	 *
	 * @param document
	 *            the document containing the sections
	 */
	private static void addDataIdToTabs(Document document) {
		Elements selections = document.select(SECTIONS_SELECTOR);
		for (Element selection : selections) {
			if (!selection.hasAttr(DATA_ID_ATTR)) {
				selection.attr(DATA_ID_ATTR, RandomStringUtils.randomAlphanumeric(8));
			}
		}
	}

	/**
	 * Removes temporary tags from the document. This tags are used for track focus on widgets.
	 *
	 * @param document
	 *            the document contains the selection
	 */
	private static void removeTempTags(Document document) {
		Elements selections = document.select(TEMP_TAGS);
		for (Element selection : selections) {
			selection.remove();
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
	private static void removeAttribute(Document document, String selector, String attribute) {
		document.select(selector).removeAttr(attribute);
	}

	/**
	 * Removes empty attributes from html.
	 *
	 * @param html
	 *            html which empty attributes to remove.
	 * @return html with empty attributes removed.
	 */
	private static String removeEmptyAttributes(String html) {
		return EMPTY_ATTRIBUTE_PATTERN.matcher(html).replaceAll("");
	}

	/**
	 * Trims ending whatspaces from tags.
	 *
	 * @param content
	 *            content to trim.
	 * @return trimmed content.
	 */
	private static String trimTagWhitespaces(String content) {
		return TRIM_TAGS_PATTERN.matcher(content).replaceAll("<$1$2>");
	}

	/**
	 * Unwraps the widgets and removes other redundant widget elements, such as the drag handlers.
	 *
	 * @param document
	 *            document object
	 * @return the sanitized document object
	 */
	private static Document sanitizeWidgetElements(Document document) {
		document.getElementsByClass(WIDGET_WRAPPER_CLASS).unwrap();
		document.getElementsByClass(WIDGET_DRAG_HANDLER_CLASS).remove();
		return document;
	}

	private static Document removeWrappingHtmlAndBody(Document document) {
		document.getElementsByTag("html").unwrap();
		document.getElementsByTag("head").remove();
		document.getElementsByTag("body").unwrap();
		return document;
	}
}