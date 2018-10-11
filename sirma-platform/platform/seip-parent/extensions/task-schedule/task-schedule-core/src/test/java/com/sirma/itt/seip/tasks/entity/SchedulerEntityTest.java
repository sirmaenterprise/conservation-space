package com.sirma.itt.seip.tasks.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.esotericsoftware.kryo.KryoException;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.model.SerializableValue;
import com.sirma.itt.seip.serialization.SerializationHelper;

/**
 * Test for {@link SchedulerEntity}
 *
 * @author BBonev
 */
public class SchedulerEntityTest {

	@Test
	@SuppressWarnings("unchecked")
	public void testToSchedulerEntry_kryoException() throws Exception {
		SchedulerEntity entity = new SchedulerEntity();
		entity.setContextData(new SerializableValue());
		SerializationHelper helper = mock(SerializationHelper.class);
		when(helper.deserialize(any())).thenThrow(KryoException.class);
		assertNull(entity.toSchedulerEntry(helper));
	}

	@Test
	public void testToSchedulerEntry_wrongType() throws Exception {
		SchedulerEntity entity = new SchedulerEntity();
		entity.setContextData(new SerializableValue());
		SerializationHelper helper = mock(SerializationHelper.class);
		when(helper.deserialize(any())).thenReturn(new Object());
		assertNull(entity.toSchedulerEntry(helper));
	}

	@Test
	public void testEquals() throws Exception {
		SchedulerEntity e1 = new SchedulerEntity();
		SchedulerEntity e2 = new SchedulerEntity();
		assertFalse(e1.equals(null));
		assertTrue(e1.equals(e1));
		assertTrue(e1.equals(e2));
		e1.setId(1L);
		assertFalse(e1.equals(e2));
		BaseEntity e3 = new BaseEntity();
		e3.setId(1L);
		assertFalse(e1.equals(e3));

		e2.setId(1L);
		assertTrue(e1.equals(e2));
		e1.setIdentifier("1");
		assertFalse(e1.equals(e2));
		e2.setIdentifier("1");
		assertTrue(e1.equals(e2));
	}

	@Test
	public void testHashCode() throws Exception {
		SchedulerEntity e1 = new SchedulerEntity();
		SchedulerEntity e2 = new SchedulerEntity();
		assertTrue(e1.hashCode() == e2.hashCode());
		e1.setId(1L);
		assertFalse(e1.hashCode() == e2.hashCode());
		e2.setId(1L);
		assertTrue(e1.hashCode() == e2.hashCode());
		e1.setIdentifier("1");
		assertFalse(e1.hashCode() == e2.hashCode());
		e2.setIdentifier("1");
		assertTrue(e1.hashCode() == e2.hashCode());
	}
}
