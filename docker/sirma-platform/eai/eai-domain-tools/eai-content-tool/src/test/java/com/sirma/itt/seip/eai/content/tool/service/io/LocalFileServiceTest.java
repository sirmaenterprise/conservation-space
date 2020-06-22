/**
 * 
 */
package com.sirma.itt.seip.eai.content.tool.service.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

/**
 * Tests for {@link LocalFileService}
 * 
 * @author gshevkedov
 */
public class LocalFileServiceTest {

	private File tempFile;

	@Test
	public void testCreateFile() {
		tempFile = new File("test");
		File created = LocalFileService.createFile(tempFile, "xlsx", ".xlsx");
		assertNotNull(created);
		assertTrue(created.getParentFile().exists());
	}

	@Test
	public void testDelete() throws IOException {
		tempFile = new File("testDelete");
		try (FileWriter fileWriter = new FileWriter(tempFile)) {
			fileWriter.write("test");
		}
		assertTrue(tempFile.exists());
		LocalFileService.deleteFile(tempFile);
		assertFalse(tempFile.delete());
	}

	@Test
	public void testCreateDirectory() {
		tempFile = LocalFileService.createDirectory(null, "xlsx");
		assertNotNull(tempFile);
		assertTrue(tempFile.exists());

	}

	@Test
	public void testInit() {
		assertNotNull(LocalFileService.init(null));
	}

	@After
	public void tearDown() {
		LocalFileService.deleteFile(tempFile);
	}
}
