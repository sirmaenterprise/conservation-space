package com.sirma.itt.seip.script;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.LinkIterable;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.ReflectionUtils;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * The Class ScriptEvaluatorTest.
 *
 * @author BBonev
 */
@Test(enabled = true)
public class ScriptEvaluatorTest extends EmfTest {

	private static final String TEST_FUNCTION = "function sum(a, b) { return a + b; }";

	@InjectMocks
	private ScriptEvaluatorImpl scriptEvaluator;
	@Mock
	private LinkService linkService;
	@Mock
	private SchedulerService schedulerService;
	@Spy
	private InstanceProxyMock<SchedulerService> schedulerServiceProxy = new InstanceProxyMock<>();
	@Spy
	private TaskExecutor taskExecutor = new TaskExecutorFake();
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	private TypeConverter converter;

	/**
	 * Initializes the.
	 */
	@BeforeMethod
	public void beforeMethod() {
		scriptEvaluator = createTestInstance();
		converter = createTypeConverter();

		converter.addConverter(EmfInstance.class, ScriptInstance.class, this::createScriptNode);

		super.beforeMethod();

		ScriptSchedulerExecutor schedulerExecutor = new ScriptSchedulerExecutor();
		ReflectionUtils.setFieldValue(schedulerExecutor, "scriptEvaluator", scriptEvaluator);

		schedulerServiceProxy.set(schedulerService);
		when(schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED)).thenReturn(new DefaultSchedulerConfiguration());
		when(schedulerService.schedule(eq(ScriptSchedulerExecutor.NAME), any(), any())).then(a -> {
			schedulerExecutor.execute(a.getArgumentAt(2, SchedulerContext.class));
			return null;
		});
	}

	/**
	 * Test engine integration.
	 */
	public void testEngineIntegration() {
		try {
			TimeTracker tracker = TimeTracker.createAndStart();
			Object object = scriptEvaluator.eval("{  2 + 2; }", null);
			Assert.assertNotNull(object);
			Assert.assertEquals(object, 4);
			// long notCompiled = tracker.stop();

			tracker.begin();
			object = scriptEvaluator.eval("{  2 + 2; }", null);
			Assert.assertNotNull(object);
			Assert.assertEquals(object, 4);
			// long compiled = tracker.stop();
			// Assert.assertTrue(notCompiled > compiled);
		} finally {
			scriptEvaluator.reset();
		}
	}

	/**
	 * Creates the test instance.
	 *
	 * @return the script evaluator impl
	 */
	private static ScriptEvaluatorImpl createTestInstance() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		ScriptEngineManagerProvider provider = new ScriptEngineManagerProvider();
		ReflectionUtils.setFieldValue(impl, "scriptEngineManager", provider.provide());

		ReflectionUtils.setFieldValue(impl, "scriptEngineName",
				new ConfigurationPropertyMock<>(ScriptEvaluator.DEFAULT_LANGUAGE));
		impl.initialize();
		return impl;
	}

	/**
	 * Test null script.
	 */
	@Test(expectedExceptions = EmfRuntimeException.class, expectedExceptionsMessageRegExp = ".*null.*")
	public void testNullScript() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		try {
			impl.eval(null, null);
		} finally {
			impl.reset();
		}
	}

	/**
	 * Test invalid scripts.
	 */
	@Test(expectedExceptions = ScriptException.class, expectedExceptionsMessageRegExp = ".*parsing.*")
	public void testInvalidScripts() {
		try {
			scriptEvaluator.eval("{ return 2 + 2; }", null);
		} finally {
			scriptEvaluator.reset();
		}
	}

	/**
	 * Test not compilable scripts.
	 */
	public void testNotCompilableScripts() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl() {
			@Override
			protected boolean isCompilationEnabled() {
				return false;
			}
		};
		try {
			ScriptEngineManagerProvider provider = new ScriptEngineManagerProvider();
			ReflectionUtils.setFieldValue(impl, "scriptEngineManager", provider.provide());

			Object object = impl.eval("{  2 + 2; }", null);
			Assert.assertNotNull(object);
			Assert.assertEquals(object, 4);
		} finally {
			impl.reset();
		}
	}

	/**
	 * Test not compilable scripts.
	 */
	public void testWithBindings() {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("a", 2);
			map.put("b", 2);
			Object object = scriptEvaluator.eval("{ a + b; }", map);
			Assert.assertNotNull(object);
			Assert.assertEquals(object, 4.0);
		} finally {
			scriptEvaluator.reset();
		}
	}

	/**
	 * Test with global bindings.
	 */
	public void testWithGlobalBindings() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		try {
			GlobalBindingsExtension extension = Mockito.mock(GlobalBindingsExtension.class);
			Map<String, Object> global = new HashMap<>();
			global.put("a", 2);
			Mockito.when(extension.getBindings()).thenReturn(global);

			ScriptEngineManagerProvider provider = new ScriptEngineManagerProvider();
			ReflectionUtils.setFieldValue(provider, "globalBindings", new ArrayList<>(
					Collections.singletonList(extension)));
			ReflectionUtils.setFieldValue(impl, "scriptEngineManager", provider.provide());
			ReflectionUtils.setFieldValue(impl, "scriptEngineName",
					new ConfigurationPropertyMock<>(ScriptEvaluator.DEFAULT_LANGUAGE));
			impl.initialize();

			Map<String, Object> map = new HashMap<>();
			map.put("b", 2);
			Object object = impl.eval("a + b", map);
			Assert.assertNotNull(object);
			Assert.assertEquals(object, 4.0);
		} finally {
			impl.reset();
		}
	}

	/**
	 * Test with script node.
	 */
	public void testWithScriptNode() {
		try {
			Map<String, Object> map = new HashMap<>();
			Instance instance = new EmfInstance();
			instance.setId("id");
			instance.setProperties(new HashMap<>());
			ReflectionUtils.setFieldValue(instance, "reference",
					new InstanceReferenceMock("id", mock(DataTypeDefinition.class), instance));

			ScriptInstance node = converter.convert(ScriptInstance.class, instance);
			map.put("workflow", node);

			List<LinkReference> references = new ArrayList<>(1);
			LinkReference ref = new LinkReference();
			ref.setFrom(new InstanceReferenceMock("id", mock(DataTypeDefinition.class)));
			ref.setTo(new InstanceReferenceMock("id2", mock(DataTypeDefinition.class)));
			references.add(ref);
			Mockito.when(linkService.getLinks(any(InstanceReference.class), eq("linkId"))).thenReturn(references);

			List<LinkInstance> instances = new ArrayList<>(1);
			LinkInstance linkInstance = new LinkInstance();
			linkInstance.setFrom(instance);

			Instance extected = new EmfInstance();
			extected.setId("id2");
			extected.setProperties(new HashMap<>());
			linkInstance.setTo(extected);
			instances.add(linkInstance);

			Mockito.when(linkService.convertToLinkInstance(eq(references))).thenReturn(instances);

			Object object = scriptEvaluator.eval(" workflow.getLinks(\"linkId\");", map);
			Assert.assertNotNull(object);
			Assert.assertTrue(object.getClass().isArray());
			ScriptInstance[] list = (ScriptInstance[]) object;
			ScriptInstance value = list[0];
			Assert.assertTrue(value != null);
			Assert.assertNotNull(value.getTarget());
			Assert.assertEquals(value.getTarget().getId(), "id2");
		} finally {
			scriptEvaluator.reset();
		}
	}

	/**
	 * Test json conversion.
	 */
	public void testJsonConversion() {
		try {
			Object evaluation = scriptEvaluator.eval(" var t = eval('({ \"test\":2 })'); t.test; ", null);
			Assert.assertNotNull(evaluation);
			Assert.assertEquals(evaluation, 2);

			evaluation = scriptEvaluator.eval(" var json = '{ \"test\":2 }';  var t = toJson(json); t.test; ", null);
			Assert.assertNotNull(evaluation);
			Assert.assertEquals(evaluation, 2);

			evaluation = scriptEvaluator.eval(" var t = toJson('{ \"test\":2 }'); t.test; ", null);
			Assert.assertNotNull(evaluation);
			Assert.assertEquals(evaluation, 2);

			evaluation = scriptEvaluator.eval("JSON.stringify({" + "    id  : 1," + "    type : \"2\"," + "    title : 3 });",
					null);
			Assert.assertNotNull(evaluation);
			JsonAssert.assertJsonEquals(evaluation, "{\"id\":1, \"type\":\"2\", \"title\": 3 }");
		} finally {
			scriptEvaluator.reset();
		}
	}

	/**
	 * Test with precompiled scripts that are provided externally
	 */
	public void testWithPrecompiledScripts() {
		ScriptEvaluatorImpl impl = new ScriptEvaluatorImpl();
		try {
			ScriptEngineManagerProvider provider = new ScriptEngineManagerProvider();

			ReflectionUtils.setFieldValue(provider, "globalBindings",
					new InstanceProxyMock<GlobalBindingsExtension>(new GlobalBindingsExtension() {
						@Override
						public Map<String, Object> getBindings() {
							return Collections.emptyMap();
						}

						@Override
						public Collection<String> getScripts() {
							return Collections.singleton(TEST_FUNCTION);
						}
					}));
			ReflectionUtils.setFieldValue(impl, "scriptEngineManager", provider.provide());
			ReflectionUtils.setFieldValue(impl, "scriptEngineName",
					new ConfigurationPropertyMock<>(ScriptEvaluator.DEFAULT_LANGUAGE));
			impl.initialize();

			Object object = impl.eval("sum(2,5)", null);
			Assert.assertNotNull(object);
			Assert.assertEquals(object, 7.0);
		} finally {
			impl.reset();
		}
	}

	@Test
	public void test_scriptedPredicate() {
		Predicate<Map<String, Object>> predicate = scriptEvaluator
				.createScriptedPredicate(" if (2 == 2 ) { true } else { false }");
		assertNotNull(predicate);
		assertTrue(predicate.test(Collections.emptyMap()));

		predicate = scriptEvaluator.createScriptedPredicate(" if (2 != 2 ) { true } else { false }");
		assertNotNull(predicate);
		assertFalse(predicate.test(Collections.emptyMap()));
	}

	@Test(expectedExceptions = ScriptException.class)
	public void test_scriptedPredicate_invalidScriptResult() {

		Predicate<Map<String, Object>> predicate = scriptEvaluator
				.createScriptedPredicate(" if (2 == 2 ) { 'test' } else { false }");
		assertNotNull(predicate);
		assertTrue(predicate.test(Collections.emptyMap()));
	}

	@Test(expectedExceptions = ScriptException.class)
	public void test_scriptedPredicate_invalidScript() {

		Predicate<Map<String, Object>> predicate = scriptEvaluator
				.createScriptedPredicate(" if (2 == a ) { 'test' } else { 'test2' }");
		assertNotNull(predicate);
		assertTrue(predicate.test(Collections.emptyMap()));
	}

	@Test
	public void test_scriptedFunction() {

		Function<Map<String, Object>, String> function = scriptEvaluator
				.createScriptedFunction(" if (2 == 2 ) { 'test' } else { 'test2' }");
		assertNotNull(function);
		assertEquals(function.apply(Collections.emptyMap()), "test");

		function = scriptEvaluator.createScriptedFunction(" if (2 != 2 ) { 'test' } else { 'test2' }");
		assertNotNull(function);
		assertEquals(function.apply(Collections.emptyMap()), "test2");
	}

	@Test(expectedExceptions = ScriptException.class)
	public void test_scriptedFunction_invalidScript() {

		Function<Map<String, Object>, String> predicate = scriptEvaluator
				.createScriptedFunction(" if (2 == a ) { 'test' } else { 'test2' }");
		assertNotNull(predicate);
		predicate.apply(Collections.emptyMap());
	}

	@Test
	public void test_PersistentAsyncExecution() {

		Executable function = mock(Executable.class);

		scriptEvaluator.scheduleEval(" a.execute();", Collections.singletonMap("a", function), true);

		verify(function).execute();
	}

	@Test
	public void test_nonPersistentAsyncExecution() {

		Executable function = mock(Executable.class);

		scriptEvaluator.scheduleEval(" a.execute();", Collections.singletonMap("a", function), false);

		verify(function).execute();
	}

	private ScriptInstance createScriptNode(EmfInstance instance) {
		ScriptInstance node = new ScriptMock(linkService, converter);
		node.setTarget(instance);
		return node;
	}

	public static class ScriptMock extends EmfInstance implements ScriptInstance {

		private Instance instance;
		private LinkService links;
		private TypeConverter typeConverter;

		ScriptMock(LinkService linkService, TypeConverter converter) {
			links = linkService;
			typeConverter = converter;
		}

		@Override
		public Instance getTarget() {
			return instance;
		}

		@Override
		public ScriptInstance setTarget(Instance instance) {
			this.instance = instance;
			return this;
		}

		public ScriptInstance[] getLinks(String linkId) {
			return typeConverter
					.convert(ScriptInstance.class, new LinkIterable<>(
							links.convertToLinkInstance(links.getLinks(getTarget().toReference(), linkId))))
						.toArray(new ScriptInstance[0]);
		}

	}
}
