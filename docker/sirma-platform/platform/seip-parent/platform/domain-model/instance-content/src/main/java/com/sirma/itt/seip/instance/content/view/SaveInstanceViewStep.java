package com.sirma.itt.seip.instance.content.view;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.instance.content.view.SaveInstanceViewStep.NAME;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Trackable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerAction;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Handles the saving of instance view content. Checks if there is a content send with the target instance as property.
 * If there is one it is converted to {@link Content} and saved as {@link Content#PRIMARY_VIEW} for the target instance.
 * If there is no content the default template for the definition with which the instance is saved is retrieved and used
 * as default content for the instance view.
 * <p>
 * The standard rollback mechanism from the save steps will not work in this case, because the actual files are
 * updated/overridden without transaction. This means that, if the current step is executed, the files will be changed,
 * but if the save process fails in some of the remaining steps, the content could not be reverted, because we don't
 * have the old version of it.<br />
 * To resolve this problem is used scheduled task that will contain the old version of the content and it will be
 * scheduled in case that the transaction for the save process is not successful. The task is persistent so it will be
 * executed even if the server go down. This should guarantee that any changes to the view of the target instance, made
 * during the save process will be rollbacked.
 *
 * @author A. Kunchev
 */
@Named(NAME)
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 10)
public class SaveInstanceViewStep implements InstanceSaveStep, SchedulerAction {

	static final String NAME = "saveInstanceView";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String INSTANCE_ID_KEY = "instanceId";
	private static final String OLD_VIEW_CONTENT_KEY = "oldViewContent";
	private static final String VERSIONABLE_KEY = "versionable";
	private static final String NAME_KEY = "name";
	private static final String CONTENT_ID_KEY = "contentId";
	private static final String OPERATION_KEY = "operation";

	private static final Long DELAY_BETWEEN_RETRIES = 2L;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private TemplateService templateService;

	@Inject
	private InstancePropertyNameResolver nameResolver;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();

		rollbackViewOnProcessFailure(instance);

		Serializable contentAsString = instance.remove(DefaultProperties.TEMP_CONTENT_VIEW);
		String viewId = null;
		if (contentAsString instanceof String) {
			viewId = saveContent(instance, buildInstanceView((String) contentAsString, saveContext));
			// Resets the template link, because the user could change the template. Also the template could be changed
			// to blank in which case the link between the old template and the instance should be removed
			String templateId = getTemplateId(instance);
			if (TemplateService.DEFAULT_TEMPLATE_ID.equals(templateId)) {
				instance.remove(LinkConstants.HAS_TEMPLATE, nameResolver);
			} else {
				instance.addIfNotNull(LinkConstants.HAS_TEMPLATE, templateId, nameResolver);
			}
		} else if (!instanceContentService.getContent(instance, Content.PRIMARY_VIEW).exists() || isTemplateChanged(instance)) {
			// when the instance doesn't have a view assigned, it is retrieved based on the default template. It is
			// executed for instances that are created without being opened in the web
			viewId = saveContent(instance, getContentFromTemplate(saveContext));
		}

		saveContext.setViewId(Optional.ofNullable(viewId));
	}

	@SuppressWarnings("unchecked")
	private boolean isTemplateChanged(Instance instance) {
		if (instance instanceof Trackable) {
			String templateProperty = nameResolver.resolve(instance, LinkConstants.HAS_TEMPLATE);
			// look for any changes in the template property
			return ((Trackable<Serializable>) instance).changes().anyMatch(change -> change.getProperty().equals(templateProperty));
		}
		return false;
	}

	private static Content buildInstanceView(String contentAsString, InstanceSaveContext context) {
		switch (context.getVersionContext().getVersionMode()) {
			case NONE:
			case UPDATE:
				return createInstanceView(contentAsString, false);
			default:
				return createInstanceView(contentAsString, context.getInstance().type().isVersionable());
		}
	}

	private static Content createInstanceView(String contentAsString, boolean versionable) {
		return Content
				.createEmpty()
					.setContent(contentAsString, StandardCharsets.UTF_8)
					.setName(UUID.randomUUID() + "-instanceView.html")
					.setMimeType(MediaType.TEXT_HTML)
					.setPurpose(Content.PRIMARY_VIEW)
					.setVersionable(versionable)
					.setView(true)
					.setIndexable(true);
	}

	private String saveContent(Instance instance, Content content) {
		return instanceContentService.saveContent(instance, content).getContentId();
	}

	private String getTemplateId(Instance instance) {
		final Serializable templateProperty = instance.remove(LinkConstants.HAS_TEMPLATE, nameResolver);
		String templateId = null;
		if (templateProperty instanceof String) {
			templateId = (String) templateProperty;
		} else if (templateProperty instanceof Collection && isNotEmpty((Collection<?>) templateProperty)) {
			templateId = (String) ((Collection<?>) templateProperty).iterator().next();
		}

		return templateId;
	}

	/**
	 * Retrieve the instance's creatable or uploadable template content if possible. Used when the instance doesn't have
	 * a primary content on it's own.
	 *
	 * @return the instance's template content
	 */
	private Content getContentFromTemplate(InstanceSaveContext context) {
		Instance instance = context.getInstance();
		// unset the property because it is intermediate and should not be persisted
		String templateId = getTemplateId(instance);
		String content;
		if (TemplateService.DEFAULT_TEMPLATE_ID.equals(templateId)) {
			instance.remove(LinkConstants.HAS_TEMPLATE, nameResolver);
			content = templateService.getContent(TemplateService.DEFAULT_TEMPLATE_ID);
		} else if (StringUtils.isNotBlank(templateId)) {
			instance.add(LinkConstants.HAS_TEMPLATE, templateId, nameResolver);
			content = templateService.getContent(templateId);
		} else {
			// if no template is selected at this point, the CREATE template should be applied
			Template template = templateService.getTemplate(new TemplateSearchCriteria(instance.getIdentifier(),
					TemplatePurposes.CREATABLE, instance.getProperties()));
			instance.add(LinkConstants.HAS_TEMPLATE, template.getCorrespondingInstance(), nameResolver);
			content = template.getContent();
		}

		return buildInstanceView(content, context);
	}

	private void rollbackViewOnProcessFailure(Instance instance) {
		try {
			ContentInfo info = instanceContentService.getContent(instance, Content.PRIMARY_VIEW);
			SchedulerContext context = info.exists() ? buildForUpdate(instance, info) : buildForDelete(instance);
			SchedulerConfiguration configuration = buildConfig(instance.getId());
			transactionSupport
					.invokeOnFailedTransactionInTx(() -> schedulerService.schedule(NAME, configuration, context));
		} catch (IOException e) {
			throw new EmfRuntimeException("Failed to prepare the old view content for rollback.", e);
		}
	}

	private static SchedulerConfiguration buildConfig(Serializable id) {
		return new DefaultSchedulerConfiguration(String.join("-", NAME, id.toString()))
				.setType(SchedulerEntryType.TIMED)
					.setScheduleTime(new Date())
					.setRemoveOnSuccess(true)
					.setPersistent(true)
					.setTransactionMode(TransactionMode.REQUIRED)
					.setMaxRetryCount(2)
					.setRetryDelay(DELAY_BETWEEN_RETRIES)
					.setIncrementalDelay(true);
	}

	private static SchedulerContext buildForDelete(Instance instance) {
		SchedulerContext context = new SchedulerContext(2);
		context.put(OPERATION_KEY, RollbackOperation.DELETE);
		context.put(INSTANCE_ID_KEY, instance.getId());
		return context;
	}

	private static SchedulerContext buildForUpdate(Instance instance, ContentInfo info) throws IOException {
		SchedulerContext context = new SchedulerContext(6);
		context.put(OPERATION_KEY, RollbackOperation.UPDATE);
		context.put(CONTENT_ID_KEY, info.getContentId());
		context.put(NAME_KEY, info.getName());
		context.put(VERSIONABLE_KEY, instance.type().isVersionable());
		context.put(OLD_VIEW_CONTENT_KEY, info.asString(StandardCharsets.UTF_8));
		context.put(INSTANCE_ID_KEY, instance.getId());
		return context;
	}

	@Override
	public void beforeExecute(SchedulerContext context) throws Exception {
		LOGGER.info("Starting rollback of view content for instance - {}", context.get(INSTANCE_ID_KEY));
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		boolean rollbacked = false;
		RollbackOperation operation = context.getIfSameType(OPERATION_KEY, RollbackOperation.class);
		if (RollbackOperation.DELETE.equals(operation)) {
			rollbacked = delete(context);
		} else {
			rollbacked = update(context);
		}

		if (rollbacked) {
			LOGGER.info("Content rollback/{} for instance - {} completed successfully.", operation,
					context.get(INSTANCE_ID_KEY));
		}
	}

	/**
	 * Just created instance. If the save fails, the saved content will just hang "there", so it should removed.
	 */
	private boolean delete(SchedulerContext context) {
		Serializable instanceId = context.get(INSTANCE_ID_KEY);
		if (!instanceContentService.getContent(instanceId, Content.PRIMARY_VIEW).exists()) {
			return false;
		}

		return instanceContentService.deleteContent(instanceId, Content.PRIMARY_VIEW);
	}

	/**
	 * Replaces with the view before the save if the save process fails.
	 */
	private boolean update(SchedulerContext context) {
		String contentId = context.getIfSameType(CONTENT_ID_KEY, String.class);
		if (!instanceContentService.getContent(contentId, "any").exists()) {
			return false;
		}

		String content = context.getIfSameType(OLD_VIEW_CONTENT_KEY, String.class);
		Boolean isVersionable = context.getIfSameType(VERSIONABLE_KEY, Boolean.class);
		String name = context.getIfSameType(NAME_KEY, String.class);
		Serializable instanceId = context.get(INSTANCE_ID_KEY);

		Content contentToUpdate = createInstanceView(content, isVersionable)
				.setContentId(contentId)
					.setName(name)
					.setDetectedMimeTypeFromContent(false);

		// when updating, alfresco store requires instance instead of just id
		return instanceContentService.updateContent(contentId, new EmfInstance(instanceId), contentToUpdate).exists();
	}

	@Override
	public void afterExecute(SchedulerContext context) throws Exception {
		LOGGER.info("Finished rollback of view content for instance - {}", context.get(INSTANCE_ID_KEY));
	}

	@Override
	public String getName() {
		return NAME;
	}

	enum RollbackOperation {
		DELETE, UPDATE;
	}
}
