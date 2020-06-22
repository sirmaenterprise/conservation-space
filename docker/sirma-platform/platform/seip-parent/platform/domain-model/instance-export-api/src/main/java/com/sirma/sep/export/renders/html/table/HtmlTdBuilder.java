package com.sirma.sep.export.renders.html.table;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Builder for <b>td</b> tag.
 * 
 * @author Boyan Tonchev
 */
public class HtmlTdBuilder implements HtmlBuilder {

	/**
	 * Hold all values of td tags.
	 */
	private List<HtmlValueBuilder> tdValues = new LinkedList<>();
	
	/**
	 * TD element.
	 */
	private final Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");
	
	/**
	 * How many column take current td. 
	 */
	private int colspan = 1;

	/**
	 * Add <code>value</code> to td.
	 * @param value to be added.
	 */
	public void addValue(HtmlValueBuilder value) {
		tdValues.add(value);
	}

	/**
	 * Build td element fill it with values and ad it to <code>tr</code>
	 * @param tr element where created td have to be added.
	 */
	public void build(Element tr) {
		for (HtmlValueBuilder tdValue : tdValues) {
			tdValue.build(td);
		}
		addAttribute(JsoupUtil.ATTRIBUTE_COLSPAN, String.valueOf(colspan));
		tr.appendChild(td);
	}

	@Override
	public void addAttribute(String key, String value) {
		td.attr(key, value);
	}
	
	@Override
	public void addClass(String className) {
		td.addClass(className);
	}

	/**
	 * 
	 * @return how many column take current td.
	 */
	public int getColspan() {
		return colspan;
	}

	/**
	 * Set how many column to take current td.
	 * @param colspan
	 */
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}

	@Override
	public void addStyle(String style) {
		JsoupUtil.addStyle(td, style);
	}
}
