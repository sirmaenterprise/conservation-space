package com.sirmaenterprise.sep.export.renders.html.table;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.export.renders.utils.JsoupUtil;

/**
 * Tests for HtmlValueElementBuilder.
 * 
 * @author Boyan Tonchev
 *
 */
@SuppressWarnings("static-method")
public class HtmlValueElementBuilderTest {

	@Test
	public void htmlValueElementBuilderTest() {
		String html = "<div>any html</div>";
		String valueOfAttribute = "value-of-attribute";
		String firstStyle = "first-style;";
		String secondStyle = "second-style;";
		HtmlValueElementBuilder builder = new HtmlValueElementBuilder(new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), ""), html);
		builder.addAttribute(JsoupUtil.ATTRIBUTE_TITLE, valueOfAttribute);
		builder.addStyle(firstStyle);
		builder.addStyle(secondStyle);
		
		Element element = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		builder.build(element);
		
		Assert.assertEquals(element.select("span").attr(JsoupUtil.ATTRIBUTE_TITLE), valueOfAttribute);
		Assert.assertEquals(element.select("span").attr(JsoupUtil.ATTRIBUTE_STYLE), firstStyle + secondStyle);
		Assert.assertEquals(element.text(), "any html");
	}
}
