package com.sirma.itt.pm.web.menu;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;

/**
 * The Class PMMenuActionTest.
 * 
 * @author svelikov
 */
@Test
public class PMMenuActionTest extends PMTest {

	/** The action. */
	private final PMMenuAction action;

	/** The authority service. */
	private final AuthorityService authorityService;

	/** The project instance. */
	private final ProjectInstance projectInstance;

	/**
	 * Instantiates a new pM menu action test.
	 */
	public PMMenuActionTest() {
		action = new PMMenuAction();

		projectInstance = createProjectInstance(Long.valueOf(1), "dmsId");

		authorityService = Mockito.mock(AuthorityService.class);

		ReflectionUtils.setField(action, "authorityService", authorityService);
	}

	/**
	 * Render members menu.
	 */
	@SuppressWarnings("boxing")
	public void renderMembersMenuTest() {
		// if no instance is passed we should not render the menu
		boolean renderMembersMenu = action.renderMembersMenu(null);
		Assert.assertFalse(renderMembersMenu);

		// if user has no privileges to manage resources we should not render the menu
		Mockito.when(
				authorityService.isActionAllowed(projectInstance,
						PmActionTypeConstants.MANAGE_RESOURCES, "")).thenReturn(Boolean.FALSE);
		renderMembersMenu = action.renderMembersMenu(projectInstance);
		Assert.assertFalse(renderMembersMenu);

		// if user has privileges to manage resources we should render the menu
		Mockito.when(
				authorityService.isActionAllowed(projectInstance,
						PmActionTypeConstants.MANAGE_RESOURCES, "")).thenReturn(Boolean.TRUE);
		renderMembersMenu = action.renderMembersMenu(projectInstance);
		Assert.assertTrue(renderMembersMenu);
	}
}
