package com.sirma.sep.instance.content.patch.revisions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.sql.DataSource;

import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.InstanceContentService;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Patch that will schedule tasks for deleting all of the primary view content for latest revisions. This is done,
 * because all of the functionality related to latest revision is removed and all of the related data should be removed
 * as well.
 *
 * @author A. Kunchev
 */
public class RemoveContentsForLatestRevisions implements CustomTaskChange {

	private static final String GET_LATEST_REVISIONS_CONTENT_IDS_QUERY = "SELECT id FROM seip_content"
			+ " WHERE instance_id like '%rlatest' and purpose = '" + Content.PRIMARY_VIEW + "'";

	private TransactionSupport transactionSupport;

	private DataSource datasource;

	private InstanceContentService instanceContentService;

	private int numberOfContentToDelete;

	@Override
	public void setUp() throws SetupException {
		BeanManager manager = CDI.getCachedBeanManager();
		AnnotationLiteral<Default> annotationLiteral = CDI.getDefaultLiteral();
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, manager, annotationLiteral);
		datasource = CDI.instantiateBean(DatabaseConfiguration.class, manager, annotationLiteral).getDataSource();
		instanceContentService = CDI.instantiateBean(InstanceContentService.class, manager, annotationLiteral);
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		transactionSupport.invokeInTx(() -> {
			List<String> ids = getContentIdsForAllLatestRevisions();
			numberOfContentToDelete = ids.size();
			if (ids.isEmpty()) {
				return null;
			}

			ids.forEach(id -> instanceContentService.deleteContent(id, Content.PRIMARY_VIEW, 10, TimeUnit.MINUTES));
			return null;
		});
	}

	private List<String> getContentIdsForAllLatestRevisions() throws CustomChangeException {
		try (Statement statement = datasource.getConnection().createStatement();
				ResultSet queryResults = statement.executeQuery(GET_LATEST_REVISIONS_CONTENT_IDS_QUERY)) {
			List<String> contentIds = new ArrayList<>();
			while (queryResults.next()) {
				contentIds.add(queryResults.getString(1));
			}

			return contentIds;
		} catch (SQLException e) {
			throw new CustomChangeException("Failed to retrieve the contents for all of the latest revisions.", e);
		}
	}

	@Override
	public String getConfirmationMessage() {
		return "The tasks for removal of the contents for latest revisions were scheduled successfully."
				+ " The number of the contents that will be deleted is: " + numberOfContentToDelete;
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