package com.sirma.itt.pm.testutil;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

import com.sirma.itt.cmf.testutil.TestPackageBuilder;
import com.sirma.itt.cmf.testutil.TestableJarModules;

// TODO: Auto-generated Javadoc
/**
 * The Enum JarPackages.
 */
public enum PmJarPackages implements TestableJarModules {

	/** The resources. */
	RESOURCES() {
		@Override
		public JavaArchive add(TestPackageBuilder builder, JavaArchive archive) {
			PmResourceImporter resourceImpoter = new PmResourceImporter(builder, archive);
			return resourceImpoter.importData();
		}

	};

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.testutil.TestableJarModules#add(com.sirma.itt.cmf .testutil.
	 * TestResourceBuilder, org.jboss.shrinkwrap.api.spec.JavaArchive)
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaArchive add(TestPackageBuilder builder, JavaArchive archive) {
		return archive;
	}
}