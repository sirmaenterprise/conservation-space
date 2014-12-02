package com.sirma.itt.objects.services.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.emf.executors.ExecutableOperation;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;

/**
 * Executor for filter object creation. Used for saving searches.
 * 
 * @author nvelkov
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 511)
public class CreateFilterExecutor extends CreateObjectExecutor {

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
		return ObjectActionTypeConstants.CREATE_FILTER;
	}

}
