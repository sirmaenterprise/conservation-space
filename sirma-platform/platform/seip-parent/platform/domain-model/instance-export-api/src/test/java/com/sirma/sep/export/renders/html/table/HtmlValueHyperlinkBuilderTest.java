package com.sirma.sep.export.renders.html.table;

import static org.junit.Assert.assertEquals;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Tests for HtmlValueHtmlBuilder.
 *
 * @author Boyan Tonchev
 */
@SuppressWarnings("static-method")
public class HtmlValueHyperlinkBuilderTest {

	@Test
	public void builderTest() {

		String html = "<a align=\"center\" href=\"url of hypelink\"><span style=\"color:blue\">link label</span></a>";
		String url = "url of hypelink";
		String linkLabel = "link label";
		String align = "center";
		Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");

		HtmlValueHtmlBuilder htmlValueHtmlBuilder = new HtmlValueHtmlBuilder(html);
		htmlValueHtmlBuilder.addAttribute(JsoupUtil.ATTRIBUTE_ALIGN, align);
		htmlValueHtmlBuilder.build(td);

		assertEquals(html, td.html());
		assertEquals(align, td.select("a").attr(JsoupUtil.ATTRIBUTE_ALIGN));
		assertEquals(url, td.select("a").attr(JsoupUtil.ATTRIBUTE_HREF));
		assertEquals("color:blue", td.select("span").attr(JsoupUtil.ATTRIBUTE_STYLE));
		assertEquals(linkLabel, td.select("span").text());
	}
}