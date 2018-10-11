package com.sirma.itt.seip.instance.headers;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionContextProperties;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.sep.instance.batch.BatchRequestBuilder;
import com.sirma.sep.instance.batch.BatchService;
import com.sirma.sep.instance.batch.StreamBatchRequest;

/**
 * Implementation of {@link InstanceHeaderService} that stores the registered headers in a database.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/11/2017
 */
@ApplicationScoped
class InstanceHeaderServiceImpl implements InstanceHeaderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");
	private static final Pattern HTML_COMMENT = Pattern.compile("<!--.*?-->");
	private static final String SELECT_INSTANCES_TO_REINDEX = ResourceLoadUtil.loadResource(InstanceHeaderServiceImpl.class, "selectInstanceForDefinition.sparql");
	@Inject
	private InstanceHeaderDao headerDao;
	@Inject
	private SchedulerService schedulerService;

	@Inject
	private ExpressionsManager expressionsManager;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private BatchService batchService;
	@Inject
	private SearchService searchService;

	@Override
	public void trackHeader(String definitionId, String headerValue) {
		Objects.requireNonNull(definitionId, "DefinitionId is required parameter");
		Optional<HeaderEntity> entity = headerDao.findByDefinitionId(definitionId);
		HeaderEntity headerEntity;
		String currentHeaderValue = null;
		if (entity.isPresent()) {
			headerEntity = entity.get();
			currentHeaderValue = headerEntity.getHeader();
		} else {
			headerEntity = new HeaderEntity();
			headerEntity.setDefinitionId(definitionId);
		}

		headerEntity.setHeader(headerValue);
		headerDao.persist(headerEntity);

		triggerReindexingIfChanged(definitionId, headerValue, currentHeaderValue);
	}

	private void triggerReindexingIfChanged(String definitionId, String newHeaderValue, String currentHeaderValue) {
		String normalizedNewValue = normalize(newHeaderValue);
		String normalizedCurrentValue = normalize(currentHeaderValue);
		if (!nullSafeEquals(normalizedNewValue, normalizedCurrentValue)) {
			LOGGER.info("Detected label change for definition [{}] with new header\n{}", definitionId, newHeaderValue);
			scheduleReindexing(definitionId);
		}
	}

	/**
	 * Remove any non relevant whitespace characters (removes formatting) and http comments in the persisted header so
	 * that the checks for reindexing not to be triggered if such changes are done to the headers
	 *
	 * @param value the header format to update
	 * @return the updated header value format
	 */
	private String normalize(String value) {
		String trimmed = StringUtils.trimToNull(value);
		if (trimmed == null) {
			return null;
		}
		String withoutWhitespaces = WHITESPACE.matcher(trimmed).replaceAll("");
		return HTML_COMMENT.matcher(withoutWhitespaces).replaceAll("");
	}

	private void scheduleReindexing(String definitionId) {
		SchedulerConfiguration configuration = schedulerService.buildEmptyConfiguration(
				SchedulerEntryType.TIMED)
				.setScheduleTime(new Date())
				.setRemoveOnSuccess(true)
				.setMaxRetryCount(10)
				.setRetryDelay(TimeUnit.MINUTES.toSeconds(5))
				.setMaxActivePerGroup(InstanceHeaderReindexingAction.NAME, 1); // one reindexing at a time
		SchedulerContext context = InstanceHeaderReindexingAction.buildContext(definitionId);
		schedulerService.schedule(InstanceHeaderReindexingAction.NAME, configuration, context);
	}

	@Override
	public Optional<String> getHeader(String definitionId) {
		return headerDao.findByDefinitionId(definitionId).map(HeaderEntity::getHeader);
	}

	@Override
	public Optional<String> evaluateHeader(Instance instance) {
		Optional<String> header = getHeader(instance.getIdentifier());
		if (!header.isPresent() || StringUtils.isBlank(header.get())) {
			LOGGER.debug("No header defined for instance: {}", instance.getId());
			return Optional.empty();
		}
		if (!expressionsManager.isExpression(header.get())) {
			LOGGER.warn("Instance {} header to an expression: {}", instance.getId(), header.get());
			return Optional.empty();
		}
		ExpressionContext context = createContext(instance);

		String evaluated = expressionsManager.evaluateRule(header.get(), String.class, context, instance);
		evaluated = StringUtils.trimToEmpty(evaluated);
		// the field has a second expression
		if (StringUtils.isNotBlank(evaluated) && expressionsManager.isExpression(evaluated)) {
			evaluated = expressionsManager.evaluateRule(evaluated, String.class, context, instance);
		}

		if (StringUtils.isBlank(evaluated)) {
			LOGGER.warn("Instance {} header evaluated to empty string: {}", instance.getId(), header.get());
			return Optional.empty();
		}
		return Optional.of(extractHeaderText(evaluated));
	}

	private ExpressionContext createContext(Instance instance) {
		ExpressionContext context = new ExpressionContext();
		context.put(ExpressionContextProperties.CURRENT_INSTANCE, instance);
		context.put(ExpressionContextProperties.LANGUAGE, systemConfiguration.getSystemLanguage());

		DefinitionModel model = definitionService.getInstanceDefinition(instance);
		PropertyDefinition propertyDefinition = model.getField(DefaultProperties.HEADER_LABEL)
				.orElseGet(() -> model.getField(DefaultProperties.HEADER_BREADCRUMB).orElse(null));
		addNonNullValue(context, ExpressionContextProperties.TARGET_FIELD, (Serializable) propertyDefinition);
		return context;
	}

	private static String extractHeaderText(String header) {
		return Jsoup.parse(header, "").body().text();
	}

	@Override
	public void reindexDefinition(String definitionId) {
		LOGGER.info("Triggered Instance reindexing about instances with definition: {}", definitionId);
		Map<String, Serializable> parameters = Collections.singletonMap("definitionId",
				Objects.requireNonNull(definitionId, "Definition identifier is required"));

		StreamBatchRequest reindexInstanceLabelsRequest = BatchRequestBuilder.fromSearch("reindexInstanceLabels",
				SELECT_INSTANCES_TO_REINDEX, parameters, "instance", searchService);
		reindexInstanceLabelsRequest.setChunkSize(1000);
		reindexInstanceLabelsRequest.setPartitionsCount(2);

		batchService.execute(reindexInstanceLabelsRequest);
	}
}
