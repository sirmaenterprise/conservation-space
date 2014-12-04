package org.activiti.engine.impl.util.json;

// TODO: Auto-generated Javadoc
/**
 * The JSONException is thrown by the JSON.org classes when things are amiss.
 * @author JSON.org
 * @version 2008-09-18
 */
public class JSONException extends RuntimeException {
    
    /** The Constant serialVersionUID. */
	private static final long serialVersionUID = 0;
	
	/** The cause. */
	private Throwable cause;

    /**
     * Constructs a JSONException with an explanatory message.
     * @param message Detail about the reason for the exception.
     */
    public JSONException(String message) {
        super(message);
    }

    /**
     * Instantiates a new jSON exception.
     *
     * @param t the t
     */
    public JSONException(Throwable t) {
        super(t.getMessage());
        this.cause = t;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getCause()
     */
    public Throwable getCause() {
        return this.cause;
    }
}
