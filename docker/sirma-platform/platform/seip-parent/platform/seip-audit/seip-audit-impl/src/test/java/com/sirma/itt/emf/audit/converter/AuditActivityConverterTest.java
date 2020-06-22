package com.sirma.itt.emf.audit.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the functionality for converting in {@link AuditActivityConverter}.
 *
 * @author nvelkov
 * @author Mihail Radkov
 */
@RunWith(EasyMockRunner.class)
public class AuditActivityConverterTest {

	@Mock
	private FieldValueRetrieverService fieldValueRetrieverService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@TestSubject
	private AuditActivityConverter converter = new AuditActivityConverterImpl();

	/**
	 * Tests the logic behind {@link AuditActivityConverter#convertActivities(List)} when the provided activities have
	 * correct data in them - such as system IDs and context.
	 */
	@Test
	public void testActivityConvertingWithData() {
		Capture<String[]> retrieverCapture = mockRetrieverService();
		mockNamespaceRegistry();

		// Activity with some data
		AuditActivity activity = new AuditActivity();
		activity.setObjectSystemID("id");
		activity.setContext("1");

		List<AuditCommand> auditCommands = new ArrayList<>();
		AuditCommand command = EasyMock.createMock(AuditCommand.class);
		auditCommands.add(command);
		ReflectionUtils.setFieldValue(converter, "auditCommands", auditCommands);

		Capture<AuditActivity> activityCapture = EasyMock.newCapture();
		Capture<AuditContext> contextCapture = EasyMock.newCapture();
		mockAuditCommand(command, activityCapture, contextCapture);

		converter.convertActivities(Arrays.asList(activity));

		Assert.assertTrue(retrieverCapture.hasCaptured());
		Assert.assertTrue(activityCapture.hasCaptured());
		Assert.assertTrue(contextCapture.hasCaptured());

		// Tests if it's a copy
		AuditActivity capturedActivity = activityCapture.getValue();
		Assert.assertTrue(capturedActivity != activity);

		AuditContext capturedContext = contextCapture.getValue();
		Assert.assertFalse(capturedContext.getObjectHeaders().isEmpty());
	}

	/**
	 * Tests the logic behind {@link AuditActivityConverter#convertActivities(List)} when the provided activities
	 * <b>don't</b> have correct data in them - such as system IDs and context.
	 */
	@Test
	public void testActivityConvertingWithoutData() {
		Capture<String[]> retrieverCapture = mockRetrieverService();
		mockNamespaceRegistry();

		// Activity without data
		AuditActivity activity = new AuditActivity();

		List<AuditCommand> auditCommands = new ArrayList<>();
		AuditCommand command = EasyMock.createMock(AuditCommand.class);
		auditCommands.add(command);
		ReflectionUtils.setFieldValue(converter, "auditCommands", auditCommands);

		Capture<AuditActivity> activityCapture = EasyMock.newCapture();
		Capture<AuditContext> contextCapture = EasyMock.newCapture();
		mockAuditCommand(command, activityCapture, contextCapture);

		converter.convertActivities(Arrays.asList(activity));

		Assert.assertFalse(retrieverCapture.hasCaptured());
		Assert.assertTrue(activityCapture.hasCaptured());
		Assert.assertTrue(contextCapture.hasCaptured());

		// Tests if it's a copy
		AuditActivity capturedActivity = activityCapture.getValue();
		Assert.assertTrue(capturedActivity != activity);

		AuditContext capturedContext = contextCapture.getValue();
		Assert.assertTrue(capturedContext.getObjectHeaders().isEmpty());
	}

	/**
	 * Assigns the provided captures to the audit command.
	 *
	 * @param command
	 *            - the audit command
	 * @param activityCapture
	 *            - a capture for audit activity
	 * @param contextCapture
	 *            - a capture for audit context
	 */
	private void mockAuditCommand(AuditCommand command, Capture<AuditActivity> activityCapture,
			Capture<AuditContext> contextCapture) {
		command.assignLabel(EasyMock.capture(activityCapture), EasyMock.capture(contextCapture));
		EasyMock.expectLastCall().times(1);
		EasyMock.replay(command);
	}

	/**
	 * Mocks the {@link FieldValueRetrieverService} to return some dummy data.
	 */
	private Capture<String[]> mockRetrieverService() {
		Map<String, String> labels = new HashMap<>();
		labels.put("1", "awesome_label");

		Capture<String[]> idsCapture = EasyMock.newCapture();
		EasyMock
				.expect(fieldValueRetrieverService.getLabels(EasyMock.anyString(), EasyMock.capture(idsCapture),
						EasyMock.anyObject(SearchRequest.class)))
					.andReturn(labels)
					.anyTimes();
		EasyMock.replay(fieldValueRetrieverService);
		return idsCapture;
	}

	/**
	 * Mocks the {@link NamespaceRegistryService} to return some dummy data.
	 */
	private void mockNamespaceRegistry() {
		EasyMock
				.expect(namespaceRegistryService.buildFullUri(EasyMock.anyString()))
					.andReturn("someFullUri")
					.anyTimes();
		EasyMock.replay(namespaceRegistryService);
	}

}
