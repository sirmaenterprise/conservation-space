package com.sirma.sep.model.management.meta;

import java.util.List;
import java.util.Objects;

/**
 * Describes a validation model for a {@link com.sirma.sep.model.ModelNode}'s {@link com.sirma.sep.model.management.ModelAttribute}.
 *
 * @author Mihail Radkov
 */
public class ModelMetaInfoValidation {

	private boolean mandatory;

	private boolean updateable;

	private List<String> affected;

	private List<ModelMetaInfoRule> rules;

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isUpdateable() {
		return updateable;
	}

	public void setUpdateable(boolean updateable) {
		this.updateable = updateable;
	}

	public List<String> getAffected() {
		return affected;
	}

	public void setAffected(List<String> affected) {
		this.affected = affected;
	}

	public List<ModelMetaInfoRule> getRules() {
		return rules;
	}

	public void setRules(List<ModelMetaInfoRule> rules) {
		this.rules = rules;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModelMetaInfoValidation)) {
			return false;
		}
		ModelMetaInfoValidation that = (ModelMetaInfoValidation) o;
		return mandatory == that.mandatory && updateable == that.updateable && Objects.equals(rules, that.rules)
				&& Objects.equals(affected, that.affected);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mandatory, updateable, rules, affected);
	}
}
