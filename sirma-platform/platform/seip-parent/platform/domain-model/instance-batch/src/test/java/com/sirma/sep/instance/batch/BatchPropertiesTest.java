package com.sirma.sep.instance.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.batch.runtime.JobExecution;

import org.junit.Before;
import org.junit.Test;

import com.mchange.util.AssertException;

/**
 * Test for {@link BatchProperties}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/09/2017
 */
public class BatchPropertiesTest {

	private BatchProperties batchProperties = new BatchProperties();

	private Properties properties = new Properties();

	@Before
	public void setUp() throws Exception {
		properties.clear();
		JobExecution mock = mock(JobExecution.class);
		when(mock.getJobParameters()).thenReturn(properties);
		JobOperatorMock.setJobExecution(mock);
	}

	@Test
	public void getJobProperty() throws Exception {
		properties.put("someProperty", "propertyValue");
		assertEquals("propertyValue", batchProperties.getJobProperty(1L, "someProperty"));
		assertNull(batchProperties.getJobProperty(1L, "someOtherProperty"));
		assertEquals("defaultValue", batchProperties.getJobProperty(1L, "someOtherProperty", "defaultValue"));
	}

	@Test
	public void getJobId() throws Exception {
		properties.put(BatchProperties.JOB_ID, "someJobId");
		assertEquals("someJobId", batchProperties.getJobId(1L));
	}

	@Test
	public void getTenantId() throws Exception {
		properties.put(BatchProperties.TENANT_ID, "tenant.com");
		assertEquals("tenant.com", batchProperties.getTenantId(1L));
	}

	@Test
	public void getRequestId() throws Exception {
		properties.put(BatchProperties.REQUEST_ID, "someRequestId");
		assertEquals("someRequestId", batchProperties.getRequestId(1L));
	}

	@Test
	public void getChunkSize() throws Exception {
		assertFalse(batchProperties.getChunkSize(1L).isPresent());
		properties.put(BatchProperties.CHUNK_SIZE, "23");
		assertEquals(new Integer(23), batchProperties.getChunkSize(1L).orElseThrow(AssertException::new));
	}

}
