package com.sirma.itt.seip.concurrent;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

/**
 * Test for {@link SimpleFuture}
 *
 * @author BBonev
 */
public class SimpleFutureTest {

	@Test(expected = ExecutionException.class)
	public void getShouldFailOnExecutionError() throws Exception {
		SimpleFuture<Object> future = new SimpleFuture<>(new Exception());
		future.get();
	}

	@Test(expected = ExecutionException.class)
	public void getShouldFailOnExecutionErrorViaMethod() throws Exception {
		FutureCallback callback = mock(FutureCallback.class);
		SimpleFuture<Object> future = new SimpleFuture<>(callback);
		future.failed(new Exception());
		try {
			future.get();
		} catch (ExecutionException e) {
			verify(callback).failed(any());
			throw e;
		}
	}

	@Test(expected = ExecutionException.class)
	public void getShouldFailOnExecutionErrorViaMethod_noCallback() throws Exception {
		SimpleFuture<Object> future = new SimpleFuture<>();
		future.failed(new Exception());
		future.get();
	}

	@Test(expected = ExecutionException.class)
	public void shouldNotSetTwiceException() throws Exception {
		FutureCallback callback = mock(FutureCallback.class);
		SimpleFuture<Object> future = new SimpleFuture<>(callback);
		future.failed(new Exception());
		future.failed(new Exception());
		try {
			future.get();
		} catch (ExecutionException e) {
			verify(callback).failed(any());
			throw e;
		}
	}

	@Test(expected = CancellationException.class)
	public void getShouldFailInCanceled() throws Exception {
		FutureCallback callback = mock(FutureCallback.class);
		SimpleFuture future = new SimpleFuture<>(callback);
		future.cancel();
		future.get();
		verify(callback).cancelled();
	}

	@Test(expected = CancellationException.class)
	public void getShouldFailInCanceled_noCallback() throws Exception {
		SimpleFuture future = new SimpleFuture<>();
		future.cancel();
		future.get();
	}

	@Test(expected = CancellationException.class)
	public void getShouldNotTwice() throws Exception {
		FutureCallback callback = mock(FutureCallback.class);
		SimpleFuture future = new SimpleFuture<>(callback);
		future.cancel();
		future.cancel();
		try {
			future.get();
		} catch (CancellationException e) {
			verify(callback).cancelled();
			throw e;
		}
	}

	@Test
	public void getShouldReturnResult() throws Exception {
		SimpleFuture future = new SimpleFuture<>(new Object());
		assertNotNull(future.get());
	}

	@Test
	public void getShouldReturnCompleteArg() throws Exception {
		FutureCallback callback = mock(FutureCallback.class);
		SimpleFuture future = new SimpleFuture<>(callback);
		future.completed(new Object());
		assertNotNull(future.get());
		verify(callback).completed(any());
	}

	@Test
	public void getShouldReturnCompleteArg_noCallback() throws Exception {
		SimpleFuture future = new SimpleFuture<>();
		future.completed(new Object());
		assertNotNull(future.get());
	}

	@Test
	public void shouldNotCompleteTwice() throws Exception {
		FutureCallback callback = mock(FutureCallback.class);
		SimpleFuture future = new SimpleFuture<>(callback);
		future.completed(new Object());
		future.completed(new Object());
		assertNotNull(future.get());
		verify(callback).completed(any());
	}

	@Test
	public void getTimedShouldNotBlockIfDataIsPresent() throws Exception {
		SimpleFuture future = new SimpleFuture<>();
		future.completed(new Object());
		assertNotNull(future.get(1, TimeUnit.SECONDS));
	}

	@Test(expected = TimeoutException.class)
	public void getTimedShouldFailIfNoDataIsSet() throws Exception {
		SimpleFuture future = new SimpleFuture<>();
		future.get(1, TimeUnit.MILLISECONDS);
	}

}
