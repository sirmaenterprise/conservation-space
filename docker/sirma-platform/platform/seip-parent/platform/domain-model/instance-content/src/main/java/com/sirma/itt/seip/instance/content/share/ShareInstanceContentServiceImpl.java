package com.sirma.itt.seip.instance.content.share;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.shared.ShareCodeUtils;
import com.sirma.itt.seip.shared.exception.ShareCodeValidationException;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.sep.content.ContentConfigurations;
import com.sirma.sep.export.ExportURIBuilder;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.instance.content.share.BaseShareInstanceContentAction.buildEventConfiguration;
import static com.sirma.itt.seip.instance.content.share.BaseShareInstanceContentAction.buildImmediateConfiguration;
import static com.sirma.itt.seip.instance.content.share.BaseShareInstanceContentAction.createContext;

/**
 * Creates publicly accessible URLs that can be shared to users who are not registered in SES.
 *
 * @author A. Kunchev
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
@ApplicationScoped
public class ShareInstanceContentServiceImpl implements ShareInstanceContentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String SERVICE_ENDPOINT = "/emf/share/content/";

	@Inject
	private DomainInstanceService domainInstanceService;
	@Inject
	private DatabaseIdManager idManager;
	@Inject
	private SystemConfiguration systemConfiguration;
	@Inject
	private ContentConfigurations contentConfigurations;
	@Inject
	private ExportURIBuilder uriBuilder;
	@Inject
	private SchedulerService schedulerService;
	@Inject
	private SecurityContext securityContext;

	@Override
	public String getSharedContentURI(String id, String contentFormat) {
		final String newContentId = idManager.generateId().toString();
		scheduleTask(id, contentFormat, newContentId, buildImmediateConfiguration());
		return generateSharedLink(newContentId).getFirst();
	}

	@Override
	public String createContentShareTask(String id, String contentFormat) {
		final String newContentId = idManager.generateId().toString();
		StringPair sharedLink = generateSharedLink(newContentId);
		scheduleTask(id, contentFormat, newContentId,
					 buildEventConfiguration(schedulerService, sharedLink.getSecond()));
		return sharedLink.getFirst();
	}

	private StringPair generateSharedLink(String contentId) {
		String sharedCode;
		try {
			String secretKey = contentConfigurations.getShareCodeSecretKey().getOrFail();
			String user = securityContext.getEffectiveAuthentication().getIdentityId();
			sharedCode = ShareCodeUtils.construct(contentId, user, secretKey);
		} catch (ShareCodeValidationException e) {
			throw new EmfRuntimeException("Failed to generate shared content link for content - " + contentId, e);
		}
		String sharedLink =
				systemConfiguration.getSystemAccessUrl().get().toString() + SERVICE_ENDPOINT + contentId + "?shareCode="
						+ sharedCode;

		return new StringPair(sharedLink, sharedCode);
	}

	private void scheduleTask(String id, String contentFormat, String newContentId,
			SchedulerConfiguration configuration) {
		Instance instance = domainInstanceService.loadInstance(id);
		if (instance.isUploaded()) {
			scheduleAction(ShareContentUploadedInstancesAction.ACTION_NAME, instance, newContentId, contentFormat,
						   configuration);
			LOGGER.trace("Scheduled immediate task for uploaded instance - {}", id);
		} else {
			scheduleAction(ShareContentCreatedInstanceAction.ACTION_NAME, instance, newContentId, contentFormat,
						   configuration);
			LOGGER.trace("Scheduled immediate task for created instance - {}", id);
		}
	}

	private void scheduleAction(String actionName, Instance instance, String contentId, String format,
			SchedulerConfiguration configuration) {
		String token = uriBuilder.getCurrentJwtToken();
		String id = (String) instance.getId();
		SchedulerContext context = createContext(id, instance.getString(TITLE, ""), token, contentId, format);
		schedulerService.schedule(actionName, configuration, context);
	}

	@Override
	public void triggerContentShareTask(String taskIdentifier) {
		schedulerService.onEvent(new ShareInstanceContentEvent(taskIdentifier));
	}

	@Override
	public Map<String, String> getSharedContentURIs(Collection<String> id, String contentFormat) {
		throw new NotImplementedException();
	}

	@Override
	public Map<String, String> createContentShareTasks(Collection<String> ids, String contentFormat) {
		Function<String, String> createTask = id -> createContentShareTask(id, contentFormat);
		return ids.stream().collect(Collectors.toMap(Function.identity(), createTask, CollectionUtils.throwingMerger(), LinkedHashMap::new));
	}

	@Override
	public void triggerContentShareTasks(Collection<String> tasksIdentifiers) {
		tasksIdentifiers.forEach(this::triggerContentShareTask);
	}
}
