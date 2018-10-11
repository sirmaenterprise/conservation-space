package com.sirma.sep.cls.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract POJO containing the common properties for {@link CodeList} and {@link CodeValue}.
 *
 * @author Mihail Radkov
 */
public abstract class Code {

	private String value;

	private List<CodeDescription> descriptions;

	private String extra1;

	private String extra2;

	private String extra3;

	public String getValue() {
		return value;
	}

	public Code setValue(String value) {
		this.value = value;
		return this;
	}

	public List<CodeDescription> getDescriptions() {
		if (descriptions == null) {
			descriptions = new ArrayList<>();
		}
		return descriptions;
	}

	public Code setDescriptions(List<CodeDescription> descriptions) {
		this.descriptions = descriptions;
		return this;
	}

	public String getExtra1() {
		return extra1;
	}

	public Code setExtra1(String extra1) {
		this.extra1 = extra1;
		return this;
	}

	public String getExtra2() {
		return extra2;
	}

	public Code setExtra2(String extra2) {
		this.extra2 = extra2;
		return this;
	}

	public String getExtra3() {
		return extra3;
	}

	public Code setExtra3(String extra3) {
		this.extra3 = extra3;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, extra1, extra2, extra3, descriptions);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Code)) {
			return false;
		}
		Code code = (Code) obj;
		return Objects.equals(value, code.value) && Objects.equals(extra1, code.extra1) && Objects.equals(extra2, code.extra2)
				&& Objects.equals(extra3, code.extra3) && Objects.equals(descriptions, code.descriptions);
	}
}
