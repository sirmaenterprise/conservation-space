package com.sirma.sep.export.renders.html.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.nodes.Element;

import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Builder for html value.
 * 
 * @author Boyan Tonchev
 */
public class HtmlValueHtmlBuilder implements HtmlValueBuilder {

	private String html;

	protected List<String> styles = new ArrayList<>();

	protected Map<String, String> attrebutes = new HashMap<>();
	
	protected List<String> classNames = new ArrayList<>();

	/**
	 * Instantiates a HtmlValueHtmlBuilder for pre-formatted html which to be added directly.
	 *
	 * @param html
	 *            which to be directly added to doc.
	 */
	public HtmlValueHtmlBuilder(String html) {
		this.html = html;
	}

	@Override
	public void addAttribute(String name, String value) {
		attrebutes.put(name, value);
	}

	@Override
	public void build(Element td) {
		td.append(html);
		for (String style : styles) {
			JsoupUtil.addStyle(td, style);
		}

		for (Entry<String, String> entrySet : attrebutes.entrySet()) {
			td.attr(entrySet.getKey(), entrySet.getValue());
		}
		
		for (String className: classNames) {
			td.addClass(className);
		}
	}

	@Override
	public void addStyle(String style) {
		styles.add(style);
	}
	
	@Override
	public void addClass(String className) {
		classNames.add(className);
	}
}
