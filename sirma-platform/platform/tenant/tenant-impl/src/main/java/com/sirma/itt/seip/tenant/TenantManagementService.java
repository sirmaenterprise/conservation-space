package com.sirma.itt.seip.tenant;

import java.lang.invoke.MethodHandles;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModel;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.tenant.wizard.exception.TenantUpdateException;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * The service executes the steps in order.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class TenantManagementService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** The steps. */
	@Inject
	@ExtensionPoint(value = TenantStep.CREATION_STEP_NAME)
	private Iterable<TenantStep> steps;

	/** The steps. */
	@Inject
	@ExtensionPoint(value = TenantStep.UPDATE_STEP_NAME)
	private Iterable<TenantStep> updateSteps;

	@Inject
	private TenantManager tenantManager;

	/**
	 * Execute the step creation. The model is expected to be updated with all required data. If any error in step
	 * occurs all rollback methods for already executed steps are invoked as well on the current
	 *
	 * @param models
	 *            the populated models
	 */
	@SuppressWarnings("boxing")
	public void create(TenantInitializationModel models) {
		TenantInitializationContext tenantInitializationContext = new TenantInitializationContext();
		Deque<TenantStep> executed = new LinkedList<>();
		TimeTracker timeTracker = TimeTracker.createAndStart();
		LOGGER.info("Beginning new tenant creation..");
		try {
			steps.forEach(step -> invokeStep(step, models, tenantInitializationContext, executed));
			LOGGER.info("Tenant creation completed. Process took {} ms", timeTracker.stop());
		} catch (Exception e) {
			LOGGER.error("Tenant creation failure due to {} - going to rollback!", e.getMessage(), e);
			rollback(executed, tenantInitializationContext, models);
			throw new TenantCreationException("Tenant creation failure!", e);
		}
	}

	/**
	 * Execute update steps. The model is expected to be updated with all required data. If any error in step occurs all
	 * rollback methods for already executed steps are invoked as well on the current
	 *
	 * @param models
	 *            the populated models
	 * @param tenantId
	 *            Id of the tenant
	 */
	@SuppressWarnings("boxing")
	public void update(TenantInitializationModel models, String tenantId) {
		if (!tenantManager.tenantExists(tenantId)) {
			throw new TenantUpdateException("Cannot update non existing tenant!");
		}

		TenantInitializationContext tenantInitializationContext = new TenantInitializationContext();
		tenantInitializationContext.setTenantInfo(tenantManager.getTenant(tenantId).get().toTenantInfo());

		Deque<TenantStep> executed = new LinkedList<>();
		TimeTracker timeTracker = TimeTracker.createAndStart();
		LOGGER.info("Begin updating tenant with id: {}", tenantId);
		try {
			updateSteps.forEach(step -> invokeStep(step, models, tenantInitializationContext, executed));
			// notify observers
			tenantManager.finishTenantActivation(tenantId);
			LOGGER.info("Tenant {} update completed. Process took {} ms", tenantId, timeTracker.stop());
		} catch (Exception e) {
			LOGGER.error("Tenant {} update failure due to {} - going to rollback!", tenantId, e.getMessage(), e);
			rollback(executed, tenantInitializationContext, models);
			throw new TenantCreationException("Tenant " + tenantId + " update failure!", e);
		}
	}

	private static void invokeStep(TenantStep creationStep, TenantInitializationModel models,
			TenantInitializationContext tenantInitializationContext, Deque<TenantStep> executed) {
		String stepName = creationStep.getIdentifier();

		LOGGER.info("Processing step: {}...", stepName);
		executed.push(creationStep);

		TenantStepData current = models.get(stepName);
		Objects.requireNonNull(current, "Unindentified creation step " + stepName);

		creationStep.execute(current, tenantInitializationContext);
		LOGGER.info("Step: " + stepName + " is completed!");
	}

	private static void rollback(Deque<TenantStep> executed, TenantInitializationContext tenantInitializationContext,
			TenantInitializationModel models) {
		TenantStep step;
		while (!executed.isEmpty()) {
			step = executed.pop();
			try {
				LOGGER.info("Rolling back step: " + step.getIdentifier());
				step.rollback(models.get(step.getIdentifier()), tenantInitializationContext);
			} catch (Exception e) {
				LOGGER.warn("Rollback step {} failed with: {}", step.getIdentifier(), e.getMessage(), e);
			}
		}
		LOGGER.info("Rollback complete!");
	}

	/**
	 * Gets the model for tenant initialization. It is a new blank model that is legitimate for step update and for
	 * later invocation of {@link #create(TenantInitializationModel)}
	 *
	 * @return the model to be updated before {@link #create(TenantInitializationModel)}
	 */
	public TenantInitializationModel provideModel() {
		TenantInitializationModel model = new TenantInitializationModel();
		steps.iterator().forEachRemaining(step -> model.add(step.provide()));
		return model;
	}

	/**
	 * Provides list with active tenant ids
	 *
	 * @return List with tenant ids
	 */
	public List<String> getTenantIds() {
		return tenantManager
				.getActiveTenantsInfo(false)
					.map(tenant -> tenant.getTenantId())
					.collect(Collectors.toList());
	}
}
