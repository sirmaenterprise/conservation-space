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

package org.opensaml.xml.security.credential.criteria;

import junit.framework.TestCase;

import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.criteria.KeyLengthCriteria;

/**
 *
 */
public class EvaluableKeyLengthCredentialCriteriaTest extends TestCase {
    
    private BasicCredential credential;
    private String keyAlgo;
    private Integer keyLength;
    private KeyLengthCriteria criteria;
    
    public EvaluableKeyLengthCredentialCriteriaTest() {
        keyAlgo = "AES";
        keyLength = 128;
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        credential = new BasicCredential();
        credential.setSecretKey(SecurityTestHelper.generateKey(keyAlgo, keyLength, null));
        
        criteria = new KeyLengthCriteria(keyLength);
    }
    
    public void testSatifsy() {
        EvaluableKeyLengthCredentialCriteria evalCrit = new EvaluableKeyLengthCredentialCriteria(criteria);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }

    public void testNotSatisfy() {
        criteria.setKeyLength(keyLength * 2);
        EvaluableKeyLengthCredentialCriteria evalCrit = new EvaluableKeyLengthCredentialCriteria(criteria);
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testCanNotEvaluate() {
        credential.setSecretKey(null);
        EvaluableKeyLengthCredentialCriteria evalCrit = new EvaluableKeyLengthCredentialCriteria(criteria);
        assertNull("Credential should have been unevaluable against the criteria", evalCrit.evaluate(credential));
    }
    
    public void testRegistry() throws SecurityException {
        EvaluableCredentialCriteria evalCrit = EvaluableCredentialCriteriaRegistry.getEvaluator(criteria);
        assertNotNull("Evaluable criteria was unavailable from the registry", evalCrit);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
}
