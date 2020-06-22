package com.sirma.sep.export.renders.html.table;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Builder for <b>tr</b> tag
 * @author Boyan Tonchev
 *
 */
public class HtmlTrBuilder implements HtmlBuilder {

	/**
	 * Hold all <b>td</b> in current <b>tr</b>.
	 */
	private List<HtmlTdBuilder> tds = new ArrayList<>();
	
	/**
	 * Tr element.
	 */
	private final Element tr = new Element(Tag.valueOf(JsoupUtil.TAG_TR), "");

	/**
	 * Build <b>tr</b> element with <code>columnCount</count> count and add it to <code>table</code>
	 * @param table - table where created tr element have to be added. 
	 * @param columnCount column count of table.
	 */
	public void build(Element table, int columnCount) {
		int createdTds = 0;
		for (HtmlTdBuilder td : tds) {
			td.build(tr);
			createdTds += td.getColspan();
		}
		//We check if current row have less column than table
		//if true we create empty column to fit table column count.
		while (createdTds < columnCount) {
			createdTds++;
			new HtmlTdBuilder().build(tr);
		}
		table.appendChild(tr);
	}
	
	@Override
	public void addAttribute(String key, String value) {
		tr.attr(key, value);
	}
	
	/**
	 * Fetch td with index <code>tdIndex</code> if exist if not will create it.
	 * @param tdIndex - index of td.
	 * @return td with index <code>tdIndex</code>.
	 */
	public HtmlTdBuilder getTd(int tdIndex) {
		int realIndex = getTrSize();
		while (realIndex <= tdIndex) {
			realIndex++;
			HtmlTdBuilder td = new HtmlTdBuilder();
			tds.add(td);
		}
		return tds.get(tdIndex);
	}

	/**
	 * Calculate column count of row it take in mind colspan of ever td in row. 
	 * @return tr column count. 
	 */
	public int getTrSize() {
		int realIndex = 0;
		for (HtmlTdBuilder td : tds) {
			realIndex += td.getColspan();
		}
		return realIndex;
	}
	
	@Override
	public void addStyle(String style) {
		JsoupUtil.addStyle(tr, style);
	}

	@Override
	public void addClass(String className) {
		tr.addClass(className);
	}
}
