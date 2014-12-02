package com.sirma.itt.cmf.testutil;

import org.jboss.shrinkwrap.api.spec.WebArchive;

// TODO: Auto-generated Javadoc
/**
 * The TestableWarModules is interface to represent extension the the testable war archive, attached
 * by the method {@link #add(TestResourceBuilder, WebArchive)}.
 */
public interface TestableWarModules {

	/**
	 * Adds the extension to the archive and returns the new archive.
	 *
	 * @param builder the builder
	 * @param archive the archive
	 * @return the web archive
	 */
	WebArchive add(TestPackageBuilder builder, WebArchive archive);
}
