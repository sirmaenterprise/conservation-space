/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.ws.soap.util;

import java.util.List;

import org.opensaml.xml.AttributeExtensibleXMLObject;

/**
 * Helper methods for working with SOAP.
 */
public class SOAPHelper {

    /**
     * Privatae constructor.
     */
    private SOAPHelper() {
    }

    /**
     * Adds a "mustUnderstand" attribute to the given SOAP object.
     * 
     * @param soapObject the SOAP object to add the attribute to
     * @param mustUnderstand whether mustUnderstand is true or false
     */
    public static void addMustUnderstandAttribute(AttributeExtensibleXMLObject soapObject, boolean mustUnderstand) {

    }

    /**
     * Adds an "actor" attribute to the given SOAP object.
     * 
     * @param soapObject the SOAP object to add the attribute to
     * @param actorURI the URI of the actor
     */
    public static void addActorAttribute(AttributeExtensibleXMLObject soapObject, String actorURI) {

    }

    /**
     * Adds a single encoding style to the given SOAP object. If existing encodingStyles are present, the given style
     * will be added to the existing list.
     * 
     * @param soapObject the SOAP object to add the attribute to
     * @param encodingStyle the encoding style to add
     */
    public static void addEncodingStyle(AttributeExtensibleXMLObject soapObject, String encodingStyle) {

    }

    /**
     * Adds an "encodingStyle" attribute to the given SOAP object.
     * 
     * @param soapObject the SOAP object to add the attribute to
     * @param encodingStyles the list of encoding styles to add
     */
    public static void addEncodingStyles(AttributeExtensibleXMLObject soapObject, List<String> encodingStyles) {

    }
}
