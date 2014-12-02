package com.sirma.itt.cmf.testutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;

import com.sirma.itt.emf.domain.Pair;

/**
 * The ResourceImporter is responsible to import test and main resources
 */
public class ResourceImporter {

	/** The java archive. */
	protected JavaArchive javaArchive;

	/** The builder. */
	protected TestPackageBuilder builder;

	/** The test resources dir. */
	protected File testResourcesDir;

	/** The main resources dir. */
	protected File mainResourcesDir;

	/**
	 * Instantiates a new resource impoter.
	 *
	 * @param builder
	 *            the builder
	 * @param javaArchive
	 *            the java archive
	 */
	public ResourceImporter(TestPackageBuilder builder, JavaArchive javaArchive) {
		this.builder = builder;
		this.javaArchive = javaArchive;
		testResourcesDir = new File("src/test/resources/");
		mainResourcesDir = new File("src/main/resources/");
	}

	/**
	 * Import data.
	 *
	 * @return the java archive
	 */
	public JavaArchive importData() {
		recursiveAddResources(mainResourcesDir, "src/main/resources/");
		recursiveAddResources(testResourcesDir, "src/test/resources/");
		return javaArchive;
	}

	/**
	 * Recursive add resources to a jar file.
	 *
	 * @param resources
	 *            the resources
	 * @param prefix
	 *            the prefix
	 */
	protected void recursiveAddResources(File resources, String prefix) {
		String replacement = pathToString(prefix.split("/"));
		File[] listFiles = resources.listFiles();
		for (File file : listFiles) {
			if (file.isDirectory()) {
				recursiveAddResources(file, prefix);
			} else {
				String newName = file.toString().replace(replacement, "").replaceAll("\\\\", "/");
				javaArchive.addAsResource(file, newName);
			}
		}
	}

	/**
	 * Convert to string list of string, separated by the.
	 *
	 * @param split
	 *            the split
	 * @return the string {@link File#separatorChar}
	 */
	protected String pathToString(String[] split) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < split.length; i++) {
			String file = split[i];
			builder.append(file).append(File.separatorChar);
		}
		return builder.toString();
	}

	/**
	 * Import external resources.
	 *
	 * @param libs
	 *            the libs
	 */
	protected void importExternalResources(List<Pair<File, MavenResolvedArtifact>> libs) {

	}

	/**
	 * Unzip jar to specified location with its structure.
	 *
	 * @param destinationDir
	 *            the destination dir bse path
	 * @param jarPath
	 *            the jar file location
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void unzipJar(File destinationDir, File jarPath) throws IOException {
		JarFile jar = new JarFile(jarPath);
		// now create all files
		for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
			JarEntry entry = enums.nextElement();
			if (!isResourceAccepted(entry.getName())) {
				continue;
			}
			File file = new File(destinationDir, entry.getName());
			file.getParentFile().mkdirs();
			File nextFile = new File(destinationDir, entry.getName());
			InputStream is = jar.getInputStream(entry);
			FileOutputStream fos = new FileOutputStream(nextFile);
			IOUtils.copy(is, fos);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(fos);
		}
		jar.close();
	}

	/**
	 * Checks if is resource accepted.
	 *
	 * @param name
	 *            the name
	 * @return true, if is resource accepted
	 */
	protected boolean isResourceAccepted(String name) {
		return true;
	}

}
