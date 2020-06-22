package com.sirma.itt.seip.instance.content;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PURPOSE_IDOC;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for {@link AssignPrimaryContentStep}.
 *
 * @author A. Kunchev
 */
public class AssignPrimaryContentStepTest {

	@InjectMocks
	private AssignPrimaryContentStep step;

	@Mock
	private InstanceContentService instanceContentService;

	@Before
	public void setup() {
		step = new AssignPrimaryContentStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void beforeSave_withoutPrimaryContentIdPurposableInstance_purposeNotNull() {
		WithPurpose instance = new WithPurpose("instance-id");
		instance.add(PRIMARY_CONTENT_ID, null);
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		step.beforeSave(context);
		verify(instanceContentService, never()).assignContentToInstance(anyString(), eq("instance-id"),
				eq(Content.PRIMARY_CONTENT));
		assertEquals(PURPOSE_IDOC, ((Purposable) context.getInstance()).getPurpose());
	}

	@Test
	public void beforeSave_withPrimaryContentIdNotPurposableInstance() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		instance.add(PRIMARY_CONTENT_ID, "instance-primary-content-id");
		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		step.beforeSave(context);
		verify(instanceContentService).assignContentToInstance(eq("instance-primary-content-id"), eq("instance-id"),
				eq(Content.PRIMARY_CONTENT));
	}

	@Test
	public void testRollbackBeforeSaveWithAssignedContent() {
		step.rollbackBeforeSave(mockContext(true));

		verify(instanceContentService, times(1)).deleteContent("contentId", "any");
	}

	@Test
	public void testRollbackBeforeSaveWithoutAssignedContent() {
		step.rollbackBeforeSave(mockContext(false));

		verify(instanceContentService, times(0)).deleteContent("contentId", "any");
	}

	private InstanceSaveContext mockContext(boolean assigned) {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "contentId");

		InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation());
		context.put(AssignPrimaryContentStep.CONTENT_ASSIGNED_KEY, assigned);

		return context;
	}

	@Test
	public void getName() {
		assertEquals("assignPrimaryContent", step.getName());
	}

	private static class WithPurpose extends EmfInstance implements Purposable {

		private static final long serialVersionUID = 7878167648791474136L;

		private static final String PURPOSE = "purpose";

		public WithPurpose(String id) {
			setId(id);
		}

		@Override
		public String getPurpose() {
			return getString(PURPOSE);
		}

		@Override
		public void setPurpose(String purpose) {
			add(PURPOSE, purpose);
		}

	}

}
