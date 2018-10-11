package com.sirma.sep.export.renders.html.table;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Builder for <b>p</b> tag.
 * 
 * @author Boyan Tonchev
 */
public class HtmlValueTextBuilder extends HtmlValueElementBuilder {

	private final Element strongStyleElement = new Element(Tag.valueOf(JsoupUtil.TAG_STRONG), "");

	/**
	 * Text which will be visualize into cell.
	 */
	private String text;

	private boolean isBold = false;

	/**
	 * Instantiates a WordValue builder for simple text.
	 *
	 * @param text
	 *            the text of value.
	 */
	public HtmlValueTextBuilder(String text) {
		super(new Element(Tag.valueOf(JsoupUtil.TAG_P), ""));
		this.text = text;
	}

	/**
	 * Initialize for builder.
	 * 
	 * @param text
	 *            - text to be added to paragraph.
	 * @param isBold
	 *            - text to be styled in bold.
	 */
	public HtmlValueTextBuilder(String text, boolean isBold) {
		this(text);
		this.isBold = isBold;
	}

	@Override
	public void build(Element td) {
		if (StringUtils.isNotBlank(text)) {
			if (isBold) {
				strongStyleElement.text(text);
				element.appendChild(strongStyleElement);
			} else {
				element.text(text);
			}
		}
		td.appendChild(element);
	}

}
