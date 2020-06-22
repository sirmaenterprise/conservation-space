package com.sirma.itt.seip.annotations.model;

import com.sirma.itt.seip.domain.instance.EmfInstance;

/**
 * {@link SpecificResource} represents the resource where the annotation is placed
 *
 * @author BBonev
 */
public class SpecificResource extends EmfInstance {

	private static final long serialVersionUID = -1672131624139736775L;

	@Override
	public String toString() {
		return new StringBuilder(1024)
				.append("SpecificResource [id=")
					.append(getId())
					.append(", properties=")
					.append(getProperties())
					.append("]")
					.toString();
	}

}
