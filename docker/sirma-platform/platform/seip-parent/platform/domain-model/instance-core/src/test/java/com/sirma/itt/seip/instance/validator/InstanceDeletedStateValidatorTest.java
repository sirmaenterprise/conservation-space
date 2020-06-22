package com.sirma.itt.seip.instance.validator;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_DELETED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.ValidationContext;

/**
 * Unit test for {@link InstanceDeletedStateValidator}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class InstanceDeletedStateValidatorTest {

	@InjectMocks
	private InstanceDeletedStateValidator validator;

	@Mock
	private InstanceService instanceService;

	@Mock
	private LabelProvider labelProvider;

	@Test
	public void validate_passes() {
		EmfInstance instance = new EmfInstance();
		instance.add(IS_DELETED, Boolean.FALSE);
		when(instanceService.loadDeleted(any())).thenReturn(Optional.of(instance));
		ValidationContext context = new ValidationContext(instance, new Operation());
		validator.validate(context);
		assertTrue(context.getMessages().isEmpty());
	}

	@Test
	public void validate_alreadyDeleted_errorMsgAvailable() {
		EmfInstance instance = new EmfInstance();
		instance.add(IS_DELETED, Boolean.TRUE);
		when(instanceService.loadDeleted(any())).thenReturn(Optional.of(instance));
		ValidationContext context = new ValidationContext(instance, new Operation());
		validator.validate(context);
		assertFalse(context.getMessages().isEmpty());
	}
}