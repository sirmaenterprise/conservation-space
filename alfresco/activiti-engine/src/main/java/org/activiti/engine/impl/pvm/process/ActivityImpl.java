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

package org.activiti.engine.impl.pvm.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;


// TODO: Auto-generated Javadoc
/**
 * The Class ActivityImpl.
 *
 * @author Tom Baeyens
 */
public class ActivityImpl extends ScopeImpl implements PvmActivity, HasDIBounds {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The outgoing transitions. */
  protected List<TransitionImpl> outgoingTransitions = new ArrayList<TransitionImpl>();
  
  /** The named outgoing transitions. */
  protected Map<String, TransitionImpl> namedOutgoingTransitions = new HashMap<String, TransitionImpl>();
  
  /** The incoming transitions. */
  protected List<TransitionImpl> incomingTransitions = new ArrayList<TransitionImpl>();
  
  /** The activity behavior. */
  protected ActivityBehavior activityBehavior;
  
  /** The parent. */
  protected ScopeImpl parent;
  
  /** The is scope. */
  protected boolean isScope;
  
  /** The is async. */
  protected boolean isAsync;
  
  /** The is exclusive. */
  protected boolean isExclusive;
  
  // Graphical information
  /** The x. */
  protected int x = -1;
  
  /** The y. */
  protected int y = -1;
  
  /** The width. */
  protected int width = -1;
  
  /** The height. */
  protected int height = -1;
  
  /**
   * Instantiates a new activity impl.
   *
   * @param id the id
   * @param processDefinition the process definition
   */
  public ActivityImpl(String id, ProcessDefinitionImpl processDefinition) {
    super(id, processDefinition);
  }

  /**
   * Creates the outgoing transition.
   *
   * @return the transition impl
   */
  public TransitionImpl createOutgoingTransition() {
    return createOutgoingTransition(null);
  }

  /**
   * Creates the outgoing transition.
   *
   * @param transitionId the transition id
   * @return the transition impl
   */
  public TransitionImpl createOutgoingTransition(String transitionId) {
    TransitionImpl transition = new TransitionImpl(transitionId, processDefinition);
    transition.setSource(this);
    outgoingTransitions.add(transition);
    
    if (transitionId!=null) {
      if (namedOutgoingTransitions.containsKey(transitionId)) {
        throw new PvmException("activity '"+id+" has duplicate transition '"+transitionId+"'");
      }
      namedOutgoingTransitions.put(transitionId, transition);
    }
    
    return transition;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmActivity#findOutgoingTransition(java.lang.String)
   */
  public TransitionImpl findOutgoingTransition(String transitionId) {
    return namedOutgoingTransitions.get(transitionId);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "Activity("+id+")";
  }
  
  /**
   * Gets the parent activity.
   *
   * @return the parent activity
   */
  public ActivityImpl getParentActivity() {
    if (parent instanceof ActivityImpl) {
      return (ActivityImpl) parent;
    }
    return null;
  }


  // restricted setters ///////////////////////////////////////////////////////
  
  /**
   * Sets the outgoing transitions.
   *
   * @param outgoingTransitions the new outgoing transitions
   */
  protected void setOutgoingTransitions(List<TransitionImpl> outgoingTransitions) {
    this.outgoingTransitions = outgoingTransitions;
  }

  /**
   * Sets the parent.
   *
   * @param parent the new parent
   */
  protected void setParent(ScopeImpl parent) {
    this.parent = parent;
  }

  /**
   * Sets the incoming transitions.
   *
   * @param incomingTransitions the new incoming transitions
   */
  protected void setIncomingTransitions(List<TransitionImpl> incomingTransitions) {
    this.incomingTransitions = incomingTransitions;
  }

  // getters and setters //////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmActivity#getOutgoingTransitions()
   */
  @SuppressWarnings("unchecked")
  public List<PvmTransition> getOutgoingTransitions() {
    return (List) outgoingTransitions;
  }

  /**
   * Gets the activity behavior.
   *
   * @return the activity behavior
   */
  public ActivityBehavior getActivityBehavior() {
    return activityBehavior;
  }

  /**
   * Sets the activity behavior.
   *
   * @param activityBehavior the new activity behavior
   */
  public void setActivityBehavior(ActivityBehavior activityBehavior) {
    this.activityBehavior = activityBehavior;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmActivity#getParent()
   */
  public ScopeImpl getParent() {
    return parent;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmActivity#getIncomingTransitions()
   */
  @SuppressWarnings("unchecked")
  public List<PvmTransition> getIncomingTransitions() {
    return (List) incomingTransitions;
  }

  /**
   * Checks if is scope.
   *
   * @return true, if is scope
   */
  public boolean isScope() {
    return isScope;
  }

  /**
   * Sets the scope.
   *
   * @param isScope the new scope
   */
  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#getX()
   */
  public int getX() {
    return x;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#setX(int)
   */
  public void setX(int x) {
    this.x = x;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#getY()
   */
  public int getY() {
    return y;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#setY(int)
   */
  public void setY(int y) {
    this.y = y;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#getWidth()
   */
  public int getWidth() {
    return width;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#setWidth(int)
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#getHeight()
   */
  public int getHeight() {
    return height;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.process.HasDIBounds#setHeight(int)
   */
  public void setHeight(int height) {
    this.height = height;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmActivity#isAsync()
   */
  public boolean isAsync() {
    return isAsync;
  }
  
  /**
   * Sets the async.
   *
   * @param isAsync the new async
   */
  public void setAsync(boolean isAsync) {
    this.isAsync = isAsync;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmActivity#isExclusive()
   */
  public boolean isExclusive() {
    return isExclusive;
  }
    
  /**
   * Sets the exclusive.
   *
   * @param isExclusive the new exclusive
   */
  public void setExclusive(boolean isExclusive) {
    this.isExclusive = isExclusive;
  }
  
}
