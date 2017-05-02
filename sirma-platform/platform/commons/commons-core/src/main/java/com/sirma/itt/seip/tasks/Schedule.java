package com.sirma.itt.seip.tasks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sirma.itt.seip.runtime.boot.Startup;

/**
 * Schedule for execution the annotated method. Any arguments of the annotated method will be injected. If the annotated
 * method has a {@code com.sirma.itt.seip.tasks.SchedulerContext} argument it will be passed non <code>null</code>
 * context instance where the scheduled method could add data to be available at the next activation.
 * <p>
 * The cron expression that will be used to schedule the activation can be defined by:
 * <ul>
 * <li>statically by specifying it directly in the annotation field {@link #expression()}
 * <li>loaded via configuration definition identified by the {@link #expressionConfig()}. Configuration for the
 * expression could be defined by:
 * <ul>
 * <li>Define {@link #expressionConfig()} and define
 * {@code com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition} on a constant or field
 * <li>Define {@code com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition} on the scheduled
 * method. The fields {@link #expression()} and {@link #expressionConfig()} may be empty in this case.
 * </ul>
 * </ul>
 *
 * <pre>
 * <code>
 * class ScheduledInvocations {
 *
 *  &#64;Schedule(expression = "0 0/01 * ? * *", transactionMode = TransactionMode.NOT_SUPPORTED)
 *  static void callEveryMinute(MailService mailService) {
 *  	mailService.checkForNewMail();
 *  }
 *
 *  &#64;Startup(async = true)
 *  &#64;Schedule(transactionMode = TransactionMode.NOT_SUPPORTED)
 *  &#64;com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition(name = "cache.user.update.schedule", defaultValue = "0 0/15 * ? * *")
 *  void callEvery15Minutes(SchedulerContext context, MailService mailService) {
 *  	// method body
 *  }
 * }
 * </code>
 * </pre>
 *
 * The example above defines 2 methods for asynchronous execution.<br>
 * The method <code>callEveryMinute</code> has a static execution configuration for every minute and requests
 * <code>MailService</code> to be injected on invocation. The method is static and so will not require the class
 * <code>ScheduledInvocations</code> to be instantiated in order to execute the method. <br>
 * The method <code>callEvery15Minutes</code> will schedule the given method to be executed every 15 minutes. The
 * execution is bound to the configuration <code>cache.user.update.schedule</code> and on change of that configuration
 * the method will be executed on the new interval specified.<br>
 * The method will be executed on startup also. If the {@link Startup} annotation is not present then the method will be
 * executed only every 15 minutes and not be executed on application start. Note that the method is not static and so
 * the class <code>ScheduledInvocations</code> will be instantiated as CDI bean before execution. Valid non contextual
 * scope may be applied to the class to minimize the instantiated instances if needed.
 * <p>
 * Valid scopes for scheduled beans are:
 * <ul>
 * <li>{@link javax.enterprise.inject.Default}
 * <li>{@link javax.enterprise.context.ApplicationScoped}
 * <li>{@link javax.inject.Singleton}
 * <li>{@link javax.ejb.Singleton}
 * <li>{@link javax.ejb.Stateless}
 * </ul>
 *
 * @author BBonev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Schedule {

	/**
	 * Cron expression that defines the interval at which the annotated method will be called. Required value if there
	 * is no {@link #expressionConfig()} defined or
	 * {@code com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition} on the method
	 *
	 * <pre>
	 * See com.sirma.itt.seip.tasks.SchedulerConfiguration#setCronExpression(String)
	 * </pre>
	 *
	 * @return cron expression
	 */
	String expression() default "";

	/**
	 * Configuration name to be used to resolve configuration that defines the cron expression to be used for scheduler
	 * entry. This is required if there is no {@link #expression()} value defined or there is no
	 * {@code com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition} defined on the method.
	 *
	 * @return configuration key that holds a cron expression
	 */
	String expressionConfig() default "";

	/**
	 * Unique identifier that defines the scheduled expression. If not specified the identifier will be the: <br>
	 * <code>[class simple name].[method name]</code>
	 *
	 * <pre>
	 * See com.sirma.itt.seip.tasks.SchedulerConfiguration#setIdentifier(String)
	 * </pre>
	 *
	 * @return custom task identifier
	 */
	String identifier() default "";

	/**
	 * Transaction mode used to execute the method. Default value is {@link TransactionMode#REQUIRED}.
	 *
	 * <pre>
	 * See com.sirma.itt.seip.tasks.SchedulerConfiguration#setTransactionMode(TransactionMode)
	 * </pre>
	 *
	 * @return the transaction mode
	 */
	TransactionMode transactionMode() default TransactionMode.REQUIRED;

	/**
	 * Maximum retries to try to execute the operation. By default no retries will be done.
	 *
	 * <pre>
	 * See com.sirma.itt.seip.tasks.SchedulerConfiguration#setMaxRetryCount(int)
	 * </pre>
	 *
	 * @return max retries
	 */
	int maxRetries() default 0;

	/**
	 * Delay between the retries in seconds. The default delay is 60 seconds.
	 *
	 * <pre>
	 * See com.sirma.itt.seip.tasks.SchedulerConfiguration#setRetryDelay(Long)
	 * </pre>
	 *
	 * @return reply delay in seconds
	 */
	long retryDelay() default 60;

	/**
	 * Controls if the delay on error should be incremental. Default value is <code>false</code>.
	 *
	 * <pre>
	 * See com.sirma.itt.seip.tasks.SchedulerConfiguration#setIncrementalDelay(boolean)
	 * </pre>
	 *
	 * @return if the reply should be incremental
	 */
	boolean incrementalDelay() default false;

	/**
	 * Controls if the execution should continue on error after retrying {@link #maxRetries()}. If set to
	 * <code>true</code> the execution will be stopped after reaching the configured {@link #maxRetries()}. If set to
	 * <code>false</code> {@link #maxRetries()} configuration will be ignored.
	 *
	 * <pre>
	 * See com.sirma.itt.seip.tasks.SchedulerConfiguration#setContinueOnError(boolean)
	 * </pre>
	 * 
	 * @return if the processing should be stopped on error
	 */
	boolean stopOnError() default false;

	/**
	 * Specifies if the scheduled method is considered a system functionality and should be run in the system
	 * environment or run in tenant environment. If set to <code>false</code> the annotated method will be called in the
	 * context for all possible tenants if tenant module is present.
	 *
	 * <pre>
	 * See com.sirma.itt.seip.tasks.SchedulerConfiguration#setSystemSpecific(boolean)
	 * </pre>
	 * 
	 * @return if this should be run as system
	 */
	boolean system() default true;
}
