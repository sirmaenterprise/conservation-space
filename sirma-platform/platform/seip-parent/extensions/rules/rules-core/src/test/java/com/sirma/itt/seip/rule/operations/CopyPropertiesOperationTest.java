package com.sirma.itt.seip.rule.operations;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleOperation;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.rule.BaseRuleTest;

/**
 * The Class CopyPropertiesOperationTest.
 *
 * @author hlungov
 */
@Test
public class CopyPropertiesOperationTest extends BaseRuleTest {

	/** The instance service. */
	@Mock
	private InstanceService instanceService;

	/** The copy properties operation. */
	@InjectMocks
	private CopyPropertiesOperation copyPropertiesOperation;

	/** The Constant TEST_VALUE. */
	private static final String TEST_VALUE = "test";

	/** The Constant TEST_VALUE_2. */
	private static final String TEST_VALUE_2 = "test2";

	/** The Constant DEFAULT_SEPARATOR. */
	private static final String DEFAULT_SEPARATOR = ", ";

	/** The Constant TITLE_PROPERTY. */
	private static final String TITLE_PROPERTY = "title";

	/** The Constant PROPERTY_MAPPING. */
	private static final String PROPERTY_MAPPING = "propertyMapping";

	/**
	 * Configure test.
	 */
	public void configureTest() {
		copyPropertiesOperation.configure(configuration);
		Mockito.verify(configuration, times(0)).getIfSameType(RuleOperation.EVENT_ID, String.class);

		configuration.put(PROPERTY_MAPPING, Collections.emptyList());
		copyPropertiesOperation.configure(configuration);
		Mockito.verify(configuration, times(0)).getIfSameType(RuleOperation.EVENT_ID, String.class);

		configuration.put(PROPERTY_MAPPING, getPropertyMapping(false, "concatenate"));
		copyPropertiesOperation.configure(configuration);
		Mockito.verify(configuration, atLeastOnce()).getIfSameType(RuleOperation.EVENT_ID, String.class);

		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		copyPropertiesOperation.configure(configuration);
		Mockito.verify(configuration, atLeastOnce()).getIfSameType(RuleOperation.EVENT_ID, String.class);
	}

	/**
	 * Test processing started.
	 */
	public void testProcessingStarted() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		Mockito.doReturn(new HashMap<>()).when(documentInstance).getProperties();
		Context<String, Object> context = new Context<>();
		Context<String, Object> executionContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		copyPropertiesOperation.processingStarted(executionContext, context);
		Mockito.verify(instanceService).refresh(documentInstance);
	}

	/**
	 * Test processing ended.
	 */
	public void testProcessingEnded() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		Mockito.doReturn(new HashMap<>()).when(documentInstance).getProperties();
		Context<String, Object> context = new Context<>();
		Context<String, Object> executionContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		copyPropertiesOperation.processingEnded(executionContext, context);
		Mockito.verify(instanceService).save(documentInstance, Operation.NO_OPERATION);
	}

	/**
	 * Execute empty properties test.
	 */
	public void executeEmptyPropertiesTest() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		Mockito.doReturn(new HashMap<>()).when(objectInstance).getProperties();
		Mockito.doReturn(new HashMap<>()).when(documentInstance).getProperties();
		Context<String, Object> context = new Context<>();
		Context<String, Object> executionContext = buildRuleContext(documentInstance, previousVerDocInstance, null);
		copyPropertiesOperation.processingStarted(executionContext, context);
		copyPropertiesOperation.execute(executionContext, objectInstance, context);
		copyPropertiesOperation.processingEnded(executionContext, context);
		Mockito.verify(instanceService).save(documentInstance, Operation.NO_OPERATION);
	}

	/**
	 * Execute diff properties test_no duplicate.
	 */
	public void executeDiffPropertiesTest_string_noDuplicate() {
		StringBuilder result = new StringBuilder(
				TEST_VALUE.length() + DEFAULT_SEPARATOR.length() + TEST_VALUE_2.length());
		result.append(TEST_VALUE).append(DEFAULT_SEPARATOR).append(TEST_VALUE_2);
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, TEST_VALUE_2);
		Map<String, Serializable> docProperties = new HashMap<>(1);
		docProperties.put(TITLE_PROPERTY, TEST_VALUE);
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		Assert.assertTrue(docProperties.get(TITLE_PROPERTY).toString().equals(result.toString()));
	}

	/**
	 * Execute diff properties test_with duplicate.
	 */
	public void executeDiffPropertiesTest_string_withDuplicate() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, TEST_VALUE);
		Map<String, Serializable> docProperties = new HashMap<>(1);
		docProperties.put(TITLE_PROPERTY, TEST_VALUE);
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		Assert.assertTrue(docProperties.get(TITLE_PROPERTY).toString().equals(TEST_VALUE));
	}

	/**
	 * Execute diff properties test_string_with duplicate_with extra.
	 */
	public void executeDiffPropertiesTest_string_withDuplicate_withExtra() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, TEST_VALUE);
		Map<String, Serializable> docProperties = new HashMap<>(1);
		docProperties.put(TITLE_PROPERTY, TEST_VALUE + 2);
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		Assert.assertTrue(docProperties.get(TITLE_PROPERTY).toString().equals(TEST_VALUE + "2, " + TEST_VALUE));
	}

	/**
	 * Execute diff properties test_with duplicate.
	 */
	public void executeDiffPropertiesTest_collection_withDuplicate() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, TEST_VALUE);
		Map<String, Serializable> docProperties = new HashMap<>(1);
		docProperties.put(TITLE_PROPERTY, new LinkedList<>(Collections.singletonList(TEST_VALUE)));
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		Assert.assertTrue(docProperties.get(TITLE_PROPERTY).equals(Collections.singletonList(TEST_VALUE)));
	}

	/**
	 * Execute diff properties test_collection_no duplicate.
	 */
	public void executeDiffPropertiesTest_collection_noDuplicate() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, TEST_VALUE_2);
		Map<String, Serializable> docProperties = new HashMap<>(1);
		docProperties.put(TITLE_PROPERTY, new LinkedList<>(Collections.singletonList(TEST_VALUE)));
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		Assert.assertTrue(docProperties.get(TITLE_PROPERTY).equals(Arrays.asList(TEST_VALUE, TEST_VALUE_2)));
	}

	/**
	 * Execute diff properties test_collections_with duplicate.
	 */
	public void executeDiffPropertiesTest_collections_withDuplicate() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, new LinkedList<>(Collections.singletonList(TEST_VALUE)));
		Map<String, Serializable> docProperties = new HashMap<>(1);
		docProperties.put(TITLE_PROPERTY, new LinkedList<>(Collections.singletonList(TEST_VALUE)));
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		Assert.assertTrue(docProperties.get(TITLE_PROPERTY).equals(Collections.singletonList(TEST_VALUE)));
	}

	/**
	 * Execute diff properties test_collections_no duplicate.
	 */
	public void executeDiffPropertiesTest_collections_noDuplicate() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "concatenate"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, new LinkedList<>(Collections.singletonList(TEST_VALUE_2)));
		Map<String, Serializable> docProperties = new HashMap<>(1);
		docProperties.put(TITLE_PROPERTY, new LinkedList<>(Collections.singletonList(TEST_VALUE)));
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		Assert.assertTrue(docProperties.get(TITLE_PROPERTY).equals(Arrays.asList(TEST_VALUE, TEST_VALUE_2)));
	}

	/**
	 * Execute diff properties overrride test.
	 */
	public void executeDiffPropertiesOverrideTest() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "override"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, TEST_VALUE + " add");
		Map<String, Serializable> docProperties = new HashMap<>(1);
		docProperties.put(TITLE_PROPERTY, TEST_VALUE);
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		Assert.assertTrue(docProperties.get(TITLE_PROPERTY).toString().equals(TEST_VALUE + " add"));
	}

	/**
	 * Execute diff properties skip test.
	 */
	public void executeDiffPropertiesSkipTest_present() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "skip"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, TEST_VALUE + " add");
		Map<String, Serializable> docProperties = new HashMap<>(1);
		docProperties.put(TITLE_PROPERTY, TEST_VALUE);
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		assertEquals(docProperties.get(TITLE_PROPERTY).toString(), TEST_VALUE);
	}

	/**
	 * Execute diff properties skip test.
	 */
	public void executeDiffPropertiesSkipTest_notPresent() {
		configuration.clear();
		configuration.put(PROPERTY_MAPPING, getPropertyMapping(true, "skip"));
		copyPropertiesOperation.configure(configuration);
		Map<String, Serializable> objProperties = new HashMap<>(1);
		objProperties.put(TITLE_PROPERTY, TEST_VALUE);
		Map<String, Serializable> docProperties = new HashMap<>(1);
		Mockito.doReturn(objProperties).when(objectInstance).getProperties();
		Mockito.doReturn(docProperties).when(documentInstance).getProperties();
		copyPropertiesOperation.execute(buildRuleContext(documentInstance, previousVerDocInstance, null),
				objectInstance, null);
		assertNotNull(docProperties.get(TITLE_PROPERTY));
		assertEquals(docProperties.get(TITLE_PROPERTY).toString(), TEST_VALUE);
	}

	/**
	 * Gets the property mapping.
	 *
	 * @param withOnDuplicateConfig
	 *            the with on duplicate config
	 * @param operation
	 *            the operation
	 * @return the property mapping
	 */
	private List<Map<String, Object>> getPropertyMapping(boolean withOnDuplicateConfig, String operation) {
		List<Map<String, Object>> propertyMapping = new ArrayList<>(1);
		Map<String, Object> mapping = new HashMap<>(3);
		mapping.put("from", TITLE_PROPERTY);
		mapping.put("to", TITLE_PROPERTY);
		if (withOnDuplicateConfig) {
			Map<String, String> onDupblicate = new HashMap<>(2);
			onDupblicate.put("operation", operation);
			onDupblicate.put("separator", ", ");
			mapping.put("onDuplicate", onDupblicate);
		}
		propertyMapping.add(mapping);
		return propertyMapping;
	}
}
