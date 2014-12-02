package com.sirma.cmf.web.form;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.form.builder.CheckboxFieldBuilder;
import com.sirma.cmf.web.util.TestUtil;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;

/**
 * Test for checkbox field builder.
 * 
 * @author svelikov
 */
@Test
public class CheckboxFieldBuilderTest extends FormBuilderBaseTest {

	private static final String CHECKBOX1 = "checkbox1";
	/**
	 * The class under test.
	 */
	private final CheckboxFieldBuilder builder;

	/**
	 * Constructor.
	 */
	public CheckboxFieldBuilderTest() {
		builder = new CheckboxFieldBuilder(null, null) {
			@Override
			protected ValueExpression createValueExpression(String ve, Class<?> valueType) {
				return null;
			}

			@Override
			protected String getId(PropertyDefinition propertyDefinition, String propertyName) {
				return propertyName;
			}
		};

		ReflectionUtils.setField(builder, "log", Logger.getLogger(builder.getClass()));
		ReflectionUtils.setField(builder, "builderHelper", builderHelper);
	}

	/**
	 * Test for getComponentInstance method.
	 */
	public void getComponentInstanceTest() {
		UIComponent componentInstance = builder.getComponentInstance();

		Assert.assertNotNull(componentInstance);
		Assert.assertEquals(componentInstance instanceof HtmlSelectBooleanCheckbox, true);
	}

	/**
	 * Builds the output field test.
	 */
	public void buildOutputFieldTest() {
		WritablePropertyDefinition fieldDefinition = TestUtil.getFieldDefinition();
		fieldDefinition.setName(CHECKBOX1);
		builder.setPropertyDefinition(fieldDefinition);

		HtmlSelectBooleanCheckbox outputField = (HtmlSelectBooleanCheckbox) builder
				.buildOutputField();
		Assert.assertNotNull(outputField);
		Assert.assertNotNull(outputField.getId());
		// this can't be tested this way
		// Assert.assertNotNull(outputField.getValueExpression("value"));
		Assert.assertEquals(outputField.isDisabled(), true);
		Assert.assertEquals(outputField.getAttributes().get("styleClass"),
				BuilderCssConstants.CMF_PREVIEW_FIELD);

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
