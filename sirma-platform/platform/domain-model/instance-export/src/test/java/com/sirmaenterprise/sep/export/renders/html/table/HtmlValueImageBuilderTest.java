package com.sirmaenterprise.sep.export.renders.html.table;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.export.renders.utils.JsoupUtil;

/**
 * Tests for HtmlValueImageBuilder.
 * 
 * @author Boyan Tonchev
 *
 */
@SuppressWarnings("static-method")
public class HtmlValueImageBuilderTest {

	/**
	 * Test method build scenarios with exception.
	 */
	@Test
	public void buildTest() {
		HtmlValueImageBuilder htmlValueImageBuilder = new HtmlValueImageBuilder(null, "tile");
		Element td = new Element(Tag.valueOf(JsoupUtil.TAG_TD), "");
		htmlValueImageBuilder.build(td);
		
		Assert.assertEquals(td.html(), "");
	}
}
