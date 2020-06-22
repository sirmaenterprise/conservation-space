package com.sirma.itt.seip.tx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.annotation.NoOperation;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;

/**
 * Concrete implementation of transaction support that works with JTA transactions intercepting. This implementation
 * should be faster than the EJB and with less memory footprint.
 *
 * @author BBonev
 */
@Named("jta")
@NoOperation
@ApplicationScoped
class JtaTransactionSupport implements TransactionSupport {

	@Inject
	private EventService eventService;

	@Override
	@Transactional
	public <E> E invokeInTx(Callable<E> callable) {
		return call(callable);
	}

	@Override
	@Transactional
	public <T, R> R invokeFunctionInTx(Function<T, R> function, T arg) {
		return function.apply(arg);
	}

	@Override
	@Transactional
	public <T> void invokeConsumerInTx(Consumer<T> consumer, T arg) {
		consumer.accept(arg);
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public <E> E invokeInNewTx(Callable<E> callable) {
		return call(callable);
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public <E> E invokeInNewTx(Callable<E> callable, int txTimeout, TimeUnit timeUnit) {
		return call(callable);
	}

	protected static <E> E call(Callable<E> callable) {
		try {
			return callable.call();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public <T, R> R invokeFunctionInNewTx(Function<T, R> function, T arg) {
		return function.apply(arg);
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public <T> void invokeConsumerInNewTx(Consumer<T> consumer, T arg) {
		consumer.accept(arg);
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public <T, Y> void invokeBiConsumerInNewTx(BiConsumer<T, Y> consumer, T arg1, Y arg2) {
		consumer.accept(arg1, arg2);
	}

	@Override
	public void invokeAfterTransactionCompletion(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new AfterTransactionEvent(executable));
	}

	@Override
	public void invokeOnSuccessfulTransaction(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new AfterTransactionSuccessEvent(executable));
	}

	@Override
	public void invokeOnFailedTransaction(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new AfterTransactionFailureEvent(executable));
	}

	@Override
	public void invokeBeforeTransactionCompletion(Executable executable) {
		if (executable == null) {
			return;
		}
		eventService.fire(new BeforeTransactionEvent(executable));
	}

	@Override
	public void invokeAfterTransactionCompletionInTx(Executable executable) {
		invokeAfterTransactionCompletion(executable);
	}

	@Override
	public void invokeOnSuccessfulTransactionInTx(Executable executable) {
		invokeOnSuccessfulTransaction(executable);
	}

	@Override
	public void invokeOnFailedTransactionInTx(Executable executable) {
		invokeOnFailedTransaction(executable);
	}

	@Override
	public void invokeBeforeTransactionCompletionInTx(Executable executable) {
		invokeBeforeTransactionCompletion(executable);
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public void invokeInNewTx(Executable executable) {
		executable.execute();
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public void invokeInNewTx(Executable executable, int txTimeout, TimeUnit timeUnit) {
		executable.execute();
	}
}
