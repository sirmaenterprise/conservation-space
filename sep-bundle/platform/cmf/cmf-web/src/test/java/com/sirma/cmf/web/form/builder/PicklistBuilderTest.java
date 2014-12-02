package com.sirma.cmf.web.form.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.Assert;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.util.TestUtil;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;

/**
 * The Class PicklistBuilderTest.
 * 
 * @author svelikov
 */
// @Test
public class PicklistBuilderTest extends CMFTest {

	/**
	 * The class under test.
	 */
	private final PicklistBuilder builder;

	/**
	 * Constructor.
	 */
	public PicklistBuilderTest() {
		builder = new PicklistBuilder(null, null);

		ReflectionUtils.setField(builder, "log", Logger.getLogger(builder.getClass()));
	}

	/**
	 * Gets the picklist init parameters test.
	 */
	public void getPicklistInitParametersTest() {
		WritablePropertyDefinition fieldDefinition = TestUtil.getFieldDefinition("picklist",
				"assignee", "an..30", "editable", null);

		List<ControlParam> uiParams = new ArrayList<ControlParam>();
		uiParams.add(TestUtil.createParam("TRIGGER_BUTTON_TITLE", "TRIGGER_BUTTON_TITLE", "Chose"));
		uiParams.add(TestUtil.createParam("ACCEPT_BUTTON_TITLE", "ACCEPT_BUTTON_TITLE", "Accept"));
		uiParams.add(TestUtil.createParam("PANEL_HEADER", "PANEL_HEADER", "Picklist"));

		TestUtil.createControlDefinition("picklist", null, uiParams);
		fieldDefinition.setControlDefinition(null);
		builder.setPropertyDefinition(fieldDefinition);

		String picklistInitParameters = builder.getPicklistInitParameters();
		String exprectedParametersString = "{'' : '', '' : '', '' : ''}";
		Assert.assertEquals(picklistInitParameters, exprectedParametersString);
	}
}
