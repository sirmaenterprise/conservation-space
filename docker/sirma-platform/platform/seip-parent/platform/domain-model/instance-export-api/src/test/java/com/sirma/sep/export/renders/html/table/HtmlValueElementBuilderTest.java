package com.sirma.sep.export.renders.html.table;

import static org.junit.Assert.assertEquals;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Tests for HtmlValueElementBuilder.
 *
 * @author Boyan Tonchev
 */
@SuppressWarnings("static-method")
public class HtmlValueElementBuilderTest {

	@Test
	public void htmlValueElementBuilderTest() {
		String html = "<div>any html</div>";
		String valueOfAttribute = "value-of-attribute";
		String firstStyle = "first-style;";
		String secondStyle = "second-style;";
		HtmlValueElementBuilder builder = new HtmlValueElementBuilder(new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), ""),
				html);
		builder.addAttribute(JsoupUtil.ATTRIBUTE_TITLE, valueOfAttribute);
		builder.addStyle(firstStyle);
		builder.addStyle(secondStyle);

		Element element = new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), "");
		builder.build(element);

		assertEquals(valueOfAttribute, element.select("span").attr(JsoupUtil.ATTRIBUTE_TITLE));
		assertEquals(firstStyle + secondStyle, element.select("span").attr(JsoupUtil.ATTRIBUTE_STYLE));
		assertEquals("any html", element.text());
	}
}
