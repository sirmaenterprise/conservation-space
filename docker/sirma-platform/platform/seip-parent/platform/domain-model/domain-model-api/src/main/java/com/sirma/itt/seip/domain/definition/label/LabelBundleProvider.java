package com.sirma.itt.seip.domain.definition.label;

/**
 * This extension point represented by this class was renamed to {@link LabelResolverProvider}. <br>
 * This class should act as a proxy for that code to work properly.
 *
 * @deprecated Replaced by {@link LabelResolverProvider} since 31/10/2018 (2.25)
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/10/2018
 */
@Deprecated
public class LabelBundleProvider {
	public static final String TARGET_NAME = LabelResolverProvider.TARGET_NAME;

	private LabelBundleProvider() {
		// dummy class
	}
}
