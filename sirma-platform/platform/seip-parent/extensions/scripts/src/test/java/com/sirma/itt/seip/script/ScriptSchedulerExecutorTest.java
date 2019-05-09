package com.sirma.itt.seip.script;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * The Class ScriptSchedulerExecutorTest.
 *
 * @author BBonev
 */
public class ScriptSchedulerExecutorTest {

	/** The Constant SCRIPT. */
	private static final String SCRIPT = "function sum(a,b) { return a+b } sum(2,3);";

	/** The script evaluator. */
	@Mock
	private ScriptEvaluator scriptEvaluator;

	/** The executor. */
	@InjectMocks
	private ScriptSchedulerExecutor executor;

	@Mock
	private TypeConverter converter;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test execute.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testExecute() throws Exception {
		SchedulerContext context = new SchedulerContext();
		context.put(ScriptSchedulerExecutor.PARAM_SCRIPT, SCRIPT);
		executor.execute(context);

		verify(scriptEvaluator).eval(anyString(), eq(SCRIPT), any());
	}

	/**
	 * Test execute_with instance.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testExecute_withInstance() throws Exception {
		SchedulerContext context = new SchedulerContext();
		InstanceReference reference = InstanceReferenceMock.createGeneric("emf:instance");
		Instance instance = reference.toInstance();
		when(converter.convert(ScriptInstance.class, instance)).thenReturn(mock(ScriptInstance.class));

		Map<String, Object> bindings = new HashMap<>();
		bindings.put("root", reference);

		context.put(ScriptSchedulerExecutor.PARAM_SCRIPT, SCRIPT);
		context.put(ScriptSchedulerExecutor.PARAM_BINDINGS, (Serializable) bindings);
		executor.execute(context);

		verify(scriptEvaluator).eval(anyString(), eq(SCRIPT), anyMap());
		verify(converter).convert(ScriptInstance.class, instance);
	}

	/**
	 * Test execute_with instance.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testExecute_withInstance_notFound() throws Exception {
		SchedulerContext context = new SchedulerContext();
		InstanceReference reference = mock(InstanceReference.class);
		when(reference.toInstance()).thenThrow(NullPointerException.class);

		Map<String, Object> bindings = new HashMap<>();
		bindings.put("root", reference);

		context.put(ScriptSchedulerExecutor.PARAM_SCRIPT, SCRIPT);
		context.put(ScriptSchedulerExecutor.PARAM_BINDINGS, (Serializable) bindings);
		executor.execute(context);

		verify(scriptEvaluator).eval(anyString(), eq(SCRIPT), anyMap());
		verify(converter, never()).convert(eq(ScriptInstance.class), any(Instance.class));
	}
}
