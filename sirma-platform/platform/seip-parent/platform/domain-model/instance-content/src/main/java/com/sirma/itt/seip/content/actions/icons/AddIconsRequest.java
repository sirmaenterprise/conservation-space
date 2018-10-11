package com.sirma.itt.seip.content.actions.icons;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 *	Used to contain the additional properties needed to execute the add icons action properly.
 *
 * @author Nikolay Ch
 */
public class AddIconsRequest extends ActionRequest {
	public static final String OPERATION_NAME = "addIcons";

	private static final long serialVersionUID = 1L;
	private Map<Serializable, String> purposeIconMapping;

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	/**
	 * Setter for the mapping
	 *
	 * @param purposeIconMapping
	 *            the mapping
	 */
	public void setPurposeIconMapping(Map<Serializable, String> purposeIconMapping) {
		this.purposeIconMapping = purposeIconMapping;
	}

	/**
	 * Getter for the mapping.
	 *
	 * @return the mapping
	 */
	public Map<Serializable, String> getPurposeIconMapping() {
		return purposeIconMapping;
	}

}
