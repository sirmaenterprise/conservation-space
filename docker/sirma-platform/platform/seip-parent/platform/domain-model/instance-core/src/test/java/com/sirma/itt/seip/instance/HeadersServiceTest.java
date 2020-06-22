package com.sirma.itt.seip.instance;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;

/**
 * Test for {@link HeadersService}.
 * 
 * @author BBonev
 */
@Test
public class HeadersServiceTest extends EmfTest {

	@InjectMocks
	private HeadersService headersService;

	@Mock
	private ExpressionsManager expressionsManager;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private LabelProvider labelProvider;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		for (int i = 0; i < 7; i++) {
			when(labelProvider.getLabel("label" + i, userPreferences.getLanguage())).thenReturn("Header " + i);
		}
		when(expressionsManager.evaluateRule(anyString(), eq(String.class), any(ExpressionContext.class),
				any(Instance.class))).then(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void test_generateSingleHeader_invalid() {
		assertNull(headersService.generateInstanceHeader(null, null));
		// no instance
		assertNull(headersService.generateInstanceHeader(null, "header6"));

		Instance instance = new EmfInstance();
		instance.setId("emf:instance1");

		// no properties
		assertNull(headersService.generateInstanceHeader(instance, "header6"));

		// normally the instance should have loaded properties
		instance.add("property", "Some value");

		// no header
		assertNull(headersService.generateInstanceHeader(instance, null));

		// no definition
		assertNull(headersService.generateInstanceHeader(instance, "header6"));
	}

	@Test
	public void test_generateSingleHeader() {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance1");
		// normally the instance should have loaded properties
		instance.add("property", "Some value");
		when(definitionService.getInstanceDefinition(instance)).thenReturn(createDefinitionMock());

		// generate header that is located in region also
		String header = headersService.generateInstanceHeader(instance, "header6");

		assertEquals(header, "Header 6");
	}

	@Test
	public void test_generateInstanceHeaders_invalidData() {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance1");

		// no properties
		headersService.generateInstanceHeaders(instance, "header1");
		assertFalse(instance.isPropertyPresent("header1"));

		// normally the instance should have loaded properties
		instance.add("property", "Some value");

		// no headers
		headersService.generateInstanceHeaders(instance, (String[]) null);
		assertTrue(instance.getProperties().size() == 1);
		headersService.generateInstanceHeaders(instance, new String[0]);
		assertTrue(instance.getProperties().size() == 1);

		// no model
		headersService.generateInstanceHeaders(instance, "header1");
		assertFalse(instance.isPropertyPresent("header1"));
	}

	@Test
	public void test_generateInstanceHeaders_noSave() {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance1");
		// normally the instance should have loaded properties
		instance.add("property", "Some value");
		when(definitionService.getInstanceDefinition(instance)).thenReturn(createDefinitionMock());

		// generate header that is located in region also
		headersService.generateInstanceHeaders(instance, "header1", "header2", "header6");

		assertTrue(instance.isPropertyPresent("header1"));
		assertTrue(instance.isPropertyPresent("header2"));
		assertTrue(instance.isPropertyPresent("header6"));

		assertEquals(instance.getString("header1"), "Header 1");
		assertEquals(instance.getString("header2"), "Header 2");
		assertEquals(instance.getString("header6"), "Header 6");

	}

	private static DefinitionModel createDefinitionMock() {
		DefinitionMock mock = new DefinitionMock();
		mock.getFields().add(buildField("header1", "label1"));
		mock.getFields().add(buildField("header2", "label2"));
		mock.getFields().add(buildField("header3", "label3"));
		mock.getFields().add(buildField("header4", null));
		mock.getFields().add(buildField("", "label5"));

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.getFields().add(buildField("header6", "label6"));
		mock.getRegions().add(region);

		return mock;
	}

	private static PropertyDefinition buildField(String name, String labelId) {
		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setIdentifier(name);
		field.setLabelId(labelId);
		return field;
	}
}
