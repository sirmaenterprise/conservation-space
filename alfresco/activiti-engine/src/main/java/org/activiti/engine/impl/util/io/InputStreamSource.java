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
package org.activiti.engine.impl.util.io;

import java.io.InputStream;


// TODO: Auto-generated Javadoc
/**
 * The Class InputStreamSource.
 *
 * @author Tom Baeyens
 */
public class InputStreamSource implements StreamSource {
  
  /** The input stream. */
  InputStream inputStream;
  
  /**
   * Instantiates a new input stream source.
   *
   * @param inputStream the input stream
   */
  public InputStreamSource(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.util.io.StreamSource#getInputStream()
   */
  public InputStream getInputStream() {
    return inputStream;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "InputStream";
  }
}
