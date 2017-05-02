package com.sirma.itt.seip.annotations.model;

import com.sirma.itt.seip.domain.instance.EmfInstance;

/**
 * Instance that represent the annotation body
 *
 * @author BBonev
 */
public class AnnotationBody extends EmfInstance {

	private static final long serialVersionUID = -7516426242701022843L;

	@Override
	public String toString() {
		return new StringBuilder(512)
				.append("Body [id=")
					.append(getId())
					.append(", properties=")
					.append(getProperties())
					.append("]")
					.toString();
	}
}
