package com.sirma.sep.model.management.meta;

import java.util.List;
import java.util.Objects;

/**
 * Describes rule outcome.
 *
 * @author Stella Djulgerova
 */
public class RuleOutcome {

	private Boolean updateable;

	private Boolean mandatory;

	private Boolean visible;

	private List<Object> values;

	public Boolean isUpdateable() {
		return updateable;
	}

	public void setUpdateable(Boolean updateable) {
		this.updateable = updateable;
	}

	public Boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Boolean isVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RuleOutcome)) {
			return false;
		}
		RuleOutcome that = (RuleOutcome) o;
		return updateable == that.updateable && mandatory == that.mandatory && visible == that.visible
				&& Objects.equals(values, that.values);
	}

	@Override
	public int hashCode() {
		return Objects.hash(updateable, mandatory, visible, values);
	}
}
