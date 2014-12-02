package org.opensaml.xml.security.trust;

import org.opensaml.xml.security.credential.CredentialResolver;

/**
 * Evaluates the trustworthiness and validity of a token against 
 * implementation-specific requirements based on trusted credentials
 * obtained via a credential resolver.
 *
 * @param <TokenType> the token type this trust engine evaluates
 */
public interface TrustedCredentialTrustEngine<TokenType> extends TrustEngine<TokenType> {

    /**
     * Gets the credential resolver used to recover trusted credentials that 
     * may be used to validate tokens.
     *
     * @return credential resolver used to recover trusted credentials 
     *         that may be used to validate tokens
     */
    public CredentialResolver getCredentialResolver();
}
