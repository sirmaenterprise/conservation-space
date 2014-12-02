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

package org.opensaml.xml.encryption.validator;

import org.opensaml.xml.BaseXMLObjectValidatorTestCase;
import org.opensaml.xml.encryption.DataReference;
import org.opensaml.xml.encryption.KeyReference;
import org.opensaml.xml.encryption.ReferenceList;

/**
 *
 */
public class ReferenceListSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    public ReferenceListSchemaValidatorTest() {
        targetQName = ReferenceList.DEFAULT_ELEMENT_NAME;
        validator = new ReferenceListSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        ReferenceList rl = (ReferenceList) target;
        
        rl.getReferences().add((DataReference) buildXMLObject(DataReference.DEFAULT_ELEMENT_NAME));
        rl.getReferences().add((KeyReference) buildXMLObject(KeyReference.DEFAULT_ELEMENT_NAME));
    }
    
    public void testEmptyReferenceList() {
        ReferenceList transforms = (ReferenceList) target;
        
        transforms.getReferences().clear();
        assertValidationFail("ReferenceList child list was empty, should have failed validation");
    }

}
