package com.sirma.itt.cmf.beans.model;

import com.sirma.itt.cmf.constants.SectionProperties;

/**
 * Instance that represents a generic folder
 * 
 * @author BBonev
 */
public class FolderInstance extends SectionInstance {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2206604685418799801L;

	@Override
	public boolean isStandalone() {
		return true;
	}

	@Override
	public void setStandalone(boolean standalone) {
		// always standalone
	}

	@Override
	public String getPurpose() {
		String purpose = super.getPurpose();
		if (purpose == null) {
			// default purpose value
			purpose = SectionProperties.PURPOSE_FOLDER;
		}
		return purpose;
	}

}
