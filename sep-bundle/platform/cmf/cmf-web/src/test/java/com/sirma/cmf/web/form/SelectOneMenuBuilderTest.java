package com.sirma.cmf.web.form;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlSelectOneMenu;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.form.builder.SelectOneMenuBuilder;
import com.sirma.cmf.web.util.TestUtil;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;

/**
 * The Class SelectOneMenuBuilderTest.
 * 
 * @author svelikov
 */
@Test
public class SelectOneMenuBuilderTest extends FormBuilderBaseTest {

	/** The builder. */
	private final SelectOneMenuBuilder builder;

	/**
	 * Instantiates a new single line field builder test.
	 */
	public SelectOneMenuBuilderTest() {
		super();

		builder = new SelectOneMenuBuilder(null, null) {
			@Override
			protected ValueExpression createValueExpression(String ve, Class<?> valueType) {
				return null;
			}
		};

		ReflectionUtils.setField(builder, "log", Logger.getLogger(builder.getClass()));
		ReflectionUtils.setField(builder, "builderHelper", builderHelper);
	}

	/**
	 * Test for getComponentInstance method.
	 */
	public void getComponentInstanceTest() {
		UIComponent component = builder.getComponentInstance();

		Assert.assertNotNull(component);
		Assert.assertEquals(component instanceof HtmlSelectOneMenu, true);

		List<UIComponent> children = component.getChildren();
		Assert.assertEquals(children.size(), 2);

		Assert.assertEquals(children.get(0) instanceof UISelectItem, true);
		Assert.assertEquals(children.get(1) instanceof UISelectItems, true);
	}

	/**
	 * Adds the select items test.
	 */
	public void addSelectItemsTest() {
		WritablePropertyDefinition fieldDefinition = TestUtil.getFieldDefinition();
		fieldDefinition.setCodelist(1);
		builder.setPropertyDefinition(fieldDefinition);

		List<UIComponent> children = new ArrayList<UIComponent>();
		builder.addSelectItems(children);

		Assert.assertEquals(children.size(), 1);
		Assert.assertEquals(children.get(0) instanceof UISelectItems, true);
		UISelectItems selectItems = (UISelectItems) children.get(0);

		// test value

		// test itemLabel

		// test var
	}

	/**
	 * Adds the noselection option test.
	 */
	public void addNoselectionOptionTest() {
		List<UIComponent> children = new ArrayList<UIComponent>();
		builder.addNoselectionOption(children);

		Assert.assertEquals(children.size(), 1);
		Assert.assertEquals(children.get(0) instanceof UISelectItem, true);

		Assert.assertEquals(children.get(0).getAttributes().get("noSelectionOption"), true);
	}

	private static final String ONE_FILTER_VE_STRING = "#{cls.getFilteredCodeValues(1, formUtil.toArray('filter1')).keySet()}";
	private static final String TWO_FILTER_VE_STRING = "#{cls.getFilteredCodeValues(1, formUtil.toArray('filter1','filter2')).keySet()}";

	/**
	 * Creates the value expression string test.
	 */
	public void createValueExpressionStringTest() {
		WritablePropertyDefinition fieldDefinition = TestUtil.getFieldDefinition();
		Set<String> filters = new HashSet<String>();
		filters.add("filter1");
		fieldDefinition.setFilters(filters);
		builder.setPropertyDefinition(fieldDefinition);

		String veString = builder.createValueExpressionString(1);

		// check with 1 filter
		Assert.assertEquals(veString, ONE_FILTER_VE_STRING);

		// check with 2 filters
		filters.add("filter2");
		veString = builder.createValueExpressionString(1);
		Assert.assertEquals(veString, TWO_FILTER_VE_STRING);

	}
}
