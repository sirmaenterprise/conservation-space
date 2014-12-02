package com.sirma.itt.cmf.testutil;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

// TODO: Auto-generated Javadoc
/**
 * The Enum JarPackages.
 */
public enum JarPackages implements TestableJarModules {

	/** The adapters mock. */
	ADAPTERS_MOCK() {
		@Override
		public JavaArchive add(TestPackageBuilder builder, JavaArchive archive) {
			Class<?>[] buildMockAdapters = builder.buildMockAdapters();
			JavaArchive jarArchive = archive.addClasses(buildMockAdapters);
			// jarArchive = builder.activateAlternatives(jarArchive, buildMockAdapters);
			Class<?>[] buildMockAdaptersExtensions = builder.buildMockAdaptersExtensions();
			jarArchive = archive.addClasses(buildMockAdaptersExtensions);
			return jarArchive;
		}

	},

	/** The resources. */
	RESOURCES() {
		@Override
		public JavaArchive add(TestPackageBuilder builder, JavaArchive archive) {
			ResourceImporter resourceImpoter = new ResourceImporter(builder, archive);
			return resourceImpoter.importData();
		}

	},
	/** The security. */
	SECURITY() {
		@Override
		public JavaArchive add(TestPackageBuilder builder, JavaArchive archive) {
			JavaArchive jarArchive = archive.addClasses(builder.buildPermissionServices());
			return jarArchive;
		}

	},

	/** The case creation. */
	CASE_CREATION() {
		@Override
		public JavaArchive add(TestPackageBuilder builder, JavaArchive archive) {
			JavaArchive jarArchive = archive.addClasses(builder.buildTypeInstances());
			return jarArchive;
		}

	},

	/** The dozer. */
	DOZER() {
		@Override
		public JavaArchive add(TestPackageBuilder builder, JavaArchive archive) {
			return archive.addClasses(builder.buildDozerExtensions());
		}
	},

	/** The basic - the least required. */
	BASIC() {
		@Override
		public JavaArchive add(TestPackageBuilder builder, JavaArchive archive) {
			return archive.addClasses(builder.buildCacheClasses())
					.addClasses(builder.buildDefinitions()).addClasses(builder.buildConverters())
					.addClasses(builder.buildDefinitionCallbacks())
					.addClasses(builder.buildJaxBDefinitions())
					.addClasses(builder.buildModelCasses())
					.addClasses(builder.buildEntityClasses())
					.addClasses(builder.buildConfigClasses()).addClasses(builder.buildDaoClasses())
					.addClasses(builder.buildUtilityClasses())
					.addClasses(builder.buildImplServices())
					.addClasses(builder.buildStateExtensionsServices())
					.addClasses(builder.buildXmlClasses());

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