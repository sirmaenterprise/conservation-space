package com.sirma.itt.emf.audit.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.emf.audit.command.instance.AuditObjectSubTypeCommand;
import com.sirma.itt.emf.audit.command.instance.AuditObjectTypeCommand;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the logic in {@link AuditObjectTypeCommand}
 *
 * @author Mihail Radkov
 */
public class AuditObjectTypeCommandTest {

	@InjectMocks
	private AuditCommand command = new AuditObjectTypeCommand();
	@Mock
	private FieldValueRetrieverService retrieverService;
	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	/**
	 * Before method.
	 */
	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(namespaceRegistryService.getShortUri(anyString())).then(a -> a.getArgumentAt(0, String.class));
	}

	/**
	 * Tests the retrieval of the object type for specific instance.
	 */
	@Test
	public void testObjectTypeCommand() {
		AuditablePayload payload = AuditCommandTest.getTestPayload();
		AuditActivity activity = new AuditActivity();

		// Correct test
		command.execute(payload, activity);
		assertEquals("emf:Case", activity.getObjectType());

		// Null instance
		activity = new AuditActivity();
		payload = new AuditablePayload(null, null, null, true);
		command.execute(payload, activity);
		assertNull(activity.getObjectType());
	}

	/**
	 * Tests the logic in {@link AuditObjectTypeCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has no object type or it's empty.
	 */
	@Test
	public void testLabelAssigningWithoutObjectType() {
		AuditActivity activity = new AuditActivity();

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectTypeLabel());

		activity.setObjectType("");

		command.assignLabel(activity, null);
		Assert.assertNull(activity.getObjectTypeLabel());
	}

	/**
	 * Tests the logic in {@link AuditObjectSubTypeCommand#assignLabel(AuditActivity, AuditContext)} when the provided
	 * activity has object type.
	 */
	@Test
	public void testLabelAssigning() {
		AuditActivity activity = new AuditActivity();
		activity.setObjectType("object_type");

		when(retrieverService.getLabel(eq(FieldId.OBJECT_TYPE), eq("object_type"), any(SearchRequest.class)))
				.thenReturn("LABEL labeL");

		command.assignLabel(activity, null);

		Assert.assertEquals("LABEL labeL", activity.getObjectTypeLabel());
	}
}
