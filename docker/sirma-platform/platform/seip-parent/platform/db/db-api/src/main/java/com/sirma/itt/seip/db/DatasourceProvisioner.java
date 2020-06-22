package com.sirma.itt.seip.db;

import com.sirma.itt.seip.exception.RollbackedException;

/**
 * Datasource provisioner responsible for creating datasources in the application server.
 *
 * @author nvelkov
 */
public interface DatasourceProvisioner {

	/**
	 * Creates the xa datasource.
	 *
	 * @param model
	 *            the datasource model
	 * @throws RollbackedException
	 *             if the outcome of the operation is 'failed'
	 */
	void createXaDatasource(DatasourceModel model) throws RollbackedException;

	/**
	 * Gets the XA data source database property
	 *
	 * @param datasourceName
	 *            the datasource name
	 * @return the xa datasource database
	 */
	String getXaDataSourceDatabase(String datasourceName);

	/**
	 * Gets the XA data source port property
	 *
	 * @param datasourceName
	 *            the datasource name
	 * @return the xa datasource port
	 */
	String getXaDataSourcePort(String datasourceName);

	/**
	 * Gets the XA data source server name property
	 *
	 * @param datasourceName
	 *            the datasource name
	 * @return the xa datasource server name
	 */
	String getXaDataSourceServerName(String datasourceName);

	/**
	 * Removes the datasource for the given tenant.
	 *
	 * @param datasource
	 *            the datasource
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	void removeDatasource(String datasource) throws RollbackedException;
}
