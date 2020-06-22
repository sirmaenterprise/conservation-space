package com.sirma.sep.model.management.persistence;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.operation.ModelChangeSet;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;
import com.sirma.sep.model.management.operation.ModelChangeSetStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Dao for storing and accessing persisted model changes.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 07/08/2018
 */
public class ModelChangesDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String VERSION = "version";
	public static final String STATUS = "status";

	@Inject
	private DbDao dbDao;

	private ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Retrieve all changes after the given version
	 *
	 * @param version the last known version to fetch
	 * @return changes after the given version
	 */
	public List<ModelChangeSetInfo> getChangesSince(Long version) {
		if (version == null) {
			return Collections.emptyList();
		}
		return queryChanges(ModelChangeEntity.QUERY_CHANGES_SINCE_KEY, Collections.singletonList(new Pair<>(VERSION, version)));
	}

	public List<ModelChangeSetInfo> getNotDeployedChanges(Long version) {
		if (version == null) {
			return Collections.emptyList();
		}
		return queryChanges(ModelChangeEntity.QUERY_NOT_DEPLOYED_CHANGES_KEY,
				Arrays.asList(new Pair<>(VERSION, version), new Pair<>(STATUS,
						Collections.singletonList(ModelChangeSetStatus.APPLIED.toString()))));
	}

	private List<ModelChangeSetInfo> queryChanges(String query, List<Pair> arguments) {
		List<ModelChangeEntity> changes = dbDao.fetchWithNamed(query, arguments);
		return changes.stream().map(fromEntity()).collect(Collectors.toList());
	}

	private Function<ModelChangeEntity, ModelChangeSetInfo> fromEntity() {
		return entity -> {
			ModelChangeSetInfo info = new ModelChangeSetInfo();
			info.setIndex(entity.getId());
			info.setAppliedOn(entity.getAppliedOn());
			info.setCreatedBy(entity.getCreatedBy());
			info.setCreatedOn(entity.getCreatedOn());
			info.setDeployedOn(entity.getDeployedOn());
			info.setFailedOn(entity.getFailedOn());
			info.setInitialVersion(entity.getInitialVersion());
			info.setAppliedVersion(entity.getAppliedVersion());
			info.setChangeSet(materializeChange(entity.getChangeData()));
			info.setStatus(ModelChangeSetStatus.valueOf(entity.getStatus()));
			info.setStatusMessage(entity.getStatusMessage());
			return info;
		};
	}

	private ModelChangeSet materializeChange(String change) {
		try {
			return objectMapper.readValue(change, ModelChangeSet.class);
		} catch (IOException e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	/**
	 * Stores the given collection of changes and assign then to the given request id
	 *
	 * @param requestId the request id to bind the changes to
	 * @param changes the changes to store
	 */
	public void saveChanges(String requestId, Collection<ModelChangeSetInfo> changes) {
		changes.stream().map(toEntity(requestId)).forEach(dbDao::saveOrUpdate);
	}

	public void saveChange(ModelChangeSetInfo change) {
		if (change.getIndex() == null) {
			throw new IllegalArgumentException("Trying to update non persisted change: " + change);
		}
		ModelChangeEntity entity = toEntity(null).apply(change);
		dbDao.saveOrUpdate(entity);
	}

	private Function<ModelChangeSetInfo, ModelChangeEntity> toEntity(String requestId) {
		return change -> {
			ModelChangeEntity entity;
			if (change.getIndex() == null) {
				entity = new ModelChangeEntity();
				entity.setRequestId(requestId);
			} else {
				entity = dbDao.find(ModelChangeEntity.class, change.getIndex());
			}
			entity.setPath(change.getChangeSet().getPath().toString());
			entity.setCreatedBy(change.getCreatedBy());
			entity.setCreatedOn(change.getCreatedOn());
			entity.setAppliedOn(change.getAppliedOn());
			entity.setFailedOn(change.getFailedOn());
			entity.setDeployedOn(change.getDeployedOn());
			entity.setInitialVersion(change.getInitialVersion());
			entity.setAppliedVersion(change.getAppliedVersion());
			entity.setChangeData(convertChange(change.getChangeSet()));
			entity.setStatusMessage(change.getStatusMessage());
			entity.setStatus(change.getStatus().toString());
			return entity;
		};
	}

	private String convertChange(ModelChangeSet changeSet) {
		try {
			return objectMapper.writeValueAsString(changeSet);
		} catch (JsonProcessingException e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	/**
	 * Retrieve changes assigned to the given request id that are not applied, yet.
	 *
	 * @param requestId the request id to look for
	 * @return changes bound to the given request id and not applied, yet
	 */
	public List<ModelChangeSetInfo> getNotAppliedChangesForRequestId(String requestId) {
		return queryChanges(ModelChangeEntity.QUERY_CHANGES_BY_REQUEST_ID_KEY,
				Collections.singletonList(new Pair<>("requestId", requestId)));
	}

	/**
	 * Retrieve changes affecting the given node identified by the given path and it's sub paths.<br>
	 * For example if searched @{code /definition=genericCase} all changes for the given definition and it's fields
	 * and attributes will be returned
	 *
	 * @param nodeAddress the request node address to look for
	 * @return not applied changes to the given node
	 */
	public List<ModelChangeSetInfo> getNotAppliedChangesForNode(Path nodeAddress, long version) {
		LOGGER.trace("Fetching non applied changes for id={}  version={}", nodeAddress, version);
		return queryChanges(ModelChangeEntity.QUERY_NOT_DEPLOYED_CHANGES_FOR_NODE_KEY,
				Arrays.asList(new Pair<>("nodeAddress", nodeAddress.toString() + '%'), new Pair<>(VERSION, version)));
	}

	/**
	 * Get paths for nodes that are not deployed yet, up to the provided applied version.
	 *
	 * @param version specifies the max version of changes to fetch
	 * @return list path from changes not deployed, yet
	 */
	public List<String> getNonDeployedPaths(long version) {
		return dbDao.fetchWithNamed(ModelChangeEntity.QUERY_NOT_DEPLOYED_PATHS_BEFORE_VERSION_KEY,
				Arrays.asList(new Pair<>(VERSION, version),
						new Pair<>(STATUS, Collections.singletonList(ModelChangeSetStatus.APPLIED.toString()))));
	}

	/**
	 * Mark changes as deployed by their database ids
	 *
	 * @param deployedChangeIds the affected changes to update
	 * @return the number of updated records
	 */
	public int markAsDeployed(Set<Long> deployedChangeIds) {
		if (CollectionUtils.isEmpty(deployedChangeIds)) {
			return 0;
		}
		int updated = dbDao.executeUpdate(ModelChangeEntity.UPDATE_AS_DEPLOYED_KEY,
				Arrays.asList(new Pair<>("deployedOn", new Date()),
						new Pair<>(STATUS, ModelChangeSetStatus.DEPLOYED.toString()),
						new Pair<>("ids", deployedChangeIds)));
		if (updated != deployedChangeIds.size()) {
			LOGGER.warn("Some of the changes are not deployed as someone else has already deployed them. "
					+ "Expected to update {} but updated only {}", deployedChangeIds.size(), updated);
		}
		return updated;
	}

	/**
	 * Query the highers model number of change set
	 *
	 * @return the found number or empty optional if no change is present in the database
	 */
	public OptionalLong findLastAppliedVersion() {
		List<Long> result = dbDao.fetchWithNamed(ModelChangeEntity.QUERY_LAST_KNOWN_MODEL_VERSION_KEY,
				Collections.emptyList());
		if (result.isEmpty() || result.get(0) == null) {
			return OptionalLong.empty();
		}
		return OptionalLong.of(result.get(0));
	}
}
