package com.sirma.itt.seip.runtime;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import com.sirma.itt.seip.runtime.boot.DeploymentException;
import com.sirma.itt.seip.time.TimeTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.context.ThreadFactories;
import com.sirma.itt.seip.util.LoggingUtil;

/**
 * {@link BootStrategy} implementation that realize the default strategy.
 * <p>
 * The default strategy is to load the deployment phase synchronously and all other after deployment is complete in a
 * separate thread of the deployment process.
 *
 * @author BBonev
 */
public class DefaultBootStrategy implements BootStrategy {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** The component provider. */
	@Inject
	private StartupComponentProvider componentProvider;

	@Inject
	private Event<BeforePhaseStartEvent> beforeStartEvent;

	@Inject
	private Event<AfterPhaseStartEvent> afterPhaseStartEvent;

	@Inject
	private Instance<ServerControllerService> serverController;

	@Inject
	private SecurityContextManager securityManager;

	private ExecutorService executorService;

	@PostConstruct
	void initializeAsync() {
		executorService = Executors.newFixedThreadPool(6, ThreadFactories.createSystemThreadFactory(securityManager));
	}

	@Override
	public void executeStrategy(ServletContext servletContext, Consumer<ServletContext> onComplete) {

		printInfoMessage("DEPLOYMENT STARTED...");

		executorService.submit(() -> loadApplicationAfterDeployment(servletContext, onComplete));
	}

	/**
	 * Load application.
	 */
	private void loadApplicationAfterDeployment(ServletContext servletContext, Consumer<ServletContext> onComplete) {
		Queue<Executable> steps = createSteps();

		try {
			Iterator<Executable> it = steps.iterator();
			while (it.hasNext()) {
				try {
					it.next().execute();
				} catch(EmfRuntimeException e){
					if (e.getCause() instanceof DeploymentException) {
						printInfoMessage("REDEPLOYING APPLICATION");
						return;
					}
					LOGGER.error(e.getMessage(), e);
				}
				catch (RuntimeException e) {
					LOGGER.error(e.getMessage(), e);
					return;
				} finally {
					it.remove();
				}
			}
		} finally {
			executorService.shutdown();
			executorService = null;
			onComplete.accept(servletContext);
		}
	}

	private Queue<Executable> createSteps() {
		Queue<Executable> steps = new LinkedList<>();

		steps.add(this::waitForDeploymentToFinish);
		steps.add(() -> loadPhase(StartupPhase.DEPLOYMENT));
		steps.add(() -> waitForStepToBeAllowed(StartupPhase.DEPLOYMENT, StartupPhase.BEFORE_APP_START));
		steps.add(() -> printInfoMessage("DEPLOYMENT COMPLETED! INITIALIZING PLATFORM..."));
		steps.add(() -> loadPhase(StartupPhase.BEFORE_APP_START));
		steps.add(() -> printInfoMessage("INITIALIZING APPLICATION..."));
		steps.add(() -> waitForStepToBeAllowed(StartupPhase.BEFORE_APP_START, StartupPhase.AFTER_APP_START));
		steps.add(() -> loadPhase(StartupPhase.AFTER_APP_START));
		steps.add(() -> printInfoMessage("APPLICATION STARTED!"));
		steps.add(() -> RuntimeInfo.instance().seal());
		steps.add(() -> loadPhase(StartupPhase.STARTUP_COMPLETE));
		return steps;
	}

	private void waitForDeploymentToFinish() {
		if (serverController.isUnsatisfied()) {
			return;
		}
		if (serverController.isAmbiguous()) {
			throw new EmfRuntimeException("More that one server status checker found!");
		}
		LOGGER.info("Waiting deployment to finish...");
		ServerControllerService checker = serverController.get();
		while (!checker.isDeploymentFinished()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.warn("Sleep interrupted will continue with loading", e);
			}
		}
		LOGGER.info("Deployment completed.");
	}

	private void waitForStepToBeAllowed(StartupPhase completedPhase, StartupPhase nextPhase) {
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

	private void loadPhase(StartupPhase phase) {
		RuntimeInfo.instance().setPhase(phase);
		List<StartupComponent> foundComponents = componentProvider.getComponentsForPhase(phase);

		// sort the components before firing the event if order is changed during the event we will sort them once again
		// this is to inform the observers of the event the correct order in which the components will be executed
		foundComponents.sort(DefaultBootStrategy::compareComponents);
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
	private void waitForAsyncTasks(StartupPhase phase, CountDownLatch sync, Collection<Future<?>> asyncTasks) {

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
					serverController.get().undeployAllDeployments();
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
	private void fireBeforePhaseEvent(StartupPhase phase, Collection<StartupComponent> providedComponents) {
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
	private Collection<Future<?>> startComponents(Collection<StartupComponent> components, CountDownLatch sync) {
		return components
				.stream()
				.sorted(DefaultBootStrategy::compareComponents)
				.map(component -> invokeOrScheduleComponent(component, sync)) // invoke tasks
				.filter(Objects::nonNull) // return async tasks only
				.collect(Collectors.toList());
	}

	static int compareComponents(StartupComponent o1, StartupComponent o2) {
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
	private Future<?> invokeOrScheduleComponent(final StartupComponent component, final CountDownLatch sync) {
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
	private void startComponent(StartupComponent component, CountDownLatch sync) {
		LOGGER.info("Loading component: {} during {}", component.getName(), component.getPhase());
		try {
			TimeTracker timeTracker = TimeTracker.createAndStart();
			component.execute();
			LOGGER.debug("Component {} loading took {} ms", component.getName(), timeTracker.stop());
		} catch (Exception e) {
			if (e.getCause() instanceof DeploymentException) {
				throw new EmfRuntimeException(e.getCause());
			}
			LOGGER.error("Component {} throw an exception on startup", component.getName(), e);
			if (component.getPhase() == StartupPhase.DEPLOYMENT) {
				serverController.get().undeployAllDeployments();
				throw new EmfRuntimeException(
						"Deployment failed due to error in component " + component.getName() + " and was undeployed.",
						e);
			}
		} finally {
			sync.countDown();
		}
	}


	/**
	 * Prints big info message.
	 *
	 * @param message
	 *            the message
	 */
	private static void printInfoMessage(String message) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(LoggingUtil.buildInfoMessage(message));
		}
	}
}
