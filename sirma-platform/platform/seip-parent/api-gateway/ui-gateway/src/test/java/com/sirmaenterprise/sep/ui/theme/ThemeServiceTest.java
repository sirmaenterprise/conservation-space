package com.sirmaenterprise.sep.ui.theme;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.collections.Maps;

public class ThemeServiceTest {
	@Mock private DefinitionService definitionService;

	@InjectMocks private ThemeService service;

	@Before public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test public void getNoThemeInfoTest() {
		when(definitionService.find(anyString())).thenReturn(null);
		assertEquals(Maps.newHashMap(), service.getUiTheme());
	}

	@Test
	public void getNoThemePropertyDefinitions() {
		DefinitionModel model = new DefinitionModelMock();
		when(definitionService.find(anyString())).thenReturn(model);
		assertEquals(Maps.newHashMap(),service.getUiTheme());
	}

	@Test
	public void serializeThemeDefinitionPropertyProperly() {
		DefinitionModelMock model = new DefinitionModelMock();
		PropertyDefinitionMock stylingDefinition = new PropertyDefinitionMock();
		stylingDefinition.setDefaultValue("{" +
				"\"top_header_primary_color\": \"#005096\"," +
						"\"top_header_secondary_color\": \"#005096\"," +
						"\"top_header_item_color\": \"\"," +
						"\"top_header_item_secondary_color\": \"\"," +
						"\"link_color\": \"\","+
						"\"link_hover_color\": \"\"," +
						"\"link_disabled_color\": \"\"," +
						"\"default_font_color\": \"\","+
						"\"tooltip_color\": \"\"," +
						"\"tooltip_background_color\": \"\"}");
		stylingDefinition.setName("styles");

		model.getFields().add(stylingDefinition);
		when(definitionService.find(anyString())).thenReturn(model);

		Map<String,String> expected = new HashMap<>();
		expected.put("top_header_primary_color", "#005096");
		expected.put("top_header_secondary_color", "#005096");
		assertEquals(expected, service.getUiTheme());
	}

	@Test
	public void definitionSerializationErrorTest() {
		DefinitionModelMock model = new DefinitionModelMock();
		PropertyDefinitionMock stylingDefinition = new PropertyDefinitionMock();
		stylingDefinition.setDefaultValue("{'invalid':'item'");
		stylingDefinition.setName("styles");
		model.getFields().add(stylingDefinition);
		when(definitionService.find(anyString())).thenReturn(model);

		assertEquals(Collections.emptyMap(), service.getUiTheme());
	}


	private static class DefinitionModelMock implements DefinitionModel {

		private static final long serialVersionUID = 1L;
		private List<PropertyDefinition> fields = new LinkedList<>();

		@Override
		public Integer getHash() {
			return null;
		}

		@Override
		public void setHash(Integer hash) {
			// empty
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public Node getChild(String name) {
			return null;
		}

		@Override
		public String getIdentifier() {
			return null;
		}

		@Override
		public void setIdentifier(String identifier) {
			// empty
		}

		@Override
		public List<PropertyDefinition> getFields() {
			return fields;
		}

		@Override
		public Long getRevision() {
			return null;
		}

		@Override
		public String getType() {
			return null;
		}

	}
}