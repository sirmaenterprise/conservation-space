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
import org.opensaml.xml.schema.XSString;

/**
 * Tests XSStringSchemaValidator.
 */
public class XSStringSchemaValidatorTest extends BaseXMLObjectValidatorTestCase {
    
    /**
     * Constructor.
     *
     */
    public XSStringSchemaValidatorTest() {
        targetQName = XSString.TYPE_NAME;
        // Default is to not allow empty content.
        validator = new XSStringSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        XSString xsString = (XSString) target;
        xsString.setValue("This is some data");
    }
    
    /**
     *  Test empty content.
     */
    public void testEmpty() {
        XSString xsString = (XSString) target;
        
        xsString.setValue(null);
        assertValidationFail("Content was null, should raise a Validation Exception");
        
        xsString.setValue("");
        assertValidationFail("Content was empty, should raise a Validation Exception");
        
        xsString.setValue("   ");
        assertValidationFail("Content was all whitespace, should raise a Validation Exception");
    }

    /**
     *  Test empty content, with empty allowed.
     */
    public void testEmptyAllowed() {
        // Default is to not allow empty content, override here.
        validator = new XSStringSchemaValidator(true);
        XSString xsString = (XSString) target;
        
        xsString.setValue(null);
        assertValidationPass("Content was null, validator configured to allow empty content");
        
        xsString.setValue("");
        assertValidationPass("Content was empty, validator configured to allow empty content");
        
        xsString.setValue("      ");
        assertValidationPass("Content was all whitespace, validator configured to allow empty content");
    }
}
