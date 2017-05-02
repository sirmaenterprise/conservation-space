package com.sirmaenterprise.sep.export.renders.html.table;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.export.renders.utils.JsoupUtil;

/**
 * Tests for HtmlValueHyperlinkBuilder.
 * 
 * @author Boyan Tonchev
 */
@SuppressWarnings("static-method")
public class HtmlValueHyperlinkBuilderTest {

	/**
	 * Test builder
	 */
	@Test
	public void builderTest() {

		String html = "<a align=\"center\" href=\"url of hypelink\"><span style=\"color:blue\">link label</span></a>";
		String url = "url of hypelink";
		String linkLabel = "link label";
		String align = "center";
		Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");

		HtmlValueHyperlinkBuilder htmlValueHyperlinkBuilder = new HtmlValueHyperlinkBuilder(html);
		htmlValueHyperlinkBuilder.addAttribute(JsoupUtil.ATTRIBUTE_ALIGN, align);
		htmlValueHyperlinkBuilder.build(td);

		Assert.assertEquals(td.html(), html);
		Assert.assertEquals(td.select("a").attr(JsoupUtil.ATTRIBUTE_ALIGN), align);
		Assert.assertEquals(td.select("a").attr(JsoupUtil.ATTRIBUTE_HREF), url);
		Assert.assertEquals(td.select("span").attr(JsoupUtil.ATTRIBUTE_STYLE), "color:blue");
		Assert.assertEquals(td.select("span").text(), linkLabel);
	}
}