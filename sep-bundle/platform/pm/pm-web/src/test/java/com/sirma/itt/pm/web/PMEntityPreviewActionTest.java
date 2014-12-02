package com.sirma.itt.pm.web;

import static org.mockito.Matchers.any;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The Class PMEntityPreviewActionTest.
 * 
 * @author svelikov
 */
@Test
public class PMEntityPreviewActionTest extends PMTest {

	/** The action. */
	private final PMEntityPreviewAction action;

	/** The authority service. */
	private final AuthorityService authorityService;

	/**
	 * Instantiates a new pM entity preview action test.
	 */
	public PMEntityPreviewActionTest() {
		action = new PMEntityPreviewAction();

		authorityService = Mockito.mock(AuthorityService.class);

		ReflectionUtils.setField(action, "authorityService", authorityService);
	}

	/**
	 * Can open project test.
	 */
	public void canOpenProjectTest() {
		// if no instance is passed
		boolean canOpenProject = action.canOpenProject(null);
		assertFalse(canOpenProject);

		// if has no privileges to open the project
		Mockito.when(
				authorityService.hasPermission(any(Permission.class), any(ProjectInstance.class),
						any(EmfUser.class))).thenReturn(Boolean.FALSE);
		canOpenProject = action.canOpenProject(createProjectInstance(Long.valueOf(1), "dmsId"));
		assertFalse(canOpenProject);

		// if has privileges to open the project
		Mockito.when(
				authorityService.hasPermission(any(Permission.class), any(ProjectInstance.class),
						any(EmfUser.class))).thenReturn(Boolean.TRUE);
		canOpenProject = action.canOpenProject(createProjectInstance(Long.valueOf(1), "dmsId"));
		assertTrue(canOpenProject);
	}
}
