package com.sirma.cmf.web.form.control;

/**
 * The Interface ChecklistControl.
 * 
 * @author svelikov
 */
public interface ChecklistControl {

	/**
	 * The Enum ChecklistParmeter.
	 */
	public enum ChecklistParmeter {

		/** The style class. */
		ALLOW_CHANGE("allowChange");

		/** The ui param. */
		private String controlParam;

		/**
		 * @param controlParam
		 *            the ui param
		 */
		private ChecklistParmeter(String controlParam) {
			this.controlParam = controlParam;
		}

		/**
		 * Getter method for controlParam.
		 * 
		 * @return the controlParam
		 */
		public String getControlParam() {
			return controlParam;
		}

		/**
		 * Setter method for controlParam.
		 * 
		 * @param controlParam
		 *            the controlParam to set
		 */
		public void setControlParam(String controlParam) {
			this.controlParam = controlParam;
		}

		/**
		 * Gets the param enum.
		 * 
		 * @param requestedParam
		 *            the requested param
		 * @return the param enum
		 */
		public static ChecklistParmeter getParamEnum(String requestedParam) {
			ChecklistParmeter[] params = values();
			for (ChecklistParmeter param : params) {
				if (param.name().equals(requestedParam)) {
					return param;
				}
			}

			return null;
		}

	}

	/**
	 * The Enum ChecklistUIParameter.
	 */
	public enum ChecklistUIParameter {

		/** The style class. */
		STYLE_CLASS("styleClass"),

		/** The style. */
		STYLE("style");

		/** The ui param. */
		private String uiParam;

		/**
		 * Instantiates a new data table ui parameter.
		 * 
		 * @param uiParam
		 *            the ui param
		 */
		private ChecklistUIParameter(String uiParam) {
			this.uiParam = uiParam;
		}

		/**
		 * Getter method for uiParam.
		 * 
		 * @return the uiParam
		 */
		public String getUiParam() {
			return uiParam;
		}

		/**
		 * Setter method for uiParam.
		 * 
		 * @param uiParam
		 *            the uiParam to set
		 */
		public void setUiParam(String uiParam) {
			this.uiParam = uiParam;
		}

	}

}
