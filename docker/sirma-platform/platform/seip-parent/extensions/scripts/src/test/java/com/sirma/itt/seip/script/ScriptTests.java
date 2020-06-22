package com.sirma.itt.seip.script;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testng.annotations.BeforeMethod;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * Test collection of JS scripts.
 *
 * @author BBonev
 */
@RunWith(DataProviderRunner.class)
public class ScriptTests extends ScriptTest {

	/**
	 * The parent before method is for TestNG
	 */
	@Before
	@Override
	public void beforeMethod() {
		super.beforeMethod();
	}

	/**
	 * Tests this sciprt <code><pre>
	 * var revisions = {};
	 * revisions["test"] = 1;
	 * revisions["test"];
	 * </pre></code>
	 */
	@Test
	public void testMapAccess() {
		String script = "var revisions = {};\r\n" + "revisions[\"test\"] = 1;\r\n" + "revisions[\"test\"];";
		Object eval = eval(script);
		Assert.assertNotNull(eval);
	}

	/**
	 * <code><pre>
	 * function test(value) {
	 * 	var json = toJson(value);
	 * 	if (!json) {
	 * 		return;
	 * 	}
	 * 	var result;
	 * 	for (var i = 0; i < json.length; i++) {
	 * 	 	result = json[i];
	 * 	}
	 * 	return result;
	 * }
	 * test(value);</pre></code>
	 */
	@Test
	public void testJsonArrayIteration() {
		String script = "function test(value) {\r\n" + "	var json = toJson(value);\r\n" + "	if (!json) {\r\n"
				+ "		return;\r\n" + "	}\r\n" + "	var result;\r\n"
				+ "	for (var i = 0; i < json.length; i++) {\r\n" + "		result = json[i];\r\n" + "	}\r\n"
				+ "	return result;\r\n" + "}\r\n" + "\r\n" + "test(value);";

		Map<String, Object> map = new HashMap<>(1);
		JSONArray array = new JSONArray();
		array.put(5);
		map.put("value", array.toString());
		Object eval = eval(script, map);
		Assert.assertNotNull(eval);
	}

	/**
	 * Method references.
	 */
	@Test
	public void methodReferences() {
		String script = "function test() { return 1; } function callTest(name) { return name(); } var a = { name : test, prop:'test'}; callTest(a.name)";
		Object object = eval(script);
		Assert.assertNotNull(object);
		Assert.assertEquals(object, 1);
	}

	/**
	 * Test logging.
	 *
	 * @param script
	 *            the script
	 */
	@Test
	@UseDataProvider("loggingProvider")
	public void testLogging(String script) {
		eval(script);
	}

	/**
	 * Test return from if without return
	 */
	@Test
	public void testReturnFromIfWithoutReturn() {
		Object object = eval(" if (true) { 'value' }");
		assertNotNull(object);
	}

	@Test
	public void testCollectionIterationAsArray() {

		EmfInstance instance = new EmfInstance();
		instance.add("collection", (Serializable) Arrays.asList("value1", "value2"));
		Object object = eval("var values = root.get('collection');"
				+ "var result = [];\n"
				+ "for(i = 0; i < values.length; i++) {\n"
				+ "result.push(values[i].toString());\n"
				+ "}"
				+ "\n result;", instance);
		assertNotNull(object);
		assertTrue(object instanceof ScriptObjectMirror);
		ScriptObjectMirror jsObject = (ScriptObjectMirror) object;
		Object[] objects = jsObject.to(Object[].class);
		assertEquals(objects.length, 2);
	}

	/**
	 * Logging provider.
	 *
	 * @return the object[][]
	 */
	@DataProvider
	public static Object[][] loggingProvider() {
		return new Object[][] { { "log.info('Info message');" }, { "log.error('Error message');" },
				{ "log.debug('Debug message');" }, { "log.warn('Warning message');" }, { "log.info(4);" } };
	}

	@Override
	protected void registerConverters(TypeConverter typeConverter) {
		typeConverter.addConverter(EmfInstance.class, ScriptInstance.class, instance -> {
			ScriptInstance mock = mock(ScriptInstance.class);
			when(mock.get(anyString())).then(a -> instance.get(a.getArgumentAt(0, String.class)));
			return mock;
		});
	}
}
