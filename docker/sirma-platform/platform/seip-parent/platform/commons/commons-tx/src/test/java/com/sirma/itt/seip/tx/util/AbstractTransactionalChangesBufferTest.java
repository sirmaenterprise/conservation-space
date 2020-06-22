package com.sirma.itt.seip.tx.util;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link AbstractTransactionalChangesBuffer}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/11/2017
 */
public class AbstractTransactionalChangesBufferTest {

	@InjectMocks
	private Buffer buffer;
	@Mock
	private TransactionSupport transactionSupport;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		doAnswer(a -> {
			a.getArgumentAt(0, Executable.class).execute();
			return null;
		}).when(transactionSupport).invokeBeforeTransactionCompletion(any());
		doAnswer(a -> {
			a.getArgumentAt(0, Executable.class).execute();
			return null;
		}).when(transactionSupport).invokeOnSuccessfulTransactionInTx(any());
	}

	@Test
	public void shouldPassChangesToTheTransactionCompletionHandler() throws Exception {
		buffer.add("change1");
		buffer.add("change2");

		buffer.registerOnTransactionCompletionHandler(
				values -> assertEquals(Arrays.asList("change1", "change2"), values));
	}

	@Test
	public void shouldDoNothingIfNoChangesAreRegistered() throws Exception {
		AtomicBoolean gate = new AtomicBoolean(true);
		buffer.registerOnTransactionCompletionHandler(
				values -> gate.set(false));
		assertTrue("The value should not be changed", gate.get());
	}

	@Test
	public void shouldNotCallTheTransactionCompletionHandlerTwice() throws Exception {
		AtomicInteger gate = new AtomicInteger();
		buffer.add("change1");
		buffer.registerOnTransactionCompletionHandler(values -> gate.incrementAndGet());
		buffer.registerOnTransactionCompletionHandler(values -> gate.incrementAndGet());
		assertEquals("The value should not be changed", 1, gate.get());
	}

	static class Buffer extends AbstractTransactionalChangesBuffer<String> {
		Buffer(TransactionSupport transactionSupport) {
			super(transactionSupport);
		}
	}
}
