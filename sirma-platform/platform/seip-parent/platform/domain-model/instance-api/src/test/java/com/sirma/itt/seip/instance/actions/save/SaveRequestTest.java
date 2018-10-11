package com.sirma.itt.seip.instance.actions.save;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.InstanceSaveContext;

/**
 * Test for {@link SaveRequest}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class SaveRequestTest {

	@Before
	public void setup() {
		TypeConverterUtil.setTypeConverter(mock(TypeConverter.class));
	}

	@Test
	public void buildCreateRequest_shouldBuildRequestWithCreateOperation() {
		SaveRequest request = SaveRequest.buildCreateRequest(new EmfInstance("id"));
		assertEquals(ActionTypeConstants.CREATE, request.getUserOperation());
	}

	@Test
	public void buildUpdateRequest_shouldBuildRequestWithEditOperation() {
		SaveRequest request = SaveRequest.buildUpdateRequest(new EmfInstance("id"));
		assertEquals(ActionTypeConstants.EDIT_DETAILS, request.getUserOperation());
	}

	@Test
	public void buildSaveRequestWithPredicate_noInstanceId_shouldBuildRequetWithCreateOperation() {
		SaveRequest request = SaveRequest.buildSaveRequest(new EmfInstance(), instance -> false);
		assertEquals(ActionTypeConstants.CREATE, request.getUserOperation());
	}

	@Test
	public void buildSaveRequestWithPredicate_isPersistedFalse_shouldBuildRequetWithCreateOperation() {
		SaveRequest request = SaveRequest.buildSaveRequest(new EmfInstance("id"), instance -> false);
		assertEquals(ActionTypeConstants.CREATE, request.getUserOperation());
	}

	@Test
	public void buildSaveRequestWithPredicate_isPersistedTrue_shouldBuildRequetWithEditOperation() {
		SaveRequest request = SaveRequest.buildSaveRequest(new EmfInstance("id"), instance -> true);
		assertEquals(ActionTypeConstants.EDIT_DETAILS, request.getUserOperation());
	}

	@Test
	public void toSaveContext_shouldBuildSaveContextWithAvailableData() {
		Instance instance = new EmfInstance("id");
		Date versionCreatedOn = new Date();
		InstanceSaveContext saveContext = SaveRequest
				.buildSaveRequest(instance, versionCreatedOn, ActionTypeConstants.EDIT_DETAILS)
					.toSaveContext();
		assertEquals(ActionTypeConstants.EDIT_DETAILS, saveContext.getOperation().getUserOperationId());
		assertEquals(instance, saveContext.getInstance());
		assertEquals(versionCreatedOn, saveContext.getVersionCreationDate());
	}
}