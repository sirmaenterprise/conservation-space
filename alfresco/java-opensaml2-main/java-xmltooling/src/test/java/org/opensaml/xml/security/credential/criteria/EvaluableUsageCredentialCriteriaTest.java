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
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;

/**
 *
 */
public class EvaluableUsageCredentialCriteriaTest extends TestCase {
    
    private BasicCredential credential;
    private UsageType usage;
    private UsageCriteria criteria;
    
    public EvaluableUsageCredentialCriteriaTest() {
        usage = UsageType.SIGNING;
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        credential = new BasicCredential();
        credential.setUsageType(usage);
        
        criteria = new UsageCriteria(usage);
    }
    
    public void testSatifsyExactMatch() {
        EvaluableUsageCredentialCriteria evalCrit = new EvaluableUsageCredentialCriteria(criteria);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testSatisfyWithUnspecifiedCriteria() {
        criteria.setUsage(UsageType.UNSPECIFIED);
        EvaluableUsageCredentialCriteria evalCrit = new EvaluableUsageCredentialCriteria(criteria);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testSatisfyWithUnspecifiedCredential() {
        credential.setUsageType(UsageType.UNSPECIFIED);
        EvaluableUsageCredentialCriteria evalCrit = new EvaluableUsageCredentialCriteria(criteria);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }

    public void testNotSatisfy() {
        criteria.setUsage(UsageType.ENCRYPTION);
        EvaluableUsageCredentialCriteria evalCrit = new EvaluableUsageCredentialCriteria(criteria);
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    /* With BasicCredential, can't set UsageType to null, so can't really test.
    public void testCanNotEvaluate() {
        credential.setUsageType(null);
        EvaluableUsageCredentialCriteria evalCrit = new EvaluableUsageCredentialCriteria(criteria);
        assertNull("Credential should have been unevaluable against the criteria", evalCrit.evaluate(credential));
    }
    */
    
    public void testRegistry() throws SecurityException {
        EvaluableCredentialCriteria evalCrit = EvaluableCredentialCriteriaRegistry.getEvaluator(criteria);
        assertNotNull("Evaluable criteria was unavailable from the registry", evalCrit);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
}
