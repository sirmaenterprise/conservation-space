package com.sirma.itt.seip.tx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.annotation.NoOperation;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;

/**
 * Provides transaction support via stateless ejb bean. This implementation may have bigger footprint compared to jta
 * implementation.
 *
 * @author BBonev
 */
@Named("ejb")
@NoOperation
@Stateless
public class EJBTransactionSupport implements TransactionSupport {

	@Inject
	private EventService eventService;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E> E invokeInTx(Callable<E> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T, R> R invokeFunctionInTx(Function<T, R> function, T arg) {
		return function.apply(arg);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T> void invokeConsumerInTx(Consumer<T> consumer, T arg) {
		consumer.accept(arg);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T, Y> void invokeBiConsumerInNewTx(java.util.function.BiConsumer<T, Y> consumer, T arg1, Y arg2) {
		consumer.accept(arg1, arg2);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <E> E invokeInNewTx(Callable<E> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <E> E invokeInNewTx(Callable<E> callable, int txTimeout, TimeUnit timeUnit) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T, R> R invokeFunctionInNewTx(Function<T, R> function, T arg) {
		return function.apply(arg);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> void invokeConsumerInNewTx(Consumer<T> consumer, T arg) {
		consumer.accept(arg);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void invokeAfterTransactionCompletion(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new AfterTransactionEvent(executable));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void invokeOnSuccessfulTransaction(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new AfterTransactionSuccessEvent(executable));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void invokeOnFailedTransaction(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new AfterTransactionFailureEvent(executable));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void invokeBeforeTransactionCompletion(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new BeforeTransactionEvent(executable));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void invokeAfterTransactionCompletionInTx(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new AfterTransactionEvent(() -> invokeInNewTx(executable)));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void invokeOnSuccessfulTransactionInTx(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new AfterTransactionSuccessEvent(() -> invokeInNewTx(executable)));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void invokeOnFailedTransactionInTx(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new AfterTransactionFailureEvent(() -> invokeInNewTx(executable)));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void invokeBeforeTransactionCompletionInTx(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new BeforeTransactionEvent(() -> invokeInNewTx(executable)));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void invokeInNewTx(Executable executable) {
		executable.execute();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void invokeInNewTx(Executable executable, int txTimeout, TimeUnit timeUnit) {
		executable.execute();
	}

}
