/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.xml.signature.validator;

import org.opensaml.xml.BaseXMLObjectValidatorTestCase;
import org.opensaml.xml.signature.RetrievalMethod;
import org.opensaml.xml.signature.Transform;

/**
 *
 */
public class RetrievalMethodSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public RetrievalMethodSchemaValidatorTest() {
        targetQName = RetrievalMethod.DEFAULT_ELEMENT_NAME;
        validator = new RetrievalMethodSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        RetrievalMethod rm = (RetrievalMethod) target;
        
        rm.setURI("urn:string:foo");
    }
    
    public void testMissingURI() {
        RetrievalMethod rm = (RetrievalMethod) target;
        
        rm.setURI(null);
        assertValidationFail("RetrievalMethod URI was null, should have failed validation");
        
        rm.setURI("");
        assertValidationFail("RetrievalMethod URI was empty, should have failed validation");
        
        rm.setURI("       ");
        assertValidationFail("RetrievalMethod URI was all whitespace, should have failed validation");
    }

}
