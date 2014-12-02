package com.sirma.cmf.web.form.builder;

import java.util.LinkedHashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.util.TestUtil;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * The Class SelectOneMenuBuilderTest.
 * 
 * @author svelikov
 */
@Test
public class SelectOneMenuBuilderTest {

	/** The builder. */
	private SelectOneMenuBuilder builder;

	/**
	 * Instantiates a new select one menu builder test.
	 */
	public SelectOneMenuBuilderTest() {
		builder = new SelectOneMenuBuilder(null, null);
	}

	/**
	 * Creates the value expression string test.
	 */
	public void createValueExpressionStringTest() {

		// check with one filter
		ReflectionUtils.setField(builder, "propertyDefinition", TestUtil.getFieldDefinition());
		String veString = builder.createValueExpressionString(226);
		String expected = "#{cls.getFilteredCodeValues(226, formUtil.toArray('filter1')).keySet()}";
		Assert.assertEquals(veString, expected);

		// check with two filters
		Set<String> filters = new LinkedHashSet<String>();
		filters.add("filter1");
		filters.add("filter2");
		ReflectionUtils.setField(builder, "propertyDefinition",
				TestUtil.getFieldDefinition("label", "name", "type", "displayType", filters));
		veString = builder.createValueExpressionString(226);
		expected = "#{cls.getFilteredCodeValues(226, formUtil.toArray('filter1','filter2')).keySet()}";
		Assert.assertEquals(veString, expected);

		// check with no filters
		filters = new LinkedHashSet<String>();
		ReflectionUtils.setField(builder, "propertyDefinition",
				TestUtil.getFieldDefinition("label", "name", "type", "displayType", filters));
		veString = builder.createValueExpressionString(226);
		expected = "#{cls.getFilteredCodeValues(226, formUtil.toArray('')).keySet()}";
		Assert.assertEquals(veString, expected);
	}

}
