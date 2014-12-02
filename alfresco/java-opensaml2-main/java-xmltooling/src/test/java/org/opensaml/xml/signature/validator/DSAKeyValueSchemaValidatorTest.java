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
import org.opensaml.xml.signature.DSAKeyValue;
import org.opensaml.xml.signature.P;
import org.opensaml.xml.signature.PgenCounter;
import org.opensaml.xml.signature.Q;
import org.opensaml.xml.signature.Seed;
import org.opensaml.xml.signature.Y;

/**
 *
 */
public class DSAKeyValueSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    private P pValue;
    private Q qValue;
    private PgenCounter pgenCounterValue;
    private Seed seedValue;
    
    public DSAKeyValueSchemaValidatorTest() {
        targetQName = DSAKeyValue.DEFAULT_ELEMENT_NAME;
        validator = new DSAKeyValueSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        DSAKeyValue keyValue = (DSAKeyValue) target;
        
        keyValue.setY((Y) buildXMLObject(Y.DEFAULT_ELEMENT_NAME));
        
        pValue = (P) buildXMLObject(P.DEFAULT_ELEMENT_NAME);
        qValue = (Q) buildXMLObject(Q.DEFAULT_ELEMENT_NAME);
        pgenCounterValue = (PgenCounter) buildXMLObject(PgenCounter.DEFAULT_ELEMENT_NAME);
        seedValue = (Seed) buildXMLObject(Seed.DEFAULT_ELEMENT_NAME);
    }
    
    public void testAlternateValidValues() {
        DSAKeyValue keyValue = (DSAKeyValue) target;
        
        keyValue.setP(pValue);
        keyValue.setQ(qValue);
        assertValidationPass("RSAKeyValue contained P and Q, should have passed validation");
        
        keyValue.setPgenCounter(pgenCounterValue);
        keyValue.setSeed(seedValue);
        assertValidationPass("RSAKeyValue contained P and Q, PgenCounter and Seed, should have passed validation");
        
        keyValue.setP(null);
        keyValue.setQ(null);
        assertValidationPass("RSAKeyValue contained PgenCounter and Seed, should have passed validation");
    }
    
    public void testPQCombos() {
        DSAKeyValue keyValue = (DSAKeyValue) target;
        
        keyValue.setP(pValue);
        keyValue.setQ(null);
        assertValidationFail("RSAKeyValue did contained P without Q, should have failed validation");
        
        keyValue.setP(null);
        keyValue.setQ(qValue);
        assertValidationFail("RSAKeyValue did contained Q without P, should have failed validation");
    }
    
    public void testPgenCounterSeedCombos() {
        DSAKeyValue keyValue = (DSAKeyValue) target;
        
        keyValue.setPgenCounter(pgenCounterValue);
        keyValue.setSeed(null);
        assertValidationFail("RSAKeyValue did contained PgenCounter without Seed, should have failed validation");
        
        keyValue.setPgenCounter(null);
        keyValue.setSeed(seedValue);
        assertValidationFail("RSAKeyValue did contained Seed without PgenCounter, should have failed validation");
    }
}
