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

package org.opensaml.xml.security;

import java.io.ByteArrayInputStream;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.xml.security.Init;
import org.apache.xml.security.algorithms.JCEMapper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoProvider;
import org.opensaml.xml.security.keyinfo.provider.DSAKeyValueProvider;
import org.opensaml.xml.security.keyinfo.provider.InlineX509DataProvider;
import org.opensaml.xml.security.keyinfo.provider.RSAKeyValueProvider;
import org.opensaml.xml.util.Base64;

/**
 * Some utility methods for doing security, credential, key and crypto-related tests.
 */
public final class SecurityTestHelper {
    
    /** Constructor. */
    private SecurityTestHelper() { }
    
    /**
     * Build Java certificate from base64 encoding.
     * 
     * @param base64Cert base64-encoded certificate
     * @return a native Java X509 certificate
     * @throws CertificateException thrown if there is an error constructing certificate
     */
    public static java.security.cert.X509Certificate buildJavaX509Cert(String base64Cert) throws CertificateException {
        CertificateFactory  cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream input = new ByteArrayInputStream(Base64.decode(base64Cert));
        return (java.security.cert.X509Certificate) cf.generateCertificate(input);
    }
    
    /**
     * Build Java CRL from base64 encoding.
     * 
     * @param base64CRL base64-encoded CRL
     * @return a native Java X509 CRL
     * @throws CertificateException thrown if there is an error constructing certificate
     * @throws CRLException  thrown if there is an error constructing CRL
     */
    public static java.security.cert.X509CRL buildJavaX509CRL(String base64CRL)
            throws CertificateException, CRLException {
        CertificateFactory  cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream input = new ByteArrayInputStream(Base64.decode(base64CRL));
        return (java.security.cert.X509CRL) cf.generateCRL(input);
    }
    
    /**
     * Build Java DSA public key from base64 encoding.
     * 
     * @param base64EncodedKey base64-encoded DSA public key
     * @return a native Java DSAPublicKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static DSAPublicKey buildJavaDSAPublicKey(String base64EncodedKey) throws KeyException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(base64EncodedKey));
        return (DSAPublicKey) buildKey(keySpec, "DSA");
    }
    
    /**
     * Build Java RSA public key from base64 encoding.
     * 
     * @param base64EncodedKey base64-encoded RSA public key
     * @return a native Java RSAPublicKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static RSAPublicKey buildJavaRSAPublicKey(String base64EncodedKey) throws KeyException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(base64EncodedKey));
        return (RSAPublicKey) buildKey(keySpec, "RSA");
    }
    
    /**
     * Build Java RSA private key from base64 encoding.
     * 
     * @param base64EncodedKey base64-encoded RSA private key
     * @return a native Java RSAPrivateKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static RSAPrivateKey buildJavaRSAPrivateKey(String base64EncodedKey)  throws KeyException {
        PrivateKey key =  buildJavaPrivateKey(base64EncodedKey);
        if (! (key instanceof RSAPrivateKey)) {
            throw new KeyException("Generated key was not an RSAPrivateKey instance");
        }
        return (RSAPrivateKey) key;
    }
    
    /**
     * Build Java DSA private key from base64 encoding.
     * 
     * @param base64EncodedKey base64-encoded DSA private key
     * @return a native Java DSAPrivateKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static DSAPrivateKey buildJavaDSAPrivateKey(String base64EncodedKey)  throws KeyException {
        PrivateKey key =  buildJavaPrivateKey(base64EncodedKey);
        if (! (key instanceof DSAPrivateKey)) {
            throw new KeyException("Generated key was not a DSAPrivateKey instance");
        }
        return (DSAPrivateKey) key;
    }
    
    /**
     * Build Java private key from base64 encoding. The key should have no password.
     * 
     * @param base64EncodedKey base64-encoded private key
     * @return a native Java PrivateKey
     * @throws KeyException thrown if there is an error constructing key
     */
    public static PrivateKey buildJavaPrivateKey(String base64EncodedKey)  throws KeyException {
        return SecurityHelper.decodePrivateKey(Base64.decode(base64EncodedKey), null);
    }
    
    /**
     * Generates a public key from the given key spec.
     * 
     * @param keySpec {@link KeySpec} specification for the key
     * @param keyAlgorithm key generation algorithm, only DSA and RSA supported
     * 
     * @return the generated {@link PublicKey}
     * 
     * @throws KeyException thrown if the key algorithm is not supported by the JCE or the key spec does not
     *             contain valid information
     */
    public static PublicKey buildKey(KeySpec keySpec, String keyAlgorithm) throws KeyException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyException(keyAlgorithm + "algorithm is not supported by the JCE", e);
        } catch (InvalidKeySpecException e) {
            throw new KeyException("Invalid key information", e);
        }
    }
    
    /**
     * Randomly generates a Java JCE symmetric Key object from the specified XML Encryption algorithm URI.
     * 
     * @param algoURI  The XML Encryption algorithm URI
     * @return a randomly-generated symmteric key
     * @throws NoSuchProviderException  provider not found
     * @throws NoSuchAlgorithmException algorithm not found
     */
    public static SecretKey generateKeyFromURI(String algoURI) 
            throws NoSuchAlgorithmException, NoSuchProviderException {
        String jceAlgorithmName = JCEMapper.getJCEKeyAlgorithmFromURI(algoURI);
        int keyLength = JCEMapper.getKeyLengthFromURI(algoURI);
        return generateKey(jceAlgorithmName, keyLength, null);
    }
    
    /**
     * Randomly generates a Java JCE KeyPair object from the specified XML Encryption algorithm URI.
     * 
     * @param algoURI  The XML Encryption algorithm URI
     * @param keyLength  the length of key to generate
     * @return a randomly-generated KeyPair
     * @throws NoSuchProviderException  provider not found
     * @throws NoSuchAlgorithmException  algorithm not found
     */
    public static KeyPair generateKeyPairFromURI(String algoURI, int keyLength) 
            throws NoSuchAlgorithmException, NoSuchProviderException {
        String jceAlgorithmName = JCEMapper.getJCEKeyAlgorithmFromURI(algoURI);
        return generateKeyPair(jceAlgorithmName, keyLength, null);
    }
    
    /**
     * Generate a random symmetric key.
     * 
     * @param algo key algorithm
     * @param keyLength key length
     * @param provider JCA provider
     * @return randomly generated symmetric key
     * @throws NoSuchAlgorithmException algorithm not found
     * @throws NoSuchProviderException provider not found
     */
    public static SecretKey generateKey(String algo, int keyLength, String provider) 
            throws NoSuchAlgorithmException, NoSuchProviderException {
        SecretKey key = null;
        KeyGenerator keyGenerator = null;
        if (provider != null) {
            keyGenerator = KeyGenerator.getInstance(algo, provider);
        } else {
            keyGenerator = KeyGenerator.getInstance(algo);
        }
        keyGenerator.init(keyLength);
        key = keyGenerator.generateKey();
        return key;
    }
    
    /**
     * Generate a random asymmetric key pair.
     * 
     * @param algo key algorithm
     * @param keyLength key length
     * @param provider JCA provider
     * @return randomly generated key
     * @throws NoSuchAlgorithmException algorithm not found
     * @throws NoSuchProviderException provider not found
     */
    public static KeyPair generateKeyPair(String algo, int keyLength, String provider) 
            throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGenerator = null;
        if (provider != null) {
            keyGenerator = KeyPairGenerator.getInstance(algo, provider);
        } else {
            keyGenerator = KeyPairGenerator.getInstance(algo);
        }
        keyGenerator.initialize(keyLength);
        return keyGenerator.generateKeyPair();
    }
    
    /**
     * Generate a random symmetric key and return in a BasicCredential.
     * 
     * @param algorithmURI The XML Encryption algorithm URI
     * @return a basic credential containing a randomly generated symmetric key
     * @throws NoSuchAlgorithmException algorithm not found
     * @throws NoSuchProviderException provider not found
     */
    public static Credential generateKeyAndCredential(String algorithmURI) 
            throws NoSuchAlgorithmException, NoSuchProviderException {
        SecretKey key = SecurityTestHelper.generateKeyFromURI(algorithmURI);
        BasicCredential credential = new BasicCredential();
        credential.setSecretKey(key);
        return credential;
    }
    
    /**
     * Generate a random asymmetric key pair and return in a BasicCredential.
     * 
     * @param algorithmURI The XML Encryption algorithm URI
     * @param keyLength key length
     * @param includePrivate if true, the private key will be included as well
     * @return a basic credential containing a randomly generated asymmetric key pair
     * @throws NoSuchAlgorithmException algorithm not found
     * @throws NoSuchProviderException provider not found
     */
    public static Credential generateKeyPairAndCredential(String algorithmURI, int keyLength, boolean includePrivate) 
            throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = SecurityTestHelper.generateKeyPairFromURI(algorithmURI, keyLength);
        BasicCredential credential = new BasicCredential();
        credential.setPublicKey(keyPair.getPublic());
        if (includePrivate) {
            credential.setPrivateKey(keyPair.getPrivate());
        }
        return credential;
    }
    
    /**
     * Get a basic KeyInfo credential resolver which can process standard inline
     * data - RSAKeyValue, DSAKeyValue, X509Data.
     * 
     * @return a new KeyInfoCredentialResolver instance
     */
    public static KeyInfoCredentialResolver buildBasicInlineKeyInfoResolver() {
        List<KeyInfoProvider> providers = new ArrayList<KeyInfoProvider>();
        providers.add( new RSAKeyValueProvider() );
        providers.add( new DSAKeyValueProvider() );
        providers.add( new InlineX509DataProvider() );
        return new BasicProviderKeyInfoCredentialResolver(providers);
    }
    
    static {
        // We use some Apache XML Security utility functions, so need to make sure library
        // is initialized.
        if (!Init.isInitialized()) {
            Init.init();
        }
    }
}
