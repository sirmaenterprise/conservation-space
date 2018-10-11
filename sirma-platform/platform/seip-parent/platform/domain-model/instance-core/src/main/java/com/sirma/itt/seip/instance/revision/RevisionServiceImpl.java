package com.sirma.itt.seip.instance.revision;

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
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.SET_PRIMARY_CONTENT;
import static com.sirma.itt.seip.instance.revision.steps.PublishStep.Steps.SYNC_THUMBNAIL;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonException;
import javax.json.JsonObject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.context.Option;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.CMInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.context.InstanceContextService;
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
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.json.JSON;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Default implementation for {@link RevisionService}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class RevisionServiceImpl implements RevisionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RevisionServiceImpl.class);

	/**
	 * Name of definition field where is the configuration of revision context.<br/>
	 * Example of configuration:
	 * <pre>
	 *  <b>&#60;field name="revisionContextConfiguration" type="an..350" displayType="system"&#62;</b>
	 *      &#60;value&#62;{
	 *        "recordContextDefinitionId" : "genericRecordSpace",
	 *        "recordContextName" : "Simple document archive space"
	 *      }&#60;/value&#62;
	 *  &#60;/field&#62;
	 * </pre>
	 */
	static final String REVISION_CONTEXT_CONFIGURATION_FIELD_NAME = "revisionContextConfiguration";

	/**
	 * Json key of revision context configuration which value is definition id of revision context.<br/>
	 * Example of configuration:
	 * <pre>
	 *  <b>&#60;field name="revisionContextConfiguration" type="an..350" displayType="system"&#62;</b>
	 *      &#60;value&#62;{
	 *        "recordContextDefinitionId" : "genericRecordSpace",
	 *        "recordContextName" : "Simple document archive space"
	 *      }&#60;/value&#62;
	 *  &#60;/field&#62;
	 * </pre>
	 */
	static final String REVISION_CONTEXT_DEFINITION_ID = "recordContextDefinitionId";

	/**
	 * Json key of revision context configuration which value is name of revision context.<br/>
	 * Example of configuration:
	 * <pre>
	 *  <b>&#60;field name="revisionContextConfiguration" type="an..350" displayType="system"&#62;</b>
	 *      &#60;value&#62;{
	 *        "recordContextDefinitionId" : "genericRecordSpace",
	 *        "recordContextName" : "Simple document archive space"
	 *      }&#60;/value&#62;
	 *  &#60;/field&#62;
	 * </pre>
	 */
	static final String REVISION_CONTEXT_NAME = "recordContextName";

	/**
	 * Semantic query parameters. Used in query fetched revision context. See {@link #SELECT_CONTEXT_FOR_REVISION}.
	 */
	static final String RECORD_SPACE_QUERY_PARAMETER = "recordSpace";
	static final String TITLE_QUERY_PARAMETER = "title";

	static final String RECORD_RDF_TYPE = EMF.PREFIX + ":" + EMF.RECORD_SPACE.getLocalName();

	private static final String OBSOLETE_STATUS = "OBSOLETE";
	private static final Operation CLONE = new Operation(ActionTypeConstants.CLONE);
	private static final Operation OBSOLETE = new Operation(ActionTypeConstants.OBSOLETE);

	private static final String LATEST_PUBLISHED_RELATION = "emf:lastPublishedRevision";
	private static final String LAST_REVISION_RELATION = "emf:lastRevision";

	private static final OverrideInfo EMPTY_INFO = new OverrideInfo(null, null, null);

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

	private static final String[] PUBLISH_UPLOAD = { INITIAL.getName(), SET_PRIMARY_CONTENT.getName(),
			REPLACE_EXPORTED_TABS.getName(), REMOVE_NON_PUBLISHED_TABS.getName(), COPY_RELATIONS.getName(),
			SYNC_THUMBNAIL.getName(), SAVE_REVISION_VIEW.getName(), COPY_PERMISSIONS.getName() };

	private static final String SELECT_CONTEXT_FOR_REVISION = ResourceLoadUtil.loadResource(RevisionServiceImpl.class,
																							"SELECT_CONTEXT_FOR_REVISION.sparql");

	private static final String DISABLED_VALIDATION_REASON = "Revisions may have been created without properties which now are mandatory, but theirs status have to be changed to \"OBSOLETED\".";

	@Inject
	private InstanceContextService instanceContextService;

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

	@Inject
	private DefinitionService definitionService;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private TemplateService templateService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private InstancePropertyNameResolver fieldConverter;

	// TODO implement via JMS queue
	@Override
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
			Instance publishedInstance = publishInstanceIn(publishRequest, EMPTY_INFO);
			instanceLoadDecorator.decorateInstance(publishedInstance);
			return publishedInstance;
		} finally {
			Options.DISABLE_AUTOMATIC_ID_GENERATION.disable();
		}
	}

	/**
	 * Publish instance the given instance as parent to the given location instance. The new revision will be a child to
	 * the given publish location instance.
	 */
	private Instance publishInstanceIn(PublishInstanceRequest publishRequest, OverrideInfo overrideInfo) {
		String userOperation = Operation.getUserOperationId(publishRequest.getTriggerOperation());
		boolean isRejectOperation = userOperation != null && userOperation.toLowerCase().contains("reject");
		boolean isInRejectedState = stateService.isInState(PrimaryStates.REJECTED,
														   publishRequest.getInstanceToPublish());

		if (isRejectOperation || isInRejectedState) {
			return publishRejected(publishRequest, overrideInfo);
		}
		return publishApproved(publishRequest, overrideInfo);
	}

	private Instance publishRejected(PublishInstanceRequest publishRequest, OverrideInfo overrideInfo) {
		Instance instanceToPublish = publishRequest.getInstanceToPublish();
		Operation operation = publishRequest.getTriggerOperation();

		Instance lastRevision = getLastRevision(instanceToPublish);

		Instance newRevision = createNewRevision(publishRequest, overrideInfo.getRevisionNumber());

		eventService.fire(new PublishRejectedRevisionEvent(instanceToPublish, newRevision));

		linkToSuccessorRevision(lastRevision, newRevision);

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
	 * Creates the new revision from the given instance using the provided plugin. The created revision will have a
	 * revision number based on the current number or if the argument <code>subElementRevisionNumber</code> is non null
	 * will be used directly without incrementing.
	 *
	 * @param publishRequest           the publish request
	 * @param subElementRevisionNumber if passed a non <code>null</code> value will be used to override the revision number of the published
	 *                                 sub elements
	 * @return the created revision
	 */
	private Instance createNewRevision(PublishInstanceRequest publishRequest, String subElementRevisionNumber) {
		Instance instanceToPublish = publishRequest.getInstanceToPublish();
		Instance revision = createRevisionFor(instanceToPublish);
		setPublishMetadata(revision);

		// allow overriding the revision number of the created revision this is used to set the same revision in the sub
		// elements as the one of the master instance
		String nextVersion = subElementRevisionNumber;
		if (nextVersion == null) {
			nextVersion = getNextMajorVersion(getCurrentRevision(instanceToPublish));
		}

		// update versions
		setRevision(instanceToPublish, nextVersion);
		setRevision(revision, nextVersion);

		setRevisionId(instanceToPublish, revision, nextVersion);

		linkCurrentWithNewRevision(instanceToPublish, revision);

		// assigns context of revision if configured.
		getOrCreateContextForRevision(revision).ifPresent(contextOfRevision -> {
			revision.add(InstanceContextService.HAS_PARENT, contextOfRevision.getId(), fieldConverter);
			instanceContextService.bindContext(revision, contextOfRevision);
		});

		eventService.fire(new CreatedRevisionEvent(instanceToPublish, revision));
		setAsLastRevisionTo(instanceToPublish, revision);

		stepRunner.getRunner(getPublishSteps(publishRequest)).run(new PublishContext(publishRequest, revision));

		return revision;
	}

	private Instance createRevisionFor(Instance source) {
		Instance revision = objectService.clone(source, CLONE);
		revision.addIfNotNull(UNIQUE_IDENTIFIER, CMInstance.getContentManagementId(source, null));
		// status is not cloned by default so we copy it here
		PropertiesUtil.copyValue(source, revision, STATUS);

		instanceVersionService.populateVersion(revision);
		return revision;
	}

	private void setPublishMetadata(Instance revision) {
		revision.add(PUBLISHED_BY, securityContext.getAuthenticated().getSystemId(), fieldConverter);
		revision.add(DefaultProperties.PUBLISHED_ON, new Date(), fieldConverter);
	}

	private Serializable getCurrentRevision(Instance instance) {
		SearchArguments<? extends Instance> arg = new SearchArguments<>();
		arg.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		arg.setStringQuery("SELECT ?instance ?revision where { ?instance emf:revisionNumber ?revision}");
		arg.setArguments(new HashMap<>(Collections.singletonMap("instance", instance.getId())));
		return searchService.stream(arg, ResultItemTransformer.asSingleValue("revision"))
				.findFirst()
				.orElse(null);
	}

	private static String getNextMajorVersion(Serializable currentVersion) {
		String version = "1.0";
		if (currentVersion instanceof String) {
			try {
				Double parseDouble = Double.parseDouble(currentVersion.toString());
				BigDecimal bigDecimal = BigDecimal.valueOf(parseDouble);
				bigDecimal = bigDecimal.setScale(bigDecimal.scale(), RoundingMode.DOWN).add(BigDecimal.ONE);
				version = new StringBuilder(8).append(bigDecimal.intValue()).append(".0").toString();
			} catch (NumberFormatException e) {
				LOGGER.warn("Invalid revision format expected floating point but was {}", currentVersion, e);
			}
		}
		return version;
	}

	private void setRevision(Instance instance, Serializable version) {
		instance.add(REVISION_NUMBER, version, fieldConverter);
	}

	private void setRevisionId(Instance instance, Instance revision, String version) {
		// generate new id and set it
		idManager.unregisterId(revision.getId());
		revision.setId(createRevisionId(instance, version));
		idManager.register(revision);
	}

	private void linkCurrentWithNewRevision(Instance current, Instance newRevision) {
		current.append(LinkConstants.HAS_REVISION, newRevision.getId(), fieldConverter);
		newRevision.append(LinkConstants.IS_REVISION_OF, current.getId(), fieldConverter);
	}

	private void setAsLastRevisionTo(Instance instanceToPublish, Instance revision) {
		instanceToPublish.add(LAST_REVISION_RELATION, revision.getId(), fieldConverter);
	}

	private static String[] getPublishSteps(PublishInstanceRequest publishRequest) {
		boolean isUploaded = publishRequest.getInstanceToPublish().isUploaded();
		if (!StringUtils.isEmpty(publishRequest.getContentIdToPublish())) {
			return PUBLISH_UPLOAD;
		} else if (publishRequest.isAsPdf()) {
			if (isUploaded) {
				return PUBLISH_UPLOADED_AS_PDF;
			}
			return PUBLISH_IDOC_AS_PDF;
		} else if (isUploaded) {
			return PUBLISH_UPLOADED;
		}
		return PUBLISH_IDOC;
	}

	private void linkToSuccessorRevision(Instance currentRevision, Instance newRevision) {
		if (currentRevision == null || newRevision == null) {
			return;
		}
		currentRevision.add(LinkConstants.NEXT_REVISION, newRevision.getId(), fieldConverter);
		newRevision.add(LinkConstants.PREVIOUS_REVISION, currentRevision.getId(), fieldConverter);
	}

	/**
	 * Save the given instance and activates the given options and deactivates them at the end of the call. The method
	 * deactivates the automatic links creation by default
	 *
	 * @param ctx              the instance context that carry the needed information to perform the save operation.
	 * @param overrideState    if non <code>null</code> value is passed it will be used to override the state of the saved instance
	 * @param disableStaleData the disable stale data option, should not be <code>null</code>
	 * @param disableAudit     the disable audit option, should not be <code>null</code>
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
	 * Publish any related instances and add them in the context of the given instance
	 *
	 * @param publishRequest the instance to publish
	 * @param revision       the parent revision instance
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
															 stateService.getPrimaryState(revision),
															 (String) getCurrentRevision(instanceToPublish));

				PublishInstanceRequest subInstancePublishRequest = publishRequest.copyForNewInstance(instance);
				Instance newSubRevision = publishInstanceIn(subInstancePublishRequest, overrideInfo);
				addRevisionSubElementToRevision(publishRequest, revision, newSubRevision);
			}
		} finally {
			DISABLE_STALE_DATA_CHECKS.disable();
		}
	}

	@SuppressWarnings("squid:S1172") // unused parameter
	private Collection<Instance> getSubElements(PublishInstanceRequest publishRequest) {
		// TODO: implement children fetching based on the request
		return Collections.emptyList();
	}

	@SuppressWarnings("squid:S1172") // unused parameter
	private void addRevisionSubElementToRevision(PublishInstanceRequest publishRequest, Instance revision,
			Instance childElement) {
		// TODO: implement child linking
	}

	private Instance publishApproved(PublishInstanceRequest publishRequest, OverrideInfo overrideInfo) {
		Instance instanceToPublish = publishRequest.getInstanceToPublish();
		Operation operation = publishRequest.getTriggerOperation();

		Instance newRevision = createNewRevision(publishRequest, overrideInfo.getRevisionNumber());

		Instance currentRevision = getLastRevision(instanceToPublish);
		eventService.fire(new PublishApprovedRevisionEvent(instanceToPublish, newRevision, currentRevision));

		// if there are previous revisions change their status to obsolete
		makePreviousRevisionsObsolete(instanceToPublish);

		changeCurrentPublished(instanceToPublish, newRevision);
		linkToSuccessorRevision(currentRevision, newRevision);

		// save all new objects to database
		InstanceSaveContext newRevisionSaveContext = InstanceSaveContext.create(newRevision, operation);
		newRevisionSaveContext.getVersionContext().setVersionMode(VersionMode.MAJOR);
		save(newRevisionSaveContext, overrideInfo.getRevisionState(), NO_OP, NO_OP);

		InstanceSaveContext publishedInstanceSaveContext = InstanceSaveContext.create(instanceToPublish, operation);
		publishedInstanceSaveContext.getVersionContext().setVersionMode(VersionMode.MAJOR);
		save(publishedInstanceSaveContext, overrideInfo.getPrimaryState(), DISABLE_STALE_DATA_CHECKS, NO_OP);

		publishSubElements(publishRequest, newRevision);

		return newRevision;
	}

	private void changeCurrentPublished(Instance instanceToPublish, Instance revision) {
		instanceToPublish.add(LATEST_PUBLISHED_RELATION, revision.getId(), fieldConverter);
	}

	/**
	 * Finds all previous revisions for the given instance and if their status is different than 'OBSOLETE', makes it so
	 * in order to have only one approved published revision.<br>
	 * The status of the rejected revisions is also changed (business requirement).
	 */
	private void makePreviousRevisionsObsolete(Instance instance) {
		// Validations during save is disabled because they can failed. Revisions may have been created with
		// different definitions and now validations can fail. See CMF-29184.
		getRevisionsWithDifferentState(instance.getId(), OBSOLETE_STATUS).forEach(revision -> save(
				InstanceSaveContext.create(revision, OBSOLETE).disableValidation(DISABLED_VALIDATION_REASON), null,
				NO_OP, NO_OP));
	}

	/**
	 * Retrieves all revisions for specific instance, with state different than the passed.
	 */
	private Collection<Instance> getRevisionsWithDifferentState(Serializable instanceId, String status) {
		Context<String, Object> context = new Context<>(2);
		context.put("objectUri", instanceId);
		context.put("statusArg", status);
		return searchRevisions(context, "customQueries/getRevisionsWithDifferentStatus");
	}

	private Serializable createRevisionId(Instance instance, String version) {
		return idManager.getRevisionId(instance.getId(), version);
	}

	@Override
	public boolean isRevisionSupported(Instance instanceToCheck) {
		return instanceToCheck != null;
	}

	@Override
	public Instance getLastRevision(Instance instance) {
		return getRevision(instance, LAST_REVISION_RELATION);
	}

	/**
	 * Gets the revision relation to the given instance with the give link type.
	 *
	 * @return the revision
	 */
	private Instance getRevision(Instance instance, String linkId) {
		List<LinkReference> links = linkService.getSimpleLinks(instance.toReference(), linkId);
		if (links.isEmpty()) {
			return null;
		}
		LinkReference reference = links.get(0);
		return reference.getTo().toInstance();
	}

	@Override
	public <I extends Instance> Collection<I> getRevisions(InstanceReference reference, boolean asceding) {
		Context<String, Object> context = new Context<>(3);
		context.put("objectUri", reference.getId());
		context.put("orderDirection", asceding ? "ASC" : "DESC");
		context.put("draftRevision", asceding ? "0.0" : "999999.0");
		return searchRevisions(context, "customQueries/getRevisions");
	}

	@SuppressWarnings("unchecked")
	private <I extends Instance> Collection<I> searchRevisions(Context<String, Object> context, String queryFilter) {
		SearchArguments<? extends Instance> args = searchService.getFilter(queryFilter, SearchInstance.class, context);
		if (args == null) {
			LOGGER.warn("Could not find query [{}]", queryFilter);
			return Collections.emptyList();
		}

		searchService.searchAndLoad(Instance.class, args);
		return (Collection<I>) args.getResult();
	}

	@Override
	public boolean isRevision(Instance instanceToCheck) {
		Serializable serializable = instanceToCheck.get(REVISION_TYPE, fieldConverter);
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
			List<LinkReference> links = linkService.getSimpleLinks(revision.toReference(), LinkConstants.IS_REVISION_OF);
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

	@RunAsSystem
	@Override
	public Optional<Instance> getOrCreateContextForRevision(Instance revision) {
		return getOrCreateContextForRevision((DefinitionModel) definitionService.getInstanceDefinition(revision));
	}

	@RunAsSystem
	@Override
	public void createRevisionsContexts() {
		definitionService.getAllDefinitions().forEach(this::getOrCreateContextForRevision);
	}

	private Optional<Instance> getOrCreateContextForRevision(DefinitionModel definitionModel) {
		JsonObject configuration = getRevisionContextConfiguration(definitionModel);
		if (configuration == null) {
			return Optional.empty();
		}

		String revisionContextDefinitionId = configuration.getString(REVISION_CONTEXT_DEFINITION_ID);
		if (!isRecordSpaceDefinition(revisionContextDefinitionId)) {
			return Optional.empty();
		}

		String revisionContextName = configuration.getString(REVISION_CONTEXT_NAME);
		Instance revisionContext = searchForRevisionContext(revisionContextName);
		if (revisionContext != null) {
			return Optional.of(revisionContext);
		}

		// if context is not found we will create it.
		Instance instance = domainInstanceService.createInstance(revisionContextDefinitionId, null);
		instance.add(DefaultProperties.TITLE, revisionContextName);
		updateContextTemplate(instance);

		transactionSupport.invokeInNewTx(() -> domainInstanceService.save(
				InstanceSaveContext.create(instance, new Operation(ActionTypeConstants.CREATE))));
		return Optional.of(instance);
	}

	/**
	 * Get configuration of revision context from <code>definitionModel</code>.
	 *
	 * @param definitionModel - the definition model.
	 * @return revision context configuration or null if something is wrong or configuration is not exist.
	 */
	private JsonObject getRevisionContextConfiguration(DefinitionModel definitionModel) {

		if (!(definitionModel instanceof GenericDefinition)) {
			return null;
		}

		Optional<PropertyDefinition> revisionContextConfiguration = ((GenericDefinition) definitionModel).getConfiguration(
				REVISION_CONTEXT_CONFIGURATION_FIELD_NAME);

		// checks if configuration exist.
		if (!revisionContextConfiguration.isPresent()) {
			return null;
		}

		String value = revisionContextConfiguration.get().getDefaultValue();
		// checks if value missing.
		if (value == null) {
			return null;
		}

		try {
			JsonObject configuration = JSON.readObject(value, Function.identity());
			// checks if mandatory property exist.
			if (!configuration.containsKey(REVISION_CONTEXT_DEFINITION_ID) || !configuration.containsKey(
					REVISION_CONTEXT_NAME)) {
				return null;
			}
			return configuration;
		} catch (JsonException e) {
			LOGGER.warn("Invalid configuration for revision context in definition with id: "
								+ definitionModel.getIdentifier(), e);
		}
		return null;
	}

	/**
	 * Checks if definition with id <code>definitionId</code> has rdfType emf:RecordSpace.
	 *
	 * @param definitionId the definition id.
	 * @return true if definition with id <code>definitionId</code> exist and has rdfType emf:RecordSpace.
	 */
	private boolean isRecordSpaceDefinition(String definitionId) {
		DefinitionModel model = definitionService.find(definitionId);
		if (model == null) {
			return false;
		}
		Optional<PropertyDefinition> rdfType = model.getField(DefaultProperties.SEMANTIC_TYPE);
		if (!rdfType.isPresent()) {
			return false;
		}
		String shortUri = namespaceRegistryService.getShortUri(rdfType.get().getDefaultValue());
		return RECORD_RDF_TYPE.equals(shortUri);
	}

	/**
	 * Checks is there template for <code>revisionContext</code>. If found update <code>revisionContext</code> property
	 * {@link LinkConstants#HAS_TEMPLATE}
	 *
	 * @param revisionContext - the revision context.
	 */
	private void updateContextTemplate(Instance revisionContext) {
		Template template = templateService.getTemplate(
				new TemplateSearchCriteria(revisionContext.getIdentifier(), TemplatePurposes.CREATABLE,
										   revisionContext.getProperties()));
		if (template != null && !TemplateService.DEFAULT_TEMPLATE_ID.equals(template.getId())) {
			revisionContext.add(LinkConstants.HAS_TEMPLATE, template.getCorrespondingInstance(), fieldConverter);
		}
	}

	/**
	 * Searches for instance of emf:RecordSpace with title <code>revisionContextName</code>
	 *
	 * @param revisionContextName - the revision context name.
	 * @return - instance if found otherwise null;
	 */
	private Instance searchForRevisionContext(String revisionContextName) {
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setDialect(SearchDialects.SPARQL);
		searchArguments.setStringQuery(SELECT_CONTEXT_FOR_REVISION);
		searchArguments.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		searchArguments.getArguments().put(RECORD_SPACE_QUERY_PARAMETER, RECORD_RDF_TYPE);
		searchArguments.getArguments().put(TITLE_QUERY_PARAMETER, revisionContextName);

		searchService.searchAndLoad(Instance.class, searchArguments);

		List<Instance> results = searchArguments.getResult();
		return results.isEmpty() ? null : results.get(0);
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
		 * @param primaryState   the primary state
		 * @param revisionState  the revision state
		 * @param revisionNumber the revision number
		 */
		public OverrideInfo(String primaryState, String revisionState, String revisionNumber) {
			this.primaryState = primaryState;
			this.revisionState = revisionState;
			this.revisionNumber = revisionNumber;
		}

		public String getPrimaryState() {
			return primaryState;
		}

		public String getRevisionState() {
			return revisionState;
		}

		public String getRevisionNumber() {
			return revisionNumber;
		}
	}
}
