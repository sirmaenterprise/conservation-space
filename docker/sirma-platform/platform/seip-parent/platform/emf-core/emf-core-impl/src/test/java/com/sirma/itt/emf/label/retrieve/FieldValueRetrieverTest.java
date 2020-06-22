package com.sirma.itt.emf.label.retrieve;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test the {@link FieldValueRetrieverService}.
 *
 * @author nvelkov
 */
@Test
public class FieldValueRetrieverTest {

	private FieldValueRetrieverTestHelper testHelper = new FieldValueRetrieverTestHelper();
	private FieldValueRetrieverServiceImpl fieldValueRetrieverService;

	/**
	 * Re-initializes the {@link FieldValueRetrieverService}.
	 */
	@BeforeMethod
	public void beforeMethod() {
		fieldValueRetrieverService = new FieldValueRetrieverServiceImpl();
	}

	/**
	 * Test the username retriever. In this scenario the username is passed and the user display name is succesfully
	 * returned.
	 */
	@Test
	public void testUsernameRetrieverSuccess() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockUsernameFieldValueRetrieverInstance(true));
		String user = fieldValueRetrieverService.getLabel(FieldId.USERNAME, "something");
		Assert.assertEquals(user, "mockedUser");
	}

	/**
	 * Test the username retriever when a user display name has not been found. In that case the retriever should return
	 * the original value.
	 */
	@Test
	public void testUsernameRetrieverNotFound() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockUsernameFieldValueRetrieverInstance(false));
		String user = fieldValueRetrieverService.getLabel(FieldId.USERNAME, "something");
		Assert.assertEquals(user, "something");
	}

	/**
	 * Test the username retriever when a null has been passed to the retriever . In that case the retriever should
	 * return null.
	 */
	@Test
	public void testUsernameRetrieverNull() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockUsernameFieldValueRetrieverInstance(false));
		String user = fieldValueRetrieverService.getLabel(FieldId.USERNAME, null);
		Assert.assertEquals(user, null);
	}

	/**
	 * Test username values retriever when no filter is applied.
	 */
	@Test
	public void testUsernameValuesRetrieverNoFilter() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockUsernameFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.USERNAME, null, 0, 10);
		Assert.assertEquals(values.getTotal(), new Long(20));
		Assert.assertEquals(values.getResults().size(), 10);
		Assert.assertEquals(values.getResults().get(0).getFirst(), "0");
		Assert.assertEquals(values.getResults().get(0).getSecond(), "User 0");
	}

	/**
	 * Test username values retriever with applied filter. Filter must work with starts with comparison and should be
	 * case insenstive.
	 */
	@Test
	public void testUsernameValuesRetrieverWithFilter() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockUsernameFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.USERNAME, "uSeR", 0, 10);
		Assert.assertEquals(values.getTotal(), new Long(10));
		Assert.assertEquals(values.getResults().size(), 10);
		for (Pair<String, String> value : values.getResults()) {
			Assert.assertTrue(value.getSecond().toLowerCase().startsWith("user"));
		}
	}

	/**
	 * Test username values retriever paging. Testing that offset and limit parameters are properly applied.
	 */
	@Test
	public void testUsernameValuesRetrieverPaging() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockUsernameFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.USERNAME, null, 10, 5);
		Assert.assertEquals(values.getTotal(), new Long(20));
		Assert.assertEquals(values.getResults().size(), 5);
		Assert.assertEquals(values.getResults().get(0).getFirst(), "5");
		Assert.assertEquals(values.getResults().get(0).getSecond(), "User 5");
	}

	/**
	 * Test username values retriever with null values for parameters to ensure that method works with default values.
	 */
	@Test
	public void testUsernameValuesRetrieverNullParams() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockUsernameFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.USERNAME, null, null, null);
		Assert.assertEquals(values.getTotal(), new Long(20));
		Assert.assertEquals(values.getResults().size(), 20);
	}

	/**
	 * Test the object type retriever. In this scenario the object type is passed and the object type label is
	 * succesfully returned.
	 */
	@Test
	public void testObjectTypeRetrieverSuccess() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectTypeFieldValueRetrieverInstance(true));
		String user = fieldValueRetrieverService.getLabel(FieldId.OBJECT_TYPE, "something");
		Assert.assertEquals(user, "mockedLabel");
	}

	/**
	 * Test the object type retriever when an object type label has not been found. In that case the retriever should
	 * return the original value.
	 */
	@Test
	public void testObjectTypeRetrieverNotFound() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectTypeFieldValueRetrieverInstance(false));
		String user = fieldValueRetrieverService.getLabel(FieldId.OBJECT_TYPE, "something");
		Assert.assertEquals(user, "something");
	}

	/**
	 * Test the object type retriever when a null has been passed to the retriever . In that case the retriever should
	 * return null.
	 */
	@Test
	public void testObjectTypeRetrieverNull() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectTypeFieldValueRetrieverInstance(false));
		String user = fieldValueRetrieverService.getLabel(FieldId.OBJECT_TYPE, null);
		Assert.assertEquals(user, null);
	}

	/**
	 * Test object type values retriever when no filter is applied.
	 */
	@Test
	public void testObjectTypeValuesRetrieverNoFilter() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectTypeFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.OBJECT_TYPE, null, 0, 10);
		Assert.assertEquals(values.getTotal(), new Long(20));
		Assert.assertEquals(values.getResults().size(), 10);
		Assert.assertEquals(values.getResults().get(0).getFirst(), "instance:0");
		Assert.assertEquals(values.getResults().get(0).getSecond(), "Object type 0");
	}

	/**
	 * Test object type values retriever with applied filter. Filter must work with starts with comparison and should be
	 * case insenstive.
	 */
	@Test
	public void testObjectTypeValuesRetrieverWithFilter() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectTypeFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.OBJECT_TYPE, "Object Type 1", 0, 10);
		Assert.assertEquals(values.getTotal(), new Long(11));
		Assert.assertEquals(values.getResults().size(), 10);
		for (Pair<String, String> value : values.getResults()) {
			Assert.assertTrue(value.getSecond().toLowerCase().startsWith("object type 1"));
		}
	}

	/**
	 * Test object type values retriever paging. Testing that offset and limit parameters are properly applied.
	 */
	@Test
	public void testObjectTypeValuesRetrieverPaging() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectTypeFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.OBJECT_TYPE, null, 10, 5);
		Assert.assertEquals(values.getTotal(), new Long(20));
		Assert.assertEquals(values.getResults().size(), 5);
		Assert.assertEquals(values.getResults().get(0).getFirst(), "instance:10");
		Assert.assertEquals(values.getResults().get(0).getSecond(), "Object type 10");
	}

	/**
	 * Test object type values retriever with null values for parameters to ensure that method works with default
	 * values.
	 */
	@Test
	public void testObjectTypeValuesRetrieverNullParams() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectTypeFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.OBJECT_TYPE, null, null, null);
		Assert.assertEquals(values.getTotal(), new Long(20));
		Assert.assertEquals(values.getResults().size(), 20);
	}

	/**
	 * Test action values retriever. In this scenario the action label is succesfully found and retrieved.
	 */
	@Test
	public void testActionValuesRetriever() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockActionFieldValueRetrieverInstance(true));
		String label = fieldValueRetrieverService.getLabel(FieldId.ACTION_ID, "something");
		Assert.assertEquals(label, "mockedLabel (Task)");
	}

	/**
	 * Test action values retriever when the action label is not found so the original value is returned.
	 */
	@Test
	public void testActionValuesRetrieverNotFound() {

		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockActionFieldValueRetrieverInstance(false));
		String label = fieldValueRetrieverService.getLabel(FieldId.ACTION_ID, "something");
		Assert.assertEquals(label, "something");
	}

	/**
	 * Test action values retriever with applied filter. Filter must work with starts with comparison and should be case
	 * insenstive.
	 */
	@Test
	public void testActionValuesRetrieverWithFilter() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockActionFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.ACTION_ID, "Label 1", 0, 10);
		Assert.assertEquals(values.getTotal(), new Long(22));
		Assert.assertEquals(values.getResults().size(), 10);
		for (Pair<String, String> value : values.getResults()) {
			Assert.assertTrue(value.getSecond().toLowerCase().startsWith("label 1"));
		}
	}

	/**
	 * Test action values retriever paging. Testing that offset and limit parameters are properly applied.
	 */
	// JEE7
	@Test
	public void testActionValuesRetrieverPaging() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockActionFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.ACTION_ID, null, 10, 5);
		Assert.assertEquals(values.getTotal(), new Long(40));
		Assert.assertEquals(values.getResults().size(), 5);
		Assert.assertEquals(values.getResults().get(0).getFirst(), "cv10");
		Assert.assertEquals(values.getResults().get(0).getSecond(), "Label 10");
	}

	/**
	 * Test action values retriever with null values for parameters to ensure that method works with default values.
	 */
	@Test
	public void testActionValuesRetrieverNullParams() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockActionFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.ACTION_ID, null, null, null);
		Assert.assertEquals(values.getTotal(), new Long(40));
		Assert.assertEquals(values.getResults().size(), 40);
	}

	/**
	 * Test object state retriever. In this scenario the label is retrieved succesfully from the mocked codelist.
	 */
	@Test
	public void testObjectStateRetriever() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectStateFieldValueRetrieverInstance("someInstance", true, true));
		String label = fieldValueRetrieverService.getLabel(FieldId.OBJECT_STATE, "something",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE,
						"somethin"));
		Assert.assertEquals(label, "mockedLabel");
	}

	/**
	 * Test domain object state retriever. In this scenario the label is retriever succesfully from the mocked codelist.
	 */
	@Test
	public void testDomainObjectStateRetriever() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectStateFieldValueRetrieverInstance(ObjectInstance.class.getName(), true, true));
		String label = fieldValueRetrieverService.getLabel(FieldId.OBJECT_STATE, "something",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE,
						"somethin"));
		Assert.assertEquals(label, "mockedLabel");
	}

	/**
	 * Test object state retriever when the codelist is not found.
	 */
	@Test
	public void testObjectStateRetrieverNotFound() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectStateFieldValueRetrieverInstance(ObjectInstance.class.getName(), false, true));
		String label = fieldValueRetrieverService.getLabel(FieldId.OBJECT_STATE, "something",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE,
						"somethin"));
		Assert.assertEquals(label, "something");
	}

	/**
	 * Test object state retriever when the codevalue is not found.
	 */
	@Test
	public void testObjectStateRetrieverCodeListNotFound() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectStateFieldValueRetrieverInstance(ObjectInstance.class.getName(), true, false));
		String label = fieldValueRetrieverService.getLabel(FieldId.OBJECT_STATE, "something",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE,
						"somethin"));
		Assert.assertEquals(label, "something");
	}

	/**
	 * Test object state values retriever when no filter is applied.
	 */
	@Test
	public void testObjectStateValuesRetrieverNoFilter() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectStateFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.OBJECT_STATE, null, 0, 10);
		Assert.assertEquals(values.getTotal(), new Long(9));
		Assert.assertEquals(values.getResults().size(), 9);
		Assert.assertEquals(values.getResults().get(0).getFirst(), "cv0");
		Assert.assertEquals(values.getResults().get(0).getSecond(), "Label 0");
	}

	/**
	 * Test object state values retriever with applied filter. Filter must work with starts with comparison and should
	 * be case insenstive.
	 */
	@Test
	public void testObjectStateValuesRetrieverWithFilter() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectStateFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.OBJECT_STATE, "Label 1", 0, 10);
		Assert.assertEquals(values.getTotal(), new Long(3));
		Assert.assertEquals(values.getResults().size(), 3);
		for (Pair<String, String> value : values.getResults()) {
			Assert.assertTrue(value.getSecond().toLowerCase().startsWith("label 1"));
		}
	}

	/**
	 * Test object state values retriever paging. Testing that offset and limit parameters are properly applied.
	 */
	@Test
	public void testObjectStateValuesRetrieverPaging() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectStateFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.OBJECT_STATE, null, 5, 5);
		Assert.assertEquals(values.getTotal(), new Long(9));
		Assert.assertEquals(values.getResults().size(), 4);
		Assert.assertEquals(values.getResults().get(0).getFirst(), "cv11");
		Assert.assertEquals(values.getResults().get(0).getSecond(), "Label 11");
	}

	/**
	 * Test object state values retriever with null values for parameters to ensure that method works with default
	 * values.
	 */
	@Test
	public void testObjectStateValuesRetrieverNullParams() {
		ReflectionUtils.setFieldValue(fieldValueRetrieverService, "extensionMapping",
				testHelper.mockObjectStateFieldValuesRetrieverInstance());
		RetrieveResponse values = fieldValueRetrieverService.getValues(FieldId.OBJECT_STATE, null, null, null);
		Assert.assertEquals(values.getTotal(), new Long(9));
		Assert.assertEquals(values.getResults().size(), 9);
	}

}
