package com.sirma.itt.seip.annotation;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Literal qualifier for {@link Purpose}.
 *
 * @author A. Kunchev
 */
public class PurposeQualifier extends AnnotationLiteral<Purpose> implements Purpose {

	private static final long serialVersionUID = -6381533489037846538L;

	private final String purpose;

	public PurposeQualifier(String purpose) {
		this.purpose = purpose;
	}

	@Override
	public String value() {
		return purpose;
	}
}