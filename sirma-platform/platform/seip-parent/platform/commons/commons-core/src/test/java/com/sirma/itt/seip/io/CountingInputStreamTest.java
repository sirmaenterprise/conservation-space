/**
 *
 */
package com.sirma.itt.seip.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Tests for {@link CountingInputStream}.
 * 
 * @author BBonev
 */
public class CountingInputStreamTest {

	@Test
	public void countViaReadAll() throws Exception {
		try (CountingInputStream countingStream = new CountingInputStream(
				new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)))) {

			IOUtils.toString(countingStream);

			assertEquals(4, countingStream.getCount());
		}
	}

	@Test
	public void testWithMark() throws Exception {
		try (CountingInputStream countingStream = new CountingInputStream(
				new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)))) {
			countingStream.read();
			countingStream.mark(2);
			countingStream.read();
			countingStream.reset();
			countingStream.skip(2);
			countingStream.read();
			assertEquals(-1, countingStream.read());

			assertEquals(4, countingStream.getCount());
		}
	}

}
