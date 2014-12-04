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

package org.activiti.engine.identity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


// TODO: Auto-generated Javadoc
/**
 * The Class Picture.
 *
 * @author Tom Baeyens
 */
public class Picture {

  /** The bytes. */
  protected byte[] bytes;
  
  /** The mime type. */
  protected String mimeType;

  /**
   * Instantiates a new picture.
   *
   * @param bytes the bytes
   * @param mimeType the mime type
   */
  public Picture(byte[] bytes, String mimeType) {
    this.bytes = bytes;
    this.mimeType = mimeType;
  }

  /**
   * Gets the bytes.
   *
   * @return the bytes
   */
  public byte[] getBytes() {
    return bytes;
  }
  
  /**
   * Gets the input stream.
   *
   * @return the input stream
   */
  public InputStream getInputStream() {
    return new ByteArrayInputStream(bytes);
  }

  /**
   * Gets the mime type.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }
}
