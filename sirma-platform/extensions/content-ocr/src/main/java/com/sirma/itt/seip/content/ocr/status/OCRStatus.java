package com.sirma.itt.seip.content.ocr.status;

/**
 * The Enum OCRStatus is used to determine the OCR Engine process status.
 * 
 * @author Hristo Lungov
 */
public enum OCRStatus {

	SKIPPED("Skipped"), COMPLETED("Completed"), NOT_STARTED("Not Started"), FAILED("Failed"), EXCLUDED("Excluded");

	private String status;

	/**
	 * Constructs new enum entry.
	 *
	 * @param status
	 *            is the text
	 */
	private OCRStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return status;
	}
}
