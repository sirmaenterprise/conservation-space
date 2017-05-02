/*
 *
 */
package com.sirma.itt.seip.runtime;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.context.ThreadFactories;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.LoggingUtil;

/**
 * Controller that manages the sequence of startup events and component loading.
 *
 * @author BBonev
 */
@WebListener
public class RuntimeStartup implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** The component provider. */
	@Inject
	private StartupComponentProvider componentProvider;

	/** The event. */
	@Inject
	private Event<BeforePhaseStartEvent> beforeStartEvent;

	@Inject
	private Event<AfterPhaseStartEvent> afterPhaseStartEvent;

	@Inject
	private Event<RuntimeInitializationStart> runtimeInitEvent;

	private ExecutorService executorService;

	@Inject
	private Instance<ServerStatusChecker> deploymentChecker;

	@Inject
	private SecurityContextManager securityManager;

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		runtimeInitEvent.fire(new RuntimeInitializationStart(sce.getServletContext()));

		initializeAsync();

		printInfoMessage("DEPLOYMENT STARTED...");

		loadPhase(StartupPhase.DEPLOYMENT);

		executorService.submit(this::loadApplicationAfterDeployment);
	}

	/**
	 * Initialize async.
	 */
	private void initializeAsync() {
		executorService = Executors.newFixedThreadPool(6, ThreadFactories.createSystemThreadFactory(securityManager));
	}

	/**
	 * Load application.
	 */
	void loadApplicationAfterDeployment() {
		Queue<Executable> steps = createSteps();

		try {
			Iterator<Executable> it = steps.iterator();
			while (it.hasNext()) {
				try {
					it.next().execute();
				} catch (RuntimeException e) {
					LOGGER.error(e.getMessage(), e);
				} finally {
					it.remove();
				}
			}
		} finally {
			executorService.shutdown();
			executorService = null;
		}
	}

	private Queue<Executable> createSteps() {
		Queue<Executable> steps = new LinkedList<>();

		steps.add(this::waitForDeploymentToFinish);
		steps.add(() -> waitForStepToBeAllowed(StartupPhase.DEPLOYMENT, StartupPhase.BEFORE_APP_START));
		steps.add(() -> printInfoMessage("DEPLOYMENT COMPLETED! INITIALIZING PLATFORM..."));
		steps.add(() -> loadPhase(StartupPhase.BEFORE_APP_START));
		steps.add(() -> printInfoMessage("INITIALIZING APPLICATION..."));
		steps.add(() -> waitForStepToBeAllowed(StartupPhase.BEFORE_APP_START, StartupPhase.AFTER_APP_START));
		steps.add(() -> loadPhase(StartupPhase.AFTER_APP_START));
		steps.add(() -> printInfoMessage("APPLICATION STARTED!"));
		steps.add(() -> RuntimeInfo.instance().seal());
		return steps;
	}

	void waitForDeploymentToFinish() {
		if (deploymentChecker.isUnsatisfied()) {
			return;
		}
		if (deploymentChecker.isAmbiguous()) {
			throw new EmfRuntimeException("More that one server status checker found!");
		}
		LOGGER.info("Waiting deployment to finish...");
		ServerStatusChecker checker = deploymentChecker.get();
		while (!checker.isDeploymentFinished()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.warn("Sleep interrupted will continue with loading", e);
			}
		}
		LOGGER.info("Deployment completed.");
	}

	void waitForStepToBeAllowed(StartupPhase completedPhase, StartupPhase nextPhase) {
		AfterPhaseStartEvent event = new AfterPhaseStartEvent(completedPhase, nextPhase);
		afterPhaseStartEvent.fire(event);
		while (!event.canContinue()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.warn("Sleep interrupted will continue with loading", e);
			}
		}
	}

	/**
	 * Prints big info message.
	 *
	 * @param message
	 *            the message
	 */
	static void printInfoMessage(String message) {
		LOGGER.info(LoggingUtil.buildInfoMessage(message));
	}

	private void loadPhase(StartupPhase phase) {
		RuntimeInfo.instance().setPhase(phase);
		List<Component> foundComponents = componentProvider.getComponentsForPhase(phase);

		// sort the components before firing the event if order is changed during the event we will sort them once again
		// this is to inform the observers of the event the correct order in which the components will be executed
		Collections.sort(foundComponents, RuntimeStartup::compareComponents);
		fireBeforePhaseEvent(phase, foundComponents);

		CountDownLatch sync = new CountDownLatch(foundComponents.size());

		Collection<Future<?>> asyncTasks = startComponents(foundComponents, sync);

		waitForAsyncTasks(phase, sync, asyncTasks);
	}

	/**
	 * Check for exceptions in async tasks.
	 *
	 * @param phase
	 *            the phase
	 * @param sync
	 *            the sync
	 * @param asyncTasks
	 *            the async tasks
	 */
	private static void waitForAsyncTasks(StartupPhase phase, CountDownLatch sync, Collection<Future<?>> asyncTasks) {

		try {
			if (sync.getCount() != 0) {
				LOGGER.debug("Waiting for {} async tasks in phase {} to finish", sync.getCount(), phase);
			}
			sync.await();
			LOGGER.debug("Waiting for async tasks in phase {} to finish: done", phase);
		} catch (InterruptedException e) {
			LOGGER.debug("Component loading was interruted", e);
		}

		for (Future<?> future : asyncTasks) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Exception during asynchronous component initialization!", e);
				if (phase == StartupPhase.DEPLOYMENT) {
					throw new EmfRuntimeException("Exception during deployment phase. Stopping deployment!",
							e.getCause());
				}
			}
		}
	}

	/**
	 * Fire before phase event.
	 *
	 * @param phase
	 *            the phase
	 * @return the provided components from the event
	 */
	private void fireBeforePhaseEvent(StartupPhase phase, Collection<Component> providedComponents) {
		beforeStartEvent.fire(new BeforePhaseStartEvent(phase, providedComponents));
	}

	/**
	 * Start components.
	 *
	 * @param components
	 *            the components
	 * @param sync
	 *            the sync
	 * @return a collection of the {@link Future} objects that belong to the async tasks.
	 */
	private Collection<Future<?>> startComponents(Collection<Component> components, CountDownLatch sync) {
		return components
				.stream()
					.sorted(RuntimeStartup::compareComponents)
					.map((component) -> invokeOrScheduleComponent(component, sync)) // invoke tasks
					.filter(Objects::nonNull) // return async tasks only
					.collect(Collectors.toList());
	}

	static int compareComponents(Component o1, Component o2) {
		return Double.compare(o1.getOrder(), o2.getOrder());
	}

	/**
	 * Invoke component.
	 *
	 * @param component
	 *            the component
	 * @param sync
	 *            the sync
	 * @return the future task
	 */
	private Future<?> invokeOrScheduleComponent(final Component component, final CountDownLatch sync) {
		if (component.isAsync()) {
			return executorService.submit(() -> startComponent(component, sync));
		}
		startComponent(component, sync);
		return null;
	}

	/**
	 * Start component.
	 *
	 * @param component
	 *            the component
	 */
	private static void startComponent(Component component, CountDownLatch sync) {
		LOGGER.info("Loading component: {} during {}", component.getName(), component.getPhase());
		try {
			TimeTracker timeTracker = TimeTracker.createAndStart();
			component.start();
			LOGGER.debug("Component {} loading took {} ms", component.getName(), timeTracker.stop());
		} catch (Exception e) {
			LOGGER.error("Component {} throw an exception on startup", component.getName(), e);
			if (component.getPhase() == StartupPhase.DEPLOYMENT) {
				throw new EmfRuntimeException("Deployment failed due to error in component " + component.getName(), e);
			}
		} finally {
			sync.countDown();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// implement me!

	}
}
