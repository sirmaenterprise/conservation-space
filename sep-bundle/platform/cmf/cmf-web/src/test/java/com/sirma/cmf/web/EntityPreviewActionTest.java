package com.sirma.cmf.web;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.model.Permission;

/**
 * The Class EntityPreviewActionTest.
 * 
 * @author svelikov
 */
@Test
public class EntityPreviewActionTest extends CMFTest {

	/** The action. */
	private final EntityPreviewAction action;

	/** The authority service. */
	private final AuthorityService authorityService;

	/**
	 * Instantiates a new entity preview action test.
	 */
	public EntityPreviewActionTest() {
		action = new EntityPreviewAction();

		authorityService = Mockito.mock(AuthorityService.class);

		ReflectionUtils.setField(action, "authorityService", authorityService);
	}

	/**
	 * Can open instance test.
	 */
	public void canOpenInstanceTest() {
		boolean canOpenInstance = action.canOpenInstance(null);
		assertFalse(canOpenInstance);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1));
		// we will test only that the method will return true or false
		// we will customize the mock to return true or false just for the test
		// if authorityService returns false, we should get false as result
		Mockito.when(
				authorityService.hasPermission(any(Permission.class), any(CaseInstance.class),
						any(Resource.class))).thenReturn(Boolean.FALSE);
		canOpenInstance = action.canOpenInstance(caseInstance);
		assertFalse(canOpenInstance);

		// if authorityService returns true, we should get true as result
		when(
				authorityService.hasPermission(any(Permission.class), any(CaseInstance.class),
						any(Resource.class))).thenReturn(Boolean.TRUE);
		canOpenInstance = action.canOpenInstance(caseInstance);
		assertTrue(canOpenInstance);
	}

	/**
	 * Can open task test. We will test only the negative cases when null is passed as instance and
	 * when provided workflow task has no workflow instance context inside. The other cases are
	 * covered in EntityPreviewActionTest#canOpenInstanceTest
	 */
	public void canOpenTaskTest() {
		boolean canOpenTask = action.canOpenTask(null);
		assertFalse(canOpenTask);
	}

	/**
	 * Can edit case test.
	 */
	public void canEditCaseTest() {
		boolean canEditCase = action.canEditCase(null);
		assertFalse(canEditCase);
	}

	/**
	 * Test method for retrieving permission based on null-able standalone instance.
	 */
	public void canEditStandaloneTaskTest() {
		boolean canEditStandaloneTask = action.canEditStandaloneTask(null);
		assertFalse(canEditStandaloneTask);
	}

}
