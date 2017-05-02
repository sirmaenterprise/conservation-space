package com.sirma.itt.seip.plugin;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;

/**
 * Represents a dummy instance of @Extension annotation to be used when fetching all available plugins.
 *
 * @author Adrian Mitev
 */
public class ExtensionLiteral extends AnnotationLiteral<Extension>implements Extension {

	private static final long serialVersionUID = 1L;

	public static final ExtensionLiteral INSTANCE = new ExtensionLiteral();

	@Override
	@Nonbinding
	public String target() {
		return "";
	}

	@Override
	@Nonbinding
	public double order() {
		return 0;
	}

	@Override
	@Nonbinding
	public int priority() {
		return 0;
	}

	@Override
	@Nonbinding
	public boolean enabled() {
		return false;
	}
}