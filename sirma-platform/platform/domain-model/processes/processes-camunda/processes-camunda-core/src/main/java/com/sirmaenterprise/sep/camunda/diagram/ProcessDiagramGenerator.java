package com.sirmaenterprise.sep.camunda.diagram;

import java.awt.Rectangle;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.Lane;
import org.camunda.bpm.engine.impl.pvm.process.LaneSet;
import org.camunda.bpm.engine.impl.pvm.process.ParticipantProcess;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * Class to generate an image based the diagram interchange information in a BPMN 2.0 process.
 * 
 * @author Joram Barrez
 */
public class ProcessDiagramGenerator {

	protected static final Map<String, ActivityDrawInstruction> activityDrawInstructions = new HashMap<>();

	// The instructions on how to draw a certain construct is
	// created statically and stored in a map for performance.
	static {
		// start event
		activityDrawInstructions.put("startEvent", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawNoneStartEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// start timer event
		activityDrawInstructions.put("startTimerEvent", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawTimerStartEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// signal catch
		activityDrawInstructions.put("intermediateSignalCatch", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawCatchingSignalEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// signal throw
		activityDrawInstructions.put("intermediateSignalThrow", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawThrowingSignalEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// end event
		activityDrawInstructions.put("endEvent", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawNoneEndEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// noneEndEvent
		activityDrawInstructions.put("noneEndEvent", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawNoneEndEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// error end event
		activityDrawInstructions.put("errorEndEvent", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawErrorEndEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// error start event
		activityDrawInstructions.put("errorStartEvent", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawErrorStartEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// task
		activityDrawInstructions.put("task", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
						activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// user task
		activityDrawInstructions.put("userTask", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawUserTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
						activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// script task
		activityDrawInstructions.put("scriptTask", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawScriptTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
						activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// service task
		activityDrawInstructions.put("serviceTask", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawServiceTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
						activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// receive task
		activityDrawInstructions.put("receiveTask", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawReceiveTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
						activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// send task
		activityDrawInstructions.put("sendTask", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawSendTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
						activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// manual task
		activityDrawInstructions.put("manualTask", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawManualTask((String) activityImpl.getProperty("name"), activityImpl.getX(),
						activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// businessRuleTask task
		activityDrawInstructions.put("businessRuleTask", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawBusinessRuleTask((String) activityImpl.getProperty("name"),
						activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// exclusive gateway
		activityDrawInstructions.put("exclusiveGateway", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawExclusiveGateway(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// inclusive gateway
		activityDrawInstructions.put("inclusiveGateway", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawInclusiveGateway(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// parallel gateway
		activityDrawInstructions.put("parallelGateway", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawParallelGateway(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// Boundary timer
		activityDrawInstructions.put("boundaryTimer", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawCatchingTimerEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// Boundary catch error
		activityDrawInstructions.put("boundaryError", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawCatchingErroEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// Boundary signal event
		activityDrawInstructions.put("boundarySignal", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawCatchingSignalEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// timer catch event
		activityDrawInstructions.put("intermediateTimer", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawCatchingTimerEvent(activityImpl.getX(), activityImpl.getY(),
						activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

		// subprocess
		activityDrawInstructions.put("subProcess", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				Boolean isExpanded = (Boolean) activityImpl.getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);
				Boolean isTriggeredByEvent = (Boolean) activityImpl.getProperty("triggeredByEvent");
				if (isTriggeredByEvent == null) {
					isTriggeredByEvent = Boolean.TRUE;
				}
				if (isExpanded != null && isExpanded == Boolean.FALSE) {
					processDiagramCreator.drawCollapsedSubProcess((String) activityImpl.getProperty("name"),
							activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(),
							activityImpl.getHeight());
				} else {
					processDiagramCreator.drawExpandedSubProcess((String) activityImpl.getProperty("name"),
							activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight(),
							isTriggeredByEvent);
				}
			}
		});

		// call activity
		activityDrawInstructions.put("callActivity", new ActivityDrawInstruction() {

			@Override
			public void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl) {
				processDiagramCreator.drawCollapsedCallActivity((String) activityImpl.getProperty("name"),
						activityImpl.getX(), activityImpl.getY(), activityImpl.getWidth(), activityImpl.getHeight());
			}
		});

	}

	/**
	 * Generates a PNG diagram image of the given process definition, using the diagram interchange information of the
	 * process.
	 * 
	 * @param processDefinition
	 *            the definition that will be used to generate the diagram.
	 * @return the input stream of the image.
	 */
	public static InputStream generatePngDiagram(ProcessDefinitionEntity processDefinition) {
		return generateDiagram(processDefinition, "png", Collections.<String> emptyList());
	}

	/**
	 * Generates a PNG diagram image of the given process definition, using the diagram interchange information of the
	 * process, and highlight active activities.
	 * 
	 * @param processDefinition
	 *            the definition that will be used to generate the diagram.
	 * @param activities
	 *            the activities that need to be highlighted.
	 * @return the input stream of the image.
	 */
	public static InputStream generatePngDiagram(ProcessDefinitionEntity processDefinition, List<String> activities) {
		return generateDiagram(processDefinition, "png", activities);
	}

	/**
	 * Generates a JPG diagram image of the given process definition, using the diagram interchange information of the
	 * process.
	 * 
	 * @param processDefinition
	 *            the definition that will be used to generate the diagram.
	 * @return the input stream of the image.
	 */
	public static InputStream generateJpgDiagram(ProcessDefinitionEntity processDefinition) {
		return generateDiagram(processDefinition, "jpg", Collections.<String> emptyList());
	}

	protected static ProcessDiagramCanvas generateDiagram(ProcessDefinitionEntity processDefinition,
			List<String> highLightedActivities) {
		ProcessDiagramCanvas processDiagramCanvas = initProcessDiagramCanvas(processDefinition);

		// Draw pool shape, if process is participant in collaboration
		if (processDefinition.getParticipantProcess() != null) {
			ParticipantProcess pProc = processDefinition.getParticipantProcess();
			processDiagramCanvas.drawPoolOrLane(pProc.getName(), pProc.getX(), pProc.getY(), pProc.getWidth(),
					pProc.getHeight());
		}

		// Draw lanes
		if (processDefinition.getLaneSets() != null && !processDefinition.getLaneSets().isEmpty()) {
			for (LaneSet laneSet : processDefinition.getLaneSets()) {
				if (laneSet.getLanes() != null && !laneSet.getLanes().isEmpty()) {
					processLaneSet(laneSet.getLanes(), processDiagramCanvas);
				}
			}
		}

		// Draw activities and their sequence-flows
		List<ActivityImpl> activities = processDefinition.getActivities();
		for (ActivityImpl activity : activities) {
			drawActivity(processDiagramCanvas, activity, highLightedActivities);
		}
		return processDiagramCanvas;
	}

	private static void processLaneSet(List<Lane> laneSet, ProcessDiagramCanvas processDiagramCanvas) {
		for (Lane lane : laneSet) {
			processDiagramCanvas.drawPoolOrLane(lane.getName(), lane.getX(), lane.getY(), lane.getWidth(),
					lane.getHeight());
		}
	}

	/**
	 * Generates diagram.
	 * 
	 * @param processDefinition
	 *            the process definition.
	 * @param imageType
	 *            the type of image.
	 * @param highLightedActivities
	 *            list of activities of to be highligh.
	 * @return the input stream of the diagram.
	 */
	public static InputStream generateDiagram(ProcessDefinitionEntity processDefinition, String imageType,
			List<String> highLightedActivities) {
		return generateDiagram(processDefinition, highLightedActivities).generateImage(imageType);
	}

	protected static void drawActivity(ProcessDiagramCanvas processDiagramCanvas, ActivityImpl activity,
			List<String> highLightedActivities) {
		String type = (String) activity.getProperty("type");
		ActivityDrawInstruction drawInstruction = activityDrawInstructions.get(type);
		if (drawInstruction != null) {

			drawInstruction.draw(processDiagramCanvas, activity);

			// Gather info on the multi instance marker
			boolean multiInstanceSequential = false, multiInstanceParallel = false, collapsed = false;
			String multiInstance = (String) activity.getProperty("multiInstance");
			if (multiInstance != null) {
				if ("sequential".equals(multiInstance)) {
					multiInstanceSequential = true;
				} else {
					multiInstanceParallel = true;
				}
			}

			// Gather info on the collapsed marker
			Boolean expanded = (Boolean) activity.getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);
			if (expanded != null) {
				collapsed = !expanded;
			}

			// Actually draw the markers
			processDiagramCanvas.drawActivityMarkers(activity.getX(), activity.getY(), activity.getWidth(),
					activity.getHeight(), multiInstanceSequential, multiInstanceParallel, collapsed);

			// Draw highlighted activities
			if (highLightedActivities.contains(activity.getId())) {
				drawHighLight(processDiagramCanvas, activity);
			}

		}

		// Outgoing transitions of activity
		for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
			List<Integer> waypoints = ((TransitionImpl) sequenceFlow).getWaypoints();
			drawWaypoints(waypoints, sequenceFlow, activity, processDiagramCanvas);
		}

		// Nested activities (boundary events)
		for (ActivityImpl nestedActivity : activity.getActivities()) {
			drawActivity(processDiagramCanvas, nestedActivity, highLightedActivities);
		}
	}

	private static void drawWaypoints(List<Integer> waypoints, PvmTransition sequenceFlow, ActivityImpl activity,
			ProcessDiagramCanvas processDiagramCanvas) {
		for (int i = 2; i < waypoints.size(); i += 2) { // waypoints.size()
			// minimally 4: x1, y1,
			// x2, y2
			boolean drawConditionalIndicator = (i == 2)
					&& sequenceFlow.getProperty(BpmnParse.PROPERTYNAME_CONDITION) != null
					&& !((String) activity.getProperty("type")).toLowerCase().contains("gateway");
			if (i < waypoints.size() - 2) {
				processDiagramCanvas.drawSequenceflowWithoutArrow(waypoints.get(i - 2), waypoints.get(i - 1),
						waypoints.get(i), waypoints.get(i + 1), drawConditionalIndicator);
			} else {
				processDiagramCanvas.drawSequenceflow(waypoints.get(i - 2), waypoints.get(i - 1), waypoints.get(i),
						waypoints.get(i + 1), drawConditionalIndicator);
			}
		}
	}

	private static void drawHighLight(ProcessDiagramCanvas processDiagramCanvas, ActivityImpl activity) {
		processDiagramCanvas.drawHighLight(activity.getX(), activity.getY(), activity.getWidth(), activity.getHeight());

	}

	protected static ProcessDiagramCanvas initProcessDiagramCanvas(ProcessDefinitionEntity processDefinition) {
		int minX = Integer.MAX_VALUE;
		int maxX = 0;
		int minY = Integer.MAX_VALUE;
		int maxY = 0;
		Rectangle dimensions = new Rectangle(maxX, maxY, minX, minY);

		if (processDefinition.getParticipantProcess() != null) {
			ParticipantProcess pProc = processDefinition.getParticipantProcess();

			dimensions.x = pProc.getX();
			dimensions.width = pProc.getX() + pProc.getWidth();
			dimensions.height = pProc.getY();
			dimensions.y = pProc.getY() + pProc.getHeight();
		}

		for (ActivityImpl activity : processDefinition.getActivities()) {
			proceesActivity(activity, dimensions);
		}

		if (processDefinition.getLaneSets() != null && !processDefinition.getLaneSets().isEmpty()) {
			processActivityLaneSet(processDefinition.getLaneSets() , dimensions);
		}

		return new ProcessDiagramCanvas(dimensions.x + 10, dimensions.y + 10, dimensions.width, dimensions.height);
	}
	
	private static void processActivityLaneSet(List<LaneSet> lanaSets, Rectangle dimensions){
		for (LaneSet laneSet : lanaSets) {
			if (laneSet.getLanes() != null && !laneSet.getLanes().isEmpty()) {
				for (Lane lane : laneSet.getLanes()) {
					processLane(lane, dimensions);
				}
			}
		}
	}

	private static void processLane(Lane lane, Rectangle dimensions) {
		// width
		if (lane.getX() + lane.getWidth() > dimensions.x) {
			dimensions.x = lane.getX() + lane.getWidth();
		}
		if (lane.getX() < dimensions.width) {
			dimensions.width = lane.getX();
		}
		// height
		if (lane.getY() + lane.getHeight() > dimensions.y) {
			dimensions.y = lane.getY() + lane.getHeight();
		}
		if (lane.getY() < dimensions.height) {
			dimensions.height = lane.getY();
		}
	}

	private static void proceesActivity(ActivityImpl activity, Rectangle dimentsions) {

		// width
		if (activity.getX() + activity.getWidth() > dimentsions.x) {
			dimentsions.x = activity.getX() + activity.getWidth();
		}
		if (activity.getX() < dimentsions.width) {
			dimentsions.width = activity.getX();
		}
		// height
		if (activity.getY() + activity.getHeight() > dimentsions.y) {
			dimentsions.y = activity.getY() + activity.getHeight();
		}
		if (activity.getY() < dimentsions.height) {
			dimentsions.height = activity.getY();
		}

		for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
			List<Integer> waypoints = ((TransitionImpl) sequenceFlow).getWaypoints();
			calculateDimensionsOfWaypoints(waypoints ,dimentsions);
		}

	}
	
	private static void calculateDimensionsOfWaypoints(List<Integer> waypoints, Rectangle dimentsions) {
		for (int i = 0; i < waypoints.size(); i += 2) {
			// width
			int pointOne = waypoints.get(i);
			if (pointOne > dimentsions.x) {
				dimentsions.x = pointOne;
			}
			if (pointOne < dimentsions.width) {
				dimentsions.width = pointOne;
			}
			// height
			int pointTwo = waypoints.get(i + 1);
			if (pointTwo > dimentsions.y) {
				dimentsions.y = pointTwo;
			}
			if (pointTwo < dimentsions.height) {
				dimentsions.height = pointTwo;
			}
		}
		
	}

	protected interface ActivityDrawInstruction {
		/**
		 * Draw method for diagram elements.
		 * 
		 * @param processDiagramCreator
		 *            the generator that draws element.
		 * @param activityImpl
		 *            the activity.
		 */
		void draw(ProcessDiagramCanvas processDiagramCreator, ActivityImpl activityImpl);

	}
}