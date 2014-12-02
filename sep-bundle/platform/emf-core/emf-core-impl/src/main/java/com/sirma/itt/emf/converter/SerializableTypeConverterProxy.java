package com.sirma.itt.emf.converter;

import java.io.Serializable;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.definition.model.DataTypeDefinition;

/**
 * Converter implementation proxy to allow injecting the non serializable implementation of the
 * {@link TypeConverter} where it's needed to be serializable.
 * 
 * @author BBonev
 */
@ApplicationScoped
@SerializableConverter
@SuppressWarnings("rawtypes")
public class SerializableTypeConverterProxy implements TypeConverter, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2036412218974344907L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object convert(DataTypeDefinition propertyType, Object value) {
		return TypeConverterUtil.getConverter().convert(propertyType, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T convert(Class<T> c, Object value) {
		return TypeConverterUtil.getConverter().convert(c, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection convert(DataTypeDefinition propertyType, Object[] values) {
		return TypeConverterUtil.getConverter().convert(propertyType, values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection convert(DataTypeDefinition propertyType, Collection values) {
		return TypeConverterUtil.getConverter().convert(propertyType, values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Collection<T> convert(Class<T> c, Collection values) {
		return TypeConverterUtil.getConverter().convert(c, values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean booleanValue(Object value) {
		return TypeConverterUtil.getConverter().booleanValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public char charValue(Object value) {
		return TypeConverterUtil.getConverter().charValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte byteValue(Object value) {
		return TypeConverterUtil.getConverter().byteValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short shortValue(Object value) {
		return TypeConverterUtil.getConverter().shortValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int intValue(Object value) {
		return TypeConverterUtil.getConverter().intValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long longValue(Object value) {
		return TypeConverterUtil.getConverter().longValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float floatValue(Object value) {
		return TypeConverterUtil.getConverter().floatValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double doubleValue(Object value) {
		return TypeConverterUtil.getConverter().doubleValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMultiValued(Object value) {
		return TypeConverterUtil.getConverter().isMultiValued(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size(Object value) {
		return TypeConverterUtil.getConverter().size(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Collection<T> getCollection(Class<T> c, Object value) {
		return TypeConverterUtil.getConverter().getCollection(c, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <F, T> void addConverter(Class<F> source, Class<T> destination, Converter<F, T> converter) {
		TypeConverterUtil.getConverter().addConverter(source, destination, converter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <F, I, T> Converter<F, T> addDynamicTwoStageConverter(Class<F> source,
			Class<I> intermediate, Class<T> destination) {
		return TypeConverterUtil.getConverter().addDynamicTwoStageConverter(source, intermediate,
				destination);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Converter getConverter(Object value, Class<T> dest) {
		return TypeConverterUtil.getConverter().getConverter(value, dest);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <F, T> Converter getConverter(Class<F> source, Class<T> dest) {
		return TypeConverterUtil.getConverter().getConverter(source, dest);
	}

}
