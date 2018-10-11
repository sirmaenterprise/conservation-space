package com.sirma.sep.model.management.meta;

import java.util.Objects;

/**
 * Describes a validation model for a {@link com.sirma.sep.model.ModelNode}'s {@link com.sirma.sep.model.management.ModelAttribute}.
 *
 * @author Mihail Radkov
 */
public class ModelMetaInfoValidation {

	private boolean mandatory;

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ModelMetaInfoValidation that = (ModelMetaInfoValidation) o;
		return mandatory == that.mandatory;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mandatory);
	}
}
