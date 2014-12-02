package com.sirma.cmf.web.document;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.document.DocumentConstants;
import com.sirma.cmf.web.document.DocumentRevertAction;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * DocumentRevertActrionTest class.
 * 
 * @author svelikov
 */
@Test
public class DocumentRevertActionTest extends CMFTest {

	/** Comment for docRevertAction. */
	private DocumentRevertAction docRevertAction;

	/** The Constant EMPTY_STRING. */
	private static final String EMPTY_STRING = "";

	private CaseDocumentsTableAction caseDocumentsTableAction;

	private DocumentService documentService;

	/** . */
	public DocumentRevertActionTest() {

		docRevertAction = new DocumentRevertAction() {

			/** Comment for serialVersionUID. */
			private static final long serialVersionUID = 1561591188194522161L;

			private DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			@Override
			public void setDocumentContext(DocumentContext docContext) {
				this.documentContext = docContext;
			}

		};

		caseDocumentsTableAction = Mockito.mock(CaseDocumentsTableAction.class);
		documentService = Mockito.mock(DocumentService.class);

		ReflectionUtils.setField(docRevertAction, "log", LOG);
		ReflectionUtils.setField(docRevertAction, "caseDocumentsTableAction",
				caseDocumentsTableAction);
		ReflectionUtils.setField(docRevertAction, "documentService", documentService);
	}

	/**
	 * Inits the version revert test.
	 */
	public void initVersionRevertTest() {
		DocumentInstance docInstance = createDocumentInstance(1L);
		String version = "1.0";
		docRevertAction.initVersionRevert(docInstance, version);

		assertEquals(docRevertAction.isMajorVersion(), false);
		assertEquals(docRevertAction.getDescription(), DocumentRevertActionTest.EMPTY_STRING);
		assertEquals(docRevertAction.getDocumentVersion(), version);
		assertEquals(docRevertAction.getHistoricalDocument(), docInstance);
	}

	/**
	 * Execute document revert test.
	 */
	public void executeDocumentRevertTest() {
		String versionDescription = "some description";
		boolean isMajorVersion = true;

		DocumentInstance docInstance = createDocumentInstance(1l);
		docInstance.setProperties(new HashMap<String, Serializable>());
		docRevertAction.getDocumentContext().put(DocumentConstants.DOCUMENT_LATEST_VERSION,
				docInstance);
		docRevertAction.setMajorVersion(isMajorVersion);
		docRevertAction.setDescription(versionDescription);

		// call action method under test
		String navigationString = docRevertAction.executeDocumentRevert();

		// asserts
		assertEquals(navigationString, NavigationConstants.RELOAD_PAGE);

		assertEquals(docInstance.getProperties().get(DocumentProperties.IS_MAJOR_VERSION),
				isMajorVersion);

		assertEquals(docInstance.getProperties().get(DocumentProperties.VERSION_DESCRIPTION),
				versionDescription);

	}
}
