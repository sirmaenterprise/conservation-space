package com.sirma.cmf.mock.jsf;

import javax.faces.context.FacesContext;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocks {@link FacesContext}. It has static method {@link FacesContextMock#mockFacesContext()} for getting mocked faces
 * context. It also has code for releasing the context, that can be used to release context for garbage collection. Its
 * recommended to release the context after unit test, to prevent unexpected behaviour.
 *
 * Example:
 *
 * <pre>
 * FacesContext context = FacesContextMock.mockFacesContext();
 * try {
 * 	ExternalContext ext = Mockito.mock(ExternalContext.class);
 * 	Mockito.when(context.getExternalContext()).thenReturn(ext);
 * 	...
 * } finally {
 * 	context.release();
 * }
 * </pre>
 *
 * @author smustafov
 */
public abstract class FacesContextMock extends FacesContext {

	private static final Release RELEASE = new Release();

	private static class Release implements Answer<Void> {
		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			setCurrentInstance(null);
			return null;
		}
	}

	public static FacesContext mockFacesContext() {
		FacesContext context = Mockito.mock(FacesContext.class);
		setCurrentInstance(context);
		Mockito.doAnswer(RELEASE).when(context).release();
		return context;
	}

}
