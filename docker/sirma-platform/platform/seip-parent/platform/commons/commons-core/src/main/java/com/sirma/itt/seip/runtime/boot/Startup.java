package com.sirma.itt.seip.runtime.boot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.PostConstruct;

import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Register class or method to be called on startup. The invocation could be configured if should be done asynchronous
 * or not. Could specify global order of all startup components or dependent components. The startup process is divided
 * into several phases. The phases are called in order so no need to define dependency to component that is in a
 * previous phase.
 * <p>
 * Components that are annotated and does not implement {@link StartupListener} interface will be created only. They
 * should expect only the {@link PostConstruct} method to be called.<br>
 * If the annotated type implements the {@link StartupListener} interface the method {@link StartupListener#onStartup()}
 * will also be invoked. <br>
 * If annotated type implements the {@link StartupListener} interface but does not have a {@link Startup} annotation
 * will be called using the default values of the annotation. <br>
 * If the {@link Startup} annotation is placed on a method with no arguments that method will be invoked after the
 * annotated type is initialized.
 *
 * @author BBonev
 * @see StartupListener
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Startup {

	/**
	 * Defines the phase on which the component to be invoked. Default phase is: {@link StartupPhase#AFTER_APP_START}
	 *
	 * @return the startup phase
	 * @see StartupPhase
	 */
	StartupPhase phase() default StartupPhase.AFTER_APP_START;

	/**
	 * Defines optional component name. If not defined the annotated type will be used. This names are used for
	 * {@link #dependsOn()} resolving.
	 *
	 * @return the step name
	 */
	String name() default "";

	/**
	 * If component should be loaded asynchronously. Note that the loading will not advance to the next phase until all
	 * asynchronous components finish with their loading in the current phase. Default mode is synchronous loading.
	 *
	 * @return if should be executed async
	 */
	boolean async() default false;

	/**
	 * The optional list of component names that need to be loaded until this component is started. This takes
	 * precedence over the {@link #order()} field.
	 *
	 * @return the dependency list (not implemented, yet)
	 */
	String[] dependsOn() default {};

	/**
	 * Defines order in which to load the components. The order is per phase. Components with same order can be loaded
	 * as the implementation decides. The one with higher order will be executed after the one with lower.
	 *
	 * @return the step order based on the other steps in the same phase
	 */
	double order() default Double.MAX_VALUE;

	/**
	 * Specifies if transaction should be started before calling the annotated method. The default behaviour is to start
	 * a transaction. The chosen mode has the following specific:
	 * <ul>
	 * <li><b>REQUIRED</b>: starts a JPA managed transaction before calling the annotated method. Default behaviour.</li>
	 * <li><b>REQUIRES_NEW</b>: same as {@code REQUIRED}</li>
	 * <li><b>NOT_SUPPORTED</b>: no transaction will be started. If the annotated method needs a custom behaviour,
	 * like managing his own transactions this mode should be used</li>
	 * </ul>
	 *
	 * @return the desired transaction mode
	 */
	TransactionMode transactionMode() default TransactionMode.REQUIRED;
}
