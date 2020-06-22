package com.sirma.sep.export;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link SupportedExportFormats}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 19/09/2017
 */
public class SupportedExportFormatsTest {
	@Test
	public void getSupportedFormat() throws Exception {
		assertEquals(SupportedExportFormats.PDF, SupportedExportFormats.getSupportedFormat("pdf"));
		assertEquals(SupportedExportFormats.WORD, SupportedExportFormats.getSupportedFormat("word"));
		assertEquals(SupportedExportFormats.XLS, SupportedExportFormats.getSupportedFormat("excel"));
		assertEquals(SupportedExportFormats.UNKNOWN, SupportedExportFormats.getSupportedFormat("some-other-format"));
	}
}