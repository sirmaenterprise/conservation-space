package com.sirma.itt.seip.instance.content.share;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.domain.event.OperationEvent;

/**
 * Event thrown to trigger the generation of content for instances that were selected to be shared publicly.
 * <br />
 * When a schedule API task for sharing a specific instance is created it is done so in such way that it will be
 * triggered later by throwing this event. See {@link BaseShareInstanceContentAction} for more information.
 */
public class ShareInstanceContentEvent implements OperationEvent {

	private final String sharedCode;

	/**
	 * Constructs a new {@link ShareInstanceContentEvent}.
	 *
	 * @param sharedCode
	 * 		the generated ShareCode, see {@link com.sirma.itt.seip.shared.ShareCode}.
	 */
	public ShareInstanceContentEvent(String sharedCode) {
		if (StringUtils.isBlank(sharedCode)) {
			throw new IllegalArgumentException("Shared code cannot be null or empty.");
		}
		this.sharedCode = sharedCode;
	}

	public String getSharedCode() {
		return sharedCode;
	}

	@Override
	public String getOperationId() {
		return "shareContent" + getSharedCode();
	}
}
