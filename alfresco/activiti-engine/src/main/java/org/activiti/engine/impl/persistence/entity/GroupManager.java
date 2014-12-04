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

package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.AbstractManager;


// TODO: Auto-generated Javadoc
/**
 * The Class GroupManager.
 *
 * @author Tom Baeyens
 * @author Saeid Mirzaei
 */
public class GroupManager extends AbstractManager {

  /**
   * Creates the new group.
   *
   * @param groupId the group id
   * @return the group
   */
  public Group createNewGroup(String groupId) {
    return new GroupEntity(groupId);
  }

  /**
   * Insert group.
   *
   * @param group the group
   */
  public void insertGroup(Group group) {
    getDbSqlSession().insert((PersistentObject) group);
  }

  /**
   * Update group.
   *
   * @param updatedGroup the updated group
   */
  public void updateGroup(Group updatedGroup) {
    GroupEntity persistentGroup = findGroupById(updatedGroup.getId());
    persistentGroup.update((GroupEntity) updatedGroup);
  }


  /**
   * Delete group.
   *
   * @param groupId the group id
   */
  public void deleteGroup(String groupId) {
    getDbSqlSession().delete("deleteMembershipsByGroupId", groupId);
    getDbSqlSession().delete("deleteGroup", groupId);
  }

  /**
   * Creates the new group query.
   *
   * @return the group query
   */
  public GroupQuery createNewGroupQuery() {
    return new GroupQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  /**
   * Find group by query criteria.
   *
   * @param query the query
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectGroupByQueryCriteria", query, page);
  }
  
  /**
   * Find group count by query criteria.
   *
   * @param query the query
   * @return the long
   */
  public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
    return (Long) getDbSqlSession().selectOne("selectGroupCountByQueryCriteria", query);
  }

  /**
   * Find group by id.
   *
   * @param groupId the group id
   * @return the group entity
   */
  public GroupEntity findGroupById(String groupId) {
    return (GroupEntity) getDbSqlSession().selectOne("selectGroupById", groupId);
  }

  /**
   * Find groups by user.
   *
   * @param userId the user id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<Group> findGroupsByUser(String userId) {
    return getDbSqlSession().selectList("selectGroupsByUserId", userId);
  }

  /**
   * Find potential starter users.
   *
   * @param proceDefId the proce def id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<Group> findPotentialStarterUsers(String proceDefId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("procDefId", proceDefId);
    return  (List<Group>) getDbSqlSession().selectOne("selectGroupByQueryCriteria", parameters);
  }
    
  
}
