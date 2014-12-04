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

import java.io.Serializable;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;


// TODO: Auto-generated Javadoc
/**
 * The Class TablePageQueryImpl.
 *
 * @author Joram Barrez
 */
public class TablePageQueryImpl implements TablePageQuery, Command<TablePage>, Serializable {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The command executor. */
  transient CommandExecutor commandExecutor;
  
  /** The table name. */
  protected String tableName;
  
  /** The order by. */
  protected String orderBy;
  
  /** The first result. */
  protected int firstResult;
  
  /** The max results. */
  protected int maxResults;

  /**
   * Instantiates a new table page query impl.
   */
  public TablePageQueryImpl() {
  }
  
  /**
   * Instantiates a new table page query impl.
   *
   * @param commandExecutor the command executor
   */
  public TablePageQueryImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.management.TablePageQuery#tableName(java.lang.String)
   */
  public TablePageQueryImpl tableName(String tableName) {
    this.tableName = tableName;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.management.TablePageQuery#orderAsc(java.lang.String)
   */
  public TablePageQueryImpl orderAsc(String column) {
    addOrder(column, AbstractQuery.SORTORDER_ASC);
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.management.TablePageQuery#orderDesc(java.lang.String)
   */
  public TablePageQueryImpl orderDesc(String column) {
    addOrder(column, AbstractQuery.SORTORDER_DESC);
    return this;
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
   * Adds the order.
   *
   * @param column the column
   * @param sortOrder the sort order
   */
  protected void addOrder(String column, String sortOrder) {
    if (orderBy==null) {
      orderBy = "";
    } else {
      orderBy = orderBy+", ";
    }
    orderBy = orderBy+column+" "+sortOrder;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.management.TablePageQuery#listPage(int, int)
   */
  public TablePage listPage(int firstResult, int maxResults) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
    return commandExecutor.execute(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public TablePage execute(CommandContext commandContext) {
    return commandContext
      .getTableDataManager()
      .getTablePage(this, firstResult, maxResults);
  }
}
