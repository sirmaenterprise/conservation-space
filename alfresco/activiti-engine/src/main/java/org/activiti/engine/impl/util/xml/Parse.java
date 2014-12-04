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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.impl.util.io.ResourceStreamSource;
import org.activiti.engine.impl.util.io.StreamSource;
import org.activiti.engine.impl.util.io.StringStreamSource;
import org.activiti.engine.impl.util.io.UrlStreamSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


// TODO: Auto-generated Javadoc
/**
 * The Class Parse.
 *
 * @author Tom Baeyens
 */
public class Parse extends DefaultHandler {
  
  /** The Constant LOGGER. */
  private static final Logger LOGGER = Logger.getLogger(Parse.class.getName());
  
  /** The Constant JAXP_SCHEMA_SOURCE. */
  private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
  
  /** The Constant JAXP_SCHEMA_LANGUAGE. */
  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
  
  /** The Constant W3C_XML_SCHEMA. */
  private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
  
  /** The Constant NEW_LINE. */
  private static final String NEW_LINE = System.getProperty("line.separator");
  
  /** The parser. */
  protected Parser parser;
  
  /** The name. */
  protected String name;
  
  /** The stream source. */
  protected StreamSource streamSource;
  
  /** The root element. */
  protected Element rootElement = null;
  
  /** The errors. */
  protected List<Problem> errors = new ArrayList<Problem>();
  
  /** The warnings. */
  protected List<Problem> warnings = new ArrayList<Problem>();
  
  /** The schema resource. */
  protected String schemaResource;

  /**
   * Instantiates a new parses the.
   *
   * @param parser the parser
   */
  public Parse(Parser parser) {
    this.parser = parser;
  }
  
  /**
   * Name.
   *
   * @param name the name
   * @return the parses the
   */
  public Parse name(String name) {
    this.name = name;
    return this;
  }
  
  /**
   * Source input stream.
   *
   * @param inputStream the input stream
   * @return the parses the
   */
  public Parse sourceInputStream(InputStream inputStream) {
    if (name==null) {
      name("inputStream");
    }
    setStreamSource(new InputStreamSource(inputStream)); 
    return this;
  }

  /**
   * Source resource.
   *
   * @param resource the resource
   * @return the parses the
   */
  public Parse sourceResource(String resource) {
    return sourceResource(resource, null);
  }

  /**
   * Source url.
   *
   * @param url the url
   * @return the parses the
   */
  public Parse sourceUrl(URL url) {
    if (name==null) {
      name(url.toString());
    }
    setStreamSource(new UrlStreamSource(url));
    return this;
  }
  
  /**
   * Source url.
   *
   * @param url the url
   * @return the parses the
   */
  public Parse sourceUrl(String url) {
    try {
      return sourceUrl(new URL(url));
    } catch (MalformedURLException e) {
      throw new ActivitiException("malformed url: "+url, e);
    }
  }
  
  /**
   * Source resource.
   *
   * @param resource the resource
   * @param classLoader the class loader
   * @return the parses the
   */
  public Parse sourceResource(String resource, ClassLoader classLoader) {
    if (name==null) {
      name(resource);
    }
    setStreamSource(new ResourceStreamSource(resource, classLoader)); 
    return this;
  }

  /**
   * Source string.
   *
   * @param string the string
   * @return the parses the
   */
  public Parse sourceString(String string) {
    if (name==null) {
      name("string");
    }
    setStreamSource(new StringStreamSource(string)); 
    return this;
  }

  /**
   * Sets the stream source.
   *
   * @param streamSource the new stream source
   */
  protected void setStreamSource(StreamSource streamSource) {
    if (this.streamSource!=null) {
      throw new ActivitiException("invalid: multiple sources "+this.streamSource+" and "+streamSource);
    }
    this.streamSource = streamSource;
  }
  
  /**
   * Execute.
   *
   * @return the parses the
   */
  public Parse execute() {
    try {
      InputStream inputStream = streamSource.getInputStream();

      if (schemaResource == null) { // must be done before parser is created
        parser.getSaxParserFactory().setNamespaceAware(false);
        parser.getSaxParserFactory().setValidating(false);
      }

      SAXParser saxParser = parser.getSaxParser(); 
      if (schemaResource != null) { 
        saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        saxParser.setProperty(JAXP_SCHEMA_SOURCE, schemaResource);
      }
      saxParser.parse(inputStream, new ParseHandler(this));
      
    } catch (Exception e) { // any exception can happen (Activiti, Io, etc.)
      throw new ActivitiException("couldn't parse '"+name+"': "+e.getMessage(), e);
    }
    
    return this;
  }

  /**
   * Gets the root element.
   *
   * @return the root element
   */
  public Element getRootElement() {
    return rootElement;
  }

  /**
   * Gets the problems.
   *
   * @return the problems
   */
  public List<Problem> getProblems() {
    return errors;
  }

  /**
   * Adds the error.
   *
   * @param e the e
   */
  public void addError(SAXParseException e) {
    errors.add(new Problem(e, name));
  }
  
  /**
   * Adds the error.
   *
   * @param errorMessage the error message
   * @param element the element
   */
  public void addError(String errorMessage, Element element) {
    errors.add(new Problem(errorMessage, name, element));
  }
  
  /**
   * Checks for errors.
   *
   * @return true, if successful
   */
  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }
  
  /**
   * Adds the warning.
   *
   * @param e the e
   */
  public void addWarning(SAXParseException e) {
    warnings.add(new Problem(e, name));
  }
  
  /**
   * Adds the warning.
   *
   * @param errorMessage the error message
   * @param element the element
   */
  public void addWarning(String errorMessage, Element element) {
    warnings.add(new Problem(errorMessage, name, element));
  }
  
  /**
   * Checks for warnings.
   *
   * @return true, if successful
   */
  public boolean hasWarnings() {
    return warnings != null && !warnings.isEmpty();
  }
  
  /**
   * Log warnings.
   */
  public void logWarnings() {
    for (Problem warning : warnings) {
      LOGGER.warning(warning.toString());
    }
  }
  
  /**
   * Throw activiti exception for errors.
   */
  public void throwActivitiExceptionForErrors() {
    StringBuilder strb = new StringBuilder();
    for (Problem error : errors) {
      strb.append(error.toString());
      strb.append(NEW_LINE);
    }
    throw new ActivitiException(strb.toString());
  }
  
  /**
   * Sets the schema resource.
   *
   * @param schemaResource the new schema resource
   */
  public void setSchemaResource(String schemaResource) {
    SAXParserFactory saxParserFactory = parser.getSaxParserFactory();
    saxParserFactory.setNamespaceAware(true);
    saxParserFactory.setValidating(true);
    try {
      saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    } catch (Exception e) {
      LOGGER.warning(e.getMessage());
    }
    this.schemaResource = schemaResource;
  }

}
