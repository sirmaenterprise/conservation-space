package com.sirma.itt.cmf.testutil;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

import com.sirma.itt.seip.testutil.ResourceImporter;
import com.sirma.itt.seip.testutil.TestPackageBuilder;
import com.sirma.itt.seip.testutil.TestableJarModules;

/**
 * The Enum JarPackages.
 */
public enum CmfJarPackages implements TestableJarModules {

	/** The resources. */
	RESOURCES() {
		@Override
		public JavaArchive add(TestPackageBuilder builder, JavaArchive archive) {
			ResourceImporter resourceImpoter = new CmfResourceImporter(builder, archive);
			return resourceImpoter.importData();
		}

	}

}