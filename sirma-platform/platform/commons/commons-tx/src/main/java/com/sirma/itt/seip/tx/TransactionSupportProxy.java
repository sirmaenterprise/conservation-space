package com.sirma.itt.seip.tx;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.util.CDI;

/**
 * Implementation for {@link TransactionSupport}.
 *
 * @author BBonev
 */
@Singleton
class TransactionSupportProxy implements TransactionSupport {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "system.transaction.provider", defaultValue = "jta", system = true, sensitive = true, type = TransactionSupport.class, label = "Defines which provider to use for transaction support. Possible values are jta and ejb")
	private ConfigurationProperty<TransactionSupport> transactionSupportProvider;

	@ConfigurationConverter
	static TransactionSupport buildTxSupport(ConverterContext context, BeanManager beanManager) {
		return CDI.instantiateBean(context.getRawValue(), TransactionSupport.class, beanManager);
	}

	@Override
	public <E> E invokeInTx(Callable<E> callable) {
		return transactionSupportProvider.get().invokeInTx(callable);
	}

	@Override
	public <T, R> R invokeFunctionInTx(Function<T, R> function, T arg) {
		return transactionSupportProvider.get().invokeFunctionInTx(function, arg);
	}

	@Override
	public <T> void invokeConsumerInTx(Consumer<T> consumer, T arg) {
		transactionSupportProvider.get().invokeConsumerInTx(consumer, arg);
	}

	@Override
	public <E> E invokeInNewTx(Callable<E> callable) {
		return transactionSupportProvider.get().invokeInNewTx(callable);
	}

	@Override
	public <T, R> R invokeFunctionInNewTx(Function<T, R> function, T arg) {
		return transactionSupportProvider.get().invokeFunctionInNewTx(function, arg);
	}

	@Override
	public <T> void invokeConsumerInNewTx(Consumer<T> consumer, T arg) {
		transactionSupportProvider.get().invokeConsumerInNewTx(consumer, arg);
	}

	@Override
	public <T, Y> void invokeBiConsumerInNewTx(BiConsumer<T, Y> consumer, T arg1, Y arg2) {
		transactionSupportProvider.get().invokeBiConsumerInNewTx(consumer, arg1, arg2);
	}

	@Override
	public void invokeAfterTransactionCompletion(Executable executable) {
		transactionSupportProvider.get().invokeAfterTransactionCompletion(executable);
	}

	@Override
	public void invokeOnSuccessfulTransaction(Executable executable) {
		transactionSupportProvider.get().invokeOnSuccessfulTransaction(executable);
	}

	@Override
	public void invokeOnFailedTransaction(Executable executable) {
		transactionSupportProvider.get().invokeOnFailedTransaction(executable);
	}

	@Override
	public void invokeBeforeTransactionCompletion(Executable executable) {
		transactionSupportProvider.get().invokeBeforeTransactionCompletion(executable);
	}

	@Override
	public void invokeAfterTransactionCompletionInTx(Executable executable) {
		// this is here and not in the proxied classes is because of the use of lambda expression
		// the interceptors are not called
		transactionSupportProvider.get().invokeAfterTransactionCompletion(() -> invokeInNewTx(executable));
	}

	@Override
	public void invokeOnSuccessfulTransactionInTx(Executable executable) {
		// this is here and not in the proxied classes is because of the use of lambda expression
		// the interceptors are not called
		transactionSupportProvider.get().invokeOnSuccessfulTransaction(() -> invokeInNewTx(executable));
	}

	@Override
	public void invokeOnFailedTransactionInTx(Executable executable) {
		// this is here and not in the proxied classes is because of the use of lambda expression
		// the interceptors are not called
		transactionSupportProvider.get().invokeOnFailedTransaction(() -> invokeInNewTx(executable));
	}

	@Override
	public void invokeBeforeTransactionCompletionInTx(Executable executable) {
		// this is here and not in the proxied classes is because of the use of lambda expression
		// the interceptors are not called
		transactionSupportProvider.get().invokeBeforeTransactionCompletion(() -> invokeInNewTx(executable));
	}

	@Override
	public void invokeInNewTx(Executable executable) {
		transactionSupportProvider.get().invokeInNewTx(executable);
	}
}
