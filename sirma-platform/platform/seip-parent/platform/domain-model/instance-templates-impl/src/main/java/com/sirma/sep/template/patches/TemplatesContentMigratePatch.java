package com.sirma.sep.template.patches;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.sirma.itt.seip.template.jaxb.TemplateDefinition;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileWithNameDescriptor;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.jaxb.ComplexFieldDefinition;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.template.db.TemplateContentEntity;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirma.sep.xml.JAXBHelper;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Migrates all templates' contents from the DMS to the appropriate content table.
 *
 * @author Vilizar Tsonev
 */
public class TemplatesContentMigratePatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private DbDao dbDao;

	private TransactionSupport transactionSupport;

	private TempFileProvider tempFileProvider;

	private RESTClient restClient;

	@Override
	public void setUp() throws SetupException {
		dbDao = CDI.instantiateBean(DbDao.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		restClient = CDI.instantiateBean(RESTClient.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		tempFileProvider = CDI.instantiateBean(TempFileProvider.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		TimeTracker tracker = TimeTracker.createAndStart();

		transactionSupport.invokeInNewTx(this::migrateContents);

		LOGGER.info("Migration of templates' contents from DMS to relational DB took {} ms.", tracker.stop());
	}

	private void migrateContents() {
		// query template identifiers and template location in DMS as dynamic query as the next patch will remove the DMS column
		List<Object[]> templateIds = dbDao.fetchWithNative("select templateid, dmsid from sep_template", CollectionUtils.emptyList());
		LOGGER.info("Initializing content migration for {} templates", templateIds.size());

		templateIds.forEach(templateRow -> {
			String templateId = templateRow[0].toString();
			String dmsId = Objects.toString(templateRow[1], null);

			if (StringUtils.isBlank(dmsId)) {
				LOGGER.debug("No remote descriptor for template {}, skipping content migration.", templateId);
				return;
			}

			Optional<FileDescriptor> optional = getContentDescriptor(dmsId);

			if (!optional.isPresent()) {
				throw new EmfApplicationException("Failed to retrieve content descriptor with file name for template ["
						+ templateId + "] having DMS ID [" + dmsId + "]");
			}
			FileDescriptor contentDescriptor = optional.get();

			String dmsContent = extractContent(contentDescriptor);

			if (StringUtils.isBlank(dmsContent)) {
				LOGGER.debug("No template content to migrate for {}", templateId);
				return;
			}

			TemplateContentEntity contentEntity = new TemplateContentEntity();
			contentEntity.setId(templateId);
			contentEntity.setContent(dmsContent);
			contentEntity.setFileName(contentDescriptor.getId());

			LOGGER.debug("Saving content record of template {} with filename {}", templateId, contentDescriptor.getId());
			dbDao.saveOrUpdate(contentEntity);
		});
	}

	private String extractContent(FileDescriptor descriptor) {
		Optional<TemplateDefinition> templateDefinition = toDefinition(descriptor);
		if (templateDefinition.isPresent()) {
			return templateDefinition.get().getFields()
					.getField()
					.stream()
					.filter(field -> CONTENT.equals(field.getName()))
					.findFirst()
					.map(ComplexFieldDefinition::getValue)
					.orElseThrow(() -> new EmfApplicationException(
							"Failed to load content for dms id: " + descriptor.getId()));
		}
		return null;
	}

	private Optional<FileDescriptor> getContentDescriptor(String dmsId) {
		String details;
		try {
			details = restClient.request(ServiceURIRegistry.NODE_DETAILS + dmsId.replaceFirst("://", "/"),
					restClient.createMethod(new GetMethod(), (String) null, true));
		} catch (DMSClientException | UnsupportedEncodingException e) {
			LOGGER.error("Error while requesting template content descriptor from DMS", e);
			return Optional.empty();
		}
		if (StringUtils.isBlank(details)) {
			return Optional.empty();
		}
		try (JsonReader detailsReader = Json.createReader(new StringReader(details))) {
			JsonObject nodeDetails = detailsReader.readObject().getJsonObject("item");
			if (nodeDetails.containsKey("fileName")) {
				String fileName = nodeDetails.getString("fileName");
				return Optional.of(
						new AlfrescoFileWithNameDescriptor(dmsId, fileName, null, fileName, restClient));
			}
			return Optional.empty();
		}
	}

	private Optional<com.sirma.itt.seip.template.jaxb.TemplateDefinition> toDefinition(FileDescriptor descriptor) {
		File file = toFile(descriptor);
		try {
			return Optional.of(JAXBHelper.load(file, com.sirma.itt.seip.template.jaxb.TemplateDefinition.class));
		} catch (Exception e) {
			LOGGER.warn("Cannot convert template file with id={} because of {}, skipping content migration.", descriptor.getId(), e.getMessage());
			return Optional.empty();
		}
	}

	private File toFile(FileDescriptor descriptor) {
		File tempFile = tempFileProvider.createTempFile("templateFile", null);
		try {
			descriptor.writeTo(tempFile);
		} catch (IOException e) {
			LOGGER.warn("Could not download template definition: ", descriptor.getId(), e);
			tempFileProvider.deleteFile(tempFile);
			tempFile = null;
		}
		return tempFile;
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
	public ValidationErrors validate(Database database) {
		return null;
	}
}
