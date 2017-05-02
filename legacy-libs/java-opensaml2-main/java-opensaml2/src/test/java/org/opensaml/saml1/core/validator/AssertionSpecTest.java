/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.saml1.core.validator;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.AttributeStatement;
import org.opensaml.saml1.core.Condition;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.saml1.core.DoNotCacheCondition;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.AssertionSchemaValidator}.
 */
public class AssertionSpecTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AssertionSpecTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new AssertionSpecValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        Assertion assertion = (Assertion) target;
        assertion.setIssuer("Issuer");
        assertion.setID("ident");
        assertion.setIssueInstant(new DateTime());
        QName name = new QName(SAMLConstants.SAML1_NS, AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        assertion.getStatements().add((AttributeStatement)buildXMLObject(name));
    }
    
    public void testDoNotCache() {
        Assertion assertion = (Assertion) target;
        QName oqname = new QName(SAMLConstants.SAML1_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        Conditions conditions = (Conditions) buildXMLObject(oqname);
        assertion.setConditions(conditions);
        oqname = new QName(SAMLConstants.SAML1_NS, DoNotCacheCondition.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        conditions.getConditions().add((Condition) buildXMLObject(oqname));
        assertValidationPass("DoNotCache allowed in SAML 1.1");
        assertion.setVersion(SAMLVersion.VERSION_10);
        assertValidationFail("DoNotCache not allowed in SAML 1.0");
    }
}