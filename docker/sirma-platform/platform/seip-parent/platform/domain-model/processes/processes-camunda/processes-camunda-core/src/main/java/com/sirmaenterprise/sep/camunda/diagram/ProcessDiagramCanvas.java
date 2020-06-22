/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.sirmaenterprise.sep.camunda.diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.commons.utils.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a canvas on which BPMN 2.0 constructs can be drawn. Some of the icons used are licenced under a Creative
 * Commons Attribution 2.5 License, see http://www.famfamfam.com/lab/icons/silk/
 * 
 * @see ProcessDiagramGenerator
 * @author Joram Barrez
 */
public class ProcessDiagramCanvas {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDiagramCanvas.class);

	// Predefined sized
	protected static final int ARROW_WIDTH = 5;
	protected static final int CONDITIONAL_INDICATOR_WIDTH = 16;
	protected static final int MARKER_WIDTH = 12;

	// Colors
	protected static final Color TASK_COLOR = new Color(255, 255, 204);
	protected static final Color BOUNDARY_EVENT_COLOR = new Color(255, 255, 255);
	protected static final Color CONDITIONAL_INDICATOR_COLOR = new Color(255, 255, 255);
	protected static final Color HIGHLIGHT_COLOR = Color.RED;

	// Strokes
	protected static final Stroke THICK_TASK_BORDER_STROKE = new BasicStroke(3.0f);
	protected static final Stroke GATEWAY_TYPE_STROKE = new BasicStroke(3.0f);
	protected static final Stroke END_EVENT_STROKE = new BasicStroke(3.0f);
	protected static final Stroke MULTI_INSTANCE_STROKE = new BasicStroke(1.3f);
	protected static final Stroke EVENT_SUBPROCESS_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f }, 0.0f);

	// icons
	protected static final int ICON_SIZE = 16;
	protected static Image userTaskImage;
	protected static Image scriptTaskImage;
	protected static Image serviceTaskImage;
	protected static Image reseiveTaskImage;
	protected static Image sendTaskImage;
	protected static Image manualTaskImage;
	protected static Image businessRuleTaskImage;
	protected static Image timerImage;
	protected static Image errorThrowImage;
	protected static Image errorCatchImage;
	protected static Image signallCatchImage;
	protected static Image signalThrowImage;

	// icons are statically loaded for performace
	static {
		try {
			userTaskImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/user.png"));
			scriptTaskImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/script.png"));
			serviceTaskImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/service.png"));
			reseiveTaskImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/receive.png"));
			sendTaskImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/send.png"));
			manualTaskImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/manual.png"));
			businessRuleTaskImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/business_rule.png"));
			timerImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/timer.png"));
			errorThrowImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/error_throw.png"));
			errorCatchImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/error_catch.png"));
			signallCatchImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/signal_catch.png"));
			signalThrowImage = ImageIO
					.read(ReflectUtil.getResourceAsStream("com/sirmaenterprise/sep/camunda/diagram/signal_throw.png"));
		} catch (IOException e) {
			LOGGER.warn("Could not load image for process diagram creation: " + e.getMessage(), e);
		}
	}

	protected int canvasWidth = -1;
	protected int canvasHeight = -1;
	protected int minX = -1;
	protected int minY = -1;
	protected BufferedImage processDiagram;
	protected Graphics2D g;
	protected FontMetrics fontMetrics;
	protected boolean closed;

	/**
	 * Creates an empty canvas with given width and height.
	 * 
	 * @param width
	 *            canvas width
	 * 
	 * @param height
	 *            canvas height
	 * 
	 */
	public ProcessDiagramCanvas(int width, int height) {
		this.canvasWidth = width;
		this.canvasHeight = height;
		this.processDiagram = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.g = processDiagram.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(Color.black);

		Font font = new Font("Arial", Font.BOLD, 11);
		g.setFont(font);
		this.fontMetrics = g.getFontMetrics();
	}

	/**
	 * Creates an empty canvas with given width and height. Allows to specify minimal boundaries on the left and upper
	 * side of the canvas. This is useful for diagrams that have white space there (eg Signavio). Everything beneath
	 * these minimum values will be cropped.
	 * 
	 * @param width
	 *            canvas width
	 * 
	 * @param height
	 *            canvas height
	 * 
	 * @param minX
	 *            Hint that will be used when generating the image. Parts that fall below minX on the horizontal scale
	 *            will be cropped.
	 * @param minY
	 *            Hint that will be used when generating the image. Parts that fall below minX on the horizontal scale
	 *            will be cropped.
	 */
	public ProcessDiagramCanvas(int width, int height, int minX, int minY) {
		this(width, height);
		this.minX = minX;
		this.minY = minY;
	}

	/**
	 * Generates an image of what currently is drawn on the canvas. Throws an {@link ActivitiException} when
	 * {@link #close()} is already called.
	 * 
	 * @param imageType
	 *            the type of image that is to be generated.
	 * 
	 * @return the input stream of the generated image.
	 */
	public InputStream generateImage(String imageType) {
		if (closed) {
			throw new ProcessEngineException("ProcessDiagramGenerator already closed");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			// Try to remove white space
			minX = (minX <= 5) ? 5 : minX;
			minY = (minY <= 5) ? 5 : minY;
			BufferedImage imageToSerialize = processDiagram;
			if (minX >= 0 && minY >= 0) {
				imageToSerialize = processDiagram.getSubimage(minX - 5, minY - 5, canvasWidth - minX + 5,
						canvasHeight - minY + 5);
			}
			ImageIO.write(imageToSerialize, imageType, out);
		} catch (IOException e) {
			throw new ProcessEngineException("Error while generating process image", e);
		} finally {
			IoUtil.closeSilently(out);
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * Closes the canvas which dissallows further drawing and releases graphical resources.
	 */
	public void close() {
		g.dispose();
		closed = true;
	}

	/**
	 * Draws a start event node.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing.
	 * @param width
	 *            the width of the node.
	 * @param height
	 *            the height of the node.
	 */
	public void drawNoneStartEvent(int x, int y, int width, int height) {
		drawStartEvent(x, y, width, height, null);
	}

	/**
	 * Draws a timer event node.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing.
	 * @param width
	 *            the width of the node.
	 * @param height
	 *            the height of the node.
	 */
	public void drawTimerStartEvent(int x, int y, int width, int height) {
		drawStartEvent(x, y, width, height, timerImage);
	}

	/**
	 * Draws a start event.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 * @param image
	 *            image to be applied.
	 */
	public void drawStartEvent(int x, int y, int width, int height, Image image) {
		g.draw(new Ellipse2D.Double(x, y, width, height));
		if (image != null) {
			g.drawImage(image, x, y, width, height, null);
		}

	}

	/**
	 * Draws a end event node.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawNoneEndEvent(int x, int y, int width, int height) {
		Stroke originalStroke = g.getStroke();
		g.setStroke(END_EVENT_STROKE);
		g.draw(new Ellipse2D.Double(x, y, width, height));
		g.setStroke(originalStroke);
	}

	/**
	 * Draws a error event event.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawErrorEndEvent(int x, int y, int width, int height) {
		drawNoneEndEvent(x, y, width, height);
		g.drawImage(errorThrowImage, x + 3, y + 3, width - 6, height - 6, null);
	}

	/**
	 * Draws a error start event.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawErrorStartEvent(int x, int y, int width, int height) {
		drawNoneStartEvent(x, y, width, height);
		g.drawImage(errorCatchImage, x + 3, y + 3, width - 6, height - 6, null);
	}

	/**
	 * Draws a catching event.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 * @param image
	 *            the image to be applied.
	 */
	public void drawCatchingEvent(int x, int y, int width, int height, Image image) {
		// event circles
		Ellipse2D outerCircle = new Ellipse2D.Double(x, y, width, height);
		int innerCircleX = x + 3;
		int innerCircleY = y + 3;
		int innerCircleWidth = width - 6;
		int innerCircleHeight = height - 6;
		Ellipse2D innerCircle = new Ellipse2D.Double(innerCircleX, innerCircleY, innerCircleWidth, innerCircleHeight);

		Paint originalPaint = g.getPaint();
		g.setPaint(BOUNDARY_EVENT_COLOR);
		g.fill(outerCircle);

		g.setPaint(originalPaint);
		g.draw(outerCircle);
		g.draw(innerCircle);

		g.drawImage(image, innerCircleX, innerCircleY, innerCircleWidth, innerCircleHeight, null);
	}

	/**
	 * Draws a catching timer event.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawCatchingTimerEvent(int x, int y, int width, int height) {
		drawCatchingEvent(x, y, width, height, timerImage);
	}

	/**
	 * Draws a catching error event.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawCatchingErroEvent(int x, int y, int width, int height) {
		drawCatchingEvent(x, y, width, height, errorCatchImage);
	}

	/**
	 * Draws a catching signal event.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawCatchingSignalEvent(int x, int y, int width, int height) {
		drawCatchingEvent(x, y, width, height, signallCatchImage);
	}

	/**
	 * Draws a throwing signal event.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawThrowingSignalEvent(int x, int y, int width, int height) {
		drawCatchingEvent(x, y, width, height, signalThrowImage);
	}

	/**
	 * Draws a sequence flow arrow.
	 * 
	 * @param srcX
	 *            The X coordinate of the start point of the line segment.
	 * @param srcY
	 *            The Y coordinate of the start point of the line segment.
	 * @param targetX
	 *            The X coordinate of the end point of the line segment.
	 * @param targetY
	 *            The Y coordinate of the end point of the line segment.
	 * @param conditional
	 *            flag if a sequence flow indicator should be drawn.
	 */
	public void drawSequenceflow(int srcX, int srcY, int targetX, int targetY, boolean conditional) {
		Line2D.Double line = new Line2D.Double(srcX, srcY, targetX, targetY);
		g.draw(line);
		drawArrowHead(line);

		if (conditional) {
			drawConditionalSequenceFlowIndicator(line);
		}
	}

	/**
	 * Draws a sequence flow.
	 * 
	 * @param srcX
	 *            The X coordinate of the start point of the line segment.
	 * @param srcY
	 *            The Y coordinate of the start point of the line segment.
	 * @param targetX
	 *            The X coordinate of the end point of the line segment.
	 * @param targetY
	 *            The Y coordinate of the end point of the line segment.
	 * @param conditional
	 *            flag if a sequence flow indicator should be drawn.
	 */
	public void drawSequenceflowWithoutArrow(int srcX, int srcY, int targetX, int targetY, boolean conditional) {
		Line2D.Double line = new Line2D.Double(srcX, srcY, targetX, targetY);
		g.draw(line);

		if (conditional) {
			drawConditionalSequenceFlowIndicator(line);
		}
	}

	/**
	 * Draws an arrow head for a specific line.
	 * 
	 * @param line
	 *            the line for the arrow.
	 */
	public void drawArrowHead(Line2D.Double line) {
		int doubleArrowWidth = 2 * ARROW_WIDTH;
		Polygon arrowHead = new Polygon();
		arrowHead.addPoint(0, 0);
		arrowHead.addPoint(-ARROW_WIDTH, -doubleArrowWidth);
		arrowHead.addPoint(ARROW_WIDTH, -doubleArrowWidth);

		AffineTransform transformation = new AffineTransform();
		transformation.setToIdentity();
		double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
		transformation.translate(line.x2, line.y2);
		transformation.rotate(angle - Math.PI / 2d);

		AffineTransform originalTransformation = g.getTransform();
		g.setTransform(transformation);
		g.fill(arrowHead);
		g.setTransform(originalTransformation);
	}

	/**
	 * Draws a sequence flow indicator for a specific line.
	 * 
	 * @param line
	 *            the line for the indicator.
	 */
	public void drawConditionalSequenceFlowIndicator(Line2D.Double line) {
		int horizontal = (int) (CONDITIONAL_INDICATOR_WIDTH * 0.7);
		int halfOfHorizontal = horizontal / 2;
		int halfOfVertical = CONDITIONAL_INDICATOR_WIDTH / 2;

		Polygon conditionalIndicator = new Polygon();
		conditionalIndicator.addPoint(0, 0);
		conditionalIndicator.addPoint(-halfOfHorizontal, halfOfVertical);
		conditionalIndicator.addPoint(0, CONDITIONAL_INDICATOR_WIDTH);
		conditionalIndicator.addPoint(halfOfHorizontal, halfOfVertical);

		AffineTransform transformation = new AffineTransform();
		transformation.setToIdentity();
		double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
		transformation.translate(line.x1, line.y1);
		transformation.rotate(angle - Math.PI / 2d);

		AffineTransform originalTransformation = g.getTransform();
		g.setTransform(transformation);
		g.draw(conditionalIndicator);

		Paint originalPaint = g.getPaint();
		g.setPaint(CONDITIONAL_INDICATOR_COLOR);
		g.fill(conditionalIndicator);

		g.setPaint(originalPaint);
		g.setTransform(originalTransformation);
	}

	/**
	 * Draws a task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawTask(String name, int x, int y, int width, int height) {
		drawTask(name, x, y, width, height, false);
	}

	/**
	 * Draws a pool or lane.
	 * 
	 * @param name
	 *            the name of the pool lane.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawPoolOrLane(String name, int x, int y, int width, int height) {
		g.drawRect(x, y, width, height);

		// Add the name as text, vertical
		if (name != null && name.length() > 0) {
			// Include some padding
			int availableTextSpace = height - 6;

			// Create rotation for derived font
			AffineTransform transformation = new AffineTransform();
			transformation.setToIdentity();
			transformation.rotate(270 * Math.PI / 180);

			Font currentFont = g.getFont();
			Font theDerivedFont = currentFont.deriveFont(transformation);
			g.setFont(theDerivedFont);

			String truncated = fitTextToWidth(name, availableTextSpace);
			int realWidth = fontMetrics.stringWidth(truncated);

			g.drawString(truncated, x + 2 + fontMetrics.getHeight(),
					3 + y + availableTextSpace - (availableTextSpace - realWidth) / 2);
			g.setFont(currentFont);
		}
	}

	/**
	 * Draws a task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 * @param thickBorder
	 *            flag if the task should be drawn with thick borders or not.
	 */
	protected void drawTask(String name, int x, int y, int width, int height, boolean thickBorder) {
		Paint originalPaint = g.getPaint();
		g.setPaint(TASK_COLOR);

		// shape
		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 20, 20);
		g.fill(rect);
		g.setPaint(originalPaint);

		if (thickBorder) {
			Stroke originalStroke = g.getStroke();
			g.setStroke(THICK_TASK_BORDER_STROKE);
			g.draw(rect);
			g.setStroke(originalStroke);
		} else {
			g.draw(rect);
		}

		// text
		if (name != null) {
			String text = fitTextToWidth(name, width);
			int textX = x + ((width - fontMetrics.stringWidth(text)) / 2);
			int textY = y + ((height - fontMetrics.getHeight()) / 2) + fontMetrics.getHeight();
			g.drawString(text, textX, textY);
		}
	}

	/**
	 * Fix text to width.
	 * 
	 * @param original
	 *            the text
	 * @param width
	 *            the width that we have to fit the text
	 * @return the fitted text.
	 */
	protected String fitTextToWidth(String original, int width) {
		String text = original;

		// remove length for "..."
		int maxWidth = width - 10;

		while (fontMetrics.stringWidth(text + "...") > maxWidth && text.length() > 0) {
			text = text.substring(0, text.length() - 1);
		}

		if (!text.equals(original)) {
			text = text + "...";
		}

		return text;
	}

	/**
	 * Draws a user task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawUserTask(String name, int x, int y, int width, int height) {
		drawTask(name, x, y, width, height);
		g.drawImage(userTaskImage, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);
	}

	/**
	 * Draws a script task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawScriptTask(String name, int x, int y, int width, int height) {
		drawTask(name, x, y, width, height);
		g.drawImage(scriptTaskImage, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);
	}

	/**
	 * Draws a service task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawServiceTask(String name, int x, int y, int width, int height) {
		drawTask(name, x, y, width, height);
		g.drawImage(serviceTaskImage, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);
	}

	/**
	 * Draws a script task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawReceiveTask(String name, int x, int y, int width, int height) {
		drawTask(name, x, y, width, height);
		g.drawImage(reseiveTaskImage, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);
	}

	/**
	 * Draws a send task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawSendTask(String name, int x, int y, int width, int height) {
		drawTask(name, x, y, width, height);
		g.drawImage(sendTaskImage, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);
	}

	/**
	 * Draws a manual task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawManualTask(String name, int x, int y, int width, int height) {
		drawTask(name, x, y, width, height);
		g.drawImage(manualTaskImage, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);
	}

	/**
	 * Draws a business rule task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawBusinessRuleTask(String name, int x, int y, int width, int height) {
		drawTask(name, x, y, width, height);
		g.drawImage(businessRuleTaskImage, x + 7, y + 7, ICON_SIZE, ICON_SIZE, null);
	}

	/**
	 * Draws a expanded sub process.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 * @param isTriggeredByEvent
	 *            is the process trigared by event.
	 */
	public void drawExpandedSubProcess(String name, int x, int y, int width, int height, Boolean isTriggeredByEvent) {
		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 20, 20);

		// Use different stroke (dashed)
		if (isTriggeredByEvent) {
			Stroke originalStroke = g.getStroke();
			g.setStroke(EVENT_SUBPROCESS_STROKE);
			g.draw(rect);
			g.setStroke(originalStroke);
		} else {
			g.draw(rect);
		}

		String text = fitTextToWidth(name, width);
		g.drawString(text, x + 10, y + 15);
	}

	/**
	 * Draws a collapsed sub process.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawCollapsedSubProcess(String name, int x, int y, int width, int height) {
		drawCollapsedTask(name, x, y, width, height, false);
	}

	/**
	 * Draws a collapsed activity.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawCollapsedCallActivity(String name, int x, int y, int width, int height) {
		drawCollapsedTask(name, x, y, width, height, true);
	}

	/**
	 * Draws a collapsed task.
	 * 
	 * @param name
	 *            the name of the task.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	protected void drawCollapsedTask(String name, int x, int y, int width, int height, boolean thickBorder) {
		// The collapsed marker is now visualized separately
		drawTask(name, x, y, width, height, thickBorder);
	}

	/**
	 * Draws a collapsed marker.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawCollapsedMarker(int x, int y, int width, int height) {
		// rectangle
		int rectangleWidth = MARKER_WIDTH;
		int rectangleHeight = MARKER_WIDTH;
		Rectangle rect = new Rectangle(x + (width - rectangleWidth) / 2, y + height - rectangleHeight - 3,
				rectangleWidth, rectangleHeight);
		g.draw(rect);

		// plus inside rectangle
		Line2D.Double line = new Line2D.Double(rect.getCenterX(), rect.getY() + 2, rect.getCenterX(),
				rect.getMaxY() - 2);
		g.draw(line);
		line = new Line2D.Double(rect.getMinX() + 2, rect.getCenterY(), rect.getMaxX() - 2, rect.getCenterY());
		g.draw(line);
	}

	/**
	 * Draws an activity marker.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 * @param multiInstanceSequential
	 *            flag for multi instance marker
	 * @param multiInstanceParallel
	 *            flag is parallel.
	 * @param collapsed
	 *            flag for collapsed.
	 */
	public void drawActivityMarkers(int x, int y, int width, int height, boolean multiInstanceSequential,
			boolean multiInstanceParallel, boolean collapsed) {
		if (collapsed) {
			if (!multiInstanceSequential && !multiInstanceParallel) {
				drawCollapsedMarker(x, y, width, height);
			} else {
				drawCollapsedMarker(x - MARKER_WIDTH / 2 - 2, y, width, height);
				if (multiInstanceSequential) {
					drawMultiInstanceMarker(true, x + MARKER_WIDTH / 2 + 2, y, width, height);
				} else if (multiInstanceParallel) {
					drawMultiInstanceMarker(false, x + MARKER_WIDTH / 2 + 2, y, width, height);
				}
			}
		} else {
			if (multiInstanceSequential) {
				drawMultiInstanceMarker(true, x, y, width, height);
			} else if (multiInstanceParallel) {
				drawMultiInstanceMarker(false, x, y, width, height);
			}
		}
	}

	/**
	 * Draws a gateway.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawGateway(int x, int y, int width, int height) {
		Polygon rhombus = new Polygon();
		rhombus.addPoint(x, y + (height / 2));
		rhombus.addPoint(x + (width / 2), y + height);
		rhombus.addPoint(x + width, y + (height / 2));
		rhombus.addPoint(x + (width / 2), y);
		g.draw(rhombus);
	}

	/**
	 * Draws a parallel gateway.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawParallelGateway(int x, int y, int width, int height) {
		// rhombus
		drawGateway(x, y, width, height);

		// plus inside rhombus
		Stroke orginalStroke = g.getStroke();
		g.setStroke(GATEWAY_TYPE_STROKE);
		Line2D.Double line = new Line2D.Double(x + 10, y + height / 2, x + width - 10, y + height / 2); // horizontal
		g.draw(line);
		line = new Line2D.Double(x + width / 2, y + height - 10, x + width / 2, y + 10); // vertical
		g.draw(line);
		g.setStroke(orginalStroke);
	}

	/**
	 * Draws a exclusive gateway.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawExclusiveGateway(int x, int y, int width, int height) {
		// rhombus
		drawGateway(x, y, width, height);

		int quarterWidth = width / 4;
		int quarterHeight = height / 4;

		// X inside rhombus
		Stroke orginalStroke = g.getStroke();
		g.setStroke(GATEWAY_TYPE_STROKE);
		Line2D.Double line = new Line2D.Double(x + quarterWidth + 3, y + quarterHeight + 3, x + 3 * quarterWidth - 3,
				y + 3 * quarterHeight - 3);
		g.draw(line);
		line = new Line2D.Double(x + quarterWidth + 3, y + 3 * quarterHeight - 3, x + 3 * quarterWidth - 3,
				y + quarterHeight + 3);
		g.draw(line);

		g.setStroke(orginalStroke);
	}

	/**
	 * Draws a inclusive gateway.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawInclusiveGateway(int x, int y, int width, int height) {
		// rhombus
		drawGateway(x, y, width, height);

		int diameter = width / 2;

		// circle inside rhombus
		Stroke orginalStroke = g.getStroke();
		g.setStroke(GATEWAY_TYPE_STROKE);
		Ellipse2D.Double circle = new Ellipse2D.Double(((width - diameter) / 2) + x, ((height - diameter) / 2) + y,
				diameter, diameter);
		g.draw(circle);
		g.setStroke(orginalStroke);
	}

	/**
	 * Draws a multi instance marker.
	 * 
	 * @param sequential
	 *            is sequential.
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawMultiInstanceMarker(boolean sequential, int x, int y, int width, int height) {
		int rectangleWidth = MARKER_WIDTH;
		int rectangleHeight = MARKER_WIDTH;
		int lineX = x + (width - rectangleWidth) / 2;
		int lineY = y + height - rectangleHeight - 3;

		Stroke orginalStroke = g.getStroke();
		g.setStroke(MULTI_INSTANCE_STROKE);

		if (sequential) {
			g.draw(new Line2D.Double(lineX, lineY, lineX + rectangleWidth, lineY));
			g.draw(new Line2D.Double(lineX, lineY + rectangleHeight / 2, lineX + rectangleWidth,
					lineY + rectangleHeight / 2));
			g.draw(new Line2D.Double(lineX, lineY + rectangleHeight, lineX + rectangleWidth, lineY + rectangleHeight));
		} else {
			g.draw(new Line2D.Double(lineX, lineY, lineX, lineY + rectangleHeight));
			g.draw(new Line2D.Double(lineX + rectangleWidth / 2, lineY, lineX + rectangleWidth / 2,
					lineY + rectangleHeight));
			g.draw(new Line2D.Double(lineX + rectangleWidth, lineY, lineX + rectangleWidth, lineY + rectangleHeight));
		}

		g.setStroke(orginalStroke);
	}

	/**
	 * Draws highLight.
	 * 
	 * @param x
	 *            the X coordinate of the drawing
	 * @param y
	 *            the Y coordinate of the drawing
	 * @param width
	 *            the width of the image.
	 * @param height
	 *            the height of of the image.
	 */
	public void drawHighLight(int x, int y, int width, int height) {
		Paint originalPaint = g.getPaint();
		Stroke originalStroke = g.getStroke();

		g.setPaint(HIGHLIGHT_COLOR);
		g.setStroke(THICK_TASK_BORDER_STROKE);

		RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 20, 20);
		g.draw(rect);

		g.setPaint(originalPaint);
		g.setStroke(originalStroke);
	}

}