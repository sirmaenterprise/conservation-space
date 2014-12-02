package com.sirma.itt.cmf.testutil;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

// TODO: Auto-generated Javadoc
/**
 * The TestableJarModules is interface to represent extension the the testable jar archive, attached
 * by the method {@link #add(TestResourceBuilder, JavaArchive)}.
 */
public interface TestableJarModules {

	/**
	 * Adds the module extension to the archive and return the enriched archive.
	 *
	 * @param builder the builder
	 * @param archive the archive
	 * @return the java archive
	 */
	JavaArchive add(TestPackageBuilder builder, JavaArchive archive);
}
