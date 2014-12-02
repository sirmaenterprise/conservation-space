package com.sirma.itt.cmf.services.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.emf.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Executor for create case operation. The revert operation here is permanent delete of the case
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = CreateCaseExecutor.TARGET_NAME, order = 120)
public class CreateCaseExecutor extends BaseInstanceExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateCaseExecutor.class);

	@Inject
	private CaseService caseService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return ActionTypeConstants.CREATE_CASE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean rollback(SchedulerContext data) {
		Instance instance = getOrCreateInstance(data);
		if (instance instanceof CaseInstance) {
			CaseInstance caseInstance = (CaseInstance) instance;
			if (caseInstance.getDmsId() != null) {
				// delete all content from DMS about the case
				try {
					caseService.delete(caseInstance, new Operation(ActionTypeConstants.DELETE),
							true);
				} catch (Exception e) {
					LOGGER.warn("Failed to rollback case instance creation by deleting due to {}",
							e.getMessage(), e);
					return false;
				}
			}
			// probably we didn't manage to create it so nothing to worry about
		} else {
			if (instance != null) {
				LOGGER.warn("Invalid instance type passed for rollback. Expected {} but was {}.",
						CaseInstance.class.getSimpleName(), instance.getClass().getSimpleName());
			} else {
				return super.rollback(data);
			}
		}

		return true;
	}

	@Override
	public JSONObject toJson(Instance instance) {
		JSONObject json = super.toJson(instance);

		CaseInstance caseInstance = (CaseInstance) instance;

		JSONArray sections = new JSONArray();
		for (SectionInstance currentSection : caseInstance.getSections()) {
			JSONObject section = new JSONObject();
			JsonUtil.addToJson(section, "id", currentSection.getId());
			JsonUtil.addToJson(section, "name", currentSection.getIdentifier());
			sections.put(section);
		}

		JsonUtil.addToJson(json, "sections", sections);

		return json;
	}
}
