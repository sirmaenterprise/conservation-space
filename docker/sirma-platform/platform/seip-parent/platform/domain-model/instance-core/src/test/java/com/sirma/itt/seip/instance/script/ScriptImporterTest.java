package com.sirma.itt.seip.instance.script;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptException;
import com.sirma.itt.seip.script.ScriptTest;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;

/**
 * The Class ScriptImporterTest.
 *
 * @author BBonev
 */
@Test
public class ScriptImporterTest extends ScriptTest {

	@Mock
	private DefinitionService definitionService;

	@InjectMocks
	private ScriptImporter importer;

	private DefinitionMock definition = new DefinitionMock();

	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		when(definitionService.find(anyString())).thenReturn(definition);
	}

	@Test
	public void testImport() {
		definition.getFields().clear();
		definition.setIdentifier("testContainer");
		FieldDefinitionImpl impl = new FieldDefinitionImpl();
		impl.setName("someScriptName");
		impl.setValue("function sum(a, b) { return a+b; }");
		definition.getFields().add(impl);

		Object object = eval(" importScript('testContainer/someScriptName'); sum(2, 4);");
		assertNotNull(object);
		assertTrue(object instanceof Number);
	}

	@Test(expectedExceptions = ScriptException.class)
	public void testScriptNotFound() {
		definition.getFields().clear();
		eval(" importScript('testContainer/someScriptName'); sum(2, 4);");
	}

	@Test
	public void testAllImport() {
		definition.getFields().clear();
		definition.setIdentifier("testContainer");
		FieldDefinitionImpl impl = new FieldDefinitionImpl();
		impl.setName("someScriptName");
		impl.setValue("function sum(a, b) { return a+b; }");
		definition.getFields().add(impl);
		impl = new FieldDefinitionImpl();
		impl.setName("someScriptName2");
		impl.setValue("function div(a, b) { return a/b; }");
		definition.getFields().add(impl);

		Object object = eval(" importScript('testContainer/*'); sum(sum(2, 4), div(4, 2));");
		assertNotNull(object);
		assertTrue(object instanceof Number);
	}

	@Test
	public void testImports() {
		definition.getFields().clear();
		definition.setIdentifier("testContainer");
		FieldDefinitionImpl impl = new FieldDefinitionImpl();
		impl.setName("someScriptName1");
		impl.setValue("function sum(a, b) { return a+b; }");
		definition.getFields().add(impl);
		impl = new FieldDefinitionImpl();
		impl.setName("someScriptName2");
		impl.setValue("function div(a, b) { return a/b; }");
		definition.getFields().add(impl);

		Object object = eval(
				" importScripts(['testContainer/someScriptName1', 'testContainer/someScriptName2']); sum(sum(2, 4), div(4, 2));");
		assertNotNull(object);
		assertTrue(object instanceof Number);
	}

	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);
		bindingsExtensions.add(importer);
	}
}