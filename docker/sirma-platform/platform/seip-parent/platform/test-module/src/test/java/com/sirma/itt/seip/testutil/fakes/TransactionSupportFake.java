package com.sirma.itt.seip.testutil.fakes;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Mock object for transaction support interface that just calls the passed callables.
 *
 * @author BBonev
 */
public class TransactionSupportFake implements TransactionSupport {

	@Override
	public <E> E invokeInTx(Callable<E> callable) {
		try {
			return callable.call();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <E> E invokeInNewTx(Callable<E> callable) {
		try {
			return callable.call();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <E> E invokeInNewTx(Callable<E> callable, int txTimeout, TimeUnit timeUnit) {
		try {
			return callable.call();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T, R> R invokeFunctionInTx(Function<T, R> function, T arg) {
		return function.apply(arg);
	}

	@Override
	public <T> void invokeConsumerInTx(Consumer<T> consumer, T arg) {
		consumer.accept(arg);
	}

	@Override
	public <T, R> R invokeFunctionInNewTx(Function<T, R> function, T arg) {
		return function.apply(arg);
	}

	@Override
	public <T> void invokeConsumerInNewTx(Consumer<T> consumer, T arg) {
		consumer.accept(arg);
	}

	@Override
	public void invokeAfterTransactionCompletion(Executable executable) {
		executable.execute();
	}

	@Override
	public void invokeOnSuccessfulTransaction(Executable executable) {
		executable.execute();
	}

	@Override
	public void invokeOnFailedTransaction(Executable executable) {
		executable.execute();
	}

	@Override
	public void invokeBeforeTransactionCompletion(Executable executable) {
		executable.execute();
	}

	@Override
	public void invokeInNewTx(Executable executable) {
		executable.execute();
	}

	@Override
	public void invokeInNewTx(Executable executable, int txTimeout, TimeUnit timeUnit) {
		executable.execute();
	}

	@Override
	public void invokeBeforeTransactionCompletionInTx(Executable executable) {
		executable.execute();
	}

	@Override
	public void invokeAfterTransactionCompletionInTx(Executable executable) {
		executable.execute();
	}

	@Override
	public void invokeOnSuccessfulTransactionInTx(Executable executable) {
		executable.execute();
	}

	@Override
	public void invokeOnFailedTransactionInTx(Executable executable) {
		executable.execute();
	}

	@Override
	public <T, Y> void invokeBiConsumerInNewTx(BiConsumer<T, Y> consumer, T arg1, Y arg2) {
		consumer.accept(arg1, arg2);
	}

}
