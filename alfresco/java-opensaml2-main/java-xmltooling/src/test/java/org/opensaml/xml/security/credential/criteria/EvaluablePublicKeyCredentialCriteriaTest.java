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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;

import junit.framework.TestCase;

import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.criteria.PublicKeyCriteria;

/**
 *
 */
public class EvaluablePublicKeyCredentialCriteriaTest extends TestCase {
    
    private BasicCredential credential;
    private String keyAlgo;
    PublicKey pubKey;
    private PublicKeyCriteria criteria;
    
    public EvaluablePublicKeyCredentialCriteriaTest() {
        keyAlgo = "RSA";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        credential = new BasicCredential();
        pubKey = SecurityTestHelper.generateKeyPair(keyAlgo, 1024, null).getPublic();
        credential.setPublicKey(pubKey);
        
        criteria = new PublicKeyCriteria(pubKey);
    }
    
    public void testSatifsy() {
        EvaluablePublicKeyCredentialCriteria evalCrit = new EvaluablePublicKeyCredentialCriteria(criteria);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }

    public void testNotSatisfyDifferentKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        criteria.setPublicKey(SecurityTestHelper.generateKeyPair(keyAlgo, 1024, null).getPublic());
        EvaluablePublicKeyCredentialCriteria evalCrit = new EvaluablePublicKeyCredentialCriteria(criteria);
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testNotSatisfyNoPublicKey() {
        credential.setPublicKey(null);
        EvaluablePublicKeyCredentialCriteria evalCrit = new EvaluablePublicKeyCredentialCriteria(criteria);
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testCanNotEvaluate() {
        //Only unevaluable case is null credential
        EvaluablePublicKeyCredentialCriteria evalCrit = new EvaluablePublicKeyCredentialCriteria(criteria);
        assertNull("Credential should have been unevaluable against the criteria", evalCrit.evaluate(null));
    }
    
    public void testRegistry() throws SecurityException {
        EvaluableCredentialCriteria evalCrit = EvaluableCredentialCriteriaRegistry.getEvaluator(criteria);
        assertNotNull("Evaluable criteria was unavailable from the registry", evalCrit);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
}
