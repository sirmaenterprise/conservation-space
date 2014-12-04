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

import org.activiti.engine.impl.persistence.entity.ByteArrayEntity;


// TODO: Auto-generated Javadoc
/**
 * The Interface ValueFields.
 *
 * @author Tom Baeyens
 */
public interface ValueFields {

  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();

  /**
   * Gets the text value.
   *
   * @return the text value
   */
  String getTextValue();
  
  /**
   * Sets the text value.
   *
   * @param textValue the new text value
   */
  void setTextValue(String textValue);

  /**
   * Gets the text value2.
   *
   * @return the text value2
   */
  String getTextValue2();
  
  /**
   * Sets the text value2.
   *
   * @param textValue2 the new text value2
   */
  void setTextValue2(String textValue2);

  /**
   * Gets the long value.
   *
   * @return the long value
   */
  Long getLongValue();
  
  /**
   * Sets the long value.
   *
   * @param longValue the new long value
   */
  void setLongValue(Long longValue);
  
  /**
   * Gets the double value.
   *
   * @return the double value
   */
  Double getDoubleValue();
  
  /**
   * Sets the double value.
   *
   * @param doubleValue the new double value
   */
  void setDoubleValue(Double doubleValue);
  
  /**
   * Gets the byte array value id.
   *
   * @return the byte array value id
   */
  String getByteArrayValueId();
  
  /**
   * Sets the byte array value.
   *
   * @param byteArrayValue the new byte array value
   */
  void setByteArrayValue(ByteArrayEntity byteArrayValue);
  
  /**
   * Gets the byte array value.
   *
   * @return the byte array value
   */
  ByteArrayEntity getByteArrayValue();
  
  /**
   * Gets the cached value.
   *
   * @return the cached value
   */
  Object getCachedValue();
  
  /**
   * Sets the cached value.
   *
   * @param deserializedObject the new cached value
   */
  void setCachedValue(Object deserializedObject);
}
