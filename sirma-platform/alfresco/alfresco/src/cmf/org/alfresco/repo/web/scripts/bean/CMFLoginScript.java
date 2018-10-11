package org.alfresco.repo.web.scripts.bean;

import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import sun.misc.BASE64Decoder;

/**
 * The Class CMFLoginScript.
 */
public class CMFLoginScript extends AbstractLoginBean {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco
	 * .web.scripts.WebScriptRequest,
	 * org.alfresco.web.scripts.WebScriptResponse)
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) {
		try {

			// extract username and password
			JSONObject json = new JSONObject(req.getContent().getContent());
			if (!json.has("username")) {
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
						"Username not specified");
			}
			String username = json.getString("username");
			if (username.isEmpty()) {
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
						"Username not specified");
			}
			if (!json.has("password")) {
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
						"Password not specified");
			}
			String password = json.getString("password");
			if (password.isEmpty()) {
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
						"Password not specified");
			}
			String passDecrypted = decrypt(password);
			return login(username, passDecrypted);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Decrypt.
	 * 
	 * @param encryptedPwd
	 *            the encrypted pwd
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	public String decrypt(String encryptedPwd) throws Exception {
		// only the first 8 Bytes of the constructor argument are used
		// as material for generating the keySpec
		DESKeySpec keySpec = new DESKeySpec("AlfrescoCMFLogin@T1st".getBytes("UTF8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(keySpec);
		BASE64Decoder base64decoder = new BASE64Decoder();

		Cipher cipher = Cipher.getInstance("DES");// cipher is not thread safe
		cipher.init(Cipher.DECRYPT_MODE, key);
		// DECODE encryptedPwd String
		byte[] encrypedPwdBytes = base64decoder.decodeBuffer(encryptedPwd);

		byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
		return new String(plainTextPwdBytes);
	}
}
