package com.sirma.sep.content.idoc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.JsonElement;
import com.sirma.sep.content.idoc.ContentNodeFactory;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.layout.LayoutManagerBuilder;
import com.sirma.sep.content.idoc.nodes.layout.LayoutNodeBuilder;
import com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableWidgetBuilder;
import com.sirma.sep.content.idoc.nodes.widgets.chart.ChartWidgetBuilder;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidgetBuilder;
import com.sirma.sep.content.idoc.nodes.widgets.content.ContentViewerWidgetBuilder;
import com.sirma.sep.content.idoc.nodes.widgets.datatable.DataTableWidgetBuilder;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidgetBuilder;
import com.sirma.sep.content.idoc.nodes.widgets.objectdata.ObjectDataWidgetBuilder;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidgetBuilder;

/**
 * The Class IdocTest.
 *
 * @author Hristo Lungov
 */
public class IdocTest {

	@BeforeClass
	public static void beforeClass() {
		ContentNodeFactory instance = ContentNodeFactory.getInstance();
		instance.registerBuilder(new LayoutNodeBuilder());
		instance.registerBuilder(new LayoutManagerBuilder());
		instance.registerBuilder(new AggregatedTableWidgetBuilder());
		instance.registerBuilder(new CommentsWidgetBuilder());
		instance.registerBuilder(new ContentViewerWidgetBuilder());
		instance.registerBuilder(new DataTableWidgetBuilder());
		instance.registerBuilder(new ImageWidgetBuilder());
		instance.registerBuilder(new ObjectDataWidgetBuilder());
		instance.registerBuilder(new RecentActivitiesWidgetBuilder());
		instance.registerBuilder(new ChartWidgetBuilder());
	}

	@Test(dataProvider = "idocs")
	public void testIdocParseString(String htmlResourceName, Collection<String> widgetsNames, Collection<String> widgetIds, int widgetsCount, int layoutManagersCount, int layoutsCount,
			int textNodesCount, int allChildren) throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(htmlResourceName)) {
			String idocContent = new String(IOUtils.toByteArray(is), "UTF-8");
			Idoc idoc = Idoc.parse(idocContent);
			Assert.assertEquals(idoc.getSections().widgets().count(), widgetsCount);
			Assert.assertEquals(idoc.widgets().count(), widgetsCount);
			Assert.assertEquals(idoc.getSections().layoutManagerNodes().count(), layoutManagersCount);
			Assert.assertEquals(idoc.getSections().layouts().count(), layoutsCount);
			Assert.assertEquals(idoc.getSections().textNodes().count(), textNodesCount);
			Assert.assertEquals(idoc.children().count(), allChildren);
			for (String widgetName : widgetsNames) {
				Stream<Widget> idocWidgets = idoc.widgets(widgetName);
				idocWidgets.forEach(widget -> {
					Assert.assertEquals(widgetName, widget.getName());
					Assert.assertFalse(widget.isLayout());
					Assert.assertFalse(widget.isLayoutManager());
					Assert.assertFalse(widget.isTextNode());
					Assert.assertTrue(widget.isWidget());
				});

			}
			for (String widgetId : widgetIds) {
				Optional<Widget> selectedWidget = idoc.selectWidget(widgetId);
				Widget widget = selectedWidget.get();
				Assert.assertEquals(widgetId, widget.getId());
				WidgetConfiguration configuration = widget.getConfiguration();
				configuration.getConfiguration().addProperty("Test", "TestValue");
			}
			String asHtml = idoc.asHtml();
			Idoc updatedIdoc = Idoc.parse(asHtml);
			for (String widgetId : widgetIds) {
				Optional<Widget> selectedWidget = updatedIdoc.selectWidget(widgetId);
				Widget widget = selectedWidget.get();
				Assert.assertEquals(widgetId, widget.getId());
				WidgetConfiguration configuration = widget.getConfiguration();
				JsonElement jsonElement = configuration.getConfiguration().get("Test");
				Assert.assertEquals("TestValue", jsonElement.getAsString());
			}
		}
	}

	@Test(dataProvider = "idocs")
	public void testIdocParseFile(String htmlResourceName, Collection<String> widgetsNames, Collection<String> widgetIds, int widgetsCount, int layoutManagersCount, int layoutsCount,
			int textNodesCount, int allChildren) throws URISyntaxException, IOException {
		URL resource = getClass().getClassLoader().getResource(htmlResourceName);
		File file = new File(resource.toURI());
		Idoc idoc = Idoc.parse(file);
		Assert.assertEquals(idoc.getSections().widgets().count(), widgetsCount);
		Assert.assertEquals(idoc.widgets().count(), widgetsCount);
		Assert.assertEquals(idoc.getSections().layoutManagerNodes().count(), layoutManagersCount);
		Assert.assertEquals(idoc.getSections().layouts().count(), layoutsCount);
		Assert.assertEquals(idoc.getSections().textNodes().count(), textNodesCount);
		Assert.assertEquals(idoc.children().count(), allChildren);

		for (String widgetName : widgetsNames) {
			Stream<Widget> idocWidgets = idoc.widgets(widgetName);
			idocWidgets.forEach(widget -> {
				Assert.assertEquals(widgetName, widget.getName());
				Assert.assertFalse(widget.isLayout());
				Assert.assertFalse(widget.isLayoutManager());
				Assert.assertFalse(widget.isTextNode());
				Assert.assertTrue(widget.isWidget());
			});
		}
		for (String widgetId : widgetIds) {
			Optional<Widget> selectedWidget = idoc.selectWidget(widgetId);
			Widget widget = selectedWidget.get();
			Assert.assertEquals(widgetId, widget.getId());
			WidgetConfiguration configuration = widget.getConfiguration();
			configuration.getConfiguration().addProperty("Test", "TestValue");
		}
		String asHtml = idoc.asHtml();
		Idoc updatedIdoc = Idoc.parse(asHtml);
		for (String widgetId : widgetIds) {
			Optional<Widget> selectedWidget = updatedIdoc.selectWidget(widgetId);
			Widget widget = selectedWidget.get();
			Assert.assertEquals(widgetId, widget.getId());
			WidgetConfiguration configuration = widget.getConfiguration();
			JsonElement jsonElement = configuration.getConfiguration().get("Test");
			Assert.assertEquals("TestValue", jsonElement.getAsString());
		}
	}

	@SuppressWarnings("boxing")
	@DataProvider(name = "idocs")
	public static Object[][] idocsProvider() {
		return new Object[][] { { "idoc-content.html",
				Arrays.asList("datatable-widget", "object-data-widget", "image-widget", "content-viewer"),
				Arrays.asList("0dda955c-e9cd-47e7-be51-ec5ccf66de5a", "a9f8689c-da4d-43c2-8707-09e80404bfea"),
				12, 4, 8, 11, 104 } };
	}

	@Test(dataProvider = "idocs")
	public void testIdocParseInputStream(String htmlResourceName, Collection<String> widgetsNames, Collection<String> widgetIds, int widgetsCount, int layoutManagersCount, int layoutsCount,
			int textNodesCount, int allChildren) throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(htmlResourceName)) {
			Idoc idoc = Idoc.parse(is);
			Assert.assertEquals(idoc.getSections().widgets().count(), widgetsCount);
			Assert.assertEquals(idoc.widgets().count(), widgetsCount);
			Assert.assertEquals(idoc.getSections().layoutManagerNodes().count(), layoutManagersCount);
			Assert.assertEquals(idoc.getSections().layouts().count(), layoutsCount);
			Assert.assertEquals(idoc.getSections().textNodes().count(), textNodesCount);
			Assert.assertEquals(idoc.children().count(), allChildren);
			for (String widgetName : widgetsNames) {
				Stream<Widget> idocWidgets = idoc.widgets(widgetName);
				idocWidgets.forEach(widget -> {
					Assert.assertEquals(widgetName, widget.getName());
					Assert.assertFalse(widget.isLayout());
					Assert.assertFalse(widget.isLayoutManager());
					Assert.assertFalse(widget.isTextNode());
					Assert.assertTrue(widget.isWidget());
				});

			}
			for (String widgetId : widgetIds) {
				Optional<Widget> selectedWidget = idoc.selectWidget(widgetId);
				Widget widget = selectedWidget.get();
				Assert.assertEquals(widgetId, widget.getId());
				WidgetConfiguration configuration = widget.getConfiguration();
				configuration.getConfiguration().addProperty("Test", "TestValue");
			}
			String asHtml = idoc.asHtml();
			Idoc updatedIdoc = Idoc.parse(asHtml);
			for (String widgetId : widgetIds) {
				Optional<Widget> selectedWidget = updatedIdoc.selectWidget(widgetId);
				Widget widget = selectedWidget.get();
				Assert.assertEquals(widgetId, widget.getId());
				WidgetConfiguration configuration = widget.getConfiguration();
				JsonElement jsonElement = configuration.getConfiguration().get("Test");
				Assert.assertEquals("TestValue", jsonElement.getAsString());
			}
		}
	}

	@Test()
	public static void emptyParseCheck() {
		Idoc idoc = Idoc.parse("");
		Assert.assertEquals(idoc.asHtml(), "<div data-tabs-counter=\"0\"></div>");
		Assert.assertEquals(idoc.getSections().widgets().count(), 0);
		Assert.assertEquals(idoc.widgets().count(), 0);
		Assert.assertEquals(idoc.children().count(), 0);
	}

	@Test()
	public static void emptyHtmlCheck() {
		Document document = Jsoup.parse("<div data-tabs-counter=\"0\"></div>", "");
		document.getElementsByTag("html").unwrap();
		document.getElementsByTag("head").remove();
		document.getElementsByTag("body").unwrap();

		Idoc idoc = new Idoc(document);
		Assert.assertEquals(idoc.asHtml(), "<div data-tabs-counter=\"0\"></div>");
		Assert.assertEquals(idoc.getSections().widgets().count(), 0);
		Assert.assertEquals(idoc.widgets().count(), 0);
		Assert.assertEquals(idoc.children().count(), 0);
	}

}
