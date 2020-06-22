package com.sirma.sep.model.management;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.exceptions.TimeoutException;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;
import com.sirma.sep.model.management.persistence.ModelChangesDao;
import com.sirma.sep.model.management.request.ModelUpdateRequest;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Service responsible for persisting the model change requests and scheduling their execution on the actual model
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 01/08/2018
 */
@ApplicationScoped
public class ModelPersistence {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@DestinationDef(maxRedeliveryAttempts = 0)
	static final String MODEL_UPDATE_QUEUE = "java:/jms.queue.ModelUpdateQueue";

	@DestinationDef(expiryDelay = 24 * 60 * 60 * 1000, maxRedeliveryAttempts = 0)
	static final String MODEL_UPDATE_RESPONSE_QUEUE = "java:/jms.queue.ModelUpdateResponseQueue";

	@Inject
	private SenderService senderService;

	@Inject
	private ModelChangesDao changesDao;

	@Inject
	private ModelOpSynchronization modelOpSynchronization;

	/**
	 * Transactionally persist the given model changes and schedule applying them to the actual model. <br>
	 * The method response represents an request identifier that could be used for checking/waiting if the given
	 * changes are applied using the {@link #waitForModelUpdate(String, long, TimeUnit)}
	 *
	 * @param updateRequest the change request to save and schedule for execution
	 * @return the request id to use to check for the status of the update.
	 */
	@Transactional
	String saveChanges(ModelChanges updateRequest) {
		String requestId = modelOpSynchronization.acquire();
		changesDao.saveChanges(requestId, updateRequest.getChanges());
		senderService.sendText(MODEL_UPDATE_QUEUE, requestId,
				SendOptions.create().replyTo(MODEL_UPDATE_RESPONSE_QUEUE));
		return requestId;
	}

	/**
	 * Converts the given json body to {@link ModelUpdateRequest} instance. <br>
	 * For internal use only.
	 *
	 * @param requestId the json to parse
	 * @return the request instance
	 */
	ModelChanges readChanges(String requestId) {
		List<ModelChangeSetInfo> changes = changesDao.getNotAppliedChangesForRequestId(requestId);
		if (changes.isEmpty()) {
			LOGGER.warn("Could not find non processed change sets under the request id {}", requestId);
			return new ModelChanges();
		}
		List<Long> versions = changes.stream()
				.map(ModelChangeSetInfo::getInitialVersion)
				.distinct()
				.sorted((l1, l2) -> -Long.compare(l1, l2))// inverse order, latest revision will be first
				.collect(Collectors.toList());
		if (versions.size() > 1) {
			LOGGER.warn("Found more than one version {} for changes assigned to request id {}", versions, requestId);
		}
		ModelChanges updateRequest = new ModelChanges();
		updateRequest.setChanges(changes);
		updateRequest.setModelVersion(versions.get(0));
		return updateRequest;
	}

	/**
	 * Save changes for the given model change in the database. <br>
	 * This method cannot be called for non persisted change sets
	 *
	 * @param change the change to update
	 */
	@Transactional
	void updateChange(ModelChangeSetInfo change) {
		changesDao.saveChange(change);
	}

	/**
	 * Waits for changes represented by the given request to become available. If the request id is already processed the method returns immediately.
	 * If the request is not processed within the specified time interval then the method will fail with {@link TimeoutException}
	 *
	 * @param changeRequestId the request id to check for
	 * @param timeout the time to wait
	 * @param unit the time unit of the specified timeout
	 */
	void waitForModelUpdate(String changeRequestId, long timeout, TimeUnit unit) {
		modelOpSynchronization.waitForRequest(changeRequestId, timeout, unit);
	}

	@QueueListener(MODEL_UPDATE_RESPONSE_QUEUE)
	void onModelUpdated(Message message) throws JMSException {
		String requestId = message.getBody(String.class);
		modelOpSynchronization.release(requestId);
	}

	List<ModelChangeSetInfo> getChangesSince(Long modelVersion) {
		return changesDao.getChangesSince(modelVersion);
	}

	/**
	 * Returns model changes that are not deployed yet based  on the provided version.
	 *
	 * @param version specifies up to which version to return changes
	 * @return list with non deployed changes
	 */
	public List<ModelChangeSetInfo> getNotDeployedChanges(long version) {
		return changesDao.getNotDeployedChanges(version);
	}

	/**
	 * Get root paths for the nodes that are not deployed yet based on the provided models version.
	 *
	 * @param version specifies which non deployed paths to retrieve, their version should be less or equal to this one
	 * @return non deployed node paths
	 */
	public Set<Path> getNonDeployedPaths(long version) {
		return changesDao.getNonDeployedPaths(version).stream()
				.map(Path::parsePath)
				.map(Path::cutOffTail)
				.collect(Collectors.toSet());
	}

	/**
	 * Fetches the current model version from the database
	 *
	 * @return the model version
	 */
	long getModelVersion() {
		return changesDao.findLastAppliedVersion().orElse(0L);
	}
}
