package com.sirma.itt.seip.instance.script;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;

/**
 * The Class InstanceScriptsProviderTest.
 *
 * @author BBonev
 */
@Test
public class InstanceScriptsProviderTest extends BaseInstanceScriptTest {

	@InjectMocks
	private InstanceScriptsProvider scriptsProvider;

	@Mock
	private TypeMappingProvider typeProvider;

	@Mock
	private DefinitionService definitionService;

	@Mock
	HeadersService headersService;

	private DefinitionMock definition = new DefinitionMock();

	private EmfInstance instance = new EmfInstance();

	Map<String, Object> bindings = new HashMap<>();

	private EmfInstance destination;

	private EmfInstance source;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		when(typeProvider.getDefinitionClass(anyString())).then(invocation -> GenericDefinition.class);
		when(definitionService.find(anyString())).thenReturn(definition);
		instance.setProperties(new HashMap<String, Serializable>());
		when(instanceService.createInstance(any(DefinitionModel.class), any(Instance.class), any(Operation.class)))
				.thenReturn(instance);

		source = new EmfInstance();
		source.setId("source");
		source.setProperties(new HashMap<String, Serializable>());
		source.getProperties().put("key1", "value1");
		source.getProperties().put("key2", null);
		source.getProperties().put("key3", 2);
		source.getProperties().put("key4", "otherValue");
		ScriptNode parentNode = converter.convert(ScriptNode.class, source);

		bindings.put("source", parentNode);

		destination = new EmfInstance();
		destination.setId("destination");
		destination.setProperties(new HashMap<String, Serializable>());
		parentNode = converter.convert(ScriptNode.class, destination);

		bindings.put("destination", parentNode);
	}

	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);
		// bindingsExtensions.add(contentScriptsProvider);
		bindingsExtensions.add(scriptsProvider);
	}

	public void testGetDefinitionForType() {
		DefinitionModel model = scriptsProvider.getDefinitionForType("type", "definitionId");
		assertNotNull(model);
	}

	public void testCreateInstanceWithoutParent() {
		ScriptNode node = scriptsProvider.create("test", "definitionId", "operation");
		assertNotNull(node);
		verify(instanceService).createInstance(definition, null, new Operation("operation"));
	}

	public void testCreateInstanceWithParent() {
		EmfInstance parent = new EmfInstance();
		parent.setId("parent");
		ScriptNode parentNode = converter.convert(ScriptNode.class, parent);
		ScriptNode node = scriptsProvider.createWithParent("test", "definitionId", "operation", parentNode);
		assertNotNull(node);
		verify(instanceService).createInstance(definition, parent, new Operation("operation"));
	}

	public void testCreateInstanceWithoutParent_viaJS() {
		Object eval = eval("create('type', 'definitionId',  'operation')");
		assertNotNull(eval);
		verify(instanceService).createInstance(definition, null, new Operation("operation"));
	}

	public void testCreateInstanceWithParent_viaJS() {
		EmfInstance parent = new EmfInstance();
		parent.setId("parent");
		ScriptNode parentNode = converter.convert(ScriptNode.class, parent);

		bindings.put("parent", parentNode);
		Object eval = eval("create('type', 'definitionId',  'operation', parent)", bindings);
		assertNotNull(eval);
		verify(instanceService).createInstance(definition, parent, new Operation("operation"));
	}

	public void testCopyProperties_noProperties_JS() {
		eval("copyProperties(source, destination)", bindings);
		assertTrue(destination.getProperties().isEmpty());
	}

	public void testCopyProperties_1StringProperty_JS() {
		eval("copyProperties(source, destination, 'key1')", bindings);
		assertFalse(destination.getProperties().isEmpty());
		assertEquals(destination.getProperties().size(), 1);
	}

	public void testCopyProperties_1ArrayProperty_JS() {
		eval("copyProperties(source, destination, ['key1'])", bindings);
		assertFalse(destination.getProperties().isEmpty());
		assertEquals(destination.getProperties().size(), 1);
	}

	public void testCopyProperties_arrayProperties_JS() {
		eval("copyProperties(source, destination, ['key1', 'key2', 'key3'])", bindings);
		assertFalse(destination.getProperties().isEmpty());
		assertEquals(destination.getProperties().size(), 2);
	}

	public void testCopyProperties_missingProperties_JS() {
		eval("copyProperties(source, destination, 'key10')", bindings);
		assertTrue(destination.getProperties().isEmpty());
	}

	@Test
	public void test_getHeader() {
		when(headersService.generateInstanceHeader(any(Instance.class), anyString()))
				.then(a -> a.getArgumentAt(0, Instance.class).get(a.getArgumentAt(1, String.class)));
		Object eval = eval("instance.getHeader(source, 'key1')", bindings);
		assertNotNull(eval);
		assertEquals(eval, "value1");

	}
}
