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

package org.opensaml.xml.schema.validator;

import org.opensaml.xml.BaseXMLObjectValidatorTestCase;
import org.opensaml.xml.schema.XSInteger;

/**
 * Tests XSStringSchemaValidator.
 */
public class XSIntegerSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    /**
     * Constructor.
     *
     */
    public XSIntegerSchemaValidatorTest() {
        targetQName = XSInteger.TYPE_NAME;
        // Default is to not allow empty content.
        validator = new XSIntegerSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        XSInteger xsInteger = (XSInteger) target;
        xsInteger.setValue(new Integer(42));
    }
    
    /**
     *  Test empty content.
     */
    public void testEmpty() {
        XSInteger xsInteger = (XSInteger) target;
        
        xsInteger.setValue(null);
        assertValidationFail("Content was null, should raise a Validation Exception");
    }

    /**
     *  Test empty content, with empty allowed.
     */
    public void testEmptyAllowed() {
        // Default is to not allow empty content, override here.
        validator = new XSIntegerSchemaValidator(true);
        XSInteger xsInteger = (XSInteger) target;
        
        xsInteger.setValue(null);
        assertValidationPass("Content was null, validator configured to allow empty content");
    }
}
