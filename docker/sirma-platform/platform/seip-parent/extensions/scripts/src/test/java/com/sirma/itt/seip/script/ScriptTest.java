package com.sirma.itt.seip.script;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.script.extensions.ScriptLogger;
import com.sirma.itt.seip.script.extensions.TypeConverterScriptExtension;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Base class for test involving server side JavaScript.
 *
 * @author BBonev
 */
public abstract class ScriptTest extends EmfTest {

	@Mock
	protected Statistics stats;

	@Spy
	protected TypeConverter converter;

	private ScriptEvaluatorImpl scriptEvaluator;

	/**
	 * Eval the given script without specific bindings
	 *
	 * @param script
	 *            the script
	 * @return the object
	 */
	public Object eval(String script) {
		return createEvaluator().eval(script, null);
	}

	/**
	 * Evaluate script with given bindings
	 *
	 * @param script
	 *            the script
	 * @param bindings
	 *            the bindings
	 * @return the object
	 */
	public Object eval(String script, Map<String, Object> bindings) {
		return createEvaluator().eval(script, bindings);
	}

	/**
	 * Evaluate the given script and set the given instance as root instance for the given execution.
	 *
	 * @param script
	 *            the script
	 * @param rootInstance
	 *            the root instance
	 * @return the object
	 */
	public Object eval(String script, Instance rootInstance) {
		Map<String, Object> bindings = new HashMap<>(5);
		bindings.put("root", converter.convert(ScriptInstance.class, rootInstance));
		return eval(script, bindings);
	}

	/**
	 * Before method.
	 */
	@Override
	@BeforeMethod
	public void beforeMethod() {
		scriptEvaluator = null;
		converter = createTypeConverter();
		super.beforeMethod();
	}

	/**
	 * After method.
	 */
	@AfterMethod
	public void afterMethod() {
		if (scriptEvaluator != null) {
			scriptEvaluator.reset();
		}
	}

	/**
	 * Creates the test instance.
	 *
	 * @return the script evaluator impl
	 */
	protected ScriptEvaluatorImpl createEvaluator() {
		if (scriptEvaluator == null) {
			scriptEvaluator = new ScriptEvaluatorImpl();
			ScriptEngineManagerProvider provider = createEngineProvider();
			ReflectionUtils.setFieldValue(scriptEvaluator, "stats", stats);
			ReflectionUtils.setFieldValue(scriptEvaluator, "scriptEngineManager", provider.provide());
			ReflectionUtils.setFieldValue(scriptEvaluator, "scriptEngineName",
					new ConfigurationPropertyMock<>(ScriptEvaluator.DEFAULT_LANGUAGE));
			scriptEvaluator.initialize();
		}

		return scriptEvaluator;
	}

	/**
	 * Creates the engine provider.
	 *
	 * @return the script engine manager provider
	 */
	protected ScriptEngineManagerProvider createEngineProvider() {
		ScriptEngineManagerProvider provider = new ScriptEngineManagerProvider();
		List<GlobalBindingsExtension> bindingsExtensions = new LinkedList<>();

		provideBindings(bindingsExtensions);
		ReflectionUtils.setFieldValue(provider, "globalBindings", bindingsExtensions);
		return provider;
	}

	/**
	 * An implementor could provide global bindings or default scripts to be loaded into the test script engine.
	 *
	 * @param bindingsExtensions
	 *            the bindings extensions
	 */
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		bindingsExtensions.add(new ScriptLogger());
		GlobalBindingsExtension converterExtension = new TypeConverterScriptExtension();
		ReflectionUtils.setFieldValue(converterExtension, "typeConverter", converter);
		bindingsExtensions.add(converterExtension);
	}

}
