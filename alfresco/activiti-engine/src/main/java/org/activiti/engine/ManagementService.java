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
package org.activiti.engine;

import java.sql.Connection;
import java.util.Map;

import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.engine.runtime.JobQuery;



// TODO: Auto-generated Javadoc
/**
 * Service for admin and maintenance operations on the process engine.
 * 
 * These operations will typically not be used in a workflow driven application,
 * but are used in for example the operational console.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 */
public interface ManagementService {

  /**
   * Get the mapping containing {table name, row count} entries of the
   * Activiti database schema.
   *
   * @return the table count
   */
  Map<String, Long> getTableCount();
  
  /**
   * Gets the metadata (column names, column types, etc.) of a certain table.
   * Returns null when no table exists with the given name.
   *
   * @param tableName the table name
   * @return the table meta data
   */
  TableMetaData getTableMetaData(String tableName);
 
  /**
   * Creates a {@link TablePageQuery} that can be used to fetch {@link TablePage}
   * containing specific sections of table row data.
   *
   * @return the table page query
   */
  TablePageQuery createTablePageQuery();
  
  /**
   * Returns a new JobQuery implementation, that can be used
   * to dynamically query the jobs.
   *
   * @return the job query
   */
  JobQuery createJobQuery();
  
  /**
   * Forced synchronous execution of a job for testing purposes.
   *
   * @param jobId id of the job to execute, cannot be null.
   */
  void executeJob(String jobId);
  
  /**
   * Sets the number of retries that a job has left.
   *
   * Whenever the JobExecutor fails to execute a job, this value is decremented. 
   * When it hits zero, the job is supposed to be dead and not retried again.
   * In that case, this method can be used to increase the number of retries. 
   * @param jobId id of the job to modify, cannot be null.
   * @param retries number of retries.
   */
  void setJobRetries(String jobId, int retries);

  /**
   * Returns the full stacktrace of the exception that occurs when the job
   * with the given id was last executed. Returns null when the job has no
   * exception stacktrace.
   *
   * @param jobId id of the job, cannot be null.
   * @return the job exception stacktrace
   */
  String getJobExceptionStacktrace(String jobId);

  /**
   * get the list of properties.
   *
   * @return the properties
   */
  Map<String, String> getProperties();
  
  /**
   * programmatic schema update on a given connection returning feedback about what happened.
   *
   * @param connection the connection
   * @param catalog the catalog
   * @param schema the schema
   * @return the string
   */
  String databaseSchemaUpgrade(Connection connection, String catalog, String schema);
}
