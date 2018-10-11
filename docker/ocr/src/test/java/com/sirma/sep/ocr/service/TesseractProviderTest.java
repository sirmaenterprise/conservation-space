package com.sirma.sep.ocr.service;

import static org.jgroups.util.Util.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link TesseractProvider}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 04/12/2017
 */
public class TesseractProviderTest {

	@Mock
	private TesseractOCRProperties ocrProperties;

	@InjectMocks
	private TesseractProvider cut = new TesseractProvider(ocrProperties);

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(ocrProperties.getDatapath()).thenReturn("tessdata");
		when(ocrProperties.getLanguage()).thenReturn("eng");
		when(ocrProperties.getEngineMode()).thenReturn(3);
	}

	@Test
	public void test_getProvider() throws Exception {
		cut.initialize();
		Tesseract provider = (Tesseract) cut.getProvider();
		assertEquals(getConfigurationField(provider, "language"), "eng");
		assertEquals(getConfigurationField(provider, "datapath"), "tessdata");
		assertEquals(getConfigurationField(provider, "ocrEngineMode"), ITessAPI.TessOcrEngineMode.OEM_DEFAULT);
		assertEquals(getConfigurationField(provider, "psm"), ITessAPI.TessPageSegMode.PSM_AUTO_OSD);

	}

	private Object getConfigurationField(Object object, String fieldName) throws Exception {
		Field field = object.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(object);
	}
}