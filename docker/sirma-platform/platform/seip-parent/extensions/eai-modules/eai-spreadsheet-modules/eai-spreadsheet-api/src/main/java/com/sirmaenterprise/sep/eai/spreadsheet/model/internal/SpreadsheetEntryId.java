package com.sirmaenterprise.sep.eai.spreadsheet.model.internal;

import java.util.Objects;

import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * SpreadsheetEntryId is a uid for entry in content. Contains sheet and row identifier
 * 
 * @author bbanchev
 */
public class SpreadsheetEntryId extends ExternalInstanceIdentifier {
	private static final long serialVersionUID = 1208568842353584797L;
	private String sheetId;

	/**
	 * Instantiates new empty object
	 */
	public SpreadsheetEntryId() {
		// needed for kryo
	}

	/**
	 * Instantiates a new {@link SpreadsheetEntryId} with two
	 *
	 * @param sheetId
	 *            the source sheet id
	 * @param externalId
	 *            - some uid of instance
	 */
	public SpreadsheetEntryId(String sheetId, String externalId) {
		super(externalId);
		Objects.requireNonNull(externalId, "External identifier is a required argument to construct model instance!");
		Objects.requireNonNull(sheetId,
				"External identifier sheet is a required argument to construct model instance!");
		this.sheetId = sheetId;
	}

	/**
	 * Serialize the entry id as {@link String} that could be deserialized using {@link #deserialize(String)} method
	 * 
	 * @return the serialized {@link String}
	 */
	public String serialize() {
		return sheetId + "/" + externalId;
	}

	/**
	 * Deserialize the entry id that is serialized using {@link #serialize()} method
	 * 
	 * @param serialized
	 *            is the source string
	 * @return the deserialized {@link SpreadsheetEntryId} or throws expcetion on invalid input argument
	 */
	public static SpreadsheetEntryId deserialize(String serialized) {
		Objects.requireNonNull(serialized,
				"Desirialization source is a required argument to construct model instance!");
		String[] compositeId = serialized.split("/");
		if (compositeId.length != 2) {
			throw new EAIRuntimeException("Unsupported spread sheet entry id: " + serialized);
		}
		return new SpreadsheetEntryId(compositeId[0], compositeId[1]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((externalId == null) ? 0 : externalId.toLowerCase().hashCode());
		result = prime * result + ((sheetId == null) ? 0 : sheetId.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SpreadsheetEntryId)) {
			return false;
		}
		SpreadsheetEntryId other = (SpreadsheetEntryId) obj;
		return EqualsHelper.nullSafeEquals(sheetId, other.sheetId, true)
				&& EqualsHelper.nullSafeEquals(externalId, other.externalId, true);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Record");
		builder.append("[id=");
		builder.append(externalId);
		builder.append(", sheet=");
		builder.append(sheetId);
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * Gets the entry sheet location index
	 * 
	 * @return the sheet id
	 */
	public String getSheetId() {
		return sheetId;
	}

}
