package com.sirma.sep.ocr.service;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests for {@link TesseractOCRProperties}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TesseractOCRPropertiesTest {

	@Autowired
	private TesseractOCRProperties ocr;

	@TestConfiguration
	@SpringBootApplication
	public static class OCRTessApplication {

		/**
		 * Invokes the {@link SpringApplication} initialization.
		 *
		 * @param args are the additional spring boot arguments
		 */
		public static void main(String[] args) {
			SpringApplication.run(OCRTessApplication.class, args);
		}
	}

	@Test
	public void testGetDatapath() throws Exception {
		assertNotNull(ocr.getDatapath());
	}

	@Test
	public void testGetLanguage() throws Exception {
		assertNotNull(ocr.getLanguage());
	}

	@Test
	public void testGetMimetype() throws Exception {
		assertNotNull(ocr.getMimetype());
		assertNotNull(ocr.getMimetype().getPattern());
	}

}
