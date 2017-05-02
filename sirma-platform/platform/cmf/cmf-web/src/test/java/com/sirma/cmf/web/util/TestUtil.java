package com.sirma.cmf.web.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;

import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.WritablePropertyDefinition;

/**
 * Utility and provider functions.
 *
 * @author svelikov
 */
public class TestUtil {

	/**
	 * Creates a {@link FieldDefinitionImpl}.
	 *
	 * @param label
	 *            label
	 * @param name
	 *            name
	 * @param type
	 *            type
	 * @param displayType
	 *            displayType
	 * @param filters
	 *            the filters
	 * @return Created FieldDefinitionImpl.
	 */
	public static WritablePropertyDefinition getFieldDefinition(String label, String name, String type,
			String displayType, Set<String> filters) {
		return buildPropertyDefinition(label, name, type, DisplayType.parse(displayType), filters);
	}

	/**
	 * Creates a {@link FieldDefinitionImpl} with predefined values.
	 *
	 * @return Created FieldDefinitionImpl.
	 */
	public static WritablePropertyDefinition getFieldDefinition() {
		Set<String> filters = new LinkedHashSet<String>();
		filters.add("filter1");
		return buildPropertyDefinition("Name", "nameField", DataTypeDefinition.TEXT, DisplayType.READ_ONLY, filters);
	}

	/**
	 * Builds a {@link FieldDefinitionImpl}.
	 *
	 * @param label
	 *            label
	 * @param name
	 *            name
	 * @param type
	 *            type
	 * @param displayType
	 *            displayType
	 * @param filters
	 *            the filters
	 * @return Created FieldDefinitionImpl.
	 */
	private static WritablePropertyDefinition buildPropertyDefinition(String label, String name, String type,
			DisplayType displayType, Set<String> filters) {
		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setId(1l);
		property.setLabelId(label);
		property.setName(name);
		property.setType(type);
		property.setDisplayType(displayType);
		property.setFilters(filters);
		return property;
	}

	/**
	 * Creates the control definition.
	 *
	 * @param controlId
	 *            the control id
	 * @param controlParams
	 *            the control params
	 * @param uiParams
	 *            the ui params
	 * @return the control definition
	 */
	public static ControlDefinition createControlDefinition(String controlId, List<ControlParam> controlParams,
			List<ControlParam> uiParams) {
		ControlDefinitionImpl controlDefinition = new ControlDefinitionImpl();
		// controlDefinition.setId(1l);
		Assert.fail("Definition model changed. Please check the test");
		controlDefinition.setIdentifier(controlId);
		controlDefinition.setControlParams(controlParams);
		controlDefinition.setUiParams(uiParams);

		return controlDefinition;
	}

	/**
	 * Creates the param.
	 *
	 * @param identifier
	 *            the identifier
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return the control param
	 */
	public static ControlParam createParam(String identifier, String name, String value) {
		ControlParamImpl param = new ControlParamImpl();
		param.setIdentifier(identifier);
		param.setName(name);
		param.setValue(value);
		return param;
	}
}
