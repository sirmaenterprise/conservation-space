package com.sirma.sep.cls.model;

import java.util.Objects;

/**
 * POJO extending the base {@link Code} by including information about the parent {@link CodeList} and the state -
 * active or inactive.
 *
 * @author Mihail Radkov
 */
public class CodeValue extends Code {

	/**
	 * The parent's {@link CodeList} value.
	 */
	private String codeListValue;

	/**
	 * The current state of the {@link CodeValue}
	 */
	private Boolean active = Boolean.TRUE;

	public String getCodeListValue() {
		return codeListValue;
	}

	public CodeValue setCodeListValue(String codeListValue) {
		this.codeListValue = codeListValue;
		return this;
	}

	public Boolean isActive() {
		return active;
	}

	public CodeValue setActive(Boolean active) {
		this.active = active;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), codeListValue, active);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CodeValue)) {
			return false;
		}
		CodeValue codeValue = (CodeValue) obj;
		return super.equals(codeValue) && Objects.equals(codeListValue, codeValue.codeListValue)
				&& Objects.equals(active, codeValue.active);
	}
}
