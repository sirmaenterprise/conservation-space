package com.sirma.sep.export.renders.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Util class for Jsoup library.
 *
 * @author Boyan Tonchev
 */
public class JsoupUtil {

	/**
	 * Constants for attribute names.
	 */
	public static final String ATTRIBUTE_STYLE = "style";
	public static final String ATTRIBUTE_WIDTH = "width";
	public static final String ATTRIBUTE_HEIGHT = "height";
	public static final String ATTRIBUTE_CELLPADDING = "cellpadding";
	public static final String ATTRIBUTE_CELLSPACING = "cellspacing";
	public static final String ATTRIBUTE_BORDER = "border";
	public static final String ATTRIBUTE_COLSPAN = "colspan";
	public static final String ATTRIBUTE_SRC = "src";
	public static final String ATTRIBUTE_ALT = "alt";
	public static final String ATTRIBUTE_TITLE = "title";
	public static final String ATTRIBUTE_ALIGN = "align";
	public static final String ATTRIBUTE_HREF = "href";

	/**
	 * Constants for tag names.
	 */
	public static final String TAG_TABLE = "table";
	public static final String TAG_TD = "td";
	public static final String TAG_TR = "tr";
	public static final String TAG_A = "a";
	public static final String TAG_SPAN = "span";
	public static final String TAG_IMG = "img";
	public static final String TAG_P = "p";
	public static final String TAG_STRONG = "strong";

	/**
	 * Hide constructor.
	 */
	private JsoupUtil() {

	}

	/**
	 * Add style to existing one.
	 *
	 * @param element
	 *            - to which style have to be added.
	 * @param styleToBeAdded
	 *            - style which will be added.
	 */
	public static void addStyle(Element element, String styleToBeAdded) {
		String style = element.attr(ATTRIBUTE_STYLE);
		style += styleToBeAdded;
		element.attr(ATTRIBUTE_STYLE, style);
	}

	/**
	 * Add style to <code>elements</code>.
	 *
	 * @param elements
	 *            - list with elements where style have to be added.
	 * @param styleToBeAdded
	 *            - style which will be added.
	 */
	public static void addStyle(Elements elements, String styleToBeAdded) {
		for (Element element : elements) {
			addStyle(element, styleToBeAdded);
		}
	}

	/**
	 * Fix header urls, where the constuction must be similar to:
	 *
	 * <pre>
	 *  &lt;span&gt;&lt;img src="some image src" /&gt;&lt;/span&gt;
	 *	&lt;span&gt;&lt;a class="instance-link" href="some instance href"&gt;&lt;span&gt;Instance Text&lt;/span&gt;&lt;/a&gt;&lt;/span&gt;
	 * </pre>
	 *
	 * <b> instance-link </b> class is mandatory for instance link url fixup.
	 *
	 * @param html
	 *            text to be parsed by JSOUP
	 * @param ui2BaseUrl
	 *            the ui2 base url
	 * @return the fixed JSOUP Document
	 */
	public static Document fixHeaderUrls(String html, String ui2BaseUrl) {
		Document jsoupDocument = Jsoup.parse(html);
		Elements images = jsoupDocument.select("span:eq(0) > img:eq(0)");
		for (Element image : images) {
			String srcAttrValue = image.attr(JsoupUtil.ATTRIBUTE_SRC);
			if (isBase64Encoded(srcAttrValue)) {
				continue;
			} else if (!srcAttrValue.contains(ui2BaseUrl)) {
				image.attr(JsoupUtil.ATTRIBUTE_SRC, ui2BaseUrl + srcAttrValue);
			}
		}

		Elements hyperLinks = jsoupDocument.select("a.instance-link");
		for (Element hyperLink : hyperLinks) {
			String hrefAttrValue = hyperLink.attr(JsoupUtil.ATTRIBUTE_HREF);
			if (!hrefAttrValue.contains(ui2BaseUrl)) {
				hyperLink.attr(JsoupUtil.ATTRIBUTE_HREF, ui2BaseUrl + hrefAttrValue);
			}
		}
		return jsoupDocument;
	}

	/**
	 * Checks if raw is base64 encoded string.
	 *
	 * @param raw
	 *            string to be tested
	 * @return true if is a valid base64 encoded string.
	 */
	public static boolean isBase64Encoded(String raw) {
		return raw.startsWith("data:image") && raw.contains(";base64,");
	}

}