package com.sirma.sep.export.renders.html.table;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;

import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Builder for JSOUP Custom element.
 * 
 * @author Hristo Lungov
 */
public class HtmlValueElementBuilder implements HtmlValueBuilder {

	protected Element element;

	/**
	 * Instantiates a new HtmlValueElementBuilder.
	 *
	 * @param element
	 *            JSOUP Node
	 */
	public HtmlValueElementBuilder(Element element) {
		this.element = element;
	}

	/**
	 * Instantiates a HtmlValueElementBuilder.
	 *
	 * @param element
	 *            JSOUP Node
	 * @param html
	 *            to add as child of current element
	 */
	public HtmlValueElementBuilder(Element element, String html) {
		this(element);
		if (StringUtils.isNotBlank(html)) {
			this.element.append(html);
		}
	}

	@Override
	public void addAttribute(String name, String value) {
		element.attr(name, value);
	}

	@Override
	public void build(Element td) {
		td.appendChild(element);
	}
	
	@Override
	public void addStyle(String style) {
		JsoupUtil.addStyle(element, style);
	}

	@Override
	public void addClass(String className) {
		element.addClass(className);
	}
}
