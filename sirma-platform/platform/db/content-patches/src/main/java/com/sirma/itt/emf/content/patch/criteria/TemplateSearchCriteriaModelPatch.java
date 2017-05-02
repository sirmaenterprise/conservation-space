package com.sirma.itt.emf.content.patch.criteria;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

import javax.sql.DataSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.NamespaceRegistryService;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Migrates search configurations to advanced search criteria model in all {@link TemplateInstance} which are not email
 * templates.
 * 
 * It parses the content as a {@link Document}, select all widgets and then uses {@link SearchCriteriaMigrator} to do
 * the actual transforming.
 * 
 * This patch does not affect modifiedBy & modifiedOn properties.
 * 
 * @author Mihail Radkov
 * @see SearchCriteriaMigrator
 */
public class TemplateSearchCriteriaModelPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String NON_EMAIL_TEMPLATES = "select templateid from sep_template where groupid != 'emailTemplate';";

	private DataSource datasource;
	private TemplateService documentTemplateService;
	private NamespaceRegistryService namespaceRegistryService;

	private SearchCriteriaMigrator searchCriteriaMigrator;

	@Override
	public String getConfirmationMessage() {
		return "Successfully migrated widget configurations' search criteria model in the templates.";
	}

	@Override
	public void setUp() throws SetupException {
		DatabaseConfiguration configuration = CDI.instantiateBean(DatabaseConfiguration.class,
				CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		datasource = configuration.getDataSource();
		documentTemplateService = CDI.instantiateBean(TemplateService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		namespaceRegistryService = CDI.instantiateBean(NamespaceRegistryService.class, CDI.getCachedBeanManager(),
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

		TimeTracker tracker = TimeTracker.createAndStart();

		Collection<String> templateIdentifiers = SearchCriteriaMigrationUtil.queryIdentifiers(datasource,
				NON_EMAIL_TEMPLATES);
		LOGGER.info("Fetched template identifiers for {} instances.", templateIdentifiers.size());

		long migratedCount = 0;

		for (String templateId : templateIdentifiers) {
			try {
				TemplateInstance template = documentTemplateService.getTemplate(templateId);
				// Content should be loaded with another call
				documentTemplateService.loadContent(template);

				boolean shouldSave = false;

				Document view = Jsoup.parse(template.getContent(), StandardCharsets.UTF_8.name(), Parser.xmlParser());
				Elements widgets = view.select(SearchCriteriaMigrationUtil.WIDGET_SELECTOR);

				for (Element widget : widgets) {
					// Inclusive bitwise OR - if at least once migrateWidget returns true it will remain true.
					shouldSave |= migrateWidget(widget);
				}

				if (shouldSave) {
					saveContent(template, view);
					migratedCount++;
				}

			} catch (Exception ex) {
				LOGGER.error("Failed to migrate template content for " + templateId, ex);
			}
		}

		LOGGER.info("Migrated search criteria in {} templates in {} ms.", migratedCount, tracker.stop());
	}

	private boolean migrateWidget(Element widget) throws UnsupportedEncodingException {
		String configAttr = widget.attr(SearchCriteriaMigrationUtil.CONFIG_ATTRIBUTE);

		Optional<String> migratedConfiguration = searchCriteriaMigrator.migrateSearchCriteria(configAttr);
		if (migratedConfiguration.isPresent()) {
			widget.attr(SearchCriteriaMigrationUtil.CONFIG_ATTRIBUTE, migratedConfiguration.get());
			return true;
		}
		return false;
	}

	private void saveContent(TemplateInstance instance, Document view) {
		String migratedView = view.toString();
		instance.add(DefaultProperties.CONTENT, migratedView);
		documentTemplateService.activate(instance);
	}

}
