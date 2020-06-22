package com.sirma.itt.emf.link.actions;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.actions.BaseExecutableOperation;
import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.Operation;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.instance.actions.OperationStatus;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;

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
 * 			inverseRelationId: "emf:dependsOn",
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

	private static final String INVERSE_RELATION_ID = "inverseRelationId";
	private static final String RELATION_ID = "relationId";
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateRelationAction.class);

	@Inject
	private LinkService linkService;

	@Override
	public String getOperation() {
		return "createLink";
	}

	@Override
	public OperationContext parseRequest(JSONObject data) {
		InstanceReference fromRef = extractReference(data, "fromId", "fromType", false);
		InstanceReference toRef = extractReference(data, "toId", "toType", false);

		OperationContext context = new OperationContext(10);
		context.put("from", fromRef);
		context.put("to", toRef);

		String relationId = JsonUtil.getStringValue(data, RELATION_ID);
		if (relationId == null) {
			throw new IllegalArgumentException("Missing relation id for key: relationId");
		}
		context.put(RELATION_ID, relationId);
		String inverseId = JsonUtil.getStringValue(data, INVERSE_RELATION_ID);
		if (inverseId == null) {
			inverseId = relationId;
		}
		context.put(INVERSE_RELATION_ID, inverseId);

		JSONObject properties = JsonUtil.getJsonObject(data, "properties");
		if (properties != null) {
			String createdBy = JsonUtil.getStringValue(properties, DefaultProperties.CREATED_BY);
			context.put("createdBy", createdBy);
		}

		return context;
	}

	@Override
	public OperationResponse execute(OperationContext data) {
		InstanceReference from = data.getIfSameType("from", InstanceReference.class);
		InstanceReference to = data.getIfSameType("to", InstanceReference.class);
		String relationId = data.getIfSameType(RELATION_ID, String.class);
		String inverseId = data.getIfSameType(INVERSE_RELATION_ID, String.class);
		Serializable createdBy = data.getIfSameType("createdBy", String.class);

		Map<String, Serializable> properties = CollectionUtils.createHashMap(5);
		if (createdBy != null) {
			Resource resource = resourceService.findResource(createdBy);
			if (resource == null) {
				LOGGER.warn("Could not find user {}. Will use system user instead!", createdBy);
			}
			createdBy = resource;
		}
		if (createdBy == null) {
			createdBy = securityContext.getAuthenticated();
		}
		properties.put(DefaultProperties.CREATED_BY, createdBy);
		properties.put(DefaultProperties.CREATED_ON, new Date());
		Pair<Serializable, Serializable> pair = linkService.link(from, to, relationId, inverseId, properties);
		return new OperationResponse(OperationStatus.COMPLETED, pair.getFirst());
	}

	@Override
	public boolean rollback(OperationContext data) {
		String relationId = data.getIfSameType(RELATION_ID, String.class);
		String inverseId = data.getIfSameType(INVERSE_RELATION_ID, String.class);
		// TODO: change to unlink permanently
		boolean unlink = linkService.unlink(data.getIfSameType("from", InstanceReference.class),
				data.getIfSameType("to", InstanceReference.class), relationId, inverseId);
		return unlink;
	}

	@Override
	public boolean couldBeAsynchronous(OperationContext data) {
		return true;
	}

	@Override
	public Map<Serializable, Operation> getDependencies(OperationContext data) {
		InstanceReference from = data.getIfSameType("from", InstanceReference.class);
		InstanceReference to = data.getIfSameType("to", InstanceReference.class);
		return CollectionUtils.addToMap(null, new Pair<Serializable, Operation>(from.getId(), Operation.USE),
				new Pair<Serializable, Operation>(to.getId(), Operation.USE));
	}

}
