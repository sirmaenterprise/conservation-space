package com.sirma.itt.cmf.testutil;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;

import com.sirma.itt.seip.testutil.ResourceImporter;
import com.sirma.itt.seip.testutil.TestPackageBuilder;

/**
 * The CmfResourceImporter is responsible to import test and main resources
 */
public class CmfResourceImporter extends ResourceImporter {

	/**
	 * Instantiates a new pm resource importer.
	 *
	 * @param builder
	 *            the builder
	 * @param javaArchive
	 *            the java archive
	 */
	public CmfResourceImporter(TestPackageBuilder builder, JavaArchive javaArchive) {
		super(builder, javaArchive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JavaArchive importData() {
		importResources(builder.dependencyLibraries());
		return super.importData();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void importResources(Map<File, MavenResolvedArtifact> libs) {
		for (Entry<File, MavenResolvedArtifact> entry : libs.entrySet()) {
			// import from cmf-core-impl:test-jar
			MavenCoordinate coordinate = entry.getValue().getCoordinate();
			if (entry.getValue().getCoordinate().getArtifactId().contains("emf-core-impl")
					&& coordinate.getClassifier().contains("tests")) {
				try {
					System.out.println("CmfResourceImporter.importResources() " + testResourcesDir.getAbsolutePath());
					unzipJar(testResourcesDir, entry.getKey());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isResourceAccepted(String name) {
		if (!name.contains("META-INF") && (name.endsWith(".xml") || name.endsWith(".jar") || name.endsWith(".ser")
				|| name.endsWith(".template") || name.endsWith("dummy"))) {
			return true;
		}
		return false;
	}

}
