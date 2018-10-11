package com.sirma.itt.seip.testutil.fakes;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Fake transaction support implementation that is capable of executing pseudo transactions in order to call any
 * registered transaction handlers only when completing the transactions in proper phases.
 *
 * @see #beginTx()
 * @see #commitTx()
 * @see #rollbackTx()
 * @see #executeInTx(Executable)
 * @see #callInTx(Callable)
 *
 * @author BBonev
 */
public class InteractiveTransactionSupportFake implements TransactionSupport {

	private List<Executable> onFailTx = new LinkedList<>();
	private List<Executable> onSuccessfulTx = new LinkedList<>();
	private List<Executable> beforeTxCompletion = new LinkedList<>();
	private List<Executable> afterTxCompletion = new LinkedList<>();

	/**
	 * Clear all registered handlers and prepare for new transaction
	 */
	public void beginTx() {
		onFailTx.clear();
		onSuccessfulTx.clear();
		beforeTxCompletion.clear();
		afterTxCompletion.clear();
	}

	/**
	 * Calls any registered handlers for before completion, after completion and successful transaction in that order
	 */
	public void commitTx() {
		beforeTxCompletion.forEach(Executable::execute);
		afterTxCompletion.forEach(Executable::execute);
		onSuccessfulTx.forEach(Executable::execute);
	}

	/**
	 * Calls any registered handlers for before completion, after completion and failed transaction in that order
	 */
	public void rollbackTx() {
		beforeTxCompletion.forEach(Executable::execute);
		afterTxCompletion.forEach(Executable::execute);
		onFailTx.forEach(Executable::execute);
	}

	/**
	 * Executes the given executable in pseudo transaction. If the invocation is successful then the {@link #commitTx()}
	 * is called otherwise {@link #rollbackTx()} methods is called.
	 *
	 * @param executable to execute
	 */
	public void executeInTx(Executable executable) {
		beginTx();
		try {
			executable.execute();
			commitTx();
		} catch (RuntimeException e) {
			rollbackTx();
			throw e;
		}
	}

	/**
	 * Calls the given callable in pseudo transaction. If the invocation is successful then the {@link #commitTx()}
	 * is called otherwise {@link #rollbackTx()} methods is called.
	 *
	 * @param callable to call
	 * @param <V> the return type
	 * @return the returned value by the callable
	 * @throws Exception if execution fails
	 */
	public <V> V callInTx(Callable<V> callable) throws Exception {
		beginTx();
		try {
			V result = callable.call();
			commitTx();
			return result;
		} catch (Exception e) {
			rollbackTx();
			throw e;
		}
	}

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
		afterTxCompletion.add(executable);
	}

	@Override
	public void invokeOnSuccessfulTransaction(Executable executable) {
		onSuccessfulTx.add(executable);
	}

	@Override
	public void invokeOnFailedTransaction(Executable executable) {
		onFailTx.add(executable);
	}

	@Override
	public void invokeBeforeTransactionCompletion(Executable executable) {
		beforeTxCompletion.add(executable);
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
		beforeTxCompletion.add(executable);
	}

	@Override
	public void invokeAfterTransactionCompletionInTx(Executable executable) {
		afterTxCompletion.add(executable);
	}

	@Override
	public void invokeOnSuccessfulTransactionInTx(Executable executable) {
		onSuccessfulTx.add(executable);
	}

	@Override
	public void invokeOnFailedTransactionInTx(Executable executable) {
		onFailTx.add(executable);
	}

	@Override
	public <T, Y> void invokeBiConsumerInNewTx(BiConsumer<T, Y> consumer, T arg1, Y arg2) {
		consumer.accept(arg1, arg2);
	}

}
