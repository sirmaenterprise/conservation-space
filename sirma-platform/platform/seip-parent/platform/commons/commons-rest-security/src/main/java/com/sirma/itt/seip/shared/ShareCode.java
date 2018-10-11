package com.sirma.itt.seip.shared;

import java.util.Date;

/**
 * Represents a share code. The sharecode is used as a security precaution to verify that only a
 * specific combination of resource and user are being accessed from the shared link (e.g. you can't
 * access a resource with an invalid sharecode). To construct/deconstruct one see
 * {@link ShareCodeUtils}.
 * 
 * @author nvelkov
 */
public class ShareCode {

	private String user;
	private Date generationTime;
	private String signature;

	/**
	 * Instantiate a {@link ShareCode} instance.
	 * 
	 * @param user
	 *            the user
	 * @param generationTime
	 *            the generation time
	 * @param signature
	 *            the signature
	 */
	public ShareCode(String user, Date generationTime, String signature) {
		super();
		this.user = user;
		this.generationTime = generationTime;
		this.signature = signature;
	}

	public String getUser() {
		return user;
	}

	public Date getGenerationTime() {
		return generationTime;
	}

	public String getSignature() {
		return signature;
	}

}
