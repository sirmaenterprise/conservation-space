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
package org.activiti.engine.impl.util.xml;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


// TODO: Auto-generated Javadoc
/**
 * The Class Parser.
 *
 * @author Tom Baeyens
 */
public class Parser {

  /** The default sax parser factory. */
  protected static SAXParserFactory defaultSaxParserFactory = SAXParserFactory.newInstance();
  
  /** The Constant INSTANCE. */
  public static final Parser INSTANCE = new Parser();

  /**
   * Creates the parse.
   *
   * @return the parses the
   */
  public Parse createParse() {
    return new Parse(this);
  }

  /**
   * Gets the sax parser.
   *
   * @return the sax parser
   * @throws Exception the exception
   */
  protected SAXParser getSaxParser() throws Exception {
    return getSaxParserFactory().newSAXParser();
  }

  /**
   * Gets the sax parser factory.
   *
   * @return the sax parser factory
   */
  protected SAXParserFactory getSaxParserFactory() {
    return defaultSaxParserFactory;
  }
}
