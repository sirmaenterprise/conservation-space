package com.sirma.itt.objects.services.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.emf.executors.EditDetailsExecutor;
import com.sirma.itt.emf.executors.ExecutableOperation;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;

/**
 * Update executor for filters.
 * 
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 513)
public class UpdateFilterExecutor extends EditDetailsExecutor {

	@Inject
	private FilterHelper filterHelper;

	@Override
	public OperationResponse execute(SchedulerContext context) {

		if (!filterHelper.validateFilter(context)) {
			JSONObject errorObject = new JSONObject();
			// REVIEW: Return i18n messages or error keys
			JsonUtil.addToJson(errorObject, "errorMsg", "A filter with that title already exists");
			return new OperationResponse(SchedulerEntryStatus.FAILED, errorObject);
		}
		return super.execute(context);
	}

	@Override
	public SchedulerContext parseRequest(JSONObject data) {
		filterHelper.convertRequest(data);
		return super.parseRequest(data);
	}

	@Override
	public String getOperation() {
		return ObjectActionTypeConstants.UPDATE_FILTER;
	}
}
