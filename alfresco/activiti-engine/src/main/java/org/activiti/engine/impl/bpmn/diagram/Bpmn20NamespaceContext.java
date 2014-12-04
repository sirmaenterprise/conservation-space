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

package org.activiti.engine.impl.bpmn.diagram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;


// TODO: Auto-generated Javadoc
/**
 * XML {@link NamespaceContext} containing the namespaces used by BPMN 2.0 XML documents.
 *
 * Can be used in {@link XPath#setNamespaceContext(NamespaceContext)}.
 * 
 * @author Falko Menge
 */
public class Bpmn20NamespaceContext implements NamespaceContext {
  
  /** The Constant BPMN. */
  public static final String BPMN = "bpmn";
  
  /** The Constant BPMNDI. */
  public static final String BPMNDI = "bpmndi";
  
  /** The Constant OMGDC. */
  public static final String OMGDC = "omgdc";
  
  /** The Constant OMGDI. */
  public static final String OMGDI = "omgdi";
  
  /** This is a protected filed so you can extend that context with your own namespaces if necessary. */
  protected Map<String, String> namespaceUris = new HashMap<String, String>();
  
  /**
   * Instantiates a new bpmn20 namespace context.
   */
  public Bpmn20NamespaceContext() {
    namespaceUris.put(BPMN, "http://www.omg.org/spec/BPMN/20100524/MODEL");
    namespaceUris.put(BPMNDI, "http://www.omg.org/spec/BPMN/20100524/DI");
    namespaceUris.put(OMGDC, "http://www.omg.org/spec/DD/20100524/DI");
    namespaceUris.put(OMGDI, "http://www.omg.org/spec/DD/20100524/DC");
  }

  /* (non-Javadoc)
   * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
   */
  public String getNamespaceURI(String prefix) {
    return namespaceUris.get(prefix);
  }

  /* (non-Javadoc)
   * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
   */
  public String getPrefix(String namespaceURI) {
    return getKeyByValue(namespaceUris, namespaceURI);
  }

  /* (non-Javadoc)
   * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
   */
  public Iterator<String> getPrefixes(String namespaceURI) {
    return getKeysByValue(namespaceUris, namespaceURI).iterator();
  }

  /**
   * Gets the keys by value.
   *
   * @param <T> the generic type
   * @param <E> the element type
   * @param map the map
   * @param value the value
   * @return the keys by value
   */
  private static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
    Set<T> keys = new HashSet<T>();
    for (Entry<T, E> entry : map.entrySet()) {
      if (value.equals(entry.getValue())) {
        keys.add(entry.getKey());
      }
    }
    return keys;
  }

  /**
   * Gets the key by value.
   *
   * @param <T> the generic type
   * @param <E> the element type
   * @param map the map
   * @param value the value
   * @return the key by value
   */
  private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
    for (Entry<T, E> entry : map.entrySet()) {
      if (value.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

}
