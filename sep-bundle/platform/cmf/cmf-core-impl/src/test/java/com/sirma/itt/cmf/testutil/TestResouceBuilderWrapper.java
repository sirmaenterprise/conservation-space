package com.sirma.itt.cmf.testutil;

import org.apache.log4j.Logger;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * The TestResouceBuilderWrapper is helper classes for dynamic build of war file. Invoke the
 * {@link #packageWar()} to produce the default war
 */
public class TestResouceBuilderWrapper {

	private Logger logger = Logger.getLogger(getClass());
	/** The test resource builder. */
	private TestPackageBuilder testResourceBuilder;

	/** The build jar. */
	private JavaArchive buildJar;

	/**
	 * Instantiates a new test resouce builder wrapper.
	 *
	 * @param testResourceBuilder the test resource builder
	 */
	public TestResouceBuilderWrapper(TestPackageBuilder testResourceBuilder) {
		this.testResourceBuilder = testResourceBuilder;
	}

	/**
	 * Inits the.
	 *
	 * @return the test resouce builder wrapper
	 */
	public TestResouceBuilderWrapper init() {
		return init(JarPackages.BASIC, JarPackages.DOZER, JarPackages.SECURITY,
				JarPackages.CASE_CREATION, JarPackages.ADAPTERS_MOCK, JarPackages.RESOURCES);
	}

	/**
	 * Inits the.
	 *
	 * @param modules the modules
	 * @return the test resouce builder wrapper
	 */
	public TestResouceBuilderWrapper init(TestableJarModules... modules) {
		buildJar = testResourceBuilder.addOptional(modules);
		return this;
	}

	/**
	 * Package jar.
	 *
	 * @param alternative the alternative
	 * @param classes the classes
	 * @return the test resouce builder wrapper
	 */
	public TestResouceBuilderWrapper addClasess(boolean alternative, Class<?>... classes) {
		buildJar = testResourceBuilder.addOptionalClasses(alternative, classes);
		return this;
	}

	/**
	 * Package jar.
	 *
	 * @param classes
	 *            the classes
	 * @return the test resouce builder wrapper
	 */
	public TestResouceBuilderWrapper addClasess(Class<?>... classes) {
		return addClasess(false, classes);
	}

	/**
	 * Package war.
	 *
	 * @return the web archive
	 */
	public WebArchive packageWar() {
		testResourceBuilder.buildJar();
		return packageWar(WarPackages.ADAPTERS);
	}

	/**
	 * Package war.
	 *
	 * @param modules
	 *            the modules
	 * @return the web archive
	 */
	public WebArchive packageWar(TestableWarModules... modules) {
		if (buildJar == null) {
			init();
		}
		WebArchive packagedWar = testResourceBuilder.packageWar(modules);
		if (logger.isDebugEnabled()) {
			packagedWar.writeTo(System.out, Formatters.VERBOSE);
		}
		return packagedWar;
	}


}
