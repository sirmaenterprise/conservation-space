package com.sirma.itt.seip.util.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test class for FileUtil.
 */
public class FileUtilTest {

	@Test
	public void testConvertToValidFileName_invalidName() {
		assertEquals("_[a-z_A-Z_0-9]_____(&____)_____!____", FileUtil.convertToValidFileName(" ^[a-z,A-Z,0-9]*$`/{(&_%#@)}\\\":;!?~<>"));
		assertEquals("validFileName.txt", FileUtil.convertToValidFileName("validFileName.txt"));
	}

	@Test
	public void testCovertToValidFileName_longName() {
		StringBuffer longName = new StringBuffer(500);
		while (longName.length() < 500) {
			longName.append("a");
		}
		assertEquals(255, FileUtil.convertToValidFileName(longName.toString()).length());
	}
}
