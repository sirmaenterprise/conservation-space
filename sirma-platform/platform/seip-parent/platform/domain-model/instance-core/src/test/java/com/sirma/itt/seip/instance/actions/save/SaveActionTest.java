package com.sirma.itt.seip.instance.actions.save;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link SaveAction}.
 *
 * @author A. Kunchev
 */
public class SaveActionTest {

	@InjectMocks
	private SaveAction action;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Before
	public void setup() {
		action = new SaveAction();
		MockitoAnnotations.initMocks(this);

		TypeConverterUtil.setTypeConverter(mock(TypeConverter.class));
	}

	@Test
	public void getName() {
		assertEquals("save", action.getName());
	}

	@Test
	public void perform_successful() {
		EmfInstance instance = new EmfInstance();
		Date versionCreatedOn = new Date();
		SaveRequest request = SaveRequest.buildSaveRequest(instance, versionCreatedOn, ActionTypeConstants.CREATE);

		action.perform(request);

		Mockito.verify(domainInstanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			assertEquals(instance, context.getInstance());
			assertEquals(versionCreatedOn, context.getVersionCreationDate());
			assertEquals(ActionTypeConstants.CREATE, context.getOperation().getOperation());
			assertEquals(ActionTypeConstants.CREATE, context.getOperation().getUserOperationId());
		})));
	}
}