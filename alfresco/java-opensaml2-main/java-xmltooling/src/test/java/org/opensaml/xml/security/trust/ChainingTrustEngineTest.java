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

package org.opensaml.xml.security.trust;

import junit.framework.TestCase;

import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.criteria.EntityIDCriteria;

/**
 * Test the chaining trust engine.
 */
public class ChainingTrustEngineTest extends TestCase {
    
    private CriteriaSet criteriaSet;
    
    private ChainingTrustEngine<FooToken> engine;
    
    private FooToken token;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        
        token = new FooToken();
        
        engine = new ChainingTrustEngine<FooToken>();
        
        criteriaSet = new CriteriaSet();
        criteriaSet.add( new EntityIDCriteria("dummyEntityID") );
    }
    
    public void testFirstTrusted() throws SecurityException {
        engine.getChain().add( new FooEngine(Boolean.TRUE));
        engine.getChain().add( new FooEngine(Boolean.FALSE));
        assertTrue("Engine # 1 evaled token as trusted", engine.validate(token, criteriaSet));
    }

    public void testSecondTrusted() throws SecurityException {
        engine.getChain().add( new FooEngine(Boolean.FALSE));
        engine.getChain().add( new FooEngine(Boolean.TRUE));
        assertTrue("Engine # 2 evaled token as trusted", engine.validate(token, criteriaSet));
    }
    
    public void testNoneTrusted() throws SecurityException {
        engine.getChain().add( new FooEngine(Boolean.FALSE));
        engine.getChain().add( new FooEngine(Boolean.FALSE));
        assertFalse("No engine evaled token as trusted", engine.validate(token, criteriaSet));
    }
    
    public void testException() {
        engine.getChain().add( new FooEngine(Boolean.FALSE));
        engine.getChain().add( new FooEngine(null));
        try {
            engine.validate(token, criteriaSet);
            fail("Should have thrown security exception");
        } catch (SecurityException e) {
            // do nothing, expected
        }
    }
    
    /** Mock token type. */
    private class FooToken {
        
    }
    
    /** Mock trust engine for FooToken. */
    private class FooEngine implements TrustEngine<FooToken> {
        
        private Boolean trusted;
        
        private FooEngine(Boolean trusted) {
            this.trusted = trusted;
        }

        /** {@inheritDoc} */
        public boolean validate(FooToken token, CriteriaSet trustBasisCriteria) throws SecurityException {
            if (trusted == null) {
                throw new SecurityException("This means an error happened");
            }
            return trusted;
        }
        
    }

}
