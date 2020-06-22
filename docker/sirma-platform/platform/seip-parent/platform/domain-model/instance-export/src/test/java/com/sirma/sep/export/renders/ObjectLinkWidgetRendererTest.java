package com.sirma.sep.export.renders;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;
import com.sirma.sep.export.renders.ObjectLinkWidgetRenderer;
import com.sirma.sep.export.renders.utils.JsoupUtil;

/**
 * Tests for ObjectLinkWidget
 * 
 * @author cdimitrov
 */
@SuppressWarnings("boxing")
public class ObjectLinkWidgetRendererTest {

	@InjectMocks
	private ObjectLinkWidgetRenderer objectLinkWidget;

	@Mock
	private InstanceService instanceService;

	@Mock
	private SystemConfiguration systemConfiguration;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void acceptTest() {
		WidgetNode widget = Mockito.mock(WidgetNode.class);

		setWidgetCriteria(widget, true, "");
		Assert.assertFalse(objectLinkWidget.accept(widget));

		setWidgetCriteria(widget, true, "widget");
		Assert.assertFalse(objectLinkWidget.accept(widget));

		setWidgetCriteria(widget, false, "");
		Assert.assertFalse(objectLinkWidget.accept(widget));

		setWidgetCriteria(widget, false, "widget");
		Assert.assertFalse(objectLinkWidget.accept(widget));

		setWidgetCriteria(widget, true, "object-link");
		Assert.assertTrue(objectLinkWidget.accept(widget));
	}

	@SuppressWarnings("static-method")
	private void setWidgetCriteria(WidgetNode widget, boolean isWidget, String title) {
		Mockito.when(widget.isWidget()).thenReturn(isWidget);
		Mockito.when(widget.getName()).thenReturn(title);
	}

	@Test
	public void renderTest() {
		WidgetNode widget = Mockito.mock(WidgetNode.class);
		JsonParser jsonParser = new JsonParser();
		JsonObject configuration = jsonParser.parse("{\"selectedObject\" : \"emf:123\"}").getAsJsonObject();

		Mockito.when(widget.getElement()).thenReturn(new Element(Tag.valueOf(JsoupUtil.TAG_SPAN), ""));
		Mockito.when(widget.getConfiguration()).thenReturn(new WidgetConfiguration(widget, configuration));
		Mockito.when(instanceService.loadDeleted(anyString())).thenReturn(Optional.empty());

		Element element = objectLinkWidget.render("emf123", widget);
		Assert.assertEquals(element.siblingElements().size(), 0);

		Mockito.when(instanceService.loadDeleted(anyString())).then(a -> {
			EmfInstance instance = new EmfInstance();

			Map<String, Serializable> properties = new HashMap<>();
			properties.put(DefaultProperties.HEADER_COMPACT, "<span><a href=\"/emf:123\"></a></span>");

			instance.setProperties(properties);
			return Optional.of(instance);
		});

		ConfigurationProperty<String> configProperty = mock(ConfigurationProperty.class);
		Mockito.when(configProperty.get()).thenReturn("host/");
		Mockito.when(systemConfiguration.getUi2Url()).thenReturn(configProperty);

		Element render = objectLinkWidget.render("emf:123", widget);
		Assert.assertEquals(render.tagName(), JsoupUtil.TAG_SPAN);
		Assert.assertEquals(render.child(0).tagName(), JsoupUtil.TAG_A);
	}
}
