/**
 *
 */
package com.sirma.sep.content;

import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.sep.content.ChainingInstanceViewPreProcessor;
import com.sirma.sep.content.InstanceViewPreProcessor;
import com.sirma.sep.content.ViewPreProcessorContext;

/**
 * @author BBonev
 *
 */
public class ChainingInstanceViewPreProcessorTest {

	@InjectMocks
	ChainingInstanceViewPreProcessor toTest;

	@Spy
	List<InstanceViewPreProcessor> extensions = new ArrayList<>();

	@Mock
	InstanceViewPreProcessor mockProcessor;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		extensions.clear();
	}

	@Test
	public void testProcess() throws Exception {
		extensions.add(mockProcessor);

		ViewPreProcessorContext context = new ViewPreProcessorContext(null, null);

		toTest.process(context);

		verify(mockProcessor).process(context);
	}
}
