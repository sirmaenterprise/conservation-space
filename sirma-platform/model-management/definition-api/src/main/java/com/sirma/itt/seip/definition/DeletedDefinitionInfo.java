package com.sirma.itt.seip.definition;

import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Response object returned from service for deleted definition information
 *
 * @author BBonev
 */
public class DeletedDefinitionInfo implements JsonRepresentable {

	/** The Constant EMPTY_INFO. */
	public static final DeletedDefinitionInfo EMPTY_INFO = new DeletedDefinitionInfo(null, null, null);

	private final Object definitionType;
	private final String definitionId;
	private final Long definitionRevision;

	/**
	 * Instantiates a new deleted definition info.
	 *
	 * @param definitionType
	 *            the definition type
	 * @param definitionId
	 *            the definition id
	 * @param definitionRevision
	 *            the definition revision
	 */
	public DeletedDefinitionInfo(Object definitionType, String definitionId, Long definitionRevision) {
		this.definitionType = definitionType;
		this.definitionId = definitionId;
		this.definitionRevision = definitionRevision;
	}

	/**
	 * Getter method for definitionType.
	 *
	 * @return the definitionType
	 */
	public Object getDefinitionType() {
		return definitionType;
	}

	/**
	 * Getter method for definitionId.
	 *
	 * @return the definitionId
	 */
	public String getDefinitionId() {
		return definitionId;
	}

	/**
	 * Getter method for definitionRevision.
	 *
	 * @return the definitionRevision
	 */
	public Long getDefinitionRevision() {
		return definitionRevision;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		builder
				.append("DefinitionInfo[id=")
					.append(definitionId)
					.append(", revision=")
					.append(definitionRevision)
					.append(", type=")
					.append(definitionType)
					.append("]");
		return builder.toString();
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "definitionId", definitionId);
		JsonUtil.addToJson(object, "definitionRevision", definitionRevision);
		if (definitionType != null) {
			JsonUtil.addToJson(object, "definitionType", definitionType.toString());
		}
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// implement me!

	}
}
