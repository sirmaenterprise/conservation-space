package com.sirma.cmf.web.instance;

import org.testng.annotations.Test;

/**
 * Tests for AttachInstanceAction.
 * 
 * @author svelikov
 */
@Test
public class AttachInstanceActionTest {

	private final AttachInstanceAction action;

	/**
	 * Instantiates a new attach instance action test.
	 */
	public AttachInstanceActionTest() {
		action = new AttachInstanceAction();
	}

	// FIXME: move and complete tests for attach from objects rest service and documents rest
	// service
	// /**
	// * Inits the test by reseting some fields.
	// */
	// @BeforeMethod
	// public void initTest() {
	// fetchNullInstance = false;
	// fetchNullSection = false;
	// referenceToDocumentInstance = false;
	// }
	//
	// public void attachDocuments() {
	// action.attachDocuments(null, null, null, null);
	//
	// // if section instance is not found we should get error message
	// fetchNullInstance = true;
	// Mockito.when(
	// typeConverter.convert(InstanceReference.class, DocumentInstance.class
	// .getSimpleName().toLowerCase())).thenReturn(documentInstance.toReference());
	// String requestData =
	// "{'currentInstanceId':'nonexistingsectionid', 'currentInstanceType':'sectioninstance', 'selectedItems':{'1':{'dbId':'emf:14b5b83e-7584-4dc6-967f-bbe3dfe31743','type':'documentinstance'}}}";
	// response = controller.attachDocuments(requestData);
	// assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
	//
	// // if section instance is found, we expect response with status code 200 and we also check
	// // if actual backend service invocation is done
	// fetchNullInstance = false;
	// referenceToDocumentInstance = true;
	// requestData =
	// "{'currentInstanceId':'emf:482c15b7-845f-4597-a9fb-a6b451a72578', 'currentInstanceType':'sectioninstance', 'selectedItems':{'1':{'dbId':'emf:14b5b83e-7584-4dc6-967f-bbe3dfe31743','type':'documentinstance'}}}";
	// response = controller.attachDocuments(requestData);
	// assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
	// Mockito.verify(instanceService, Mockito.atLeastOnce()).attach(Mockito.any(Instance.class),
	// Mockito.any(Operation.class), (Instance[]) Mockito.anyVararg());
	// Mockito.verify(instanceService, Mockito.atMost(1)).save(Mockito.any(CaseInstance.class),
	// Mockito.any(Operation.class));
	// Mockito.verify(instanceService, Mockito.atMost(1)).save(
	// Mockito.any(DocumentInstance.class), Mockito.any(Operation.class));
	// }

	// public void attachObjects() {
	// action.attachDocuments(null, null, null, null);
	// }
}
