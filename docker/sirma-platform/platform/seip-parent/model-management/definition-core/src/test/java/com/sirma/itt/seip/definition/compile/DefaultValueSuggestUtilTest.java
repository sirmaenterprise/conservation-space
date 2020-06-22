package com.sirma.itt.seip.definition.compile;

import org.junit.Test;

import com.sirma.itt.seip.domain.definition.ControlParam;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link DefaultValueSuggestUtil}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 31/08/2017
 */
public class DefaultValueSuggestUtilTest {

	@Test
	public void test_construct_function_one_expression() throws Exception {
		String expression = "text {${level1${level2${level3}}}|label}";
		List<ControlParam> controlParams = DefaultValueSuggestUtil.constructFunctionControlParams(expression);
		assertEquals(1, controlParams.size());
		assertControlParamData(controlParams.get(0), "label", "function");
		assertEquals("${level1${level2${level3}}}", controlParams.get(0).getValue());
	}

	@Test
	public void test_construct_function_multiple_expression() throws Exception {
		String expression = "text {${level1${level2${level3}}}|label} {${expression2}|label2}";
		List<ControlParam> controlParams = DefaultValueSuggestUtil.constructFunctionControlParams(expression);
		assertEquals(2, controlParams.size());
		assertControlParamData(controlParams.get(0), "label", "function");
		assertEquals("${level1${level2${level3}}}", controlParams.get(0).getValue());
		assertControlParamData(controlParams.get(1), "label2", "function");
		assertEquals("${expression2}", controlParams.get(1).getValue());
	}

	@Test
	public void test_constructPropertyBindings() throws Exception {
		String template = "text $[emf:property.linkedProperty] some other text $[emf:property]";
		List<ControlParam> controlParams = DefaultValueSuggestUtil.constructPropertyBindings(template);
		assertEquals(2, controlParams.size());
		assertControlParamData(controlParams.get(0), "emf:property.linkedProperty", "propertyNameBinding");
		assertControlParamData(controlParams.get(1), "emf:property", "propertyNameBinding");
	}

	private void assertControlParamData(ControlParam control, String name, String identifier) {
		assertEquals(name, control.getName());
		assertEquals(identifier, control.getIdentifier());
		assertEquals("default_value_pattern", control.getType());
	}

}