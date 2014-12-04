package org.activiti.engine;


// TODO: Auto-generated Javadoc
/**
 * This exception is thrown when you try to claim a task that is already claimed
 * by someone else.
 * 
 * @author Jorg Heymans
 * @author Falko Menge 
 */
public class ActivitiTaskAlreadyClaimedException extends ActivitiException {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** the id of the task that is already claimed. */
    private String taskId;
    
    /** the assignee of the task that is already claimed. */
    private String taskAssignee;
    
    /**
     * Instantiates a new activiti task already claimed exception.
     *
     * @param taskId the task id
     * @param taskAssignee the task assignee
     */
    public ActivitiTaskAlreadyClaimedException(String taskId, String taskAssignee) {
        super("Task '" + taskId + "' is already claimed by someone else.");
        this.taskId = taskId;
        this.taskAssignee = taskAssignee;
    }
    
    /**
     * Gets the task id.
     *
     * @return the task id
     */
    public String getTaskId() {
        return this.taskId;
    }

    /**
     * Gets the task assignee.
     *
     * @return the task assignee
     */
    public String getTaskAssignee(){
        return this.taskAssignee;
    }

}
