package com.sirma.cmf.web.form;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.validator.Validator;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.form.builder.FormBuilder;
import com.sirma.cmf.web.form.builder.SingleLineFieldBuilder;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.domain.Pair;

/**
 * The Class SingleLineFieldBuilderTest.
 * 
 * @author svelikov
 */
@Test
public class SingleLineFieldBuilderTest extends FormBuilderBaseTest {

	/** The builder. */
	private final SingleLineFieldBuilder builder;

	/**
	 * Instantiates a new single line field builder test.
	 */
	public SingleLineFieldBuilderTest() {
		super();

		builder = new SingleLineFieldBuilder(null, null) {
			@Override
			protected ValueExpression createValueExpression(String ve, Class<?> valueType) {
				return null;
			}
		};

		ReflectionUtils.setField(builder, "log", Logger.getLogger(builder.getClass()));
		ReflectionUtils.setField(builder, "regExGenerator", new RegExGenerator(null));
		ReflectionUtils.setField(builder, "builderHelper", builderHelper);
	}

	/**
	 * Test for getComponentInstance method.
	 */
	public void getComponentInstanceTest() {
		UIComponent componentInstance = builder.getComponentInstance();

		Assert.assertNotNull(componentInstance);
		Assert.assertEquals(componentInstance instanceof HtmlInputText, true);
	}

	/**
	 * Test for setFieldValidator method.
	 */
	public void setFieldValidatorTest() {

		String validatorType = "an..10";
		HtmlInputText componentInstance = (HtmlInputText) builder.getComponentInstance();
		builder.setFieldValidator(componentInstance, new Pair<String, String>(validatorType,
				"message"));

		// check if validator pattern is set up
		@SuppressWarnings("unchecked")
		Pair<String, String> validatorData = (Pair<String, String>) componentInstance
				.getAttributes().get(FormBuilder.VALIDATION_PATTERN);
		String expectedType = validatorData.getFirst();
		Assert.assertEquals(validatorType, expectedType);

		// check if field validator is set
		Validator validator = componentInstance.getValidators()[0];
		Assert.assertNotNull(validator);
	}

	// /**
	// * Test for method that builds a input field.
	// */
	// public void buildFieldTest() {
	// builder.setPropertyDefinition(TestUtil.getFieldDefinition("Name", null,
	// DataTypeDefinition.TEXT, "protected", null));
	//
	// UIComponent field = builder.buildField();
	// Assert.assertNotNull(field);
	//
	// // if name is not provided, the a generated one is applied
	// String name = field.getId();
	// Assert.assertTrue(name.startsWith("generatedFieldName_"));
	// String value = (String) field.getAttributes().get("value");
	// Assert.assertTrue(value.startsWith("generatedFieldName_"));
	//
	// //
	// builder.setPropertyDefinition(TestUtil.getFieldDefinition());
	// field = builder.buildField();
	// Assert.assertEquals(field.getId(), "nameField");
	// }

}
