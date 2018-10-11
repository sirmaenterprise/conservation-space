package com.sirma.itt.seip.convert;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;

/**
 * The Class TypeConverterImplTest.
 *
 * @author BBonev
 */
@Test
public class TypeConverterImplTest {

	/**
	 * Test exact convert.
	 */
	@Test
	public void testExactConvert() {
		TypeConverter converter = createConverter(SourceType.class, DestinationType.class);
		assertNotNull(converter.convert(DestinationType.class, new SourceType()));
	}

	/**
	 * Test inherited convert.
	 */
	@Test
	public void testInheritedConvert() {
		TypeConverter converter = createConverter(Type.class, DestinationType.class);
		assertNotNull(converter.convert(DestinationType.class, new SourceType()));
		assertNotNull(converter.convert(DestinationType.class, new Type() {
			// nothing to do
		}));
	}

	/**
	 * Test inherited destination convert.
	 */
	@Test
	public void testInheritedDestinationConvert() {
		TypeConverter converter = createConverter(Type.class, AbstractDestination.class);
		assertNotNull(converter.convert(DestinationType.class, new SourceType()));
		assertNotNull(converter.convert(AbstractDestination.class, new SourceType()));
		assertNotNull(converter.convert(DestinationType.class, new Type() {
			// nothing to do
		}));
	}

	/**
	 * Test missing converter.
	 */
	@Test(expectedExceptions = TypeConversionException.class)
	public void testMissingConverter() {
		createConverter(Type.class, AbstractDestination.class).convert(Type.class, new DestinationType());
	}

	/**
	 * Test missing converter with try.
	 */
	@Test
	public void testMissingConverterWithTry() {
		assertNull(
				createConverter(Type.class, AbstractDestination.class).tryConvert(Type.class, new DestinationType()));
	}

	/**
	 * Creates the converter.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <D>
	 *            the generic type
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 * @return the type converter
	 */
	private <S, D> TypeConverter createConverter(Class<S> source, Class<D> destination) {
		TypeConverter converter = new TypeConverterImpl();
		converter.addConverter(source, destination, s -> (D) new DestinationType());
		return converter;
	}

	/**
	 * The Interface Type.
	 */
	interface Type {
		// nothing to do
	}

	/**
	 * The Interface SubType.
	 */
	interface SubType extends Type {
		// nothing to do
	}

	/**
	 * The Class SourceType.
	 */
	class SourceType implements Type {
		// nothing to do
	}

	/**
	 * The Class SourceSubType.
	 */
	class SourceSubType implements SubType {
		// nothing to do
	}

	/**
	 * The Class AbstractDestination.
	 */
	abstract class AbstractDestination {
		// nothing to do
	}

	/**
	 * The Class DestinationType.
	 */
	class DestinationType extends AbstractDestination {
		// nothing to do
	}

}
