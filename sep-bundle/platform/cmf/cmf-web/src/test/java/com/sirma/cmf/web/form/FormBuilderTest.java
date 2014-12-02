package com.sirma.cmf.web.form;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputLabel;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.form.builder.FormBuilder;
import com.sirma.cmf.web.form.builder.SingleLineFieldBuilder;
import com.sirma.cmf.web.util.TestUtil;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;

/**
 * Test for form builder.
 * 
 * @author svelikov
 */
@Test
public class FormBuilderTest extends FormBuilderBaseTest {

	/**
	 * The class under test.
	 */
	private FormBuilder builder;

	/**
	 * Constructor.
	 */
	public FormBuilderTest() {
		builder = new FormBuilder(null, null) {

			@Override
			public void updateLabel(HtmlOutputLabel label) {

			}

			@Override
			public UIComponent buildField() {
				return null;
			}

			@Override
			public void updateField(UIComponent uiComponent) {
			}

			@Override
			public UIComponent getComponentInstance() {
				return null;
			}

			@Override
			public Class<?> getValueResultType() {
				return null;
			}

			@Override
			public void updateWrapper(UIComponent wrapper) {
			}

			@Override
			public void addAfterFieldContent(UIComponent wrapper) {
			}

		};

		ReflectionUtils.setField(builder, "builderHelper", builderHelper);
	}

	/**
	 * Test for method that builds a wrapper panel for the field and its label.
	 */
	public void buildFieldWrapperTest() {
		UIComponent uiComponent = builder.buildFieldWrapper();

		Assert.assertNotNull(uiComponent);

		String styleClass = (String) uiComponent.getAttributes().get("styleClass");
		assertEquals(styleClass, BuilderCssConstants.CMF_FIELD_WRAPPER);
	}

	/**
	 * Test for method that builds {@link HtmlOutputLabel}.
	 */
	public void buildLabelTest() {
		String label = "Name";
		String name = "nameField";

		// test if default values are applied properly if PropertyDefinition
		// that is provided doesn't have those fields
		HtmlOutputLabel outputLabel = (HtmlOutputLabel) builder.buildLabel();
		assertNotNull(outputLabel);

		String value = (String) outputLabel.getAttributes().get("value");
		assertEquals(value, "Missing label! Please check the definition!:");

		String forAttr = outputLabel.getFor();
		assertTrue(forAttr.startsWith("generatedFieldName_"));

		// set initialized PropertyDefinition and check if the created label
		// component is properly initialized
		builder.setPropertyDefinition(TestUtil.getFieldDefinition(label, name,
				DataTypeDefinition.TEXT, "protected", null));

		outputLabel = (HtmlOutputLabel) builder.buildLabel();
		assertNotNull(outputLabel);

		value = (String) outputLabel.getAttributes().get("value");
		assertEquals(value, label + ":");

		forAttr = outputLabel.getFor();
		assertEquals(forAttr, name);

		// check css class for generated label
		String styleClass = (String) outputLabel.getAttributes().get("styleClass");
		assertEquals(styleClass, BuilderCssConstants.CMF_DYNAMIC_FORM_LABEL);
	}

	/**
	 * Test for method that sets the disabled property on ui component.
	 */
	public void setDisabledTest() {
		SingleLineFieldBuilder singleLineFieldBuilder = new SingleLineFieldBuilder(null, null) {
			@Override
			protected ValueExpression createValueExpression(String ve, Class<?> valueType) {
				return null;
			}

			@Override
			protected String getIdForField(String propertyName) {
				return propertyName;
			}
		};
		ReflectionUtils.setField(singleLineFieldBuilder, "builderHelper", builderHelper);

		PropertyDefinition propertyDefinition = TestUtil.getFieldDefinition();
		singleLineFieldBuilder.setPropertyDefinition(propertyDefinition);
		UIComponent uiComponent = singleLineFieldBuilder.buildField();

		// setDisabled is called as result of buildField method execution

		Boolean isDisabled = (Boolean) uiComponent.getAttributes().get("disabled");
		assertEquals(isDisabled, Boolean.FALSE);
		String styleClass = (String) uiComponent.getAttributes().get("styleClass");
		assertEquals(styleClass, "form-control");

		// TODO: complete
	}

}
