package com.sirma.itt.seip.permissions.validator;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.permissions.action.AuthorityService;

/**
 * Test the permissions validator.
 *
 * @author nvelkov
 */
public class PermissionsValidatorTest {

	@Mock
	private AuthorityService authorityService;

	@Mock
	private LabelProvider labelProvider;

	@InjectMocks
	SavedSearchPermissionsValidator permissionsValidator;

	@BeforeClass
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the unique title validator with a tag instance.
	 */
	@Test
	public void validationSuccesfull() {
		mockAuthorityService(true);
		ValidationContext context = new ValidationContext(
				createInstance("test", "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#SavedFilter"),
				new Operation(ActionTypeConstants.EDIT_DETAILS));
		permissionsValidator.validate(context);
		Assert.assertEquals(context.getMessages().size(), 0);
	}

	/**
	 * Test the unique title validator with an instance that is not a tag. The validation should be succesfull.
	 */
	@Test
	public void validationFailedNoPermissions() {
		mockAuthorityService(false);
		ValidationContext context = new ValidationContext(
				createInstance("test", "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#SavedSearch"),
				new Operation(ActionTypeConstants.EDIT_DETAILS));
		permissionsValidator.validate(context);
		Assert.assertEquals(context.getMessages().size(), 1);
	}

	/**
	 * Creates a mock {@link Instance}.
	 *
	 * @param title
	 *            the mock's title
	 * @param uri
	 *            the mock's uri
	 * @return the mock instance
	 */
	private static Instance createInstance(String uri, String semanticType) {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.get(Matchers.eq(DefaultProperties.SEMANTIC_TYPE), Matchers.anyString())).thenReturn(
				semanticType);
		Mockito.when(instance.getId()).thenReturn(uri);
		return instance;
	}

	/**
	 * Mocks the authority service based on the users permissions.
	 *
	 * @param hasPermissions
	 *            true if the user has permissions.
	 */
	private void mockAuthorityService(boolean hasPermissions) {
		Mockito
				.when(authorityService.isActionAllowed(Matchers.any(Instance.class), Matchers.anyString(),
						Matchers.any()))
					.thenReturn(hasPermissions);
	}
}
