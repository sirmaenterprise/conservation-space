package com.sirma.itt.seip.instance.revision;

import static com.sirma.itt.seip.configuration.Options.DISABLE_AUDIT_LOG;
import static com.sirma.itt.seip.configuration.Options.DISABLE_AUTOMATIC_LINKS;
import static com.sirma.itt.seip.configuration.Options.DISABLE_STALE_DATA_CHECKS;
import static com.sirma.itt.seip.configuration.Options.NO_OP;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PUBLISHED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.REVISION_NUMBER;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.REVISION_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.STATUS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.UNIQUE_IDENTIFIER;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.CLONE_UPLOADED_CONTENT;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.COPY_PERMISSIONS;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.COPY_RELATIONS;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.EXPORT_CONTENT_TO_PDF;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.EXPORT_TABS_AS_PDF;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.INITIAL;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.LOCK_WIDGET_DATA;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.REMOVE_NON_PUBLISHED_TABS;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.REPLACE_EXPORTED_TABS;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.SAVE_REVISION_VIEW;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.SYNC_THUMBNAIL;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotation.Chaining;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.context.Option;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.TenantAware;
import com.sirma.itt.seip.domain.instance.CMInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.revision.steps.PublishContext;
import com.sirma.itt.seip.instance.revision.steps.PublishStepRunner;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionMode;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Default implementation for {@link RevisionService}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class RevisionServiceImpl implements RevisionService {

	private static final Operation CLONE = new Operation(ActionTypeConstants.CLONE);
	private static final Operation OBSOLETE = new Operation(ActionTypeConstants.OBSOLETE);

	private static final String LATEST_PUBLISHED_RELATION = "emf:lastPublishedRevision";
	private static final String LAST_REVISION_RELATION = "emf:lastRevision";
	private static final String LATEST = "latest";

	private static final OverrideInfo EMPTY_INFO = new OverrideInfo(null, null, null);

	private static final Logger LOGGER = LoggerFactory.getLogger(RevisionServiceImpl.class);

	private static final String[] PUBLISH_UPLOADED_AS_PDF = { INITIAL.getName(), EXPORT_CONTENT_TO_PDF.getName(),
			REMOVE_NON_PUBLISHED_TABS.getName(), COPY_RELATIONS.getName(), SYNC_THUMBNAIL.getName(),
			SAVE_REVISION_VIEW.getName(), COPY_PERMISSIONS.getName() };

	private static final String[] PUBLISH_IDOC_AS_PDF = { INITIAL.getName(), EXPORT_TABS_AS_PDF.getName(),
			REMOVE_NON_PUBLISHED_TABS.getName(), COPY_RELATIONS.getName(), REPLACE_EXPORTED_TABS.getName(),
			SYNC_THUMBNAIL.getName(), SAVE_REVISION_VIEW.getName(), COPY_PERMISSIONS.getName() };

	private static final String[] PUBLISH_UPLOADED = { INITIAL.getName(), CLONE_UPLOADED_CONTENT.getName(),
			REMOVE_NON_PUBLISHED_TABS.getName(), COPY_RELATIONS.getName(), SYNC_THUMBNAIL.getName(),
			SAVE_REVISION_VIEW.getName(), COPY_PERMISSIONS.getName() };

	private static final String[] PUBLISH_IDOC = { INITIAL.getName(), LOCK_WIDGET_DATA.getName(),
			REMOVE_NON_PUBLISHED_TABS.getName(), COPY_RELATIONS.getName(), SYNC_THUMBNAIL.getName(),
			SAVE_REVISION_VIEW.getName(), COPY_PERMISSIONS.getName() };

	@Inject
	private InstanceService objectService;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private InstanceVersionService instanceVersionService;

	@Inject
	private EventService eventService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private LinkService linkService;

	@Inject
	@Chaining
	private LinkService chainingLinkService;

	@Inject
	private StateService stateService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private SearchService searchService;

	@Inject
	private DatabaseIdManager idManager;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Inject
	private PublishStepRunner stepRunner;

	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	@Transactional
	public Instance publish(PublishInstanceRequest publishRequest) {
		Instance instanceToPublish = publishRequest.getInstanceToPublish();
		if (isRevision(instanceToPublish)) {
			LOGGER.warn("Called publish on a revision instance {}", instanceToPublish.getId());
			return null;
		}
		// disable all automatic id generations when publishing
		// because the revisions are not new instance but copies (snapshots)
		Options.DISABLE_AUTOMATIC_ID_GENERATION.enable();
		try {
			Instance publishedInstance = publishInstanceIn(publishRequest, instanceToPublish, true, EMPTY_INFO);
			instanceLoadDecorator.decorateInstance(publishedInstance);
			return publishedInstance;
		} finally {
			Options.DISABLE_AUTOMATIC_ID_GENERATION.disable();
		}
	}

	/**
	 * Publish instance the given instance as parent to the given location instance. The new revision will be a child to
	 * the given publish location instance.
	 *
	 * @param publishRequest
	 *            the publish request
	 * @param publishLocation
	 *            the publish location
	 * @param createLatest
	 *            if latest instance should be created or not
	 * @param overrideInfo
	 *            sub revision override info to use
	 * @return the created revision
	 */
	private Instance publishInstanceIn(PublishInstanceRequest publishRequest, Instance publishLocation,
			boolean createLatest, OverrideInfo overrideInfo) {
		String userOperation = Operation.getUserOperationId(publishRequest.getTriggerOperation());
		boolean isRejectOperation = userOperation != null && userOperation.toLowerCase().contains("reject");
		boolean isInRejectedState = stateService.isInState(PrimaryStates.REJECTED,
				publishRequest.getInstanceToPublish());

		if (isRejectOperation || isInRejectedState) {
			return publishRejected(publishRequest, publishLocation, overrideInfo);
		}
		return publishApproved(publishRequest, publishLocation, createLatest, overrideInfo);
	}

	private Instance publishApproved(PublishInstanceRequest publishRequest, Instance publishLocation,
			boolean createLatest, OverrideInfo overrideInfo) {
		Instance instanceToPublish = publishRequest.getInstanceToPublish();
		Operation operation = publishRequest.getTriggerOperation();

		Instance newRevision = createNewRevision(publishRequest, overrideInfo.getRevisionNumber());

		Instance latestRevision = null;
		if (createLatest) {
			latestRevision = getOrCreateLatestRevisionInstance(instanceToPublish, newRevision);
		}

		Instance currentRevision = getLastRevision(instanceToPublish);
		eventService.fire(
				new PublishApprovedRevisionEvent(instanceToPublish, newRevision, currentRevision, latestRevision));

		changeCurrentPublished(instanceToPublish, newRevision);
		linkToSuccessorRevision(currentRevision, newRevision);

		OwnedModel.setOwnedModel(newRevision, publishLocation);

		// save all new objects to database
		InstanceSaveContext newRevisionSaveContext = InstanceSaveContext.create(newRevision, operation);
		newRevisionSaveContext.getVersionContext().setVersionMode(VersionMode.MAJOR);
		save(newRevisionSaveContext, overrideInfo.getRevisionState(), NO_OP, NO_OP);

		// no override needed
		if (currentRevision != null) {
			// called with super permissions as the current user may not have a permission for the previous revision
			save(InstanceSaveContext.create(currentRevision, OBSOLETE), null, NO_OP, NO_OP);
		}

		InstanceSaveContext publishedInstanceSaveContext = InstanceSaveContext.create(instanceToPublish, operation);
		publishedInstanceSaveContext.getVersionContext().setVersionMode(VersionMode.MAJOR);
		save(publishedInstanceSaveContext, overrideInfo.getPrimaryState(), DISABLE_STALE_DATA_CHECKS, NO_OP);

		if (latestRevision != null) {
			// no need to create versions for the latest revision instance
			// it's just a proxy
			InstanceSaveContext latestSaveContext = InstanceSaveContext.create(latestRevision, operation);
			latestSaveContext.getVersionContext().setVersionMode(VersionMode.NONE);
			save(latestSaveContext, overrideInfo.getRevisionState(), DISABLE_STALE_DATA_CHECKS, DISABLE_AUDIT_LOG);
		}

		publishSubElements(publishRequest, newRevision);

		return newRevision;
	}

	private Instance publishRejected(PublishInstanceRequest publishRequest, Instance publishLocation,
			OverrideInfo overrideInfo) {
		Instance instanceToPublish = publishRequest.getInstanceToPublish();
		Operation operation = publishRequest.getTriggerOperation();

		Instance lastRevision = getLastRevision(instanceToPublish);

		Instance newRevision = createNewRevision(publishRequest, overrideInfo.getRevisionNumber());

		eventService.fire(new PublishRejectedRevisionEvent(instanceToPublish, newRevision));

		linkToSuccessorRevision(lastRevision, newRevision);

		OwnedModel.setOwnedModel(newRevision, publishLocation);

		// save all new objects to database
		InstanceSaveContext ctx = InstanceSaveContext.create(newRevision, operation);
		ctx.getVersionContext().setVersionMode(VersionMode.MAJOR);
		save(ctx, overrideInfo.getRevisionState(), NO_OP, NO_OP);
		// disable stale data because if in attached in the same case then we
		// have a problem
		ctx = InstanceSaveContext.create(instanceToPublish, operation);
		ctx.getVersionContext().setVersionMode(VersionMode.MAJOR);
		save(ctx, overrideInfo.getPrimaryState(), DISABLE_STALE_DATA_CHECKS, NO_OP);

		publishSubElements(publishRequest, newRevision);

		return newRevision;
	}

	/**
	 * Save the given instance and activates the given options and deactivates them at the end of the call. The method
	 * deactivates the automatic links creation by default
	 *
	 * @param ctx
	 *            the instance context that carry the needed information to perform the save operation.
	 * @param overrideState
	 *            if non <code>null</code> value is passed it will be used to override the state of the saved instance
	 * @param disableStaleData
	 *            the disable stale data option, should not be <code>null</code>
	 * @param disableAudit
	 *            the disable audit option, should not be <code>null</code>
	 */
	private void save(InstanceSaveContext ctx, String overrideState, Option disableStaleData, Option disableAudit) {
		DISABLE_AUTOMATIC_LINKS.enable();
		disableStaleData.enable();
		disableAudit.enable();
		// override the state if passed at all
		ctx.getOperation().setNextPrimaryState(overrideState);
		try {
			// save all new objects to database
			securityContextManager.executeAsAdmin().function(domainInstanceService::save, ctx);
		} finally {
			DISABLE_AUTOMATIC_LINKS.disable();
			disableStaleData.disable();
			disableAudit.disable();
			ctx.getOperation().setNextPrimaryState(null);
		}
	}

	/**
	 * Link to successor revision.
	 *
	 * @param currentRevision
	 *            the current revision
	 * @param newRevision
	 *            the new revision
	 */
	private void linkToSuccessorRevision(Instance currentRevision, Instance newRevision) {
		if (currentRevision == null || newRevision == null) {
			return;
		}
		linkService.link(currentRevision, newRevision, LinkConstants.NEXT_REVISION, LinkConstants.PREVIOUS_REVISION,
				LinkConstants.getDefaultSystemProperties());
	}

	private void linkCurrentWithNewRevision(Instance current, Instance newRevision) {
		linkService.link(current, newRevision, LinkConstants.HAS_REVISION, LinkConstants.IS_REVISION_OF,
				LinkConstants.getDefaultSystemProperties());
	}

	/**
	 * Publish any related instances and add them in the context of the given instance
	 *
	 * @param publishRequest
	 *            the instance to publish
	 * @param revision
	 *            the parent revision instance
	 */
	private void publishSubElements(PublishInstanceRequest publishRequest, Instance revision) {
		Collection<Instance> subElements = getSubElements(publishRequest);
		if (subElements.isEmpty()) {
			return;
		}
		try {
			Instance instanceToPublish = publishRequest.getInstanceToPublish();
			DISABLE_STALE_DATA_CHECKS.enable();
			// force the state over newly created revision
			for (Instance instance : subElements) {
				if (isRevision(instance)) {
					continue;
				}
				OverrideInfo overrideInfo = new OverrideInfo(stateService.getPrimaryState(instanceToPublish),
						stateService.getPrimaryState(revision), (String) getCurrentRevision(instanceToPublish));

				PublishInstanceRequest subInstancePublishRequest = publishRequest.copyForNewInstance(instance);
				Instance newSubRevision = publishInstanceIn(subInstancePublishRequest, revision, false, overrideInfo);
				addRevisionSubElementToRevision(publishRequest, revision, newSubRevision);
			}
		} finally {
			DISABLE_STALE_DATA_CHECKS.disable();
		}
	}

	@Override
	public Instance getOrCreateLatestRevisionInstance(Instance instanceToPublish, Instance newRevision) {
		Serializable latestRevisionId = createRevisionId(instanceToPublish, LATEST);
		Instance latestRevision = objectService.loadByDbId(latestRevisionId);
		if (latestRevision == null) {
			latestRevision = createRevisionFor(instanceToPublish);
			setRevision(latestRevision, getCurrentRevision(instanceToPublish));
			setRevisionId(instanceToPublish, latestRevision, LATEST);
			PropertiesUtil.copyValue(newRevision, latestRevision, DefaultProperties.PUBLISHED_BY);
			PropertiesUtil.copyValue(newRevision, latestRevision, DefaultProperties.PUBLISHED_ON);
			OwnedModel.setOwnedModel(latestRevision, instanceToPublish);
		} else {
			copyFromRevision(newRevision, latestRevision);
		}
		return latestRevision;
	}

	/**
	 * Creates the new revision from the given instance using the provided plugin. The created revision will have a
	 * revision number based on the current number or if the argument <code>subElementRevisionNumber</code> is non null
	 * will be used directly without incrementing.
	 *
	 * @param publishRequest
	 *            the publish request
	 * @param subElementRevisionNumber
	 *            if passed a non <code>null</code> value will be used to override the revision number of the published
	 *            sub elements
	 * @return the created revision
	 */
	private Instance createNewRevision(PublishInstanceRequest publishRequest, String subElementRevisionNumber) {
		Instance instanceToPublish = publishRequest.getInstanceToPublish();
		Instance revision = createRevisionFor(instanceToPublish);
		setPublishMetadata(revision);

		// allow overriding the revision number of the created revision
		// this is used to set the same revision in the sub elements as the one
		// of the master
		// instance
		String nextVersion = subElementRevisionNumber;
		if (nextVersion == null) {
			nextVersion = getNextMajorVersion(getCurrentRevision(instanceToPublish));
		}
		// update versions
		setRevision(instanceToPublish, nextVersion);
		setRevision(revision, nextVersion);

		setRevisionId(instanceToPublish, revision, nextVersion);

		linkCurrentWithNewRevision(instanceToPublish, revision);

		eventService.fire(new CreatedRevisionEvent(instanceToPublish, revision));
		setAsLastRevisionTo(instanceToPublish, revision);

		stepRunner.getRunner(getPublishSteps(publishRequest)).run(new PublishContext(publishRequest, revision));

		return revision;
	}

	private static String[] getPublishSteps(PublishInstanceRequest publishRequest) {
		boolean isUploaded = publishRequest.getInstanceToPublish().isUploaded();
		if (publishRequest.isAsPdf()) {
			if (isUploaded) {
				return PUBLISH_UPLOADED_AS_PDF;
			}
			return PUBLISH_IDOC_AS_PDF;
		} else if (isUploaded) {
			return PUBLISH_UPLOADED;
		}
		return PUBLISH_IDOC;
	}

	/**
	 * Sets the publish metadata.
	 *
	 * @param revision
	 *            the new publish metadata
	 */
	private void setPublishMetadata(Instance revision) {
		revision.add(PUBLISHED_BY, securityContext.getAuthenticated().getSystemId());
		revision.add(DefaultProperties.PUBLISHED_ON, new Date());
	}

	/**
	 * Sets the revision id.
	 *
	 * @param instance
	 *            the instance
	 * @param revision
	 *            the revision
	 * @param version
	 *            the version
	 */
	private void setRevisionId(Instance instance, Instance revision, String version) {
		// generate new id and set it
		idManager.unregisterId(revision.getId());
		revision.setId(createRevisionId(instance, version));
		idManager.register(revision);
	}

	/**
	 * Creates the revision id.
	 *
	 * @param instance
	 *            the instance
	 * @param version
	 *            the version
	 * @return the serializable
	 */
	private Serializable createRevisionId(Instance instance, String version) {
		return idManager.getRevisionId(instance.getId(), version);
	}

	/**
	 * Gets the next major version.
	 *
	 * @param currentVersion
	 *            the serializable
	 * @return the next major version
	 */
	private static String getNextMajorVersion(Serializable currentVersion) {
		String version = "1.0";
		if (currentVersion instanceof String) {
			try {
				Double parseDouble = Double.valueOf(currentVersion.toString());
				BigDecimal bigDecimal = BigDecimal.valueOf(parseDouble.doubleValue());
				bigDecimal = bigDecimal.setScale(bigDecimal.scale(), RoundingMode.DOWN).add(BigDecimal.ONE);
				version = new StringBuilder(8).append(bigDecimal.intValue()).append(".0").toString();
			} catch (NumberFormatException e) {
				LOGGER.warn("Invalid revision format expected floating point but was {}", currentVersion, e);
			}
		}
		return version;
	}

	private Instance createRevisionFor(Instance source) {
		Instance revision = objectService.clone(source, CLONE);
		revision.addIfNotNull(UNIQUE_IDENTIFIER, CMInstance.getContentManagementId(source, null));
		// status is not cloned by default so we copy it here
		PropertiesUtil.copyValue(source, revision, STATUS);
		instanceVersionService.setInitialVersion(revision);
		return revision;
	}

	private static Serializable getCurrentRevision(Instance instance) {
		return instance.get(REVISION_NUMBER);
	}

	private static void setRevision(Instance instance, Serializable version) {
		instance.add(REVISION_NUMBER, version);
	}

	private Collection<Instance> getSubElements(PublishInstanceRequest publishRequest) {
		// TODO: implement children fetching based on the request
		return Collections.emptyList();
	}

	private void addRevisionSubElementToRevision(PublishInstanceRequest publishRequest, Instance revision,
			Instance childElement) {
		// TODO: implement child linking
	}

	private static void copyFromRevision(Instance newRevision, Instance latestRevision) {
		Map<String, Serializable> map = new HashMap<>(newRevision.getProperties());
		map.remove(UNIQUE_IDENTIFIER);

		latestRevision.addAllProperties(map);
		latestRevision.setIdentifier(newRevision.getIdentifier());
		latestRevision.setRevision(newRevision.getRevision());

		if (newRevision instanceof TenantAware) {
			((TenantAware) latestRevision).setContainer(((TenantAware) newRevision).getContainer());
		}
		if (newRevision instanceof CMInstance) {
			((CMInstance) latestRevision).setContentManagementId(((CMInstance) newRevision).getContentManagementId());
			PropertiesUtil.copyValue(newRevision, latestRevision, UNIQUE_IDENTIFIER);
		}
	}

	@Override
	public boolean isRevisionSupported(Instance instanceToCheck) {
		return instanceToCheck != null;
	}

	@Override
	public Instance getLatestPublishedRevision(Instance instance) {
		return getRevision(instance, LATEST_PUBLISHED_RELATION);
	}

	@Override
	public Instance getLastRevision(Instance instance) {
		return getRevision(instance, LAST_REVISION_RELATION);
	}

	/**
	 * Gets the revision relation to the given instance with the give link type.
	 *
	 * @param instance
	 *            the instance
	 * @param linkId
	 *            the link id
	 * @return the revision
	 */
	private Instance getRevision(Instance instance, String linkId) {
		List<LinkReference> links = chainingLinkService.getSimpleLinks(instance.toReference(), linkId);
		if (links.isEmpty()) {
			return null;
		}
		LinkReference reference = links.get(0);
		return reference.getTo().toInstance();
	}

	/**
	 * Sets the as last revision to.
	 *
	 * @param instanceToPublish
	 *            the instance to publish
	 * @param revision
	 *            the revision
	 */
	private void setAsLastRevisionTo(Instance instanceToPublish, Instance revision) {
		chainingLinkService.unlinkSimple(instanceToPublish.toReference(), LAST_REVISION_RELATION);
		chainingLinkService.linkSimple(instanceToPublish.toReference(), revision.toReference(), LAST_REVISION_RELATION);
	}

	/**
	 * Change current published.
	 *
	 * @param instanceToPublish
	 *            the instance to publish
	 * @param revision
	 *            the revision
	 */
	private void changeCurrentPublished(Instance instanceToPublish, Instance revision) {
		chainingLinkService.unlinkSimple(instanceToPublish.toReference(), LATEST_PUBLISHED_RELATION);
		chainingLinkService.linkSimple(instanceToPublish.toReference(), revision.toReference(),
				LATEST_PUBLISHED_RELATION);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends Instance> Collection<I> getRevisions(InstanceReference reference, boolean asceding) {

		Context<String, Object> context = new Context<>(3);
		context.put("objectUri", reference.getIdentifier());
		context.put("orderDirection", asceding ? "ASC" : "DESC");
		context.put("draftRevision", asceding ? "0.0" : "999999.0");
		SearchArguments<? extends Instance> arguments = searchService.getFilter("customQueries/getRevisions",
				SearchInstance.class, context);
		if (arguments == null) {
			LOGGER.warn("Could not find query [customQueries/getRevisions]");
			return Collections.emptyList();
		}
		searchService.searchAndLoad(Instance.class, arguments);
		return (Collection<I>) arguments.getResult();
	}

	@Override
	public boolean isRevision(Instance instanceToCheck) {
		Serializable serializable = instanceToCheck.get(REVISION_TYPE);
		if (serializable == null) {
			return false;
		}
		if (!(serializable instanceof String)) {
			Serializable converted = typeConverter.convert(serializable.getClass(),
					"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#current");
			return !EqualsHelper.nullSafeEquals(serializable, converted);
		}
		return !EqualsHelper.nullSafeEquals(serializable, "emf:current");
	}

	@Override
	public Instance getRevisionOf(Instance revision) {
		if (isRevision(revision)) {
			List<LinkReference> links = linkService.getLinks(revision.toReference(), LinkConstants.IS_REVISION_OF);
			if (links.isEmpty()) {
				LOGGER.warn("Could not find the original instance for revision {}", revision.getId());
				return null;
			}
			if (links.size() > 1) {
				LOGGER.warn("Found more than one original instance for revision {}", revision.getId());
			}
			return links.get(0).getTo().toInstance();
		}
		// not a revision return the same instance
		return revision;
	}

	/**
	 * DTO object to carry the override info for sub elements
	 *
	 * @author BBonev
	 */
	private static class OverrideInfo {
		/**
		 * If passed a non <code>null</code> values they will be used to override the state of the published sub
		 * elements
		 */
		private final String primaryState;
		/**
		 * If passed a non <code>null</code> values they will be used to override the state of the published sub
		 * elements
		 */
		private final String revisionState;
		/**
		 * if passed a non <code>null</code> value will be used to override the revision number of the published sub
		 * elements
		 */
		private final String revisionNumber;

		/**
		 * Instantiates a new override info.
		 *
		 * @param primaryState
		 *            the primary state
		 * @param revisionState
		 *            the revision state
		 * @param revisionNumber
		 *            the revision number
		 */
		public OverrideInfo(String primaryState, String revisionState, String revisionNumber) {
			this.primaryState = primaryState;
			this.revisionState = revisionState;
			this.revisionNumber = revisionNumber;
		}

		/**
		 * Gets the primary state.
		 *
		 * @return the primary state
		 */
		public String getPrimaryState() {
			return primaryState;
		}

		/**
		 * Gets the revision state.
		 *
		 * @return the revision state
		 */
		public String getRevisionState() {
			return revisionState;
		}

		/**
		 * Gets the revision number.
		 *
		 * @return the revision number
		 */
		public String getRevisionNumber() {
			return revisionNumber;
		}

	}

}
