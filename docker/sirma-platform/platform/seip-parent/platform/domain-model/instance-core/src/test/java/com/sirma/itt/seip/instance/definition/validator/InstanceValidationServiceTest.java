package com.sirma.itt.seip.instance.definition.validator;

import java.io.Serializable;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validator.InstanceValidationServiceImpl;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test the context validation helper.
 *
 * @author nvelkov
 */
public class InstanceValidationServiceTest {

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private AuthorityService authorityService;
	@InjectMocks
	private InstanceValidationService helper = new InstanceValidationServiceImpl();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * A null error message should be returned when there is no error.
	 */
	@Test
	public void testInCorrectState() {
		mockIsActionAllowed(true);
		Optional<String> error = helper.canCreateOrUploadIn(new EmfInstance());
		Assert.assertFalse(error.isPresent());
	}

	/**
	 * An error message should be returned when the instance is in an incorrect state.
	 */
	@Test
	public void testInIncorrectState() {
		mockIsActionAllowed(false);
		Mockito.when(labelProvider.getValue(Matchers.anyString())).thenReturn("error");
		Optional<String> error = helper.canCreateOrUploadIn("instanceId", "create");
		Assert.assertEquals("error", error.get());
	}

	/**
	 * An error message should be returned when the instance is not found.
	 */
	@Test
	public void testInstanceNotFound() {
		Mockito.when(instanceTypeResolver.resolveReference(Matchers.any(Serializable.class)))
				.thenReturn(Optional.empty());
		Mockito.when(labelProvider.getValue(Matchers.anyString())).thenReturn("error");
		Optional<String> error = helper.canCreateOrUploadIn("instanceId", "create");
		Assert.assertEquals("error", error.get());
	}

	private void mockIsActionAllowed(boolean isActionAllowed) {
		InstanceReferenceMock reference = new InstanceReferenceMock();
		Mockito.when(instanceTypeResolver.resolveReference(Matchers.any(Serializable.class)))
				.thenReturn(Optional.of(reference));
		Mockito.when(authorityService.isActionAllowed(Matchers.any(Instance.class), Matchers.anyString(),
				Matchers.anyString())).thenReturn(isActionAllowed);
	}

}
