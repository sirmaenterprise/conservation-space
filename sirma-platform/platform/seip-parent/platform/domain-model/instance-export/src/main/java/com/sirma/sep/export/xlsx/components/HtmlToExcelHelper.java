package com.sirma.sep.export.xlsx.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Export formatted cells to excel
 * 
 * @author S.Djulgerova
 */
public class HtmlToExcelHelper {

	private static final int START_TAG = 0;
	private static final int END_TAG = 1;
	private static final char BULLET_CHARACTER = '\u2022';
	private static final String BULLET_STRING = BULLET_CHARACTER + "  ";
	private static final String NEW_LINE = System.getProperty("line.separator");

	private static final String OPENING_TAG = "<";
	private static final String CLOSING_TAG = ">";
	private static final String PARAGRAPH = "<p>";
	private static final String NEW_LINE_PARAGRAPH = PARAGRAPH + NEW_LINE;

	private static final String SELF_CLOSING_BREAK_ROW = "<br />";
	private static final String NEW_LINE_SELF_CLOSING_BREAK_ROW = SELF_CLOSING_BREAK_ROW + NEW_LINE;
	private static final String LIST_ITEM = "<li>";
	private static final String NEW_LINE_LIST_ITEM = NEW_LINE + LIST_ITEM + "  " + BULLET_STRING;

	private static final String STYLE = "style";
	private static final String DELIMITER = ":";
	private static final String SEMICOLON = ";";

	/**
	 * Convert richtext value to formatted excel cell
	 * 
	 * @param workBook
	 *            excel workbook
	 * @param html
	 *            richtext value which have to be converted to formatted cell
	 * @param cell
	 *            the cell to which text belongs
	 */
	public static void convert(HSSFWorkbook workBook, String html, Cell cell) {
		RichTextDetails cellValue = createCellValue(html, workBook);
		createCell(cellValue, workBook, cell);
	}

	/**
	 * Parse given html using Jsoup. Process raw html removing useless formatting. Clean html using regex pattent based
	 * on tags in the supplied string.
	 * 
	 * @param html
	 *            html which have to be parsed
	 * @param workBook
	 *            excel workbook
	 * @return styled richtext
	 */
	private static RichTextDetails createCellValue(String html, HSSFWorkbook workBook) {
		// clean list formatting which is programmatically added in web due to - CMF-29072
		html = html.replaceAll("<li.*?>", LIST_ITEM);
		html = html.replaceAll("<p.*?>", PARAGRAPH);
		Document source = Jsoup.parse(html);
		Map<String, TagInfo> tagMap = new LinkedHashMap<>();
		for (Element e : source.children()) {
			getInfo(e, tagMap);
		}
		String patternString = "(" + StringUtils.join(tagMap.keySet(), "|") + ")";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(processRawHtml(html));

		StringBuffer textBuffer = new StringBuffer();
		List<RichTextInfo> textInfos = new ArrayList<>();
		LinkedList<RichTextInfo> richTextBuffer = new LinkedList<>();
		while (matcher.find()) {
			matcher.appendReplacement(textBuffer, "");
			TagInfo currentTag = tagMap.get(matcher.group(1));
			if (START_TAG == currentTag.getTagType()) {
				richTextBuffer.push(getRichTextInfo(currentTag, textBuffer.length()));
			} else {
				if (!richTextBuffer.isEmpty()) {
					RichTextInfo info = richTextBuffer.pop();
					if (info != null) {
						info.setEndIndex(textBuffer.length());
						textInfos.add(info);
					}
				}
			}
		}
		matcher.appendTail(textBuffer);
		Map<Integer, HSSFFont> fontMap = buildFontMap(textInfos, workBook);
		return new RichTextDetails(textBuffer.toString(), fontMap);
	}

	/**
	 * Merge text details and set the cell value
	 * 
	 * @param cellValue
	 *            richtext details value
	 * @param workBook
	 *            excel workbook
	 * @param cell
	 *            cell to which the richtext should be applied
	 */
	private static void createCell(RichTextDetails cellValue, HSSFWorkbook workBook, Cell cell) {
		HSSFRichTextString richtext = mergeTextDetails(cellValue);
		HSSFCellStyle wrapStyle = workBook.createCellStyle();
		cell.setCellValue(richtext);
		wrapStyle.setWrapText(true);
		cell.setCellStyle(wrapStyle);
	}

	/**
	 * Merge text details for all styles and formatting in one richtext string
	 * 
	 * @param cellValue
	 * @return richtext value
	 */
	private static HSSFRichTextString mergeTextDetails(RichTextDetails cellValue) {
		StringBuilder textBuffer = new StringBuilder();
		Map<Integer, HSSFFont> mergedMap = new LinkedHashMap<>();

		for (Entry<Integer, HSSFFont> entry : cellValue.getFontMap().entrySet()) {
			mergedMap.put(entry.getKey(), entry.getValue());
		}
		textBuffer.append(cellValue.getRichText());

		// The way fonts are applied should be optimized
		HSSFRichTextString richText = new HSSFRichTextString(textBuffer.toString());
		for (int i = 0; i < textBuffer.length(); i++) {
			HSSFFont currentFont = mergedMap.get(i);
			if (currentFont != null) {
				richText.applyFont(i, i + 1, currentFont);
			}
		}
		return richText;
	}

	private static Map<Integer, HSSFFont> buildFontMap(List<RichTextInfo> textInfos, HSSFWorkbook workBook) {
		Map<Integer, HSSFFont> fontMap = new LinkedHashMap<>();
		for (RichTextInfo richTextInfo : textInfos) {
			if (richTextInfo.isValid()) {
				for (int i = richTextInfo.getStartIndex(); i < richTextInfo.getEndIndex(); i++) {
					fontMap.put(i, mergeFont(fontMap.get(i), richTextInfo.getFontStyle(), richTextInfo.getFontValue(),
							workBook));
				}
			}
		}
		return fontMap;
	}

	private static HSSFFont mergeFont(HSSFFont font, STYLES fontStyle, String fontValue, HSSFWorkbook workBook) {
		if (font == null) {
			font = workBook.createFont();
		}

		switch (fontStyle) {
			case BOLD:
				font.setBold(true);
				break;
			case EM:
				font.setItalic(true);
				break;
			case STRONG:
				font.setBold(true);
				break;
			case UNDERLINE:
				font.setUnderline(Font.U_SINGLE);
				break;
			case ITALLICS:
				font.setItalic(true);
				break;
			case COLOR:
				if (!isEmpty(fontValue) && !"null".equalsIgnoreCase(fontValue)) {
					HSSFPalette palette = workBook.getCustomPalette();
					HSSFColor myColor = palette.findSimilarColor(Integer.valueOf(fontValue.substring(1, 3), 16),
							Integer.valueOf(fontValue.substring(2, 5), 16),
							Integer.valueOf(fontValue.substring(5, 7), 16));
					font.setColor(myColor.getIndex());
				}
				break;
			case SIZE:
				int fontSize = Integer.parseInt(fontValue.substring(0, fontValue.length() - 2));
				if (fontValue.endsWith("pt")) {
					font.setFontHeightInPoints((short) fontSize);
				} else if (fontValue.endsWith("px")) {
					// Font size is received in pixels and should be converted to points for correct visualization
					font.setFontHeightInPoints((short) (fontSize * 0.75));
				}
				break;
			default:
				break;
		}

		return font;
	}

	/**
	 * Removes all unnecessary formatting from html. Extend html with specific characters if needed
	 * 
	 * @param html
	 *            raw html
	 * @return formatted html
	 */
	private static String processRawHtml(String html) {
		// This should be extend with logic for nested unordered/ordered lists !!
		return clearNewLines(html).replace("&lt;", OPENING_TAG).replace("&gt;", CLOSING_TAG).replace("&amp;", "&")
				.replace(SELF_CLOSING_BREAK_ROW, NEW_LINE_SELF_CLOSING_BREAK_ROW).replace(PARAGRAPH, NEW_LINE_PARAGRAPH)
				.replace("&nbsp;", " ").replace(LIST_ITEM, NEW_LINE_LIST_ITEM)
				.replaceFirst(NEW_LINE_PARAGRAPH, PARAGRAPH);
	}

	private static RichTextInfo getRichTextInfo(TagInfo currentTag, int startIndex) {
		String styles = currentTag.getStyle();
		if (STYLES.SPAN.getType().equalsIgnoreCase(currentTag.getTagName())
				|| STYLES.FONT.getType().equalsIgnoreCase(currentTag.getTagName()) && !isEmpty(styles)) {
			return processTags(styles.replaceAll("\\s+", ""), startIndex);
		}
		return new RichTextInfo(startIndex, -1, STYLES.fromValue(currentTag.getTagName()));
	}

	private static RichTextInfo processTags(String styles, int startIndex) {
		RichTextInfo info = null;
		for (String style : styles.split(SEMICOLON)) {
			String[] styleDetails = style.split(DELIMITER);
			if (styleDetails.length > 1) {
				info = new RichTextInfo(startIndex, -1, STYLES.fromValue(styleDetails[0]), styleDetails[1]);
			}
		}
		return info;
	}

	private static boolean isEmpty(String str) {
		return StringUtils.isEmpty(str.trim());
	}

	private static String clearNewLines(String html) {
		return html.replace("\n", "").replace("\r", "");
	}

	private static void getInfo(Element e, Map<String, HtmlToExcelHelper.TagInfo> tagMap) {
		StringBuilder style = new StringBuilder(e.attr(STYLE));
		if (!isEmpty(style.toString())) {
			style.append(SEMICOLON);
		}
		for (Attribute attribute : e.attributes()) {
			if (!STYLE.equalsIgnoreCase(attribute.getKey())) {
				style.append(attribute.getKey() + DELIMITER + attribute.getValue() + SEMICOLON);
			}
		}

		String tagName = e.tagName();
		String startTag = OPENING_TAG + tagName + CLOSING_TAG;
		if (e.attributes().size() != 0) {
			startTag = OPENING_TAG + tagName + clearNewLines(e.attributes().toString()) + CLOSING_TAG;
		}
		String endTag = "</" + tagName + CLOSING_TAG;
		if (e.tag().isSelfClosing()) {
			startTag = endTag = OPENING_TAG + tagName + " />";
		}
		// Exception added for ckEditor BR tags
		if (e.tag().isSelfClosing() && "br".equalsIgnoreCase(e.tagName())) {
			startTag = endTag = OPENING_TAG + tagName + CLOSING_TAG;
		}
		tagMap.put(startTag, new TagInfo(tagName, style.toString(), START_TAG));
		if (!e.children().isEmpty()) {
			List<Element> children = e.children();
			for (Element child : children) {
				getInfo(child, tagMap);
			}
		}
		tagMap.put(endTag, new TagInfo(tagName, END_TAG));
	}

	static class RichTextInfo {
		private int startIndex;
		private int endIndex;
		private STYLES fontStyle;
		private String fontValue;

		public RichTextInfo(int startIndex, int endIndex, STYLES fontStyle) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.fontStyle = fontStyle;
		}

		public RichTextInfo(int startIndex, int endIndex, STYLES fontStyle, String fontValue) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.fontStyle = fontStyle;
			this.fontValue = fontValue;
		}

		public int getStartIndex() {
			return startIndex;
		}

		public void setStartIndex(int startIndex) {
			this.startIndex = startIndex;
		}

		public int getEndIndex() {
			return endIndex;
		}

		public void setEndIndex(int endIndex) {
			this.endIndex = endIndex;
		}

		public STYLES getFontStyle() {
			return fontStyle;
		}

		public void setFontStyle(STYLES fontStyle) {
			this.fontStyle = fontStyle;
		}

		public String getFontValue() {
			return fontValue;
		}

		public void setFontValue(String fontValue) {
			this.fontValue = fontValue;
		}

		public boolean isValid() {
			return (startIndex != -1 && endIndex != -1 && endIndex >= startIndex);
		}
	}

	static class RichTextDetails {
		private String richText;
		private Map<Integer, HSSFFont> fontMap;

		public RichTextDetails(String richText, Map<Integer, HSSFFont> fontMap) {
			this.richText = richText;
			this.fontMap = fontMap;
		}

		public String getRichText() {
			return richText;
		}

		public void setRichText(String richText) {
			this.richText = richText;
		}

		public Map<Integer, HSSFFont> getFontMap() {
			return fontMap;
		}

		public void setFontMap(Map<Integer, HSSFFont> fontMap) {
			this.fontMap = fontMap;
		}
	}

	static class TagInfo {
		private String tagName;
		private String style;
		private int tagType;

		public TagInfo(String tagName, String style, int tagType) {
			this.tagName = tagName;
			this.style = style;
			this.tagType = tagType;
		}

		public TagInfo(String tagName, int tagType) {
			this.tagName = tagName;
			this.tagType = tagType;
		}

		public String getTagName() {
			return tagName;
		}

		public void setTagName(String tagName) {
			this.tagName = tagName;
		}

		public int getTagType() {
			return tagType;
		}

		public void setTagType(int tagType) {
			this.tagType = tagType;
		}

		public String getStyle() {
			return style;
		}

		public void setStyle(String style) {
			this.style = style;
		}
	}

	enum STYLES {
		BOLD("b"), EM("em"), STRONG("strong"), COLOR("color"), SIZE("font-size"), BACKGROUND(
				"background-color"), UNDERLINE("u"), SPAN("span"), FONT("font"), ITALLICS("i"), UNKNOWN("unknown");

		private String type;

		private STYLES(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		public static STYLES fromValue(String type) {
			for (STYLES style : values()) {
				if (style.type.equalsIgnoreCase(type)) {
					return style;
				}
			}
			return UNKNOWN;
		}
	}
}