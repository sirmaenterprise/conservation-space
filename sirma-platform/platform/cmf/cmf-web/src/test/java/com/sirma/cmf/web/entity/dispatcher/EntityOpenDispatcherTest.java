package com.sirma.cmf.web.entity.dispatcher;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * Test for EntityOpenDispatcher.
 */
@Test
public class EntityOpenDispatcherTest extends CMFTest {

	private final EntityOpenDispatcher dispatcher;

	protected boolean executeOpenInvoked;

	private Map<String, Object> viewMap;
	private Map<String, String> requestMap;

	/**
	 * Instantiates a new entity open dispatcher test.
	 */
	public EntityOpenDispatcherTest() {
		dispatcher = new EntityOpenDispatcher() {

			private DocumentContext documentContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return documentContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				this.documentContext = documentContext;
			}

			@Override
			protected void executeOpen(String instanceType, String instanceId, String tab,
					Map<String, String> requestMap, FacesContext facesContext) {
				executeOpenInvoked = true;
			}

			@Override
			protected Map<String, Object> getViewMap() {
				return viewMap;
			}

			@Override
			protected Map<String, String> getRequestMap(FacesContext facesContext) {
				return requestMap;
			}
		};

		viewMap = new HashMap<>();
		requestMap = new HashMap<>();

		ReflectionUtils.setField(dispatcher, "log", LOG);
	}

	/**
	 * Reset test.
	 */
	@BeforeMethod
	public void resetTest() {
		executeOpenInvoked = false;
	}

	/**
	 * Test for open method.
	 */
	public void openTest() {
		viewMap.put("isOpened", Boolean.TRUE);
		dispatcher.open();
		assertFalse(executeOpenInvoked);

		resetTest();
		viewMap.clear();
		dispatcher.open();
		assertTrue(executeOpenInvoked);
	}

}
