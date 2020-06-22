package com.sirma.itt.emf.semantic.patch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sirma.itt.seip.exception.EmfRuntimeException;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.FileSystemResourceAccessor;

/**
 * Test for {@link RuntimeSemanticModelPatch}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class RuntimeSemanticModelPatchTest {

	private static final String PATCH_FILE_PREFIX = "testPatch-";
	private static File patchesDir = new File("src/test/resources/patchesDir");
	private static File patchDir = new File("src/test/resources/patchDir");

	@BeforeClass
	public static void setup() {
		patchesDir.mkdir();
		patchDir.mkdir();
	}

	@AfterClass
	public static void cleanup() {
		FileUtils.deleteQuietly(patchesDir);
		FileUtils.deleteQuietly(patchDir);
	}

	@SuppressWarnings("unused")
	@Test(expected = EmfRuntimeException.class)
	public void RuntimeSemanticModelPatch_nullLocation() {
		new RuntimeSemanticModelPatch(null);
	}

	@Test
	public void getPath_directoryWithFiles() throws IOException, ChangeLogParseException {
		File.createTempFile(PATCH_FILE_PREFIX, ".rdf", patchesDir);
		File.createTempFile(PATCH_FILE_PREFIX, ".sparql", patchesDir);
		File.createTempFile(PATCH_FILE_PREFIX, ".namespaces", patchesDir);
		File.createTempFile(PATCH_FILE_PREFIX, ".ns", patchesDir);
		File.createTempFile(PATCH_FILE_PREFIX, "invalidExtension.NS", patchesDir);

		RuntimeSemanticModelPatch patch = new RuntimeSemanticModelPatch(patchesDir);

		String path = patch.getPath();
		assertNotNull(path);
		assertTrue(path.contains("patchesDir"));
		assertTrue(path.endsWith("-changelog.xml"));

		// takes ~2 seconds to parse the file
		DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParameters(),
				new FileSystemResourceAccessor());
		assertEquals(4, changeLog.getChangeSets().size());
	}

	@Test
	public void getPath_dirWithChangelogFile() throws IOException, ChangeLogParseException {
		File.createTempFile(PATCH_FILE_PREFIX, "-changelog.xml", patchDir);

		RuntimeSemanticModelPatch patch = new RuntimeSemanticModelPatch(patchDir);

		String path = patch.getPath();
		assertNotNull(path);
		assertTrue(path.contains("patchDir"));
		assertTrue(path.endsWith("-changelog.xml"));
	}

	@Test
	public void getPath_changelogFile() throws IOException, ChangeLogParseException {
		File changelog = File.createTempFile(PATCH_FILE_PREFIX, "-changelog.xml");

		RuntimeSemanticModelPatch patch = new RuntimeSemanticModelPatch(changelog);

		String path = patch.getPath();
		assertNotNull(path);
		assertTrue(path.endsWith("-changelog.xml"));
	}
}