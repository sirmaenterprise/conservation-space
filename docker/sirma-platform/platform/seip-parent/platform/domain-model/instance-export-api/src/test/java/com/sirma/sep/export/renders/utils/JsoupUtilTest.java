package com.sirma.sep.export.renders.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

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
		Elements elements = mock(Elements.class);
		Element elementOne = mock(Element.class);
		Element elementTwo = mock(Element.class);
		when(elements.iterator()).thenReturn(Arrays.asList(elementOne, elementTwo).iterator());
		when(elementOne.attr(JsoupUtil.ATTRIBUTE_STYLE)).thenReturn(oldStyleOne);
		when(elementTwo.attr(JsoupUtil.ATTRIBUTE_STYLE)).thenReturn(oldStyleTwo);

		JsoupUtil.addStyle(elements, styleToBeAdded);

		verify(elementOne).attr(JsoupUtil.ATTRIBUTE_STYLE, oldStyleOne + styleToBeAdded);
		verify(elementTwo).attr(JsoupUtil.ATTRIBUTE_STYLE, oldStyleTwo + styleToBeAdded);
	}

	/**
	 * Test image src attribute where it starts with base64 constant.
	 */
	@Test
	public void testImageBase64() {
		String html = "<span><img src=\"data:image/jpg;base64,\" />test.jpg</span>";
		String fixedHtml = JsoupUtil.fixHeaderUrls(html, "serverUrl").body().html();
		// should keep the same
		assertEquals(html, fixedHtml);
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
		assertEquals(result, fixedHtml);
	}

}
