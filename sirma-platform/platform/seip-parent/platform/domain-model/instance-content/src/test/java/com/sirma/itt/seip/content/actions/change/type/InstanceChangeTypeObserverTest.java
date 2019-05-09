package com.sirma.itt.seip.content.actions.change.type;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.BeforeOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreProvider;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.LocalStore;

/**
 * Test for {@link InstanceChangeTypeObserver}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/02/2019
 */
public class InstanceChangeTypeObserverTest {

	@InjectMocks
	private InstanceChangeTypeObserver instanceChangeTypeObserver;

	@Mock
	private InstanceService instanceService;
	@Mock
	private InstanceContentService contentService;
	@Mock
	private ContentStoreProvider contentStoreProvider;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onInstanceTypeChange_shouldDoNothingIf_InstanceNotFoundDuringCreate() throws Exception {
		instanceChangeTypeObserver.onInstanceTypeChange(new BeforeOperationExecutedEvent(new Operation(
				ActionTypeConstants.CREATE), createImageInstance()));

		verify(contentService, never()).saveContent(any(), any(Content.class));
	}

	@Test
	public void onInstanceTypeChange_shouldDoNothingIf_InstanceIsNotUploaded() throws Exception {
		instanceChangeTypeObserver.onInstanceTypeChange(new BeforeOperationExecutedEvent(new Operation(
				ActionTypeConstants.CHANGE_TYPE), new EmfInstance()));

		verify(contentService, never()).saveContent(any(), any(Content.class));
	}

	@Test
	public void onInstanceTypeChange_shouldDoNothingIf_InstanceSemanticTypeDidNotChange() throws Exception {
		mockCurrentInstance(createImageInstance());

		instanceChangeTypeObserver.onInstanceTypeChange(new BeforeOperationExecutedEvent(new Operation(
				ActionTypeConstants.CHANGE_TYPE), createImageInstance()));

		verify(contentService, never()).saveContent(any(), any(Content.class));
	}

	@Test
	public void onInstanceTypeChange_shouldDoNothingIf_CouldNotFindInstanceContent() throws Exception {
		mockCurrentInstance(createDocumentInstance());
		when(contentService.getContent(any(Serializable.class), any())).thenReturn(ContentInfo.DO_NOT_EXIST);

		instanceChangeTypeObserver.onInstanceTypeChange(new BeforeOperationExecutedEvent(new Operation(
				ActionTypeConstants.CHANGE_TYPE), createImageInstance()));

		verify(contentService, never()).saveContent(any(), any(Content.class));
	}

	@Test
	public void onInstanceTypeChange_shouldDoNothingIf_ContentStoreDoesNotChange_ImageStoreNotEnabled() throws Exception {
		mockCurrentInstance(createDocumentInstance());
		mockCurrentContentLocation(LocalStore.NAME);
		mockNewStore(LocalStore.NAME);

		instanceChangeTypeObserver.onInstanceTypeChange(new BeforeOperationExecutedEvent(new Operation(
				ActionTypeConstants.CHANGE_TYPE), createImageInstance()));

		verify(contentService, never()).saveContent(any(), any(Content.class));
	}

	@Test
	public void onInstanceTypeChange_shouldTriggerContentMove_IfChangedToImage() throws Exception {
		mockCurrentInstance(createDocumentInstance());
		mockCurrentContentLocation(LocalStore.NAME);
		mockNewStore("iiif");

		instanceChangeTypeObserver.onInstanceTypeChange(new BeforeOperationExecutedEvent(new Operation(
				ActionTypeConstants.CHANGE_TYPE), createImageInstance()));

		verify(contentService).saveContent(any(), any(Content.class));
	}

	@Test
	public void onInstanceTypeChange_shouldTriggerContentMove_IfChangedFromImage() throws Exception {
		mockCurrentInstance(createImageInstance());
		mockCurrentContentLocation("iiif");
		mockNewStore(LocalStore.NAME);

		instanceChangeTypeObserver.onInstanceTypeChange(new BeforeOperationExecutedEvent(new Operation(
				ActionTypeConstants.CHANGE_TYPE), createDocumentInstance()));

		verify(contentService).saveContent(any(), any(Content.class));
	}

	@Test
	public void onInstanceTypeChange_shouldRemoveOldContentPreview_IfExists() throws Exception {
		mockCurrentInstance(createDocumentInstance());
		mockCurrentContentLocation(LocalStore.NAME);
		mockNewStore("iiif");

		instanceChangeTypeObserver.onInstanceTypeChange(new BeforeOperationExecutedEvent(new Operation(
				ActionTypeConstants.CHANGE_TYPE), createImageInstance()));

		verify(contentService).saveContent(any(), any(Content.class));
		verify(contentService).deleteContent("emf:instanceId", Content.PRIMARY_CONTENT_PREVIEW);
	}

	private void mockNewStore(String storeName) {
		ContentStore localStore = mock(ContentStore.class);
		when(localStore.getName()).thenReturn(storeName);
		when(contentStoreProvider.getStore(any(), any())).thenReturn(localStore);
	}

	private void mockCurrentContentLocation(String storeName) {
		ContentInfo currentContentInfo = mock(ContentInfo.class);
		when(currentContentInfo.getRemoteSourceName()).thenReturn(storeName);
		when(currentContentInfo.getInstanceId()).thenReturn("emf:instanceId");
		when(currentContentInfo.getId()).thenReturn("emf:contentId");
		when(currentContentInfo.exists()).thenReturn(true);
		when(contentService.getContent(any(Serializable.class), any())).thenReturn(currentContentInfo);
	}

	private void mockCurrentInstance(Instance instance) {
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(instance);
	}

	private Instance createImageInstance() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instanceId");
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "emf:contentId");
		instance.setType(InstanceType.create(EMF.IMAGE.toString()));
		return instance;
	}

	private Instance createDocumentInstance() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instanceId");
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, "emf:contentId");
		instance.setType(InstanceType.create(EMF.DOCUMENT.toString()));
		return instance;
	}
}