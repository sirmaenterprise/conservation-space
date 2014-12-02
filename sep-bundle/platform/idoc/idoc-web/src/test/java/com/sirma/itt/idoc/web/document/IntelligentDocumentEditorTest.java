/**
 * Copyright (c) 2013 25.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.idoc.web.document;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import com.sirma.itt.cmf.beans.model.DocumentInstance;

/**
 * Tests {@link IntelligentDocumentEditor}
 * 
 * @author Adrian Mitev
 */
@org.testng.annotations.Test
public class IntelligentDocumentEditorTest {

	private static IntelligentDocumentEditor editor;

	/**
	 * Initializes CUT
	 */
	@BeforeClass
	public void init() {
		editor = new IntelligentDocumentEditor();
	}

	/**
	 * Tests canHandle() method. It should return true if the document has purpose =
	 * {@link IntelligentDocumentProperties#DOCUMENT_PURPOSE}.
	 */
	public void testCanHandle() {
		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setPurpose(IntelligentDocumentProperties.DOCUMENT_PURPOSE);

		boolean result = editor.canHandle(documentInstance);

		Assert.assertTrue(result);

		// test with random value
		documentInstance.setPurpose("something random");

		result = editor.canHandle(documentInstance);

		Assert.assertFalse(result);
	}

}
