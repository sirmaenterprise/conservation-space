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
package org.activiti.engine.management;

import java.util.ArrayList;
import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * Structure containing meta data (column names, column types, etc.) 
 * about a certain database table.
 * 
 * @author Joram Barrez
 */
public class TableMetaData {
  
  /** The table name. */
  protected String tableName;
  
  /** The column names. */
  protected List<String> columnNames = new ArrayList<String>();
  
  /** The column types. */
  protected List<String> columnTypes = new ArrayList<String>();

  /**
   * Instantiates a new table meta data.
   */
  public TableMetaData() {
    
  }
  
  /**
   * Instantiates a new table meta data.
   *
   * @param tableName the table name
   */
  public TableMetaData(String tableName) {
    this.tableName = tableName;
  }
  
  /**
   * Adds the column meta data.
   *
   * @param columnName the column name
   * @param columnType the column type
   */
  public void addColumnMetaData(String columnName, String columnType) {
    columnNames.add(columnName);
    columnTypes.add(columnType);
  }
  
  /**
   * Gets the table name.
   *
   * @return the table name
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Sets the table name.
   *
   * @param tableName the new table name
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /**
   * Gets the column names.
   *
   * @return the column names
   */
  public List<String> getColumnNames() {
    return columnNames;
  }

  /**
   * Sets the column names.
   *
   * @param columnNames the new column names
   */
  public void setColumnNames(List<String> columnNames) {
    this.columnNames = columnNames;
  }

  /**
   * Gets the column types.
   *
   * @return the column types
   */
  public List<String> getColumnTypes() {
    return columnTypes;
  }

  /**
   * Sets the column types.
   *
   * @param columnTypes the new column types
   */
  public void setColumnTypes(List<String> columnTypes) {
    this.columnTypes = columnTypes;
  }

}
