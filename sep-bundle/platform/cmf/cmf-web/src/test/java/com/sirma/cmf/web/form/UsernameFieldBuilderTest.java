package com.sirma.cmf.web.form;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.form.builder.UsernameFieldBuilder;
import com.sirma.cmf.web.util.TestUtil;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;

/**
 * The Class UsernameFieldBuilderTest.
 * 
 * @author svelikov
 */
@Test
public class UsernameFieldBuilderTest extends FormBuilderBaseTest {

	private static final String USERNAMEFIELD = "usernamefield1";

	/** The builder. */
	private final UsernameFieldBuilder builder;

	/**
	 * Instantiates a new single line field builder test.
	 */
	public UsernameFieldBuilderTest() {

		builder = new UsernameFieldBuilder(null, null) {

			@Override
			protected String getId(PropertyDefinition propertyDefinition, String propertyName) {
				return propertyName;
			}

			@Override
			protected ValueExpression createValueExpression(String stringValueExpression,
					Class<?> valueType) {
				return null;
			}
		};

		WritablePropertyDefinition fieldDefinition = TestUtil.getFieldDefinition();
		fieldDefinition.setName(USERNAMEFIELD);
		builder.setPropertyDefinition(fieldDefinition);

		ReflectionUtils.setField(builder, "log", Logger.getLogger(builder.getClass()));
		ReflectionUtils.setField(builder, "builderHelper", builderHelper);
	}

	/**
	 * Test for getComponentInstance method.
	 */
	public void getComponentInstanceTest() {
		UIComponent componentInstance = builder.getComponentInstance();

		Assert.assertNotNull(componentInstance);
		Assert.assertEquals(componentInstance instanceof HtmlOutputText, true);

		Assert.assertEquals(componentInstance.getAttributes().get("styleClass"),
				BuilderCssConstants.CMF_PREVIEW_FIELD);

		// check value expression
	}

	/**
	 * Builds the field test.
	 */
	public void buildFieldTest() {
		UIComponent component = builder.buildField();
		Assert.assertNotNull(component);
	}

}
