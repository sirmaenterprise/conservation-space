package com.sirma.sep.model.management.meta;

import java.util.List;
import java.util.Objects;

/**
 * Descriptor for a supported {@link com.sirma.sep.model.ModelNode}'s
 * {@link com.sirma.sep.model.management.meta.ControlOption}'s meta information.
 *
 * @author Stella D
 */
public class ModelControlMetaInfo extends ModelMetaInfo {

	private List<ControlOption> controlOptions;

	public List<ControlOption> getControlOptions() {
		return controlOptions;
	}

	public ModelMetaInfo setControlOptions(List<ControlOption> controlOptions) {
		this.controlOptions = controlOptions;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModelControlMetaInfo)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		ModelControlMetaInfo that = (ModelControlMetaInfo) o;
		return Objects.equals(controlOptions, that.controlOptions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), controlOptions);
	}
}
