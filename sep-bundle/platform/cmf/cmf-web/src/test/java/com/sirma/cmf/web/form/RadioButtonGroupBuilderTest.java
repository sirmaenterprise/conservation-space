package com.sirma.cmf.web.form;

import javax.el.ValueExpression;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.sirma.cmf.web.form.builder.RadioButtonGroupBuilder;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * The Class RadioButtonGroupBuilderTest.
 * 
 * @author svelikov
 */
@Test
public class RadioButtonGroupBuilderTest extends FormBuilderBaseTest {

	/** The builder. */
	private final RadioButtonGroupBuilder builder;

	/**
	 * Instantiates a new radio button group test.
	 */
	public RadioButtonGroupBuilderTest() {
		super();

		builder = new RadioButtonGroupBuilder(null, null) {
			@Override
			protected ValueExpression createValueExpression(String ve, Class<?> valueType) {
				return null;
			}
		};

		ReflectionUtils.setField(builder, "log", Logger.getLogger(builder.getClass()));
		ReflectionUtils.setField(builder, "builderHelper", builderHelper);
	}

	/**
	 * Builds the test.
	 */
	// TODO: implement test
	public void buildTest() {

	}

	/**
	 * Builds the field test.
	 */
	// TODO: implement test
	public void buildFieldTest() {

	}

	/**
	 * Test for getComponentInstance method.
	 */
	// TODO: implement test
	public void getComponentInstanceTest() {
		// UIComponent componentInstance = builder.getComponentInstance();
		//
		// Assert.assertNotNull(componentInstance);
		// Assert.assertEquals(componentInstance instanceof HtmlInputText, true);
	}

}
