package com.sirma.sep.ocr.service;

import javax.annotation.PostConstruct;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class provides access to {@link Tesseract} by first initializing all configurations needed in order to
 * execute a good optical recognition.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @see Tesseract
 * @since 04/12/2017
 */
@Component
public class TesseractProvider {

	private TesseractOCRProperties ocrProperties;
	private ITesseract tesseract;

	@Autowired
	public TesseractProvider(TesseractOCRProperties ocrProperties) {
		this.ocrProperties = ocrProperties;
	}

	/**
	 * Initializes {@link Tesseract} with the configurations that work best for documents.
	 */
	@PostConstruct
	public void initialize() {
		tesseract = new Tesseract();
		tesseract.setDatapath(ocrProperties.getDatapath());

		// This is set by default, normally the ocr language should come from the jms message.
		tesseract.setLanguage(ocrProperties.getLanguage());

		// OEM_TESSERACT_ONLY	        0	Run Tesseract only - fastest
		// OEM_CUBE_ONLY	            1	Run Cube only - better accuracy, but slower
		// OEM_TESSERACT_CUBE_COMBINED	2	Run both and combine results - best accuracy
		// OEM_DEFAULT	                3	Specify this mode to indicate that any of the above modes should be
		//                                  automatically inferred from the variables in the language-specific config,
		//                                  or if not specified in any of the above should be set to the default
		//                                  OEM_TESSERACT_ONLY.
		tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_DEFAULT);

		// Auto detection of page segmentation
		tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO_OSD);

		//Enable table detection
		tesseract.setTessVariable("textord_tablefind_recognize_tables", "1");
	}

	/**
	 * Gets the {@link Tesseract} tool.
	 *
	 * @return the {@link Tesseract} tool.
	 */
	public ITesseract getProvider() {
		return tesseract;
	}
}
