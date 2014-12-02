package com.sirma.cmf.web.actionsmanager;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.constants.allowed_action.AllowedActionType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;

/**
 * ActionButtonRenderConditions test class.
 * 
 * @author svelikov
 */
@Test
public class ActionButtonRenderConditionsTest extends CMFTest {

	/**
	 * Class under test.
	 */
	private final ActionButtonRenderConditions actionButtonRenderConditions;

	/**
	 * Initializes the test.
	 */
	public ActionButtonRenderConditionsTest() {
		actionButtonRenderConditions = new ActionButtonRenderConditions();
	}

	/**
	 * Test if method returns proper results.
	 */
	public void shouldRenderDefaultButtonTest() {
		Action action = new EmfAction(AllowedActionType.DOCUMENT_DELETE.getType());

		// no default action is passed and should return false
		boolean actualResult = actionButtonRenderConditions.shouldRenderDefaultButton(action, null);
		Assert.assertTrue(actualResult);

		// not default action is passed and should return false
		action = new EmfAction(AllowedActionType.DOCUMENT_EDIT_OFFLINE.getType());
		actualResult = actionButtonRenderConditions.shouldRenderDefaultButton(action, null);
		Assert.assertFalse(actualResult);

		action = new EmfAction(AllowedActionType.DOCUMENT_UPLOAD_NEW_VERSION.getType());
		actualResult = actionButtonRenderConditions.shouldRenderDefaultButton(action, null);
		Assert.assertFalse(actualResult);
	}

	/**
	 * Test if method returns proper results.
	 */
	public void shouldUploadNewVersionButtonTest() {
		Action action = new EmfAction(AllowedActionType.DOCUMENT_DELETE.getType());

		// different action is passed and should return false
		boolean actualResult = actionButtonRenderConditions.shouldUploadNewVersionButton(action,
				null);
		Assert.assertFalse(actualResult);

		// upload new version action is passed and should return true
		action = new EmfAction(AllowedActionType.DOCUMENT_UPLOAD_NEW_VERSION.getType());
		actualResult = actionButtonRenderConditions.shouldUploadNewVersionButton(action, null);
		Assert.assertTrue(actualResult);
	}

	/**
	 * Test if method returns proper results.
	 */
	public void renderEditOfflineAndDownloadButton() {
		Action action = new EmfAction(AllowedActionType.DOCUMENT_DELETE.getType());

		// different action is passed and should return false
		boolean actualResult = actionButtonRenderConditions.renderEditOfflineButton(action);
		Assert.assertFalse(actualResult);

		// DOCUMENT_EDIT_OFFLINE action is passed and should return true
		action = new EmfAction(AllowedActionType.DOCUMENT_EDIT_OFFLINE.getType());
		actualResult = actionButtonRenderConditions.renderEditOfflineButton(action);
		Assert.assertTrue(actualResult);
	}

	/**
	 * Render download button test.
	 */
	public void renderDownloadButtonTest() {
		Action action = new EmfAction(AllowedActionType.DOCUMENT_DELETE.getType());

		// different action is passed and should return false
		boolean actualResult = actionButtonRenderConditions.renderDownloadButton(action, null);
		Assert.assertFalse(actualResult);

		// DOWNLOAD action is passed and should return true
		action = new EmfAction(AllowedActionType.DOWNLOAD.getType());
		actualResult = actionButtonRenderConditions.renderDownloadButton(action, null);
		Assert.assertTrue(actualResult);
	}

	/**
	 * Should render task reassign button test.
	 */
	public void shouldRenderTaskReassignButtonTest() {
		Action action = new EmfAction(AllowedActionType.DOCUMENT_DELETE.getType());

		// different action is passed and should return false
		boolean actualResult = actionButtonRenderConditions.renderEditOfflineButton(action);
		Assert.assertFalse(actualResult);

		// REASSIGN_TASK action is passed and should return true
		action = new EmfAction(AllowedActionType.REASSIGN_TASK.getType());
		actualResult = actionButtonRenderConditions.shouldRenderTaskReassignButton(action, null);
		Assert.assertTrue(actualResult);
	}

	/**
	 * Render attach document button test.
	 */
	public void renderAttachDocumentButtonTest() {
		Action action = new EmfAction(AllowedActionType.DOCUMENT_DELETE.getType());

		// different action is passed and should return false
		boolean actualResult = actionButtonRenderConditions
				.renderAttachDocumentButton(action, null);
		Assert.assertFalse(actualResult);

		// required DOCUMENT_ATTACH action is passed and should return true
		action = new EmfAction(AllowedActionType.DOCUMENT_ATTACH.getType());
		actualResult = actionButtonRenderConditions.renderAttachDocumentButton(action, null);
		Assert.assertTrue(actualResult);
	}

	/**
	 * Render stop button for workflow instance.
	 */
	public void renderStopWorkflowButtonTest() {
		Action action = new EmfAction(AllowedActionType.STOP.getType());
		Instance workflowInstance = createWorkflowInstance(Long.valueOf(1));

		boolean actualResult = actionButtonRenderConditions.shouldRenderStopButton(action,
				workflowInstance);

		Assert.assertTrue(actualResult);
	}

	/**
	 * Render stop button for case instance.
	 */
	public void renderStopCaseButtonTest() {
		Action action = new EmfAction(AllowedActionType.STOP.getType());
		Instance caseInstance = createCaseInstance(Long.valueOf(1));

		boolean actualResult = actionButtonRenderConditions.shouldRenderStopButton(action,
				caseInstance);

		Assert.assertFalse(actualResult);
	}

	/**
	 * Render no permission message.
	 */
	public void renderNoPermissionMessageTest() {
		Action action = new EmfAction(ActionTypeConstants.NO_PERMISSIONS);
		boolean actionResult = actionButtonRenderConditions.renderNoPermissionMessage(action);
		Assert.assertTrue(actionResult);
	}

}
