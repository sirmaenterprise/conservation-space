package com.sirma.itt.emf.cls.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.cls.persister.CodeListPersister;
import com.sirma.itt.emf.cls.persister.SheetParser;
import com.sirma.itt.emf.cls.validator.SheetValidator;
import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.itt.emf.cls.validator.exception.SheetValidatorException;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.sep.cls.CodeListService;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;
import com.sirma.sep.cls.parser.CodeListSheet;
import com.sirma.sep.content.upload.UploadRequest;

import jxl.Sheet;

/**
 * Tests {@link CodeListResourceManagement}
 */
public class CodeListResourceManagementTest {

	@Mock
	private SheetValidator sheetValidator;

	@Mock
	private CodeListPersister persister;

	@Mock
	private SheetParser sheetParser;

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private CodeListService codeListService;

	@Mock
	private EventService eventService;

	@InjectMocks
	private CodeListResourceManagement codeListResource;

	@Before
	public void initializeTest() throws CodeValidatorException, SheetValidatorException {
		MockitoAnnotations.initMocks(this);
		mockCodeListSheetParser();
		mockPersister(true);
		mockCodeSheetValidator(true);
	}

	@Test
	public void testMarkedAsAdminResource() {
		AdminResource annotation = CodeListResourceManagement.class.getAnnotation(AdminResource.class);
		assertNotNull(annotation);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void shouldNotExtractSingleCodeList() throws JSONException, IOException {
		mockCodeListService((CodeList) null);
		codeListResource.getCodeList("999");
	}

	@Test
	public void shouldExtractSingleCodeList() throws JSONException, IOException {
		CodeList code = mockCodeList(true);
		mockCodeListService(code);
		CodeList response = codeListResource.getCodeList(code.getValue());
		assertEquals(code, response);
	}

	@Test
	public void shouldExportCodeListsAsWorkbook() throws JSONException, IOException {
		File mockedFile = File.createTempFile("temp", ".tmp");
		List<CodeList> codeLists = Collections.singletonList(mockCodeList(true));

		mockCodeListService(codeLists);
		when(tempFileProvider.createTempFile(any(), any())).thenReturn(mockedFile);

		Response response = codeListResource.exportCodeLists();
		assertEquals(mockedFile, response.getEntity());
		assertEquals(HttpStatus.SC_OK, response.getStatus());
		verify(tempFileProvider, times(1)).createTempFile(any(), any());
	}

	@Test
	public void shouldReloadCodeListsAfterOverwrite() throws CodeValidatorException, SheetValidatorException {
		UploadRequest request = mockRequest();
		codeListResource.uploadCodelists(request);
		verify(eventService, times(1)).fire(any(ResetCodelistEvent.class));
	}

	@Test
	public void shouldReloadCodeListsAfterUpload() throws SheetValidatorException, CodeValidatorException {
		UploadRequest request = mockRequest();
		codeListResource.addCodelists(request);
		verify(eventService, times(1)).fire(any(ResetCodelistEvent.class));
	}

	@Test
	public void shouldUpdateCodeList() throws CodeValidatorException {
		CodeList updated = mockCodeList(false);
		Response response = codeListResource.updateCodelist(updated);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		verify(persister, times(1)).persist(eq(updated));
	}

	@Test
	public void shouldReloadCodeListsAfterUpdate() throws CodeValidatorException {
		CodeList updated = mockCodeList(false);
		codeListResource.updateCodelist(updated);
		verify(eventService, times(1)).fire(any(ResetCodelistEvent.class));
	}

	@Test(expected = CodeValidatorException.class)
	public void shouldReturnBadRequestForInvalidListData() throws CodeValidatorException, SheetValidatorException {
		mockPersister(false);
		UploadRequest request = mockRequest();
		codeListResource.addCodelists(request);
	}

	@Test(expected = SheetValidatorException.class)
	public void shouldReturnBadRequestForInvalidSheetData() throws SheetValidatorException, CodeValidatorException {
		mockCodeSheetValidator(false);
		UploadRequest request = mockRequest();
		codeListResource.addCodelists(request);
	}

	private UploadRequest mockRequest() {
		FileItem fileItem = mock(FileItem.class);
		UploadRequest request = mock(UploadRequest.class);
		when(request.getRequestItems()).thenReturn(Collections.singletonList(fileItem));
		return request;
	}

	private CodeList mockCodeList(boolean values) {
		CodeList codeList = new CodeList();
		codeList.setValues(Collections.emptyList());
		if (values) {
			codeList.setValues(Collections.singletonList(new CodeValue()));
		}
		return codeList;
	}

	private CodeListSheet mockCodeListSheet(boolean values) {
		CodeListSheet sheet = new CodeListSheet();
		sheet.setCodeLists(Collections.singletonList(mockCodeList(values)));
		return sheet;
	}

	private void mockCodeListService(List<CodeList> codeLists) {
		when(codeListService.getCodeLists(Mockito.anyBoolean())).thenReturn(codeLists);
	}

	private void mockCodeListService(CodeList codeList) {
		Optional<CodeList> returnValue = codeList == null ? Optional.empty() : Optional.of(codeList);
		when(codeListService.getCodeList(Mockito.any(), Mockito.anyBoolean())).thenReturn(returnValue);
	}

	private void mockPersister(boolean valid) throws CodeValidatorException {
		if (!valid) {
			doThrow(new CodeValidatorException("Error")).when(persister).override(any());
			doThrow(new CodeValidatorException("Error")).when(persister).persist(anyList());
			doThrow(new CodeValidatorException("Error")).when(persister).persist(any(CodeList.class));
		}
	}

	private void mockCodeSheetValidator(boolean valid) throws SheetValidatorException {
		if (!valid) {
			doThrow(new SheetValidatorException("Error")).when(sheetValidator).getValidatedCodeListSheet(any());
		}
	}

	private void mockCodeListSheetParser() {
		when(sheetParser.parseFromSheet(any(Sheet.class))).thenReturn(mockCodeListSheet(true));
	}

}
