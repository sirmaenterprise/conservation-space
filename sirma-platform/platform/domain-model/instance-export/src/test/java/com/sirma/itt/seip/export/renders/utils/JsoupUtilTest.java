package com.sirma.itt.seip.export.renders.utils;

import java.util.Arrays;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for JsoupUtil.
 * 
 * @author Boyan Tonchev
 */
@SuppressWarnings("static-method")
public class JsoupUtilTest {

	/**
	 * Tests methods addStyle.
	 */
	@Test
	public void addStyleTest() {
		String styleToBeAdded = "style to be added";
		String oldStyleOne = "old style one ";
		String oldStyleTwo = "old style two ";
		Elements elements = Mockito.mock(Elements.class);
		Element elementOne = Mockito.mock(Element.class);
		Element elementTwo = Mockito.mock(Element.class);
		Mockito.when(elements.iterator()).thenReturn(Arrays.asList(elementOne, elementTwo).iterator());
		Mockito.when(elementOne.attr(JsoupUtil.ATTRIBUTE_STYLE)).thenReturn(oldStyleOne);
		Mockito.when(elementTwo.attr(JsoupUtil.ATTRIBUTE_STYLE)).thenReturn(oldStyleTwo);

		JsoupUtil.addStyle(elements, styleToBeAdded);

		Mockito.verify(elementOne).attr(JsoupUtil.ATTRIBUTE_STYLE, oldStyleOne + styleToBeAdded);
		Mockito.verify(elementTwo).attr(JsoupUtil.ATTRIBUTE_STYLE, oldStyleTwo + styleToBeAdded);
	}

	/**
	 * Test image src attribute where it starts with base64 constant.
	 */
	@Test
	public void testImageBase64() {
		String html = "<span><img src=\"data:image/jpg;base64,\" />test.jpg</span>";
		String fixedHtml = JsoupUtil.fixHeaderUrls(html, "serverUrl").body().html();
		// should keep the same
		Assert.assertEquals(fixedHtml, html);
	}

	/**
	 * Test image src attribute where its path.
	 */
	@Test
	public void testImagePath() {
		String html = "<span><img src=\"/test/test.jpg\" />test.jpg</span>";
		String result = "<span><img src=\"serverUrl/test/test.jpg\" />test.jpg</span>";
		String fixedHtml = JsoupUtil.fixHeaderUrls(html, "serverUrl").body().html();
		// should add serverUrl to src attribute
		Assert.assertEquals(fixedHtml, result);
	}

}
