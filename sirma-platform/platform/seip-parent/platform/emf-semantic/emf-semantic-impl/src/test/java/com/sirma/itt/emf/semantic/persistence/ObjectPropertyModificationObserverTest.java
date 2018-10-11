package com.sirma.itt.emf.semantic.persistence;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.context.ContextNotActiveException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkAddedEvent;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkRemovedEvent;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link ObjectPropertyModificationObserver}
 *
 * @author BBonev
 */
public class ObjectPropertyModificationObserverTest {

	@InjectMocks
	private ObjectPropertyModificationObserver observer;

	@Mock
	private InstanceService instanceService;
	@Spy
	private ChangedInstanceBuffer changedBuffer = new ChangedInstanceBuffer();
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	private LinkReference link;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		link = new LinkReference();
		link.setFrom(InstanceReferenceMock.createGeneric("emf:from"));
		link.setFrom(InstanceReferenceMock.createGeneric("emf:to"));
		link.setIdentifier("emf:parentOf");
	}

	@Test
	public void onLinkAdded() throws Exception {
		observer.onLinkAdded(new LinkAddedEvent(link));

		verify(instanceService).touchInstance(anyCollection());
	}

	@Test
	public void onLinkRemoved() throws Exception {
		observer.onLinkRemoved(new LinkRemovedEvent(link));

		verify(instanceService).touchInstance(anyCollection());
	}

	@Test
	public void onLinkAdded_notActiveContext() throws Exception {
		doThrow(ContextNotActiveException.class).when(changedBuffer).addChange(any());
		observer.onLinkAdded(new LinkAddedEvent(link));

		verify(instanceService).touchInstance(anyCollection());
	}

	@Test
	public void onLinkRemoved_notActiveContext() throws Exception {
		doThrow(ContextNotActiveException.class).when(changedBuffer).addChange(any());
		observer.onLinkRemoved(new LinkRemovedEvent(link));

		verify(instanceService).touchInstance(anyCollection());
	}

	@Test
	public void onLinkAdded_notActiveContextWhenGettingResults() throws Exception {
		when(changedBuffer.getChangesAndReset()).thenThrow(new ContextNotActiveException());
		observer.onLinkAdded(new LinkAddedEvent(link));

		verify(instanceService, never()).touchInstance(anyCollection());
	}
}
