package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.FileDescriptor.CountingFileDescriptor;

/**
 * Tests for {@link FileDescriptor}
 *
 * @author BBonev
 */
public class FileDescriptorTest {

	@Test
	public void testCounting() throws Exception {
		FileDescriptor descriptor = FileDescriptor
				.create(() -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), 4L);

		assertNull(FileDescriptor.enableCounting(null));

		CountingFileDescriptor countingFileDescriptor = FileDescriptor.enableCounting(descriptor);

		assertEquals(countingFileDescriptor, FileDescriptor.enableCounting(countingFileDescriptor));

		assertEquals(-1, countingFileDescriptor.getTransferredBytes());

		countingFileDescriptor.asString();

		assertEquals(4, countingFileDescriptor.getTransferredBytes());
	}

	@Test
	public void test_create() throws Exception {
		FileDescriptor descriptor = FileDescriptor.create((Supplier<String>) null, (Supplier<InputStream>) null, -1);
		assertNotNull(descriptor);
		assertNull(descriptor.getInputStream());
		assertNull(descriptor.getId());
		assertNull(descriptor.getContainerId());
	}

	@Test
	public void writeToFile() throws Exception {
		FileDescriptor descriptor = FileDescriptor.create("name",
				() -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), 4L);

		File file = new File(UUID.randomUUID().toString());
		try {
			descriptor.writeTo(file);
			try (InputStream input = new FileInputStream(file)) {
				assertEquals("test", IOUtils.toString(input));
			}
		} finally {
			file.delete();
		}
	}
}
