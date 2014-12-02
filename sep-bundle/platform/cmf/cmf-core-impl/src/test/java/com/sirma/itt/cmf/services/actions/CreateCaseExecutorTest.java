package com.sirma.itt.cmf.services.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.executors.ExecutableOperationProperties;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * The Class CreateCaseExecutorTest.
 * 
 * @author BBonev
 */
@Test
public class CreateCaseExecutorTest extends CmfTest {

	/** The create case. */
	private String createCase = "{" + " operation: \"createCase\","
			+ " definition: \"someCaseDefinition\"," + " revision: 2," + " id: \"emf:caseId\","
			+ " type: \"caseinstance\"," + " parentId: \"emf:projectId\","
			+ " parentType: \"projectinstance\","
			+ " properties : {"
			+ " 	property1: \"some property value 1\"," + " 	property2: \"true\","
			+ " 	property3: \"2323\"" + " }" + " }";

	/**
	 * Parses the request test.
	 */
	public void parseRequestTest() {

		TypeConverter typeConverter = createTypeConverter();

		CreateCaseExecutor executor = new CreateCaseExecutor();
		ReflectionUtils.setField(executor, "typeConverter", typeConverter);

		JSONObject data = JsonUtil.createObjectFromString(createCase);
		SchedulerContext context = executor.parseRequest(data);

		Assert.assertEquals(context.get(ExecutableOperationProperties.DEFINITION),
				"someCaseDefinition");
		Assert.assertEquals(context.get(ExecutableOperationProperties.REVISION), 2L);
		Assert.assertNotNull(context.getIfSameType(ExecutableOperationProperties.CTX_TARGET,
				InstanceReference.class));
		Assert.assertEquals(
				context.getIfSameType(ExecutableOperationProperties.CTX_TARGET,
						InstanceReference.class).getIdentifier(), "emf:caseId");
		Assert.assertEquals(
				context.getIfSameType(ExecutableOperationProperties.CTX_TARGET,
						InstanceReference.class).getReferenceType().getJavaClassName(),
				CaseInstance.class.getName());

		Assert.assertNotNull(context.getIfSameType(ExecutableOperationProperties.CTX_PARENT,
				InstanceReference.class));
		Assert.assertEquals(
				context.getIfSameType(ExecutableOperationProperties.CTX_PARENT,
						InstanceReference.class).getIdentifier(), "emf:projectId");
		Assert.assertEquals(
				context.getIfSameType(ExecutableOperationProperties.CTX_PARENT,
						InstanceReference.class).getReferenceType().getJavaClassName(),
				"com.sirma.itt.pm.domain.model.ProjectInstance");

		Assert.assertNotNull(context.getIfSameType(ExecutableOperationProperties.PROPERTIES,
				Map.class));
		Assert.assertTrue(context
				.getIfSameType(ExecutableOperationProperties.PROPERTIES, Map.class).containsKey(
						"property1"));
		Assert.assertTrue(context
				.getIfSameType(ExecutableOperationProperties.PROPERTIES, Map.class).containsKey(
						"property2"));
		Assert.assertTrue(context
				.getIfSameType(ExecutableOperationProperties.PROPERTIES, Map.class).containsKey(
						"property3"));

	}

	/**
	 * Test execution new instance.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(dependsOnMethods = "parseRequestTest")
	public void testExecutionNewInstance() {
		TypeConverter typeConverter = createTypeConverter();
		CaseService caseService = Mockito.mock(CaseService.class);
		PropertiesService propertiesService = Mockito.mock(PropertiesService.class);
		DictionaryService dictionaryService = Mockito.mock(DictionaryService.class);
		InstanceService instanceService = Mockito.mock(InstanceService.class);
		// ServiceRegister serviceRegister = Mockito.mock(ServiceRegister.class);

		CreateCaseExecutor executor = new CreateCaseExecutor();
		ReflectionUtils.setField(executor, "typeConverter", typeConverter);
		ReflectionUtils.setField(executor, "caseService", caseService);
		ReflectionUtils.setField(executor, "propertiesService", propertiesService);
		ReflectionUtils.setField(executor, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(executor, "instanceService", instanceService);
		// ReflectionUtils.setField(executor, "serviceRegister", serviceRegister);

		JSONObject data = JsonUtil.createObjectFromString(createCase);
		SchedulerContext context = executor.parseRequest(data);

		CaseInstance instance = new CaseInstance();
		instance.setIdentifier("someCaseDefinition");
		instance.setRevision(2L);
		instance.setId("emf:caseId");
		instance.setProperties(new HashMap<String, Serializable>());
		instance.setSections(new ArrayList<SectionInstance>(1));
		// register instance for not persisted
		SequenceEntityGenerator.register(instance);

		Operation operation = new Operation(executor.getOperation());

		Mockito.when(
				instanceService.createInstance(Mockito.any(DefinitionModel.class),
						Mockito.any(Instance.class))).thenReturn(instance);

		Mockito.when(instanceService.save(instance, operation)).thenAnswer(new Answer<Instance>() {

			@Override
			public Instance answer(InvocationOnMock invocation) throws Throwable {
				Object object = invocation.getArguments()[0];
				((DMSInstance) object).setDmsId("dmsId");
				return (Instance) object;
			}
		});

		executor.execute(context);
		Mockito.verify(dictionaryService, Mockito.atLeastOnce()).getDefinition(
				Mockito.any(Class.class), Mockito.eq("someCaseDefinition"), Mockito.eq(2L));
		Mockito.verify(propertiesService, Mockito.atLeastOnce()).convertToInternalModel(Mockito.anyMap(),
				Mockito.any(DefinitionModel.class));
		instance.setDmsId("dmsId");
		Mockito.verify(instanceService).save(instance, operation);
		Assert.assertEquals(context.get(ExecutableOperationProperties.DMS_ID), "dmsId");

		// no new instance no restore point
		Assert.assertNull(context.get(ExecutableOperationProperties.CTX_ROLLBACK));
	}

	/**
	 * Test rollback on new instance.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test(dependsOnMethods = "testExecutionNewInstance")
	public void testRollbackOnNewInstance() {
		TypeConverter typeConverter = createTypeConverter();
		CaseService caseService = Mockito.mock(CaseService.class);
		PropertiesService propertiesService = Mockito.mock(PropertiesService.class);
		DictionaryService dictionaryService = Mockito.mock(DictionaryService.class);
		InstanceService instanceService = Mockito.mock(InstanceService.class);
		// ServiceRegister serviceRegister = Mockito.mock(ServiceRegister.class);

		CreateCaseExecutor executor = new CreateCaseExecutor();
		ReflectionUtils.setField(executor, "typeConverter", typeConverter);
		ReflectionUtils.setField(executor, "caseService", caseService);
		ReflectionUtils.setField(executor, "propertiesService", propertiesService);
		ReflectionUtils.setField(executor, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(executor, "instanceService", instanceService);
		// ReflectionUtils.setField(executor, "serviceRegister", serviceRegister);

		JSONObject data = JsonUtil.createObjectFromString(createCase);
		SchedulerContext context = executor.parseRequest(data);

		CaseInstance instance = new CaseInstance();
		instance.setIdentifier("someCaseDefinition");
		instance.setRevision(2L);
		instance.setId("emf:caseId");
		instance.setProperties(new HashMap<String, Serializable>());
		instance.setSections(new ArrayList<SectionInstance>(1));

		Mockito.when(
				instanceService.createInstance(Mockito.any(DefinitionModel.class),
						Mockito.any(Instance.class))).thenReturn(instance);

		Operation operation = new Operation(executor.getOperation());

		InstanceReference reference = context.getIfSameType(
				ExecutableOperationProperties.CTX_TARGET, InstanceReference.class);
		Assert.assertNotNull(reference);
		ReflectionUtils.setField(reference, "instance", instance);

		Mockito.when(instanceService.save(instance, operation)).thenAnswer(new Answer<Instance>() {

			@Override
			public Instance answer(InvocationOnMock invocation) throws Throwable {
				Object object = invocation.getArguments()[0];
				((DMSInstance) object).setDmsId("dmsId");
				return (Instance) object;
			}
		});

		executor.execute(context);
		Mockito.verify(dictionaryService, Mockito.atLeastOnce()).getInstanceDefinition(
				Mockito.any(CaseInstance.class));
		Mockito.verify(propertiesService, Mockito.atLeastOnce()).convertToInternalModel(Mockito.anyMap(),
				Mockito.any(DefinitionModel.class));
		instance.setDmsId("dmsId");
		Mockito.verify(instanceService).save(instance, operation);
		Assert.assertEquals(context.get(ExecutableOperationProperties.DMS_ID), "dmsId");

		// no new instance no restore point
		Assert.assertNotNull(context.get(ExecutableOperationProperties.CTX_ROLLBACK));

		boolean rollback = executor.rollback(context);
		Assert.assertTrue(rollback);

		Mockito.verify(caseService).delete(instance, new Operation(ActionTypeConstants.DELETE),
				true);

	}

}
