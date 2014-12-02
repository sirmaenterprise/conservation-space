package com.sirma.cmf.web.upload;

import static org.testng.Assert.assertNull;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Class UploadRestServiceTest.
 * 
 * @author sdjulgerova
 */
@Test
public class UploadRestServiceTest extends CMFTest {

	/** The controller under test. */
	private final UploadRestService controller;

	/** The dictionary service. */
	private final DictionaryService dictionaryService;

	/** The type converter. */
	private final TypeConverter typeConverter;
	
	/** The codelist service. */
	private final CodelistService codelistService;

	/**
	 * Instantiates a new upload controller test.
	 */
	public UploadRestServiceTest() {
		controller = new UploadRestService() {

		};

		dictionaryService = Mockito.mock(DictionaryService.class);
		typeConverter = Mockito.mock(TypeConverter.class);
		codelistService = Mockito.mock(CodelistService.class);

		ReflectionUtils.setField(controller, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(controller, "typeConverter", typeConverter);
		ReflectionUtils.setField(controller, "codelistService", codelistService);
	}
	
	/**
	 * retrieve allowed types test
	 */
	public void retrieveAllowedTypesTest() {
		// for null we should get nulls
		String result = controller.retrieveAllowedTypes("sectioninstance", null);
		assertNull(result);

		Instance instance = createSectionInstance(Long.valueOf(1));
		
		// NOTE: can not be tested at the moment. I have to make rest service more tastable
		//result = controller.retrieveAllowedTypes("sectioninstance", instance.getIdentifier());
	}

}
