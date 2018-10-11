package com.sirma.itt.seip.tx;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.util.CDI;

/**
 * Implementation for {@link TransactionSupport}.
 *
 * @author BBonev
 */
@ApplicationScoped
class TransactionSupportProxy implements TransactionSupport {

	private static final String JAVA_TRANSACTION_MANAGER = "java:/TransactionManager";
	private static final String TRANSACTION_SYNCHRONIZATION_REGISTRY = "java:jboss/TransactionSynchronizationRegistry";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "system.transaction.provider", defaultValue = "jta", system = true, sensitive = true, type = TransactionSupport.class, label = "Defines which provider to use for transaction support. Possible values are jta and ejb")
	private ConfigurationProperty<TransactionSupport> transactionSupportProvider;

	private TransactionManager transactionManager;

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
	public <E> E invokeInNewTx(Callable<E> callable, int txTimeout, TimeUnit timeUnit) {
		try {
			changeTransactionTimeout((int) timeUnit.toSeconds(txTimeout));
			return transactionSupportProvider.get().invokeInNewTx(callable);
		} finally {
			// reset to default transaction timeout
			changeTransactionTimeout(0);
		}
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

	@Override
	public void invokeInNewTx(Executable executable, int txTimeout, TimeUnit timeUnit) {
		try {
			changeTransactionTimeout((int) timeUnit.toSeconds(txTimeout));
			transactionSupportProvider.get().invokeInNewTx(executable);
		} finally {
			// reset to default transaction timeout
			changeTransactionTimeout(0);
		}
	}

	private void changeTransactionTimeout(int newTimeoutSeconds) {
		try {
			getTransactionManager().setTransactionTimeout(newTimeoutSeconds);
		} catch (SystemException e) {
			throw new EmfRuntimeException("Could not set transaction timeout due to system error", e);
		}
	}

	@Produces
	TransactionManager getTransactionManager() {
		if (transactionManager == null) {
			try {
				transactionManager = InitialContext.doLookup(JAVA_TRANSACTION_MANAGER);
			} catch (NamingException e) {
				throw new ContextNotActiveException(e);
			}
		}
		return transactionManager;
	}

	@Produces
	TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
		try {
			return InitialContext.doLookup(TRANSACTION_SYNCHRONIZATION_REGISTRY);
		} catch (NamingException e) {
			throw new ContextNotActiveException(e);
		}
	}
}
