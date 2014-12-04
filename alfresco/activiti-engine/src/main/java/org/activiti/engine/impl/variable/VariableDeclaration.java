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
package org.activiti.engine.impl.variable;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;


// TODO: Auto-generated Javadoc
/**
 * The Class VariableDeclaration.
 *
 * @author Tom Baeyens
 */
public class VariableDeclaration implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The name. */
  protected String name;
  
  /** The type. */
  protected String type;
  
  /** The source variable name. */
  protected String sourceVariableName;
  
  /** The source expression. */
  protected Expression sourceExpression;
  
  /** The destination variable name. */
  protected String destinationVariableName;
  
  /** The destination expression. */
  protected Expression destinationExpression;
  
  /** The link. */
  protected String link;
  
  /** The link expression. */
  protected Expression linkExpression;
  

  /**
   * Initialize.
   *
   * @param innerScopeInstance the inner scope instance
   * @param outerScopeInstance the outer scope instance
   */
  public void initialize(VariableScope innerScopeInstance, VariableScope outerScopeInstance) {
    if (sourceVariableName!=null) {
      if (outerScopeInstance.hasVariable(sourceVariableName)) {
        Object value = outerScopeInstance.getVariable(sourceVariableName);
        innerScopeInstance.setVariable(destinationVariableName, value);      
      } else {
        throw new ActivitiException("Couldn't create variable '" 
                + destinationVariableName + "', since the source variable '"
                + sourceVariableName + "does not exist");
      }
    }
    
    if (sourceExpression!=null) {
      Object value = sourceExpression.getValue(outerScopeInstance);
      innerScopeInstance.setVariable(destinationVariableName, value);
    }
    
    if (link!=null) {
      if (outerScopeInstance.hasVariable(sourceVariableName)) {
        Object value = outerScopeInstance.getVariable(sourceVariableName);
        innerScopeInstance.setVariable(destinationVariableName, value);
      } else {
        throw new ActivitiException("Couldn't create variable '" + destinationVariableName + "', since the source variable '" + sourceVariableName
                + "does not exist");
      }
    }

    if (linkExpression!=null) {
      Object value = sourceExpression.getValue(outerScopeInstance);
      innerScopeInstance.setVariable(destinationVariableName, value);
    }

  }
  
  /**
   * Destroy.
   *
   * @param innerScopeInstance the inner scope instance
   * @param outerScopeInstance the outer scope instance
   */
  public void destroy(VariableScope innerScopeInstance, VariableScope outerScopeInstance) {
    
    if (destinationVariableName!=null) {
      if (innerScopeInstance.hasVariable(sourceVariableName)) {
        Object value = innerScopeInstance.getVariable(sourceVariableName);
        outerScopeInstance.setVariable(destinationVariableName, value);
      } else {
        throw new ActivitiException("Couldn't destroy variable " + sourceVariableName + ", since it does not exist");
      }
    }

    if (destinationExpression!=null) {
      Object value = destinationExpression.getValue(innerScopeInstance);
      outerScopeInstance.setVariable(destinationVariableName, value);
    }
    
    if (link!=null) {
      if (innerScopeInstance.hasVariable(sourceVariableName)) {
        Object value = innerScopeInstance.getVariable(sourceVariableName);
        outerScopeInstance.setVariable(destinationVariableName, value);
      } else {
        throw new ActivitiException("Couldn't destroy variable " + sourceVariableName + ", since it does not exist");
      }
    }

    if (linkExpression!=null) {
      Object value = sourceExpression.getValue(innerScopeInstance);
      outerScopeInstance.setVariable(destinationVariableName, value);
    }
  }
  
  /**
   * Instantiates a new variable declaration.
   *
   * @param name the name
   * @param type the type
   */
  public VariableDeclaration(String name, String type) {
    this.name = name;
    this.type = type;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "VariableDeclaration[" + name + ":" + type + "]";
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }
  
  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * Gets the source variable name.
   *
   * @return the source variable name
   */
  public String getSourceVariableName() {
    return sourceVariableName;
  }
  
  /**
   * Sets the source variable name.
   *
   * @param sourceVariableName the new source variable name
   */
  public void setSourceVariableName(String sourceVariableName) {
    this.sourceVariableName = sourceVariableName;
  }

  /**
   * Gets the source expression.
   *
   * @return the source expression
   */
  public Expression getSourceExpression() {
    return sourceExpression;
  }
  
  /**
   * Sets the source expression.
   *
   * @param sourceExpression the new source expression
   */
  public void setSourceExpression(Expression sourceExpression) {
    this.sourceExpression = sourceExpression;
  }
  
  /**
   * Gets the destination variable name.
   *
   * @return the destination variable name
   */
  public String getDestinationVariableName() {
    return destinationVariableName;
  }
  
  /**
   * Sets the destination variable name.
   *
   * @param destinationVariableName the new destination variable name
   */
  public void setDestinationVariableName(String destinationVariableName) {
    this.destinationVariableName = destinationVariableName;
  }

  /**
   * Gets the destination expression.
   *
   * @return the destination expression
   */
  public Expression getDestinationExpression() {
    return destinationExpression;
  }
  
  /**
   * Sets the destination expression.
   *
   * @param destinationExpression the new destination expression
   */
  public void setDestinationExpression(Expression destinationExpression) {
    this.destinationExpression = destinationExpression;
  }
  
  /**
   * Gets the link.
   *
   * @return the link
   */
  public String getLink() {
    return link;
  }
  
  /**
   * Sets the link.
   *
   * @param link the new link
   */
  public void setLink(String link) {
    this.link = link;
  }
  
  /**
   * Gets the link expression.
   *
   * @return the link expression
   */
  public Expression getLinkExpression() {
    return linkExpression;
  }

  /**
   * Sets the link expression.
   *
   * @param linkExpression the new link expression
   */
  public void setLinkExpression(Expression linkExpression) {
    this.linkExpression = linkExpression;
  }
}
