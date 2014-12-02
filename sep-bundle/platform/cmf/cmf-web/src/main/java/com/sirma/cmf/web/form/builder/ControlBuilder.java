package com.sirma.cmf.web.form.builder;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;

import com.sirma.cmf.web.form.ComponentType;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Base functionality for control builders.
 * 
 * @author svelikov
 */
public class ControlBuilder extends FormBuilder {

	/**
	 * Instantiates a new control builder.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @param codelistService
	 *            the codelist service
	 */
	public ControlBuilder(LabelProvider labelProvider, CodelistService codelistService) {
		super(labelProvider, codelistService);
	}

	/**
	 * Gets the parameters by name.
	 * 
	 * @param requestedParameterName
	 *            the requested parameter name
	 * @param controlParams
	 *            the control params
	 * @return the parameters by name
	 */
	protected List<ControlParam> getParametersByName(String requestedParameterName,
			List<ControlParam> controlParams) {

		List<ControlParam> foundParameters = new ArrayList<ControlParam>();

		if ((controlParams != null) && StringUtils.isNotNullOrEmpty(requestedParameterName)) {
			for (ControlParam currentControlParam : controlParams) {

				if (StringUtils.isNotNullOrEmpty(requestedParameterName)
						&& currentControlParam.getName().endsWith(requestedParameterName)) {

					foundParameters.add(currentControlParam);
				}
			}
		}

		// TODO: temporary commented until definitions are updated with
		// appropriate prefixes
		// Collections.sort(foundParameters, new
		// CMFControlParameterComparator());

		return foundParameters;
	}

	/**
	 * Gets the first found control parameter from the list.
	 * 
	 * @param requestedParameterName
	 *            the requested parameter name
	 * @param controlParams
	 *            the control params
	 * @return the control parameter or null if no parameter is found
	 */
	protected ControlParam getControlParameter(String requestedParameterName,
			List<ControlParam> controlParams) {
		ControlParam controlParam = null;
		if ((controlParams != null) && StringUtils.isNotNullOrEmpty(requestedParameterName)) {
			for (ControlParam currentControlParam : controlParams) {
				if (currentControlParam.getName().endsWith(requestedParameterName)) {
					controlParam = currentControlParam;
					break;
				}
			}
		}
		return controlParam;
	}

	/**
	 * Gets the control parameter value.
	 * 
	 * @param requestedParameterName
	 *            the requested parameter name
	 * @param controlParams
	 *            the control params
	 * @return the control parameter value
	 */
	public String getControlParameterValue(String requestedParameterName,
			List<ControlParam> controlParams) {
		ControlParam controlParam = null;
		if ((controlParams != null) && StringUtils.isNotNullOrEmpty(requestedParameterName)) {
			for (ControlParam currentControlParam : controlParams) {
				if (currentControlParam.getName().endsWith(requestedParameterName)) {
					controlParam = currentControlParam;
					break;
				}
			}
		}
		String value = null;
		if ((controlParam != null) && (controlParam.getValue() != null)) {
			value = controlParam.getValue();
		}
		return value;
	}

	/**
	 * Adds the group label.
	 * 
	 * @param wrapper
	 *            the wrapper
	 * @param additionalStyleClass
	 *            the additional style class
	 */
	protected void addGroupLabel(UIComponent wrapper, String additionalStyleClass) {

		String labelAttribute = propertyDefinition.getLabel();

		if (StringUtils.isNotNullOrEmpty(labelAttribute)) {

			HtmlOutputText label = (HtmlOutputText) builderHelper
					.getComponent(ComponentType.OUTPUT_TEXT);
			label.setValue(labelAttribute);

			addStyleClass(label, additionalStyleClass);

			wrapper.getChildren().add(label);
		}

	}

}
