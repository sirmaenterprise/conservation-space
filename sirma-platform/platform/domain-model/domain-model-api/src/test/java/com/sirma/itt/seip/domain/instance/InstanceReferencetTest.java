package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;

/**
 * Test for {@link InstanceReference}
 *
 * @author BBonev
 */
public class InstanceReferencetTest {

	@Test
	public void testGetRoot() throws Exception {
		ReferenceMock ref1 = new ReferenceMock("ref1", InstanceReference.ROOT_REFERENCE);
		ReferenceMock ref2 = new ReferenceMock("ref2", ref1);
		ReferenceMock ref3 = new ReferenceMock("ref3", ref2);
		ReferenceMock ref4 = new ReferenceMock("ref4", ref3);

		assertEquals(ref1, ref4.getRoot());
		assertEquals(ref1, ref2.getRoot());
		assertEquals(ref1, ref1.getRoot());

		assertNull(new ReferenceMock("ref").getRoot());

		assertTrue(InstanceReference.ROOT_REFERENCE.isRoot());
	}

	@Test
	public void testInstantiate() throws Exception {
		ReferenceMock ref = new ReferenceMock("ref");

		assertNull(ref.instantiate());

		DataTypeDefinition typeDefinition = mock(DataTypeDefinition.class);
		ref.setReferenceType(typeDefinition);

		assertNull(ref.instantiate());

		when(typeDefinition.getJavaClass()).then(a -> EmfInstance.class);

		ref.setType(InstanceType.create("emf:Case"));

		Instance instance = ref.instantiate();

		assertTrue(instance instanceof EmfInstance);
		assertNotNull(instance);
		assertNotNull(instance.type());
		assertEquals("emf:Case", instance.type().getId());

		assertNull(InstanceReference.instantiate(null));
		assertNull(InstanceReference.instantiate(new ReferenceMock("ref")));
	}

	@Test
	public void testIsValid() throws Exception {
		assertFalse(InstanceReference.isValid(null));
		assertFalse(InstanceReference.isValid(new ReferenceMock(null)));
		ReferenceMock reference = new ReferenceMock("ref");
		assertFalse(InstanceReference.isValid(reference));
		reference.setReferenceType(mock(DataTypeDefinition.class));
		assertTrue(InstanceReference.isValid(reference));
	}

	private static class ReferenceMock implements InstanceReference {

		private static final long serialVersionUID = -2475591073949350144L;
		private String identifier;
		private InstanceReference parentReference;
		private DataTypeDefinition referenceType;
		private InstanceType type;

		/**
		 * Instantiates a new reference mock.
		 *
		 * @param identifier
		 *            the identifier
		 */
		public ReferenceMock(String identifier) {
			this.identifier = identifier;
		}

		/**
		 * Instantiates a new reference mock.
		 *
		 * @param identifier
		 *            the identifier
		 * @param parentReference
		 *            the parent reference
		 */
		public ReferenceMock(String identifier, InstanceReference parentReference) {
			this.identifier = identifier;
			this.parentReference = parentReference;
		}

		@Override
		public String getIdentifier() {
			return identifier;
		}

		@Override
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		@Override
		public DataTypeDefinition getReferenceType() {
			return referenceType;
		}

		@Override
		public void setReferenceType(DataTypeDefinition referenceType) {
			this.referenceType = referenceType;
		}

		@Override
		public InstanceReference getParent() {
			return parentReference;
		}

		@Override
		public void setParent(InstanceReference parentReference) {
			this.parentReference = parentReference;
		}

		@Override
		public Instance toInstance() {
			return null;
		}

		@Override
		public void setType(InstanceType type) {
			this.type = type;
		}

		@Override
		public InstanceType getType() {
			return type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (identifier == null ? 0 : identifier.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof InstanceReference)) {
				return false;
			}
			InstanceReference other = (InstanceReference) obj;
			return nullSafeEquals(getIdentifier(), other.getIdentifier());
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("InstanceReference[").append(identifier).append("]");
			return builder.toString();
		}

	}
}
