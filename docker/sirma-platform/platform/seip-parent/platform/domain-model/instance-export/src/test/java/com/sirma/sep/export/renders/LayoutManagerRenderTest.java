package com.sirma.sep.export.renders;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Tests for LayoutManagerRender.
 *
 * @author Boyan Tonchev
 *
 */
public class LayoutManagerRenderTest {

	@InjectMocks
	private LayoutManagerRender layoutManagerRender;

	/**
	 * Runs Before method init.
	 */
	@BeforeMethod
	public void beforeClass() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests method render scenario with precentage exception.
	 *
	 * @throws URISyntaxException the URI syntax exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void renderWithPercentageExceptionTest() throws URISyntaxException, IOException {
		File layoutManager = new File(getClass().getClassLoader().getResource("layout-menager-with-precentage-exception.html").toURI());
		Document parse = Jsoup.parse(layoutManager, "UTF-8");
		Elements select = parse.select("div:eq(0)");
		ContentNode node = Mockito.mock(ContentNode.class);
		Mockito.when(node.getElement()).thenReturn(select.first());

		Element render = layoutManagerRender.render("", node);

		Assert.assertEquals(render.text(), "TEST ATE one two  tree DASF");

		Assert.assertTrue(render.select("tr:eq(0) td:eq(0)").first().hasClass("col-xs-3"));
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_STYLE), "vertical-align:top;text-align: left;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").text(), "TEST");

		Assert.assertTrue(render.select("tr:eq(0) td:eq(1)").first().hasClass("col-xs-6"));
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_STYLE), "vertical-align:top;text-align: left;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").text(), "ATE");

		Assert.assertTrue(render.select("tr:eq(0) td:eq(2)").first().hasClass("col-xs-3"));
		Assert.assertEquals(render.select("tr:eq(0) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_STYLE), "vertical-align:top;text-align: left;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");

		Assert.assertEquals(render.select("tr:eq(0) td:eq(2) div:eq(0)").first().className(), "layoutmanager cke_widget_element");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(2) div:eq(0)").first().attr("data-widget"), "6/3/3");

	}

	/**
	 * Tests method render scenario without children.
	 *
	 * @throws URISyntaxException the URI syntax exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void renderWithoutChildrenTest() throws URISyntaxException, IOException {
		File layoutManager = new File(getClass().getClassLoader().getResource("layout-menager-without-children.html").toURI());
		Document parse = Jsoup.parse(layoutManager, "UTF-8");
		Elements select = parse.select("div:eq(0)");
		ContentNode node = Mockito.mock(ContentNode.class);
		Mockito.when(node.getElement()).thenReturn(select.first());

		Element render = layoutManagerRender.render("", node);

		Assert.assertEquals(render.html(), "");

		Assert.assertEquals(render.attr(JsoupUtil.ATTRIBUTE_BORDER), "0");
		Assert.assertEquals(render.attr(JsoupUtil.ATTRIBUTE_WIDTH), "500");
	}

	/**
	 * Tests method render.
	 *
	 * @throws URISyntaxException the URI syntax exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void renderTest() throws URISyntaxException, IOException {
		File layoutManager = new File(getClass().getClassLoader().getResource("layout-menager.html").toURI());
		Document parse = Jsoup.parse(layoutManager, "UTF-8");
		Elements select = parse.select("div:eq(0)");
		ContentNode node = Mockito.mock(ContentNode.class);
		Mockito.when(node.getElement()).thenReturn(select.first());

		Element render = layoutManagerRender.render("", node);

		Assert.assertEquals(render.text(), "TEST ATE one two  tree DASF");

		Assert.assertTrue(render.select("tr:eq(0) td:eq(0)").hasClass("col-xs-3"));
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_STYLE), "vertical-align:top;text-align: left;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").text(), "TEST");

		Assert.assertTrue(render.select("tr:eq(0) td:eq(1)").hasClass("col-xs-6"));
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_STYLE), "vertical-align:top;text-align: left;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").text(), "ATE");

		Assert.assertTrue(render.select("tr:eq(0) td:eq(2)").hasClass("col-xs-3"));
		Assert.assertEquals(render.select("tr:eq(0) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_STYLE), "vertical-align:top;text-align: left;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");

		Assert.assertEquals(render.select("tr:eq(0) td:eq(2) div:eq(0)").first().className(), "layoutmanager cke_widget_element");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(2) div:eq(0)").first().attr("data-widget"), "6/3/3");

	}
	/**
	 * Tests method render without wrapper.
	 *
	 * @throws URISyntaxException the URI syntax exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void renderTestWithoutWrapper() throws URISyntaxException, IOException {
		File layoutManager = new File(getClass().getClassLoader().getResource("layout-manager-without-layoutmanager-class.html").toURI());
		Document parse = Jsoup.parse(layoutManager, "UTF-8");
		Elements select = parse.select("div:eq(0)");
		ContentNode node = Mockito.mock(ContentNode.class);
		Mockito.when(node.getElement()).thenReturn(select.first());

		Element render = layoutManagerRender.render("", node);

		Assert.assertEquals(render.text(), "TEST ATE one two  tree DASF");

		Assert.assertTrue(render.select("tr:eq(0) td:eq(0)").hasClass("col-xs-3"));
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_STYLE), "vertical-align:top;text-align: left;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(0)").text(), "TEST");

		Assert.assertTrue(render.select("tr:eq(0) td:eq(1)").hasClass("col-xs-6"));
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_STYLE), "vertical-align:top;text-align: left;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(1)").text(), "ATE");

		Assert.assertTrue(render.select("tr:eq(0) td:eq(2)").hasClass("col-xs-3"));
		Assert.assertEquals(render.select("tr:eq(0) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_STYLE), "vertical-align:top;text-align: left;");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(2)").attr(JsoupUtil.ATTRIBUTE_COLSPAN), "1");

		Assert.assertEquals(render.select("tr:eq(0) td:eq(2) div:eq(0)").first().className(), "layoutmanager cke_widget_element");
		Assert.assertEquals(render.select("tr:eq(0) td:eq(2) div:eq(0)").first().attr("data-widget"), "6/3/3");

	}
	/**
	 * Tests method accept scenario without layout manager.
	 */
	@Test
	public void notAcceptTest() {
		ContentNode node = Mockito.mock(ContentNode.class);
		Mockito.when(node.isLayoutManager()).thenReturn(false);
		Assert.assertFalse(layoutManagerRender.accept(node));
	}

	/**
	 * Tests method accept scenario with layout manager.
	 */
	@Test
	public void acceptTest() {
		ContentNode node = Mockito.mock(ContentNode.class);
		Mockito.when(node.isLayoutManager()).thenReturn(true);
		Assert.assertTrue(layoutManagerRender.accept(node));
	}
}
