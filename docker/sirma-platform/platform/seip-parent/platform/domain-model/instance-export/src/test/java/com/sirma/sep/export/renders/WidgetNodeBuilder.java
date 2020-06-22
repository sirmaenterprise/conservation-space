package com.sirma.sep.export.renders;

import com.google.gson.JsonParser;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;
import com.sirma.sep.export.renders.utils.JsoupUtil;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.mockito.Mockito.when;

/**
 * Mocked WidgetNode builder. Purpose of this class to be used test only.
 *
 * @author Boyan Tonchev.
 */
class WidgetNodeBuilder {

	public static final String LABEL_SELECTED_OBJECT_UNDEFINED_CRITERIA = "The widget has no search criteria configured.";

	private WidgetNode widget;

	private WidgetConfiguration widgetConfiguration;

	/**
	 * Initialize widget mock with parent SPAN.
	 */
	public WidgetNodeBuilder() {
		this(JsoupUtil.TAG_SPAN);
	}

	/**
	 * Initialize widget mock with parent <code>parentTag</code>
	 * @param parentTag
	 */
	public WidgetNodeBuilder(String parentTag) {
		widget = Mockito.mock(WidgetNode.class);
		Element parentOfNode = new Element(Tag.valueOf(parentTag), "");
		Element element = parentOfNode.appendElement("div");
		Mockito.when(widget.getElement()).thenReturn(element);
	}

	/**
	 * Load resource from <code>pathToConfigurationFile</code> and set it as configuration of widget.
	 * @param pathToConfigurationFile path to configuration resource.
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public WidgetNodeBuilder setConfiguration(String pathToConfigurationFile) throws URISyntaxException, IOException {
		widgetConfiguration = new WidgetConfiguration(widget, loadTestResource(pathToConfigurationFile));
		when(widget.getConfiguration()).thenReturn(widgetConfiguration);
		return this;
	}

	/**
	 * Set is widget.
	 * @param isWidget true if node is widget.
	 * @return
	 */
	public WidgetNodeBuilder setIsWidget(boolean isWidget) {
		Mockito.when(widget.isWidget()).thenReturn(isWidget);
		return this;
	}

	/**
	 * Set <code>widgetName</code> as name of widget.
	 * @param widgetName
	 * @return
	 */
	public WidgetNodeBuilder setName(String widgetName) {
		Mockito.when(widget.getName()).thenReturn(widgetName);
		return this;
	}

	/**
	 * @return the configuration of widget.
	 */
	public WidgetConfiguration getWidgetConfiguration() {
		return widgetConfiguration;
	}

	/**
	 * Build of widget mock.
	 * @return mock of widget.
	 */
	public WidgetNode build() {
		return widget;
	}

	/**
	 * Load test resource.
	 *
	 * @param resource
	 *            the resource
	 * @return the com.google.gson. json object
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private com.google.gson.JsonObject loadTestResource(String resource) throws URISyntaxException, IOException {
		URL testJsonURL = getClass().getClassLoader().getResource(resource);
		File jsonConfiguration = new File(testJsonURL.toURI());
		try (FileReader fileReader = new FileReader(jsonConfiguration)) {
			return new JsonParser().parse(fileReader).getAsJsonObject();
		}
	}
}