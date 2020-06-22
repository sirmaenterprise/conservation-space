package com.sirma.itt.seip.tx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.sirma.itt.seip.Executable;

/**
 * Helper class to provide transactional invocation.
 *
 * @author BBonev
 */
public interface TransactionSupport {

	/**
	 * Invoke the given {@link Callable} in transaction.
	 *
	 * @param <E>
	 *            the element type
	 * @param callable
	 *            the callable
	 * @return the e
	 */
	<E> E invokeInTx(Callable<E> callable);

	/**
	 * Invoke the given function in transaction
	 *
	 * @param <T>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 * @param function
	 *            the function
	 * @param arg
	 *            the arg
	 * @return the r
	 */
	<T, R> R invokeFunctionInTx(Function<T, R> function, T arg);

	/**
	 * Invoke the given function in transaction.
	 *
	 * @param <T>
	 *            the generic type
	 * @param consumer
	 *            the consumer
	 * @param arg
	 *            the arg
	 */
	<T> void invokeConsumerInTx(Consumer<T> consumer, T arg);

	/**
	 * Invoke the given {@link Callable} in new transaction.
	 *
	 * @param <E>
	 *            the element type
	 * @param callable
	 *            the callable
	 * @return the e
	 */
	<E> E invokeInNewTx(Callable<E> callable);

	/**
	 * Invoke the given {@link Callable} in new transaction with the specified transaction timeout.
	 * <p><b>Note that the new transaction timeout will affect the newly started transactions and not the currently
	 * running one</b>
	 * <br>If the new transaction timeout is greater than the one used when the current transaction then the original
	 * transaction will timeout and rollback if the new transaction takes longer to complete.
	 * <br> In other works this method <i>should not be used to increase the transaction timeout if already in
	 * transaction</i>.</p>
	 *
	 * @param <E> the element type
	 * @param callable the callable to invoke in the transaction that requires greater time to complete
	 * @param txTimeout the new transaction timeout to set
	 * @param timeUnit the transaction timeout time unit
	 * @return the invocation result
	 */
	<E> E invokeInNewTx(Callable<E> callable, int txTimeout, TimeUnit timeUnit);

	/**
	 * Invoke the given {@link Executable} in new transaction.
	 *
	 * @param executable
	 *            the executable to call
	 */
	void invokeInNewTx(Executable executable);

	/**
	 * Invoke the given {@link Executable} in new transaction with the specified transaction timeout.
	 * <p><b>Note that the new transaction timeout will affect the newly started transactions and not the currently
	 * running one</b>
	 * <br>If the new transaction timeout is greater than the one used when the current transaction then the original
	 * transaction will timeout and rollback if the new transaction takes longer to complete.
	 * <br> In other works this method <i>should not be used to increase the transaction timeout if already in
	 * transaction</i>.</p>
	 *
	 * @param executable the executable to invoke in the transaction that requires greater time to complete
	 * @param txTimeout the new transaction timeout to set
	 * @param timeUnit the transaction timeout time unit
	 */
	void invokeInNewTx(Executable executable, int txTimeout, TimeUnit timeUnit);

	/**
	 * Invoke the given function in new transaction
	 *
	 * @param <T>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 * @param function
	 *            the function
	 * @param arg
	 *            the arg
	 * @return the r
	 */
	<T, R> R invokeFunctionInNewTx(Function<T, R> function, T arg);

	/**
	 * Invoke the given {@link Consumer} in new transaction.
	 *
	 * @param <T>
	 *            the generic type
	 * @param consumer
	 *            the consumer
	 * @param arg
	 *            the arg
	 */
	<T> void invokeConsumerInNewTx(Consumer<T> consumer, T arg);

	/**
	 * Invoke the given {@link BiConsumer} in new transaction.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <Y>
	 *            the generic type
	 * @param consumer
	 *            the consumer
	 * @param arg1
	 *            the arg
	 * @param arg2
	 *            the arg2
	 */
	<T, Y> void invokeBiConsumerInNewTx(BiConsumer<T, Y> consumer, T arg1, Y arg2);

	/**
	 * Invoke before transaction completion.The invocation will manage a correct security context. <br>
	 * The method is intended to be used with lambda expressions to call logic before transaction completion.
	 *
	 * @param executable
	 *            the executable to call
	 */
	void invokeBeforeTransactionCompletion(Executable executable);

	/**
	 * Invoke after transaction completion. This will be invoked no matter if it was successful or not. The invocation
	 * will manage a correct security context.<br>
	 * The method is intended to be used with lambda expressions to call logic after transaction completion.
	 *
	 * @param executable
	 *            the executable to call
	 */
	void invokeAfterTransactionCompletion(Executable executable);

	/**
	 * Invoke on successful transaction. The invocation will manage a correct security context. <br>
	 * The method is intended to be used with lambda expressions to call logic if transaction completes successfully.
	 *
	 * @param executable
	 *            the executable to call
	 */
	void invokeOnSuccessfulTransaction(Executable executable);

	/**
	 * Invoke on failed transaction. The invocation will manage a correct security context. <br>
	 * The method is intended to be used with lambda expressions to call logic if transaction fails.
	 *
	 * @param executable
	 *            the executable to call
	 */
	void invokeOnFailedTransaction(Executable executable);

	/**
	 * Invoke before transaction completion.The invocation will manage a correct security context. <br>
	 * The method is intended to be used with lambda expressions to call logic before transaction completion.
	 * <p>
	 * This method will start new transaction to invoke the given executable. This method should be used when the
	 * invoked code need to access some transactional storage like relational database, because when the given code is
	 * executed the current transaction will be terminated and the code will fail with not active transaction exception.
	 *
	 * @param executable
	 *            the executable to call
	 */
	void invokeBeforeTransactionCompletionInTx(Executable executable);

	/**
	 * Invoke after transaction completion. This will be invoked no matter if it was successful or not. The invocation
	 * will manage a correct security context.<br>
	 * The method is intended to be used with lambda expressions to call logic after transaction completion.
	 * <p>
	 * This method will start new transaction to invoke the given executable. This method should be used when the
	 * invoked code need to access some transactional storage like relational database, because when the given code is
	 * executed the current transaction will be terminated and the code will fail with not active transaction exception.
	 *
	 * @param executable
	 *            the executable to call
	 */
	void invokeAfterTransactionCompletionInTx(Executable executable);

	/**
	 * Invoke on successful transaction. The invocation will manage a correct security context. <br>
	 * The method is intended to be used with lambda expressions to call logic if transaction completes successfully.
	 * <p>
	 * This method will start new transaction to invoke the given executable. This method should be used when the
	 * invoked code need to access some transactional storage like relational database, because when the given code is
	 * executed the current transaction will be terminated and the code will fail with not active transaction exception.
	 *
	 * @param executable
	 *            the executable to call
	 */
	void invokeOnSuccessfulTransactionInTx(Executable executable);

	/**
	 * Invoke on failed transaction. The invocation will manage a correct security context. <br>
	 * The method is intended to be used with lambda expressions to call logic if transaction fails.
	 * <p>
	 * This method will start new transaction to invoke the given executable. This method should be used when the
	 * invoked code need to access some transactional storage like relational database, because when the given code is
	 * executed the current transaction will be terminated and the code will fail with not active transaction exception.
	 *
	 * @param executable
	 *            the executable to call
	 */
	void invokeOnFailedTransactionInTx(Executable executable);
}
