package com.sirma.sep.cls.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Concrete {@link Code} acting as a parent that holds multiple unique {@link CodeValue}.
 *
 * @author Mihail Radkov
 */
public class CodeList extends Code {

	private List<CodeValue> values;

	public List<CodeValue> getValues() {
		if (values == null) {
			values = new ArrayList<>();
		}
		return values;
	}

	public CodeList setValues(List<CodeValue> values) {
		this.values = values;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CodeList)) {
			return false;
		}
		CodeList codeList = (CodeList) obj;
		return super.equals(codeList) && Objects.equals(values, codeList.values);
	}
}
