package com.sirma.itt.cmf.testutil;

import java.io.File;
import java.util.Map;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;

import com.sirma.itt.emf.domain.Pair;

/**
 * The TestPackageBuilder is an interface for dynamic build of test module packages.
 */
public interface TestPackageBuilder {

	/**
	 * Builds the cache classes.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildCacheClasses();

	/**
	 * Builds the config classes.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildConfigClasses();

	/**
	 * Builds the definitions.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildConverters();

	/**
	 * Builds the dao classes.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildDaoClasses();

	/**
	 * Builds the definition callbacks.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildDefinitionCallbacks();

	/**
	 * Builds the definitions.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildDefinitions();

	/**
	 * Builds the dozer extensions.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildDozerExtensions();

	/**
	 * Builds the entity classes.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildEntityClasses();

	/**
	 * Builds the impl services.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildImplServices();

	/**
	 * Builds the jar.
	 *
	 * @param jarModules
	 *            the jar modules
	 * @return the test resource builder
	 */
	public abstract TestPackageBuilder buildJar(TestableJarModules... jarModules);

	/**
	 * Builds the jax b definitions.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildJaxBDefinitions();

	/**
	 * Builds the adapters.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildMockAdapters();

	/**
	 * Builds the mock adapters extensions.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildMockAdaptersExtensions();

	/**
	 * Builds the model casses.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildModelCasses();

	/**
	 * Builds the permission services.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildPermissionServices();

	/**
	 * Builds the state extensions services.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildStateExtensionsServices();

	/**
	 * Builds the type instances.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildTypeInstances();

	/**
	 * Builds the utility classes.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildUtilityClasses();

	/**
	 * Builds the xml classes.
	 *
	 * @return the class[]
	 */
	public abstract Class<?>[] buildXmlClasses();

	/**
	 * Activate alternatives.
	 *
	 * @param e
	 *            the e
	 * @param classes
	 *            the classes
	 * @return the java archive
	 */
	public abstract JavaArchive activateAlternatives(JavaArchive e, Class<?>... classes);

	/**
	 * Import adapters libraries dependencies if needed
	 *
	 * @return the list
	 */
	public abstract Map<File, MavenResolvedArtifact> adaptersLibraries();

	/**
	 * Adds the optional.
	 *
	 * @param packages
	 *            the packages
	 * @return the java archive
	 */
	public abstract JavaArchive addOptional(TestableJarModules... packages);

	/**
	 * Adds optional classes in final testable jar.
	 *
	 * @param alternative
	 *            the alternative
	 * @param classes
	 *            the classes to add
	 * @return the updated java archive
	 */
	public abstract JavaArchive addOptionalClasses(boolean alternative, Class<?>... classes);

	/**
	 * Excludes given files as libraries in the final war. Pair represent artifact id, classifier
	 *
	 * @param artifactsInfo
	 *            list of artifact to exclude
	 * @return the builder itself
	 */
	public TestPackageBuilder exclude(@SuppressWarnings("unchecked") Pair<String, String>... artifactsInfo);

	/**
	 * Creates the alternatives bean xml.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the asset
	 */
	public abstract Asset createAlternativesBeanXml(Class<?>... clazz);

	/**
	 * Creates the deployment container.
	 *
	 * @return the web archive
	 */
	public abstract WebArchive createDeploymentContainer();

	/**
	 * Dependency libraries retrieved from deployment.
	 *
	 * @return the list of dependent libraries
	 */
	public abstract Map<File, MavenResolvedArtifact> dependencyLibraries();

	/**
	 * Gets the testable jar.
	 *
	 * @return the testable jar
	 */
	public abstract JavaArchive getTestableJar();

	/**
	 * Package war.
	 *
	 * @param packages
	 *            the packages
	 * @return the web archive
	 */
	public abstract WebArchive packageWar(TestableWarModules... packages);

}