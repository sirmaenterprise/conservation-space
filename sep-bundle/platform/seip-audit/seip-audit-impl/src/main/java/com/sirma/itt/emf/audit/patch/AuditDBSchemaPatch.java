package com.sirma.itt.emf.audit.patch;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import com.sirma.itt.emf.audit.db.AuditDao;

/**
 * Patches the DB for BAM. Code is taken from EMF. FIXME: Implement more complex patching service
 * that includes other data sources.
 * 
 * @author Mihail Radkov
 */
@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class AuditDBSchemaPatch {

	@Resource(name = AuditDao.DATASOURCE_NAME)
	private DataSource dataSource;

	/**
	 * Patches the DB for BAM.
	 * 
	 * @throws Exception
	 *             if a problem occurs while patching the data base
	 */
	@PostConstruct
	public void patchDatabase() throws Exception {
		DatabaseConnection dbConnection = new JdbcConnection(dataSource.getConnection());
		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(getClass()
				.getClassLoader());
		Liquibase base = new Liquibase("com/sirma/itt/emf/bam/patch/bam-changelog.xml",
				resourceAccessor, dbConnection);
		base.update(null);
	}

}
