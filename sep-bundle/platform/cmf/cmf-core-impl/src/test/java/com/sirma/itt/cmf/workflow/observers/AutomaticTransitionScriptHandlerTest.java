package com.sirma.itt.cmf.workflow.observers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskTransitionEvent;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowTransitionEvent;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlDefinitionImpl;
import com.sirma.itt.emf.definition.model.ControlParamImpl;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.script.InstanceToScriptNodeConverterProvider;
import com.sirma.itt.emf.script.ScriptEvaluator;
import com.sirma.itt.emf.script.ScriptException;
import com.sirma.itt.emf.script.ScriptNode;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * The Class AutomaticTransitionScriptHandlerTest.
 * 
 * @author BBonev
 */
@Test
public class AutomaticTransitionScriptHandlerTest extends CmfTest {

	/** The handler. */
	private AutomaticTransitionScriptHandler handler;

	/** The script evaluator. */
	private ScriptEvaluator scriptEvaluator;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		handler = new AutomaticTransitionScriptHandler();
		scriptEvaluator = Mockito.mock(ScriptEvaluator.class);
		TypeConverter typeConverter = createTypeConverter();
		ReflectionUtils.setField(handler, "scriptEvaluator", scriptEvaluator);
		ReflectionUtils.setField(handler, "typeConverter", typeConverter);
	}

	/**
	 * Test before event with valid data.
	 */
	@SuppressWarnings("unchecked")
	public void testBeforeEventWithValidData() {
		TransitionDefinition transitionDefinition = createTransition("2+2", "before");
		StandaloneTaskInstance instance = new StandaloneTaskInstance();
		instance.setId("emf:task");
		BeforeStandaloneTaskTransitionEvent event = new BeforeStandaloneTaskTransitionEvent(
				instance, transitionDefinition);
		handler.beforeStandaloneTaskTransition(event);

		Mockito.verify(scriptEvaluator, Mockito.atLeastOnce()).eval(Mockito.anyString(),
				Mockito.eq("2+2"), Mockito.any(Map.class));
	}

	/**
	 * Test before event with invalid data.
	 */
	@SuppressWarnings("unchecked")
	public void testBeforeEventWithInvalidData() {
		TransitionDefinition transitionDefinition = createTransition("2+2", "after");
		StandaloneTaskInstance instance = new StandaloneTaskInstance();
		instance.setId("emf:task");
		BeforeStandaloneTaskTransitionEvent event = new BeforeStandaloneTaskTransitionEvent(
				instance, transitionDefinition);
		handler.beforeStandaloneTaskTransition(event);

		Mockito.verify(scriptEvaluator, Mockito.never()).eval(Mockito.anyString(),
				Mockito.eq("2+2"), Mockito.any(Map.class));
	}

	/**
	 * Test after event with valid data.
	 */
	@SuppressWarnings("unchecked")
	public void testAfterEventWithValidData() {
		TransitionDefinition transitionDefinition = createTransition("2+2", "after");
		StandaloneTaskInstance instance = new StandaloneTaskInstance();
		instance.setId("emf:task");
		BeforeStandaloneTaskTransitionEvent event = new BeforeStandaloneTaskTransitionEvent(
				instance, transitionDefinition);
		handler.afterStandaloneTaskTransition(event.getNextPhaseEvent());

		Mockito.verify(scriptEvaluator, Mockito.atLeastOnce()).eval(Mockito.anyString(),
				Mockito.eq("2+2"), Mockito.any(Map.class));
	}

	/**
	 * Test after event with invalid data.
	 */
	@SuppressWarnings("unchecked")
	public void testAfterEventWithInvalidData() {
		TransitionDefinition transitionDefinition = createTransition("2+2", "before");
		StandaloneTaskInstance instance = new StandaloneTaskInstance();
		instance.setId("emf:task");
		BeforeStandaloneTaskTransitionEvent event = new BeforeStandaloneTaskTransitionEvent(
				instance, transitionDefinition);
		handler.afterStandaloneTaskTransition(event.getNextPhaseEvent());

		Mockito.verify(scriptEvaluator, Mockito.never()).eval(Mockito.anyString(),
				Mockito.eq("2+2"), Mockito.any(Map.class));
	}

	/**
	 * Test before event with valid data.
	 */
	@SuppressWarnings("unchecked")
	public void testBeforeEventWFTWithValidData() {
		TransitionDefinition transitionDefinition = createTransition("2+2", "before");
		WorkflowInstanceContext context = new WorkflowInstanceContext();
		context.setId("emf:workflow");
		TaskInstance instance = new TaskInstance();
		instance.setId("emf:task");
		instance.setContext(context);
		BeforeWorkflowTransitionEvent event = new BeforeWorkflowTransitionEvent(context, instance,
				transitionDefinition);
		handler.beforeTransition(event);

		Mockito.verify(scriptEvaluator, Mockito.atLeastOnce()).eval(Mockito.anyString(),
				Mockito.eq("2+2"), Mockito.any(Map.class));
	}

	/**
	 * Test before event with invalid data.
	 */
	@SuppressWarnings("unchecked")
	public void testBeforeEventWFTWithInvalidData() {
		TransitionDefinition transitionDefinition = createTransition("2+2", "after");
		WorkflowInstanceContext context = new WorkflowInstanceContext();
		context.setId("emf:workflow");
		TaskInstance instance = new TaskInstance();
		instance.setId("emf:task");
		instance.setContext(context);
		BeforeWorkflowTransitionEvent event = new BeforeWorkflowTransitionEvent(context, instance,
				transitionDefinition);
		handler.beforeTransition(event);

		Mockito.verify(scriptEvaluator, Mockito.never()).eval(Mockito.anyString(),
				Mockito.eq("2+2"), Mockito.any(Map.class));
	}

	/**
	 * Test after event with valid data.
	 */
	@SuppressWarnings("unchecked")
	public void testAfterEventWFTWithValidData() {
		TransitionDefinition transitionDefinition = createTransition("2+2", "after");
		WorkflowInstanceContext context = new WorkflowInstanceContext();
		context.setId("emf:workflow");
		TaskInstance instance = new TaskInstance();
		instance.setId("emf:task");
		instance.setContext(context);
		BeforeWorkflowTransitionEvent event = new BeforeWorkflowTransitionEvent(context, instance,
				transitionDefinition);
		handler.afterTransition(event.getNextPhaseEvent());

		Mockito.verify(scriptEvaluator, Mockito.atLeastOnce()).eval(Mockito.anyString(),
				Mockito.eq("2+2"), Mockito.any(Map.class));
	}

	/**
	 * Test after event with invalid data.
	 */
	@SuppressWarnings("unchecked")
	public void testAfterEventWFTWithInvalidData() {
		TransitionDefinition transitionDefinition = createTransition("2+2", "before");
		WorkflowInstanceContext context = new WorkflowInstanceContext();
		context.setId("emf:workflow");
		TaskInstance instance = new TaskInstance();
		instance.setId("emf:task");
		instance.setContext(context);
		BeforeWorkflowTransitionEvent event = new BeforeWorkflowTransitionEvent(context, instance,
				transitionDefinition);
		handler.afterTransition(event.getNextPhaseEvent());

		Mockito.verify(scriptEvaluator, Mockito.never()).eval(Mockito.anyString(),
				Mockito.eq("2+2"), Mockito.any(Map.class));
	}

	/**
	 * Test full config.
	 */
	@Test(enabled = false)
	public void testFullConfig() {
		TransitionDefinition transitionDefinition = createTransition("2+2", "after");
		WorkflowInstanceContext context = new WorkflowInstanceContext();
		context.setId("emf:workflow");
		TaskInstance instance = new TaskInstance();
		instance.setId("emf:task");
		instance.setContext(context);
		BeforeWorkflowTransitionEvent event = new BeforeWorkflowTransitionEvent(context, instance,
				transitionDefinition);
		handler.afterTransition(event.getNextPhaseEvent());

		Map<String, Object> binding = new HashMap<String, Object>();
		binding.put("root", createTypeConverter().convert(ScriptNode.class, context));

		Mockito.verify(scriptEvaluator, Mockito.atLeastOnce()).eval(Mockito.eq("javaScript"),
				Mockito.eq("2+2"), Mockito.eq(binding));
	}

	/**
	 * Test error evaluation.
	 */
	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = ScriptException.class)
	public void testErrorEvaluation() {
		TransitionDefinition transitionDefinition = createTransition(" {return 2+2; } ", "after");

		WorkflowInstanceContext context = new WorkflowInstanceContext();
		context.setId("emf:workflow");
		TaskInstance instance = new TaskInstance();
		instance.setId("emf:task");
		instance.setContext(context);
		BeforeWorkflowTransitionEvent event = new BeforeWorkflowTransitionEvent(context, instance,
				transitionDefinition);
		Mockito.when(
				scriptEvaluator.eval(Mockito.anyString(), Mockito.eq(" {return 2+2; } "),
						Mockito.anyMap())).thenThrow(ScriptException.class);

		handler.afterTransition(event.getNextPhaseEvent());
	}

	/**
	 * Test error evaluation with error suppression.
	 */
	@SuppressWarnings("unchecked")
	public void testErrorEvaluationWithErrorSuppression() {
		TransitionDefinition transitionDefinition = createTransition(" {return 2+2; } ", "after");

		ControlParamImpl param = new ControlParamImpl();
		param.setName("failOnError");
		param.setValue("false");
		transitionDefinition.getFields().get(0).getControlDefinition().getControlParams()
				.add(param);

		WorkflowInstanceContext context = new WorkflowInstanceContext();
		context.setId("emf:workflow");
		TaskInstance instance = new TaskInstance();
		instance.setId("emf:task");
		instance.setContext(context);
		BeforeWorkflowTransitionEvent event = new BeforeWorkflowTransitionEvent(context, instance,
				transitionDefinition);
		Mockito.when(
				scriptEvaluator.eval(Mockito.anyString(), Mockito.eq(" {return 2+2; } "),
						Mockito.anyMap())).thenThrow(ScriptException.class);

		handler.afterTransition(event.getNextPhaseEvent());
	}

	/**
	 * Creates the transition.
	 * 
	 * @param script
	 *            the script
	 * @param phase
	 *            the phase
	 * @return the transition definition
	 */
	private TransitionDefinition createTransition(String script, String phase) {
		TransitionDefinitionImpl transition = new TransitionDefinitionImpl();
		transition.setIdentifier("NEXT");
		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setTarget(new FieldDefinitionImpl());
		field.setValue(script);
		ControlDefinition controlDefinition = new ControlDefinitionImpl();
		controlDefinition.setIdentifier("SCRIPT");
		ControlParamImpl param = new ControlParamImpl();
		param.setName("phase");
		param.setValue(phase);
		controlDefinition.getControlParams().add(param);
		param = new ControlParamImpl();
		param.setName("language");
		param.setValue("javaScript");
		controlDefinition.getControlParams().add(param);

		field.setControlDefinition(controlDefinition);
		transition.getFields().add(field);
		return transition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TypeConverter createTypeConverter() {
		final TypeConverter converter = super.createTypeConverter();
		InstanceToScriptNodeConverterProvider nodeConverterProvider = new InstanceToScriptNodeConverterProvider();
		InstanceProxyMock<ScriptNode> proxyMock = new InstanceProxyMock<ScriptNode>(
				new ScriptNode()) {
			@Override
			public ScriptNode get() {
				ScriptNode node = new ScriptNode();
				ReflectionUtils.setField(node, "instanceService",
						Mockito.mock(InstanceService.class));
				ReflectionUtils.setField(node, "typeConverter", converter);
				ReflectionUtils.setField(node, "linkService", Mockito.mock(LinkService.class));
				return node;
			}

			@Override
			public Iterator<ScriptNode> iterator() {
				List<ScriptNode> list = new ArrayList<ScriptNode>(1);
				list.add(get());
				return list.iterator();
			}
		};
		ReflectionUtils.setField(nodeConverterProvider, "nodes", proxyMock);
		nodeConverterProvider.register(converter);
		return converter;
	}
}
