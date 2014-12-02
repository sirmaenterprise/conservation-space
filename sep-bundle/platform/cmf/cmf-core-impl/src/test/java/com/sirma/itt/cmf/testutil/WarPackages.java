package com.sirma.itt.cmf.testutil;

import org.jboss.shrinkwrap.api.spec.WebArchive;

// TODO: Auto-generated Javadoc
/**
 * The Enum WarPackages.
 */
public enum WarPackages implements TestableWarModules {

	/** The adapters. */
	ADAPTERS() {
		@Override
		public WebArchive add(TestPackageBuilder builder, WebArchive archive) {
//			List<Pair<File, MavenResolvedArtifact>> importAdapters = builder.importAdapters();
//			WebArchive warArchive = builder.importLibrariesInWar(archive, importAdapters);
			return archive;
		}
	}
//	,
//
//	/** The adapters mock. Activate alternatives as well */
//	ADAPTERS_MOCK() {
//		@Override
//		public WebArchive add(TestPackageBuilder builder, WebArchive archive) {
//			Class<?>[] buildMockAdapters = builder.buildMockAdapters();
//			WebArchive warArchive = archive.addClasses(buildMockAdapters);
//			warArchive = builder.activateAlternatives(warArchive, buildMockAdapters);
//			return warArchive;
//		}
//
//	}

}
