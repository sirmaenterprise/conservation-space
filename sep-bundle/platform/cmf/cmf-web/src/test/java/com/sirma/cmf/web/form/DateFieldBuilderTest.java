package com.sirma.cmf.web.form;

import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlPanelGroup;

import org.apache.log4j.Logger;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.form.builder.DateFieldBuilder;
import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.cmf.web.util.TestUtil;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.web.util.DateUtil;

/**
 * Test for date field builder.
 * 
 * @author svelikov
 */
@Test
public class DateFieldBuilderTest extends FormBuilderBaseTest {

	/** The Constant CONVERTER_MESSAGE. */
	private static final String CONVERTER_MESSAGE = LabelConstants.MSG_ERROR_WRONG_DATE_FORMAT;

	/** Label for date picker field. */
	private static final String DATE_LABEL = "DATE_LABEL";

	/** Date format pattern. */
	private static final String dateFormat = "MM/dd/yy";

	/**
	 * The class under test.
	 */
	private final DateFieldBuilder builder;

	/** The label provider. */
	private LabelProvider labelProvider;

	/** The date util. */
	private DateUtil dateUtil;

	/**
	 * Constructor.
	 */
	public DateFieldBuilderTest() {
		labelProvider = Mockito.mock(LabelProvider.class);
		dateUtil = Mockito.mock(DateUtil.class);
		builder = new DateFieldBuilder(labelProvider, null, dateUtil) {
			@Override
			protected ValueExpression createValueExpression(String ve, Class<?> valueType) {
				return null;
			}

			@Override
			protected String getDateConverterMessage() {
				return CONVERTER_MESSAGE;
			}
		};
		ReflectionUtils.setField(builder, "log", Logger.getLogger(builder.getClass()));
		ReflectionUtils.setField(builder, "builderHelper", builderHelper);
		Mockito.when(labelProvider.getValue(LabelConstants.DATEPICKER_DATEFORMAT_HINT)).thenReturn(
				DATE_LABEL);
		Mockito.when(dateUtil.getConverterDateFormatPattern()).thenReturn(dateFormat);
		Mockito.when(dateUtil.getConverterDatetimeFormatPattern()).thenReturn(dateFormat);
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
	 * Update field test.
	 */
	public void updateFieldTest() {
		WritablePropertyDefinition fieldDefinition = TestUtil.getFieldDefinition();
		builder.setPropertyDefinition(fieldDefinition);
		DataType datatype = new DataType();
		fieldDefinition.setDataType(datatype);
		UIInput dateField = null;

		// check for date type
		datatype.setName(DataTypeDefinition.DATE);
		dateField = (UIInput) builder.getComponentInstance();
		builder.updateField(dateField);
		String styleClass = (String) dateField.getAttributes().get("styleClass");
		Assert.assertEquals(styleClass, "nameField form-control cmf-date-field");

		// check for datetime type
		datatype.setName(DataTypeDefinition.DATETIME);
		dateField = (UIInput) builder.getComponentInstance();
		builder.updateField(dateField);
		styleClass = (String) dateField.getAttributes().get("styleClass");
		Assert.assertEquals(styleClass, "nameField form-control cmf-datetime-field");

		// check for not date ot datetime type

		// check the rest of fields
		Assert.assertEquals(dateField.getConverterMessage(), CONVERTER_MESSAGE);
		Assert.assertEquals(dateField.getAttributes().get("autocomplete"), "off");

	}

	/**
	 * Adds the after field content test.
	 */
	public void addAfterFieldContentTest() {
		WritablePropertyDefinition fieldDefinition = TestUtil.getFieldDefinition();
		builder.setPropertyDefinition(fieldDefinition);
		DataType datatype = new DataType();
		datatype.setName(DataTypeDefinition.DATE);
		fieldDefinition.setDataType(datatype);

		UIComponent wrapper = builderHelper.getComponent(ComponentType.OUTPUT_PANEL);
		wrapper.getChildren().add(builderHelper.getComponent(ComponentType.OUTPUT_LABEL));
		wrapper.getChildren().add(builderHelper.getComponent(ComponentType.INPUT_TEXT));
		wrapper.getChildren().add(builderHelper.getComponent(ComponentType.HTML_MESSAGE));
		builder.addAfterFieldContent(wrapper);

		Assert.assertEquals(wrapper.getChildCount(), 4);

		Assert.assertTrue(wrapper.getChildren().get(0) instanceof HtmlOutputLabel);
		UIComponent innerWrapper = wrapper.getChildren().get(1);
		Assert.assertTrue(innerWrapper instanceof HtmlPanelGroup);

		String styleClass = (String) innerWrapper.getAttributes().get("styleClass");
		Assert.assertEquals(styleClass, "cmf-relative-wrapper");

		List<UIComponent> innerWrapperChildren = innerWrapper.getChildren();
		// we should have an input field and an icon wrapper inside
		Assert.assertTrue(innerWrapperChildren.size() == 2);
		Assert.assertTrue(innerWrapperChildren.get(0) instanceof HtmlInputText);
		UIComponent iconImageWrapper = innerWrapperChildren.get(1);
		Assert.assertTrue(iconImageWrapper instanceof HtmlPanelGroup);
		Assert.assertEquals(iconImageWrapper.getAttributes().get("styleClass"),
				"ui-icon ui-icon-calendar cmf-calendar-img cmf-date-field-icon");
	}

	// /**
	// * Test for method that builds a input field.
	// */
	// public void buildFieldTest() {
	// builder.setPropertyDefinition(TestUtil.getFieldDefinition("Created on",
	// null, DataTypeDefinition.DATE, "protected"));
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
	// builder.setPropertyDefinition(TestUtil.getFieldDefinition("Created on",
	// "createdOn", DataTypeDefinition.DATE, "editable"));
	// field = builder.buildField();
	// Assert.assertEquals(field.getId(), "createdOn");
	// }
}
