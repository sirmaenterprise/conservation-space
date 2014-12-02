package org.opensaml.xml.security.trust;

import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;

/**
 * Evaluates the trustworthiness and validity of a token against 
 * implementation-specific requirements.
 *
 * @param <TokenType> the token type this trust engine evaluates
 */
public interface TrustEngine<TokenType> {
    
    /**
     * Validates the token against trusted information obtained in an
     * implementation-specific manner.
     *
     * @param token security token to validate
     * @param trustBasisCriteria criteria used to describe and/or resolve the information
     *          which serves as the basis for trust evaluation
     *
     * @return true if the token is trusted and valid, false if not
     *
     * @throws SecurityException thrown if there is a problem validating the security token
     */
    public boolean validate(TokenType token, CriteriaSet trustBasisCriteria) throws SecurityException;
}
