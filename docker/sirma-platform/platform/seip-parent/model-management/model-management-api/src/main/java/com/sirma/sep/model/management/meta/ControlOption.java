package com.sirma.sep.model.management.meta;

import java.util.List;
import java.util.Objects;

/**
 * Describes a control option for a {@link com.sirma.sep.model.ModelNode}'s
 * {@link com.sirma.sep.model.management.ModelAttribute}.
 *
 * @author Stella D
 */
public class ControlOption {

	private String id;

	private List<ControlOptionParam> params;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ControlOptionParam> getParams() {
		return params;
	}

	public void setParams(List<ControlOptionParam> params) {
		this.params = params;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ControlOption)) {
			return false;
		}
		ControlOption that = (ControlOption) o;
		return id == that.id && Objects.equals(params, that.params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, params);
	}
}
