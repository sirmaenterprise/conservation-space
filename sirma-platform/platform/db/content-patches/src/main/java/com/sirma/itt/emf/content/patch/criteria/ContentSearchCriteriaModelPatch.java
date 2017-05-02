package com.sirma.itt.emf.content.patch.criteria;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import javax.sql.DataSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.collections.FixedBatchSpliterator;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.NamespaceRegistryService;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Migrates search configurations to advanced search criteria model in all {@link Content} which has a
 * {@link Content#PRIMARY_VIEW} and its size is above zero.
 *
 * It parses the content as a {@link Document}, select all widgets and then uses {@link SearchCriteriaMigrator} to do
 * the actual transforming.
 *
 * This patch works with {@link InstanceContentService} and does not affect modified by and modified on properties.
 *
 * @author Mihail Radkov
 * @see SearchCriteriaMigrator
 */
public class ContentSearchCriteriaModelPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String PRIMARY_VIEW_QUERY = "select id from seip_content where purpose='"
			+ Content.PRIMARY_VIEW + "' and content_size > 0;";

	private DataSource datasource;
	private InstanceContentService instanceContentService;
	private NamespaceRegistryService namespaceRegistryService;
	private SecurityContextManager securityContextManager;
	private TransactionSupport transactionSupport;

	private SearchCriteriaMigrator searchCriteriaMigrator;

	private int concurentMigrations;

	@Override
	public String getConfirmationMessage() {
		return "Search criteria migration was sucessful!";
	}

	@Override
	public void setUp() throws SetupException {
		DatabaseConfiguration configuration = CDI.instantiateBean(DatabaseConfiguration.class,
				CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		datasource = configuration.getDataSource();
		instanceContentService = CDI.instantiateBean(InstanceContentService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		namespaceRegistryService = CDI.instantiateBean(NamespaceRegistryService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityContextManager = CDI.instantiateBean(SecurityContextManager.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// No need for this
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		searchCriteriaMigrator = new SearchCriteriaMigrator(namespaceRegistryService);
		concurentMigrations = Runtime.getRuntime().availableProcessors();

		TimeTracker tracker = TimeTracker.createAndStart();

		Collection<? extends Serializable> identifiers = SearchCriteriaMigrationUtil.queryIdentifiers(datasource,
				PRIMARY_VIEW_QUERY);
		Collection<ContentInfo> primaryViews = instanceContentService.getContent(identifiers, Content.PRIMARY_VIEW);

		LOGGER.info("Fetched content info for {} primary views in {} ms. Starting patch with {} concurent migrations",
				primaryViews.size(), tracker.stop(), concurentMigrations);
		tracker.begin();

		Function<ContentInfo, Boolean> migrateWrap = securityContextManager.wrap().function(this::migrate);
		// the minimum batch size should be 1
		int batchSize = Math.max(primaryViews.size() / concurentMigrations, 1);
		long migratedCount = FixedBatchSpliterator
				.withBatchSize(primaryViews.stream(), batchSize)
					.filter(content -> content.exists())
					.map(migrateWrap)
					.filter(Boolean.TRUE::equals)
					.count();

		LOGGER.info("Migrated search criteria in {} instances in {} ms.", migratedCount, tracker.stop());
	}

	boolean migrate(ContentInfo contentInfo) {
		try {
			Document primaryView = Jsoup.parse(contentInfo.getInputStream(), StandardCharsets.UTF_8.name(), "",
					Parser.xmlParser());

			boolean shouldSave = false;

			Elements widgets = primaryView.select(SearchCriteriaMigrationUtil.WIDGET_SELECTOR);
			for (Element widget : widgets) {
				String configAttr = widget.attr(SearchCriteriaMigrationUtil.CONFIG_ATTRIBUTE);

				Optional<String> migratedConfiguration = searchCriteriaMigrator.migrateSearchCriteria(configAttr);
				if (migratedConfiguration.isPresent()) {
					widget.attr(SearchCriteriaMigrationUtil.CONFIG_ATTRIBUTE, migratedConfiguration.get());
					shouldSave = true;
				}
			}

			if (shouldSave) {
				// store the data for a single content in it's own transaction
				transactionSupport.invokeInNewTx(() -> saveContent(contentInfo, primaryView));
				return true;
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to migrate the content for " + contentInfo.getInstanceId(), ex);
		}
		return false;
	}

	private void saveContent(ContentInfo contentInfo, Document primaryView) {

		Content content = Content.createFrom(contentInfo);
		content.setContent(primaryView.toString(), StandardCharsets.UTF_8);

		// The migrated content won't be saved correctly if a dummy instance isn't used.
		Instance dummyInstance = new EmfInstance();
		dummyInstance.setId(contentInfo.getInstanceId());

		instanceContentService.updateContent(contentInfo.getContentId(), dummyInstance, content);
	}
}
