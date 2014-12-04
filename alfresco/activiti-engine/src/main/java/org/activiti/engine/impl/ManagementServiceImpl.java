/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl;

import java.sql.Connection;
import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.cmd.ExecuteJobsCmd;
import org.activiti.engine.impl.cmd.GetJobExceptionStacktraceCmd;
import org.activiti.engine.impl.cmd.GetPropertiesCmd;
import org.activiti.engine.impl.cmd.GetTableCountCmd;
import org.activiti.engine.impl.cmd.GetTableMetaDataCmd;
import org.activiti.engine.impl.cmd.SetJobRetriesCmd;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.engine.runtime.JobQuery;


// TODO: Auto-generated Javadoc
/**
 * The Class ManagementServiceImpl.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 */
public class ManagementServiceImpl extends ServiceImpl implements ManagementService {

  /* (non-Javadoc)
   * @see org.activiti.engine.ManagementService#getTableCount()
   */
  public Map<String, Long> getTableCount() {
    return commandExecutor.execute(new GetTableCountCmd());
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ManagementService#getTableMetaData(java.lang.String)
   */
  public TableMetaData getTableMetaData(String tableName) {
    return commandExecutor.execute(new GetTableMetaDataCmd(tableName));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ManagementService#executeJob(java.lang.String)
   */
  public void executeJob(String jobId) {
    commandExecutor.execute(new ExecuteJobsCmd(jobId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ManagementService#setJobRetries(java.lang.String, int)
   */
  public void setJobRetries(String jobId, int retries) {
    commandExecutor.execute(new SetJobRetriesCmd(jobId, retries));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ManagementService#createTablePageQuery()
   */
  public TablePageQuery createTablePageQuery() {
    return new TablePageQueryImpl(commandExecutor);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ManagementService#createJobQuery()
   */
  public JobQuery createJobQuery() {
    return new JobQueryImpl(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ManagementService#getJobExceptionStacktrace(java.lang.String)
   */
  public String getJobExceptionStacktrace(String jobId) {
    return commandExecutor.execute(new GetJobExceptionStacktraceCmd(jobId));
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ManagementService#getProperties()
   */
  public Map<String, String> getProperties() {
    return commandExecutor.execute(new GetPropertiesCmd());
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ManagementService#databaseSchemaUpgrade(java.sql.Connection, java.lang.String, java.lang.String)
   */
  public String databaseSchemaUpgrade(final Connection connection, final String catalog, final String schema) {
    return commandExecutor.execute(new Command<String>(){
      public String execute(CommandContext commandContext) {
        DbSqlSessionFactory dbSqlSessionFactory = (DbSqlSessionFactory) commandContext.getSessionFactories().get(DbSqlSession.class);
        DbSqlSession dbSqlSession = new DbSqlSession(dbSqlSessionFactory, connection, catalog, schema);
        commandContext.getSessions().put(DbSqlSession.class, dbSqlSession);
        return dbSqlSession.dbSchemaUpdate();
      }
    });
  }
}
