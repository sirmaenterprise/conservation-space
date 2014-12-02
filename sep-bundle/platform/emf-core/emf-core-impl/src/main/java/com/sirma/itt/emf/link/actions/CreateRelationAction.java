package com.sirma.itt.emf.link.actions;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.executors.BaseExecutableOperation;
import com.sirma.itt.emf.executors.ExecutableOperation;
import com.sirma.itt.emf.executors.Operation;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Operation for creating a link between 2 instances. TODO: add support for simple links
 * 
 * <pre>
 * <code>{
 *  		fromId: "emf:someInstanceId",
 * 			fromType: "workflow",
 * 			toId: "emf:someDocument",
 * 			toType: "document",
 * 			relationId: "emf:dependsOn",
 * 			properties : {
 * 				createdBy: "emf:userId"
 * 			}
 * }</code>
 * </pre>
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 50)
public class CreateRelationAction extends BaseExecutableOperation {

	/** The link service. */
	@Inject
	private LinkService linkService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return "createLink";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerContext parseRequest(JSONObject data) {
		InstanceReference fromRef = extractReference(data, "fromId", "fromType", false);
		InstanceReference toRef = extractReference(data, "toId", "toType", false);

		SchedulerContext context = new SchedulerContext(10);
		context.put("from", fromRef);
		context.put("to", toRef);

		String relationId = JsonUtil.getStringValue(data, "relationId");
		if (relationId == null) {
			throw new IllegalArgumentException("Missing relation id for key: relationId");
		}
		context.put("relationId", relationId);

		JSONObject properties = JsonUtil.getJsonObject(data, "properties");
		if (properties != null) {
			String createdBy = JsonUtil.getStringValue(properties, "createdBy");
			context.put("createdBy", createdBy);
		}

		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OperationResponse execute(SchedulerContext data) {
		String relationId = data.getIfSameType("relationId", String.class);
		String createdBy = data.getIfSameType("createdBy", String.class);

		Map<String, Serializable> properties;
		if (createdBy != null) {
			properties = CollectionUtils.createHashMap(5);
			properties.put(DefaultProperties.CREATED_BY, createdBy);
		} else {
			properties = LinkConstants.DEFAULT_SYSTEM_PROPERTIES;
		}
		Pair<Serializable, Serializable> pair = linkService.link(
				data.getIfSameType("from", InstanceReference.class),
				data.getIfSameType("to", InstanceReference.class), relationId, relationId,
				properties);
		OperationResponse response = new OperationResponse(SchedulerEntryStatus.COMPLETED, pair.getFirst());
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean rollback(SchedulerContext data) {
		String relationId = data.getIfSameType("relationId", String.class);
		// TODO: change to unlink permanently
		boolean unlink = linkService.unlink(data.getIfSameType("from", InstanceReference.class),
				data.getIfSameType("to", InstanceReference.class), relationId, relationId);
		return unlink;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean couldBeAsynchronous(SchedulerContext data) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Serializable, Operation> getDependencies(SchedulerContext data) {
		InstanceReference from = data.getIfSameType("from", InstanceReference.class);
		InstanceReference to = data.getIfSameType("to", InstanceReference.class);
		return CollectionUtils.addToMap(null,
				new Pair<Serializable, Operation>(from.getIdentifier(), Operation.USE),
				new Pair<Serializable, Operation>(to.getIdentifier(), Operation.USE));
	}

}
