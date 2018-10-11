package com.sirma.sep.definition.patches;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirma.sep.definition.db.DefinitionContent;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Migrates definition content from DMS to relational database.
 *
 * @author Adrian Mitev
 */
public class DefinitionContentMigrationPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private DefintionAdapterService definitionAdapterService;

	private DbDao dbDao;

	private TransactionSupport transactionSupport;

	@Override
	public void setUp() throws SetupException {
		definitionAdapterService = CDI.instantiateBean(DefintionAdapterService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		dbDao = CDI.instantiateBean(DbDao.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		TimeTracker tracker = TimeTracker.createAndStart();

		LOGGER.info("Begin migration of definition content.");

		for (FileDescriptor fileDescriptor : definitionAdapterService.getDefinitions(GenericDefinition.class)) {
			transactionSupport.invokeInNewTx(() -> this.migrateDefinitionContent(fileDescriptor));
		}

		LOGGER.info("Finished migration of definition content in {} ms.", tracker.stop());
	}

	private void migrateDefinitionContent(FileDescriptor fileDescriptor) {
		try {
			DefinitionContent definitionContent = new DefinitionContent();
			String content = IOUtils.toString(fileDescriptor.getInputStream());
			definitionContent.setContent(content);
			definitionContent.setFileName(fileDescriptor.getFileName());
			definitionContent.setId(getDefinitionId(content, fileDescriptor.getFileName()));

			dbDao.saveOrUpdate(definitionContent);
			LOGGER.info("Migrated content for definition '{}'", fileDescriptor.getFileName());
		} catch (IOException e) {
			throw new IllegalStateException("Cannot fetch content for definition " + fileDescriptor.getFileName());
		}
	}

	private static String getDefinitionId(String content, String fileName) {
		Document document = Jsoup.parse(content);
		Elements definitionTags = document.getElementsByTag("definition");

		if (!definitionTags.isEmpty()) {
			return definitionTags.get(0).attr("id");
		} else {
			throw new IllegalStateException("<definition> tag is not found in a definition file '" + fileName + "'");
		}
	}

	@Override
	public String getConfirmationMessage() {
		return "Migrates template content to relational DB";
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// not needed
	}

	@Override
	public ValidationErrors validate(liquibase.database.Database arg0) {
		return null;
	}

}
