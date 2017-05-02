/**
 *
 */
package com.sirma.itt.seip.security.context;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.ContextualList;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.collections.ContextualSet;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.concurrent.locks.ContextualSync;
import com.sirma.itt.seip.context.Contextual;

/**
 * The Class SecurityContextaulProducerTest.
 *
 * @author BBonev
 */
@Test
public class SecurityContextaulProducerTest {

	@Mock
	SecurityContext securityContext;

	@InjectMocks
	SecurityContextualProducer producer;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(securityContext.getCurrentTenantId()).thenReturn("test");
	}

	/**
	 * Produce reference.
	 */
	public void produceReference() {
		Contextual<Object> contextual = producer.produceReference();
		assertNotNull(contextual);
		assertEquals(contextual.getContextId(), "test");
	}

	/**
	 * Produce map.
	 */
	public void produceMap() {
		ContextualMap<Object, Object> contextual = producer.produceMap();
		assertNotNull(contextual);
		assertEquals(contextual.getContextId(), "test");
	}

	/**
	 * Produce set.
	 */
	public void produceSet() {
		ContextualSet<Object> contextual = producer.produceSet();
		assertNotNull(contextual);
		assertEquals(contextual.getContextId(), "test");
	}

	/**
	 * Produce lock.
	 */
	public void produceLock() {
		ContextualLock contextual = producer.produceLock();
		assertNotNull(contextual);
		assertEquals(contextual.getContextId(), "test");
	}

	/**
	 * Produce read write lock.
	 */
	public void produceReadWriteLock() {
		ContextualReadWriteLock contextual = producer.produceReadWriteLock();
		assertNotNull(contextual);
		assertEquals(contextual.getContextId(), "test");
	}

	/**
	 * Produce contextual sync.
	 */
	public void produceContextualSync() {
		ContextualSync contextual = producer.produceContextualSync();
		assertNotNull(contextual);
		assertEquals(contextual.getContextId(), "test");
	}

	/**
	 * Produce contextual list.
	 */
	public void produceContextualList() {
		ContextualList<Object> contextual = producer.produceContextualList();
		assertNotNull(contextual);
		assertEquals(contextual.getContextId(), "test");
	}

}
