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
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.ListQueryParameterObject;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;


// TODO: Auto-generated Javadoc
/**
 * Abstract superclass for all query types.
 *
 * @param <T> the generic type
 * @param <U> the generic type
 * @author Joram Barrez
 */
public abstract class AbstractQuery<T extends Query<?,?>, U> extends ListQueryParameterObject implements Command<Object>, Query<T,U>, Serializable {
      
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The Constant SORTORDER_ASC. */
  public static final String SORTORDER_ASC = "asc";
  
  /** The Constant SORTORDER_DESC. */
  public static final String SORTORDER_DESC = "desc";
  
  /**
   * The Enum ResultType.
   */
  private static enum ResultType {
    
    /** The list. */
    LIST, 
 /** The list page. */
 LIST_PAGE, 
 /** The single result. */
 SINGLE_RESULT, 
 /** The count. */
 COUNT
  }
    
  /** The command executor. */
  protected transient CommandExecutor commandExecutor;
  
  /** The command context. */
  protected transient CommandContext commandContext;
  
  /** The order by. */
  protected String orderBy;
  
  /** The result type. */
  protected ResultType resultType;

  /** The order property. */
  protected QueryProperty orderProperty;

  /**
   * Instantiates a new abstract query.
   */
  protected AbstractQuery() {
    parameter = this;
  }
  
  /**
   * Instantiates a new abstract query.
   *
   * @param commandExecutor the command executor
   */
  protected AbstractQuery(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
  
  /**
   * Instantiates a new abstract query.
   *
   * @param commandContext the command context
   */
  public AbstractQuery(CommandContext commandContext) {
    this.commandContext = commandContext;
  }
  
  /**
   * Sets the command executor.
   *
   * @param commandExecutor the command executor
   * @return the abstract query
   */
  public AbstractQuery<T, U> setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    return this;
  }

  /**
   * Order by.
   *
   * @param property the property
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public T orderBy(QueryProperty property) {
    this.orderProperty = property;
    return (T) this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.query.Query#asc()
   */
  public T asc() {
    return direction(Direction.ASCENDING);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.query.Query#desc()
   */
  public T desc() {
    return direction(Direction.DESCENDING);
  }
  
  /**
   * Direction.
   *
   * @param direction the direction
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public T direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("You should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return (T) this;
  }
  
  /**
   * Check query ok.
   */
  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: call asc() or desc() after using orderByXX()");
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.query.Query#singleResult()
   */
  @SuppressWarnings("unchecked")
  public U singleResult() {
    this.resultType = ResultType.SINGLE_RESULT;
    if (commandExecutor!=null) {
      return (U) commandExecutor.execute(this);
    }
    return executeSingleResult(Context.getCommandContext());
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.query.Query#list()
   */
  @SuppressWarnings("unchecked")
  public List<U> list() {
    this.resultType = ResultType.LIST;
    if (commandExecutor!=null) {
      return (List<U>) commandExecutor.execute(this);
    }
    return executeList(Context.getCommandContext(), null);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.query.Query#listPage(int, int)
   */
  @SuppressWarnings("unchecked")
  public List<U> listPage(int firstResult, int maxResults) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
    this.resultType = ResultType.LIST_PAGE;
    if (commandExecutor!=null) {
      return (List<U>) commandExecutor.execute(this);
    }
    return executeList(Context.getCommandContext(), new Page(firstResult, maxResults));
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.query.Query#count()
   */
  public long count() {
    this.resultType = ResultType.COUNT;
    if (commandExecutor!=null) {
      return (Long) commandExecutor.execute(this);
    }
    return executeCount(Context.getCommandContext());
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Command#execute(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public Object execute(CommandContext commandContext) {
    if (resultType==ResultType.LIST) {
      return executeList(commandContext, null);
    } else if (resultType==ResultType.SINGLE_RESULT) {
      return executeSingleResult(commandContext);
    } else if (resultType==ResultType.LIST_PAGE) {
      return executeList(commandContext, null);
    } else {
      return executeCount(commandContext);
    }
  }

  /**
   * Execute count.
   *
   * @param commandContext the command context
   * @return the long
   */
  public abstract long executeCount(CommandContext commandContext);
  
  /**
   * Executes the actual query to retrieve the list of results.
   *
   * @param commandContext the command context
   * @param page used if the results must be paged. If null, no paging will be applied.
   * @return the list
   */
  public abstract List<U> executeList(CommandContext commandContext, Page page);
  
  /**
   * Execute single result.
   *
   * @param commandContext the command context
   * @return the u
   */
  public U executeSingleResult(CommandContext commandContext) {
    List<U> results = executeList(commandContext, null);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
     throw new ActivitiException("Query return "+results.size()+" results instead of max 1");
    } 
    return null;
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

  /**
   * Gets the order by.
   *
   * @return the order by
   */
  public String getOrderBy() {
    return orderBy;
  }
}
