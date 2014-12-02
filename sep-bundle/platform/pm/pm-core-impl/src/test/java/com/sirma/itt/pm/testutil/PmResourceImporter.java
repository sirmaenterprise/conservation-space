package com.sirma.itt.pm.testutil;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;

import com.sirma.itt.cmf.testutil.ResourceImporter;
import com.sirma.itt.cmf.testutil.TestPackageBuilder;

/**
 * The PmResourceImporter is responsible to import test and main resources
 */
public class PmResourceImporter extends ResourceImporter {

	/**
	 * Instantiates a new pm resource importer.
	 * 
	 * @param builder
	 *            the builder
	 * @param javaArchive
	 *            the java archive
	 */
	public PmResourceImporter(TestPackageBuilder builder, JavaArchive javaArchive) {
		super(builder, javaArchive);
	}

	/**
	 * {@inheritDoc}
	 */
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
			if (entry.getValue().getCoordinate().getArtifactId().contains("cmf-core-impl")
					&& coordinate.getClassifier().contains("tests")) {
				try {
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
	protected boolean isResourceAccepted(String name) {
		if (!name.contains("META-INF")
				&& (name.endsWith(".xml") || name.endsWith(".jar") || name.endsWith(".ser")
						|| name.endsWith(".template") || name.endsWith("dummy"))) {
			return true;
		}
		return false;
	}

}
