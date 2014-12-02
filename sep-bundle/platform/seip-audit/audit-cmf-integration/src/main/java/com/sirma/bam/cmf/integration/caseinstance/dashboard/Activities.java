package com.sirma.bam.cmf.integration.caseinstance.dashboard;

/**
 * This class represent recent activity. Will holds parent and root elements.
 * 
 * @author cdimitrov
 */
public class Activities extends ActivityDetails{
	
	/** The activity parent details. */
	private ParentActivity parent;
	
	/** The activity root details. */
	private RootActivity root;

	/**
	 * Class representing the root details for the activity.
	 * @author cdimitrov
	 */
	private class RootActivity extends ActivityDetails{
		/**
		 * Default root activity constructor.
		 * 
		 * @param rootTitle root activity title
		 * @param rootIconPath root activity icon path
		 */
		public RootActivity(String rootTitle, String rootIconPath) {
			setName(rootTitle);
			setIconPath(rootIconPath);
		}
	}
	
	/**
	 * Class represent the parent details for the activity.
	 * @author cdimitrov
	 *
	 */
	private class ParentActivity extends ActivityDetails{
		/**
		 * Default parent activity constructor.
		 * 
		 * @param parentTitle parent activity title
		 * @param parentIconPath parent activity icon path
		 */
		public ParentActivity(String parentTitle, String parentIconPath) {
			setName(parentTitle);
			setIconPath(parentIconPath);
		}
	}

	/**
	 * This method create parent details for the activity.
	 * 
	 * @param activityName parent activity name
	 * @param activityIcon parent activity icon path
	 */
	public void createParentActivity(String activityName, String activityIcon) {
		parent = new ParentActivity(activityName, activityIcon);
	}
	
	/**
	 * This method create root details for the activity.
	 * 
	 * @param activityName root activity name
	 * @param activityIcon root activity icon path
	 */
	public void createRootActivity(String activityName, String activityIcon) {
		root = new RootActivity(activityName, activityIcon);
	}

	/**
	 * Getter for parent activity details.
	 * 
	 * @return parent activity
	 */
	public ParentActivity getParent() {
		return parent;
	}

	/**
	 * Setter for parent activity details.
	 * 
	 * @param parent parent activity details.
	 */
	public void setParent(ParentActivity parent) {
		this.parent = parent;
	}

	/**
	 * Getter for root activity details.
	 * 
	 * @return root activity details
	 */
	public RootActivity getRoot() {
		return root;
	}

	/**
	 * Setter for root activity details.
	 * 
	 * @param root activity details
	 */
	public void setRoot(RootActivity root) {
		this.root = root;
	}
	
}
