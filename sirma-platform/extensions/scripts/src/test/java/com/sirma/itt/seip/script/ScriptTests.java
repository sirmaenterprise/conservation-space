package com.sirma.itt.seip.script;

import static org.testng.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test collection of JS scripts.
 *
 * @author BBonev
 */
@Test
public class ScriptTests extends ScriptTest {

	/**
	 * Tests this sciprt <code><pre>
	 * var revisions = {};
	 * revisions["test"] = 1;
	 * revisions["test"];
	 * </pre></code>
	 */
	public void testMapAccess() {
		String sciprt = "var revisions = {};\r\n" + "revisions[\"test\"] = 1;\r\n" + "revisions[\"test\"];";
		Object eval = eval(sciprt);
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
	public void testJsonArrayIteration() {
		String sciprt = "function test(value) {\r\n" + "	var json = toJson(value);\r\n" + "	if (!json) {\r\n"
				+ "		return;\r\n" + "	}\r\n" + "	var result;\r\n"
				+ "	for (var i = 0; i < json.length; i++) {\r\n" + "		result = json[i];\r\n" + "	}\r\n"
				+ "	return result;\r\n" + "}\r\n" + "\r\n" + "test(value);";

		Map<String, Object> map = new HashMap<>(1);
		JSONArray array = new JSONArray();
		array.put(5);
		map.put("value", array.toString());
		Object eval = eval(sciprt, map);
		Assert.assertNotNull(eval);
	}

	/**
	 * Method references.
	 */
	public void methodReferences() {
		String sciprt = "function test() { return 1; } function callTest(name) { return name(); } var a = { name : test, prop:'test'}; callTest(a.name)";
		Object object = eval(sciprt);
		Assert.assertNotNull(object);
		Assert.assertEquals(object, Double.valueOf(1));
	}

	/**
	 * Test logging.
	 *
	 * @param script
	 *            the script
	 */
	@Test(dataProvider = "loggingProvider")
	public void testLogging(String script) {
		eval(script);
	}

	/**
	 * Test return from if without return
	 */
	public void testReturnFromIfWithoutReturn() {
		Object object = eval(" if (true) { 'value' }");
		assertNotNull(object);
	}

	/**
	 * Logging provider.
	 *
	 * @return the object[][]
	 */
	@DataProvider(name = "loggingProvider")
	public Object[][] loggingProvider() {
		return new Object[][] { { "log.info('Info message');" }, { "log.error('Error message');" },
				{ "log.debug('Debug message');" }, { "log.warn('Warning message');" }, { "log.info(4);" } };
	}
}
