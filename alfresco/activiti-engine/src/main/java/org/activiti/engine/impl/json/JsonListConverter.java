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
package org.activiti.engine.impl.json;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.json.JSONArray;


// TODO: Auto-generated Javadoc
/**
 * The Class JsonListConverter.
 *
 * @param <T> the generic type
 * @author Tom Baeyens
 */
public class JsonListConverter<T> {

  /** The json object converter. */
  JsonObjectConverter<T> jsonObjectConverter;
  
  /**
   * Instantiates a new json list converter.
   *
   * @param jsonObjectConverter the json object converter
   */
  public JsonListConverter(JsonObjectConverter<T> jsonObjectConverter) {
    this.jsonObjectConverter = jsonObjectConverter;
  }

  /**
   * To json.
   *
   * @param list the list
   * @param writer the writer
   */
  public void toJson(List<T> list, Writer writer) {
    toJsonArray(list).write(writer);
  }

  /**
   * To json.
   *
   * @param list the list
   * @return the string
   */
  public String toJson(List<T> list) {
    return toJsonArray(list).toString();
  }
  
  /**
   * To json.
   *
   * @param list the list
   * @param indentFactor the indent factor
   * @return the string
   */
  public String toJson(List<T> list, int indentFactor) {
    return toJsonArray(list).toString(indentFactor);
  }
  
  /**
   * To json array.
   *
   * @param objects the objects
   * @return the jSON array
   */
  private JSONArray toJsonArray(List<T> objects) {
    JSONArray jsonArray = new JSONArray();
    for (T object: objects) {
      jsonArray.put(jsonObjectConverter.toJsonObject(object));
    }
    return jsonArray;
  }

  /**
   * To object.
   *
   * @param reader the reader
   * @return the list
   */
  public List<T> toObject(Reader reader) {
    throw new ActivitiException("not yet implemented");
  }
  
  /**
   * Gets the json object converter.
   *
   * @return the json object converter
   */
  public JsonObjectConverter<T> getJsonObjectConverter() {
    return jsonObjectConverter;
  }
  
  /**
   * Sets the json object converter.
   *
   * @param jsonObjectConverter the new json object converter
   */
  public void setJsonObjectConverter(JsonObjectConverter<T> jsonObjectConverter) {
    this.jsonObjectConverter = jsonObjectConverter;
  }
}
