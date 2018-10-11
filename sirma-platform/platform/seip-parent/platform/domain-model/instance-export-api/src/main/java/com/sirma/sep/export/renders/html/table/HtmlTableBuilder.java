package com.sirma.sep.export.renders.html.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.sirma.sep.export.renders.IdocRenderer;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Builder for <b>table</b> tag.
 * 
 * <pre>
* ----------------------------------------------------------------
* |           <b><code>title</code> will be shown here</b>       |
* ----------------------------------------------------------------
* |        |        |        |        |        |        |        |
* ----------------------------------------------------------------
* |        |        |        |        |        |        |        |
* ----------------------------------------------------------------
 * </pre>
 * 
 * @author Boyan Tonchev
 */
public class HtmlTableBuilder implements HtmlBuilder {

	/**
	 * Hold all rows of table.
	 */
	private List<HtmlTrBuilder> tableTrs = new ArrayList<>();

	/**
	 * Table element.
	 */
	private final Element table = new Element(Tag.valueOf(JsoupUtil.TAG_TABLE), "");

	private final HtmlTrBuilder titleRow = new HtmlTrBuilder();

	private boolean addHeader;

	/**
	 * Initialize table builder.
	 * 
	 * @param title
	 *            - title of table.
	 */
	public HtmlTableBuilder(String title) {
		this(title, true);
	}

	/**
	 * Initialize table builder.
	 * 
	 * @param title
	 *            - title of table.
	 * @param addHeader
	 *            - boolean indicating if table header row should be rendered
	 */
	public HtmlTableBuilder(String title, boolean addHeader) {
		this.addHeader = addHeader;

		addAttribute(JsoupUtil.ATTRIBUTE_BORDER, "1");
		addAttribute(JsoupUtil.ATTRIBUTE_CELLSPACING, "0");
		addAttribute(JsoupUtil.ATTRIBUTE_CELLPADDING, "0");
		addAttribute(JsoupUtil.ATTRIBUTE_WIDTH, "500");
		addAttribute(JsoupUtil.ATTRIBUTE_STYLE, "table-layout:fixed;");

		if (this.addHeader && StringUtils.isNotBlank(title)) {
			titleRow.getTd(0).addValue(new HtmlValueTextBuilder(title, true));
		}
	}

	/**
	 * Build table and add it to <code>wordDocument</code>.
	 *
	 * @return the table element.
	 */
	public Element build() {
		int maxSize = getColumnCount();

		if (this.addHeader) {
			buildTitleRow(maxSize);
		}
		for (HtmlTrBuilder tr : tableTrs) {
			tr.build(table, maxSize);
		}
		return table;
	}

	/**
	 * Gets the row containing the title if presented.
	 *
	 * @return The row.
	 */
	public HtmlTrBuilder getTitleRow() {
		return titleRow;
	}

	/**
	 * Build title row if it has content.
	 *
	 * @param columnCount
	 *            - column count.
	 */
	private void buildTitleRow(int columnCount) {
		if (titleRow.getTrSize() != 0) {
			HtmlTdBuilder td = titleRow.getTd(0);
			td.setColspan(columnCount);
			titleRow.build(table, columnCount);
		}
	}

	/**
	 * @return return column count of table.
	 */
	public int getColumnCount() {
		int maxSize = 0;
		for (HtmlTrBuilder tr : tableTrs) {
			maxSize = maxSize < tr.getTrSize() ? tr.getTrSize() : maxSize;
		}
		return maxSize;
	}

	/**
	 * @return the rows count of table.
	 */
	public int getRowCount() {
		return tableTrs.size();
	}

	/**
	 * Add <code>value</code> to row <code>trIndex</code> and column <code>tdIndex</code>.
	 * 
	 * @param trIndex
	 *            - the row index.
	 * @param tdIndex
	 *            - the column index.
	 * @param value
	 *            - value to be added.
	 * @return created builder.
	 */
	public HtmlTdBuilder addTdValue(int trIndex, int tdIndex, HtmlValueBuilder value) {
		return addTdValue(trIndex, tdIndex, 1, value);
	}

	/**
	 * Add <code>value</code> to row <code>trIndex</code> and column <code>tdIndex</code> which take
	 * <code>colspan</code> columns.
	 *
	 * @param trIndex
	 *            - the row index.
	 * @param tdIndex
	 *            - the column index.
	 * @param colspan
	 *            - number of columns
	 * @param value
	 *            - value to be added.
	 * @return created builder.
	 */
	public HtmlTdBuilder addTdValue(int trIndex, int tdIndex, int colspan, HtmlValueBuilder value) {
		HtmlTrBuilder tr = getRow(trIndex);
		HtmlTdBuilder td = tr.getTd(tdIndex);
		td.setColspan(colspan);
		td.addValue(value);
		return td;
	}

	/**
	 * Fetch row builder for row with index <code>rowIndex</code>. if row builder is not exist will create one and
	 * return it.
	 * 
	 * @param rowIndex
	 *            the row index it start from 0.
	 * @return row builder for row with index <code>rowIndex</code>
	 */
	private HtmlTrBuilder getRow(int rowIndex) {
		while (tableTrs.size() <= rowIndex) {
			tableTrs.add(new HtmlTrBuilder());
		}
		return tableTrs.get(rowIndex);
	}

	@Override
	public void addAttribute(String key, String value) {
		table.attr(key, value);
	}

	/**
	 * Add row with correct style for messages like: "No object could be found".
	 *
	 * @param htmlTableBuilder
	 *            the table builder where row have to be added.
	 * @param rowValue
	 *            value of non result row.
	 */
	public static void addNoResultRow(HtmlTableBuilder htmlTableBuilder, String rowValue) {
		HtmlValueTextBuilder htmlValueTextBuilder = new HtmlValueTextBuilder(rowValue);
		htmlValueTextBuilder.addAttribute(JsoupUtil.ATTRIBUTE_STYLE, "text-align: center; color: #a94442;");
		htmlTableBuilder.addTdValue(htmlTableBuilder.getRowCount(), htmlTableBuilder.getColumnCount(), 1,
				htmlValueTextBuilder);
	}

	/**
	 * Add row "Total Results:" to table.
	 * 
	 * @param htmlTableBuilder
	 *            the table builder where row have to be added.
	 * @param rowValue
	 *            - value of result row.
	 */
	public static void addTotalResultRow(HtmlTableBuilder htmlTableBuilder, String rowValue) {
		HtmlValueTextBuilder footer = new HtmlValueTextBuilder(rowValue);
		footer.addAttribute(JsoupUtil.ATTRIBUTE_STYLE, IdocRenderer.MARGIN_LEFT_15);
		htmlTableBuilder.addTdValue(htmlTableBuilder.getRowCount(), 0, htmlTableBuilder.getColumnCount(), footer);
	}

	/**
	 * Add style to td with <code>rowIndex</code> and <code>tdIndex</code>.
	 *
	 * @param rowIndex
	 *            the row index
	 * @param tdIndex
	 *            the td index
	 * @param style
	 *            additional style to be added.
	 */
	public void addTdStyle(int rowIndex, int tdIndex, String style) {
		HtmlTrBuilder row = getRow(rowIndex);
		HtmlTdBuilder td = row.getTd(tdIndex);
		td.addStyle(style);
	}

	/**
	 * Add attribute to td with <code>rowIndex</code> and <code>tdIndex</code>.
	 *
	 * @param rowIndex
	 *            the row index.
	 * @param tdIndex
	 *            the td index.
	 * @param key
	 *            the key of attribute.
	 * @param value
	 *            the value of attribute.
	 */
	public void addTdAttribute(int rowIndex, int tdIndex, String key, String value) {
		HtmlTrBuilder row = getRow(rowIndex);
		HtmlTdBuilder td = row.getTd(tdIndex);
		td.addAttribute(key, value);
	}

	/**
	 * Add class <code>className</code> to td with <code>rowIndex</code> and <code>tdIndex</code>.
	 *
	 * @param rowIndex
	 *            the row index.
	 * @param tdIndex
	 *            the td index.
	 * @param className
	 *            the class name
	 */
	public void addTdClass(int rowIndex, int tdIndex, String className) {
		HtmlTrBuilder row = getRow(rowIndex);
		HtmlTdBuilder td = row.getTd(tdIndex);
		td.addClass(className);
	}

	@Override
	public void addStyle(String style) {
		JsoupUtil.addStyle(table, style);
	}

	@Override
	public void addClass(String className) {
		table.addClass(className);
	}

	public boolean isAddHeader() {
		return addHeader;
	}
}
