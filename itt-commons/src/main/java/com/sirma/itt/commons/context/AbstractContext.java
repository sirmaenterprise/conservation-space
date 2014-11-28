/**
 * Copyright (c) 2008 21.09.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.WeakHashMap;

import com.sirma.itt.commons.encoding.context.Context;
import com.sirma.itt.commons.utils.classes.AutoClassDetection;
import com.sirma.itt.commons.utils.classes.detectors.ClassDetectorLoadingException;

/**
 * Default implementation of the context. The context is the base point for
 * retrieving properties from properties file.
 * 
 * @author Hristo Iliev
 */
public abstract class AbstractContext {

    /**
     * Contains information of failed load.
     * 
     * @author Hristo Iliev
     */
    public static final class FailedLoadInfo {

	/** policy which is used for failed load. */
	private final LoadPolicy policy;
	/**
	 * name of the input (may be null if the policy is
	 * {@link LoadPolicy#STREAM_INPUT}.
	 */
	private final String inputName;
	/** message cause of the fail. */
	private final String messageCause;
	/** {@link Throwable} cause of the fail. */
	private final Throwable throwableCause;

	/**
	 * Initialize with information of fail.
	 * 
	 * @param policy
	 *            {@link LoadPolicy}, type of used policy
	 * @param inputName
	 *            {@link String}, used input name
	 * @param cause
	 *            {@link String}, message cause
	 */
	public FailedLoadInfo(final LoadPolicy policy, final String inputName,
		final String cause) {
	    this.policy = policy;
	    this.inputName = inputName;
	    this.messageCause = cause;
	    throwableCause = null;
	}

	/**
	 * Initialize with information of fail.
	 * 
	 * @param policy
	 *            {@link LoadPolicy}, type of used policy
	 * @param inputName
	 *            {@link String}, used input name
	 * @param cause
	 *            {@link Throwable}, throwable cause
	 */
	public FailedLoadInfo(final LoadPolicy policy, final String inputName,
		final Throwable cause) {
	    this.policy = policy;
	    this.inputName = inputName;
	    messageCause = null;
	    throwableCause = cause;
	}

	/**
	 * Initialize with information of fail.
	 * 
	 * @param policy
	 *            {@link LoadPolicy}, type of used policy
	 * @param inputName
	 *            {@link String}, used input name
	 * @param messageCause
	 *            {@link String}, message cause
	 * @param throwableCause
	 *            {@link Throwable}, throwable cause
	 */
	public FailedLoadInfo(final LoadPolicy policy, final String inputName,
		final String messageCause, final Throwable throwableCause) {
	    this.policy = policy;
	    this.inputName = inputName;
	    this.messageCause = messageCause;
	    this.throwableCause = throwableCause;
	}

	/**
	 * Getter method for policy.
	 * 
	 * @return the policy
	 */
	public LoadPolicy getPolicy() {
	    return policy;
	}

	/**
	 * Getter method for inputName.
	 * 
	 * @return the inputName
	 */
	public String getInputName() {
	    return inputName;
	}

	/**
	 * Getter method for messageCause.
	 * 
	 * @return the messageCause
	 */
	public String getMessageCause() {
	    return messageCause;
	}

	/**
	 * Getter method for throwableCause.
	 * 
	 * @return the throwableCause
	 */
	public Throwable getThrowableCause() {
	    return throwableCause;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    String result;
	    String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
	    StringBuilder buffer = new StringBuilder();
	    buffer.append("Policy:\t\t").append(policy).append(newLine); //$NON-NLS-1$
	    buffer.append("Input Name:\t").append(inputName).append(newLine); //$NON-NLS-1$
	    buffer.append("Message:\t").append(messageCause).append(newLine); //$NON-NLS-1$
	    buffer.append("Throwable:\t").append(throwableCause); //$NON-NLS-1$
	    result = buffer.toString();
	    return result;
	}
    }

    /**
     * Policy of loading the configuration files in proper order.
     * 
     * @author Hristo Iliev
     */
    public enum LoadPolicy {
	/** The properties are loaded from stream. */
	STREAM_INPUT,
	/** The internal property file should be loaded. */
	INTERNAL_CONFIGURATION,
	/** The external property file should be loaded. */
	EXTERNAL_CONFIGURATION;
    }

    /** Property used to contain the keys. */
    private final Properties property = new Properties();

    /** list with all failed loads of properties. */
    private final List<FailedLoadInfo> failedLoads = new LinkedList<FailedLoadInfo>();

    /**
     * map which contains is the last returned property is due to default
     * configuration or due to the value retrieved by the properties.
     */
    private final WeakHashMap<Thread, Boolean> lastUsedDefault = new WeakHashMap<Thread, Boolean>();

    /**
     * Retrieve property from the context. If the property does not exist an
     * {@link MissingResourceException} is thrown.
     * 
     * @param propertyKey
     *            String, key to search in property
     * @return String, retrieved property
     * @exception MissingResourceException
     *                thrown if the property does not exist in the context.
     */
    public String getProperty(final String propertyKey) {
	String result = property.getProperty(propertyKey);
	if (result == null) {
	    throw new MissingResourceException(
		    propertyKey + " does not exist.", Context.class.getName(), //$NON-NLS-1$
		    propertyKey);
	}
	return result;
    }

    /**
     * Retrieve property from the context. If the property does not exist the
     * <code>defaultValue</code> is returned.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @param defaultValue
     *            {@link String}, default value to be returned if the key is not
     *            mapped
     * @return {@link String}, retrieved value
     * @see #isLastDefault()
     */
    public String getProperty(final String propertyKey,
	    final String defaultValue) {
	String result = property.getProperty(propertyKey);
	if (result == null) {
	    setLastDefault(true);
	    return defaultValue;
	}
	setLastDefault(false);
	return result;
    }

    /**
     * Get boolean value from context.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @return boolean, the retrieved property
     * @exception MissingResourceException
     *                thrown if the property does not have mapping
     * @exception TypeMismatchException
     *                thrown if the specified property is not boolean
     */
    public boolean getBooleanProperty(final String propertyKey) {
	String propertyValue = getProperty(propertyKey);
	if ("true".equalsIgnoreCase(propertyValue)) { //$NON-NLS-1$
	    return true;
	}
	if ("false".equalsIgnoreCase(propertyValue)) { //$NON-NLS-1$
	    return false;
	}
	throw TypeMismatchException.forValue(propertyValue, Boolean.TYPE);
    }

    /**
     * Get boolean value from context. If the context does not contains the
     * specified key, or the mapped value is not valid boolean value then
     * default value will be returned.<br>
     * To check if the returned value is returned due to the default property or
     * not, use {@link #isLastDefault()} method to determine.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @param defaultValue
     *            boolean, the default value to be returned if the properties
     *            does not contains the specified key or the mapped value is not
     *            valid boolean value
     * @return boolean, the retrieved property
     * @see #isLastDefault()
     */
    public boolean getBooleanProperty(final String propertyKey,
	    final boolean defaultValue) {
	String propertyValue = getProperty(propertyKey);
	if ("true".equalsIgnoreCase(propertyValue)) { //$NON-NLS-1$
	    setLastDefault(false);
	    return true;
	}
	if ("false".equalsIgnoreCase(propertyValue)) { //$NON-NLS-1$
	    setLastDefault(false);
	    return false;
	}
	setLastDefault(true);
	return defaultValue;
    }

    /**
     * Get byte property from the context.
     * 
     * @param propertyKey
     *            {@link String}, the mapped property
     * @return byte, the value mapped to the property
     * @exception MissingResourceException
     *                thrown if the property does not have mapping
     * @exception TypeMismatchException
     *                thrown if the specified property is not byte
     */
    public byte getByteProperty(final String propertyKey) {
	String propertyValue = getProperty(propertyKey);
	try {
	    return Byte.parseByte(propertyValue);
	} catch (NumberFormatException e) {
	    throw TypeMismatchException.forValue(propertyValue, Byte.TYPE, e);
	}
    }

    /**
     * Get byte value from context. If the context does not contains the
     * specified key, or the mapped value is not valid byte value then default
     * value will be returned.<br>
     * To check if the returned value is returned due to the default property or
     * not, use {@link #isLastDefault()} method to determine.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @param defaultValue
     *            byte, the default value to be returned if the properties does
     *            not contains the specified key or the mapped value is not
     *            valid byte value
     * @return byte, the retrieved property
     * @see #isLastDefault()
     */
    public byte getByteProperty(final String propertyKey,
	    final byte defaultValue) {
	String propertyValue = property.getProperty(propertyKey);
	try {
	    byte result = Byte.parseByte(propertyValue);
	    setLastDefault(false);
	    return result;
	} catch (NumberFormatException e) {
	    setLastDefault(true);
	    return defaultValue;
	}
    }

    /**
     * Get short property from the context.
     * 
     * @param propertyKey
     *            {@link String}, the mapped property
     * @return short, the value mapped to the property
     * @exception MissingResourceException
     *                thrown if the property does not have mapping
     * @exception TypeMismatchException
     *                thrown if the specified property is not short
     */
    public short getShortProperty(final String propertyKey) {
	String propertyValue = getProperty(propertyKey);
	try {
	    return Short.parseShort(propertyValue);
	} catch (NumberFormatException e) {
	    throw TypeMismatchException.forValue(propertyValue, Short.TYPE, e);
	}
    }

    /**
     * Get short value from context. If the context does not contains the
     * specified key, or the mapped value is not valid short value then default
     * value will be returned.<br>
     * To check if the returned value is returned due to the default property or
     * not, use {@link #isLastDefault()} method to determine.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @param defaultValue
     *            short, the default value to be returned if the properties does
     *            not contains the specified key or the mapped value is not
     *            valid short value
     * @return short, the retrieved property
     * @see #isLastDefault()
     */
    public short getShortProperty(final String propertyKey,
	    final short defaultValue) {
	String propertyValue = property.getProperty(propertyKey);
	try {
	    short result = Short.parseShort(propertyValue);
	    setLastDefault(false);
	    return result;
	} catch (NumberFormatException e) {
	    setLastDefault(true);
	    return defaultValue;
	}
    }

    /**
     * Get int property from the context.
     * 
     * @param propertyKey
     *            {@link String}, the mapped property
     * @return int, the value mapped to the property
     * @exception MissingResourceException
     *                thrown if the property does not have mapping
     * @exception TypeMismatchException
     *                thrown if the specified property is not int
     */
    public int getIntProperty(final String propertyKey) {
	String propertyValue = getProperty(propertyKey);
	try {
	    return Integer.parseInt(propertyValue);
	} catch (NumberFormatException e) {
	    throw TypeMismatchException
		    .forValue(propertyValue, Integer.TYPE, e);
	}
    }

    /**
     * Get int value from context. If the context does not contains the
     * specified key, or the mapped value is not valid int value then default
     * value will be returned.<br>
     * To check if the returned value is returned due to the default property or
     * not, use {@link #isLastDefault()} method to determine.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @param defaultValue
     *            int, the default value to be returned if the properties does
     *            not contains the specified key or the mapped value is not
     *            valid int value
     * @return int, the retrieved property
     * @see #isLastDefault()
     */
    public int getIntProperty(final String propertyKey, final int defaultValue) {
	String propertyValue = property.getProperty(propertyKey);
	try {
	    int result = Integer.parseInt(propertyValue);
	    setLastDefault(false);
	    return result;
	} catch (NumberFormatException e) {
	    setLastDefault(true);
	    return defaultValue;
	}
    }

    /**
     * Get long property from the context.
     * 
     * @param propertyKey
     *            {@link String}, the mapped property
     * @return long, the value mapped to the property
     * @exception MissingResourceException
     *                thrown if the property does not have mapping
     * @exception TypeMismatchException
     *                thrown if the specified property is not long
     */
    public long getLongProperty(final String propertyKey) {
	String propertyValue = getProperty(propertyKey);
	try {
	    return Long.parseLong(propertyValue);
	} catch (NumberFormatException e) {
	    throw TypeMismatchException.forValue(propertyValue, Long.TYPE, e);
	}
    }

    /**
     * Get long value from context. If the context does not contains the
     * specified key, or the mapped value is not valid long value then default
     * value will be returned.<br>
     * To check if the returned value is returned due to the default property or
     * not, use {@link #isLastDefault()} method to determine.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @param defaultValue
     *            long, the default value to be returned if the properties does
     *            not contains the specified key or the mapped value is not
     *            valid long value
     * @return long, the retrieved property
     * @see #isLastDefault()
     */
    public long getLongProperty(final String propertyKey,
	    final long defaultValue) {
	String propertyValue = property.getProperty(propertyKey);
	try {
	    long result = Long.parseLong(propertyValue);
	    setLastDefault(false);
	    return result;
	} catch (NumberFormatException e) {
	    setLastDefault(true);
	    return defaultValue;
	}
    }

    /**
     * Get float property from the context.
     * 
     * @param propertyKey
     *            {@link String}, the mapped property
     * @return float, the value mapped to the property
     * @exception MissingResourceException
     *                thrown if the property does not have mapping
     * @exception TypeMismatchException
     *                thrown if the specified property is not float
     */
    public float getFloatProperty(final String propertyKey) {
	String propertyValue = getProperty(propertyKey);
	try {
	    return Float.parseFloat(propertyValue);
	} catch (NumberFormatException e) {
	    throw TypeMismatchException.forValue(propertyValue, Float.TYPE, e);
	}
    }

    /**
     * Get float value from context. If the context does not contains the
     * specified key, or the mapped value is not valid float value then default
     * value will be returned.<br>
     * To check if the returned value is returned due to the default property or
     * not, use {@link #isLastDefault()} method to determine.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @param defaultValue
     *            float, the default value to be returned if the properties does
     *            not contains the specified key or the mapped value is not
     *            valid float value
     * @return float, the retrieved property
     * @see #isLastDefault()
     */
    public float getFloatProperty(final String propertyKey,
	    final float defaultValue) {
	String propertyValue = property.getProperty(propertyKey);
	try {
	    float result = Float.parseFloat(propertyValue);
	    setLastDefault(false);
	    return result;
	} catch (NumberFormatException e) {
	    setLastDefault(true);
	    return defaultValue;
	}
    }

    /**
     * Get double property from the context.
     * 
     * @param propertyKey
     *            {@link String}, the mapped property
     * @return double, the value mapped to the property
     * @exception MissingResourceException
     *                thrown if the property does not have mapping
     * @exception TypeMismatchException
     *                thrown if the specified property is not double
     */
    public double getDoubleProperty(final String propertyKey) {
	String propertyValue = getProperty(propertyKey);
	try {
	    return Double.parseDouble(propertyValue);
	} catch (NumberFormatException e) {
	    throw TypeMismatchException.forValue(propertyValue, Double.TYPE, e);
	}
    }

    /**
     * Get double value from context. If the context does not contains the
     * specified key, or the mapped value is not valid double value then default
     * value will be returned.<br>
     * To check if the returned value is returned due to the default property or
     * not, use {@link #isLastDefault()} method to determine.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @param defaultValue
     *            double, the default value to be returned if the properties
     *            does not contains the specified key or the mapped value is not
     *            valid double value
     * @return double, the retrieved property
     * @see #isLastDefault()
     */
    public double getDoubleProperty(final String propertyKey,
	    final double defaultValue) {
	String propertyValue = property.getProperty(propertyKey);
	try {
	    double result = Double.parseDouble(propertyValue);
	    setLastDefault(false);
	    return result;
	} catch (NumberFormatException e) {
	    setLastDefault(true);
	    return defaultValue;
	}
    }

    /**
     * Get char property from the context.
     * 
     * @param propertyKey
     *            {@link String}, the mapped property
     * @return char, the value mapped to the property
     * @exception MissingResourceException
     *                thrown if the property does not have mapping
     * @exception TypeMismatchException
     *                thrown if the specified property is not char
     */
    public char getCharProperty(final String propertyKey) {
	String propertyValue = getProperty(propertyKey);
	if (propertyValue.length() == 1) {
	    return propertyValue.charAt(0);
	}
	throw TypeMismatchException.forValue(propertyValue, Character.TYPE);
    }

    /**
     * Get char value from context. If the context does not contains the
     * specified key, or the mapped value is not valid char value then default
     * value will be returned.<br>
     * To check if the returned value is returned due to the default property or
     * not, use {@link #isLastDefault()} method to determine.
     * 
     * @param propertyKey
     *            {@link String}, key of the property
     * @param defaultValue
     *            char, the default value to be returned if the properties does
     *            not contains the specified key or the mapped value is not
     *            valid char value
     * @return char, the retrieved property
     * @see #isLastDefault()
     */
    public char getCharProperty(final String propertyKey,
	    final char defaultValue) {
	String propertyValue = property.getProperty(propertyKey);
	if (propertyValue.length() == 1) {
	    setLastDefault(false);
	    return propertyValue.charAt(0);
	}
	setLastDefault(true);
	return defaultValue;
    }

    /**
     * Get an instance of specified type initialized with the property value
     * mapped to the <code>propertyKey</code>. If the property is not mapped
     * {@link MissingResourceException} is thrown.
     * 
     * @param <E>
     *            type of the retrieved object
     * @param propertyKey
     *            {@link String}, the key in the property
     * @param objectClass
     *            {@link Class}, the type of the retrieved object
     * @return E, the retrieved instance initialized according the property
     * @throws ClassDetectorLoadingException
     *             thrown if the detector of the specified type is not available
     * @throws InstantiationException
     *             thrown if the specified information in the property is not
     *             enough to create an object
     * @throws MissingResourceException
     *             thrown if the <code>propertyKey</code> is not mapped
     */
    public <E> E getObject(final String propertyKey, final Class<E> objectClass)
	    throws ClassDetectorLoadingException, InstantiationException {
	return AutoClassDetection.createInstance(getProperty(propertyKey),
		objectClass);
    }

    /**
     * Get an instance of specified type initialized with the property value
     * mapped to the <code>propertyKey</code>. If the property is not mapped
     * {@link MissingResourceException} is thrown.
     * 
     * @param <E>
     *            type of the retrieved object
     * @param propertyKey
     *            {@link String}, the key in the property
     * @param objectClass
     *            {@link Class}, the type of the retrieved object
     * @param defaultValue
     *            E, the default return value if the load fails
     * @return E, the retrieved instance initialized according the property
     */
    public <E> E getObject(final String propertyKey,
	    final Class<E> objectClass, final E defaultValue) {
	try {
	    String propertyValue = property.getProperty(propertyKey);
	    if (propertyValue != null) {
		E result = AutoClassDetection.createInstance(propertyValue,
			objectClass);
		setLastDefault(false);
		return result;
	    }
	} catch (ClassDetectorLoadingException e) {
	    // Return default value
	} catch (InstantiationException e) {
	    // Return default value
	}
	setLastDefault(true);
	return defaultValue;
    }

    /**
     * Check is the last returned value is the default value provided to the
     * method as parameter or it is value retrieved from the properties. <br>
     * <i>Note:</i> The returned value is for the last operation with provided
     * default value. If such operation is followed with operations which does
     * not provide mechanism for returning default value if the property is not
     * found, will <b>not</b> change the value returned by this method.<br>
     * <i>Note:</i> This method is thread safe and even multiple threads to use
     * methods with default parameters, the returned value of each thread can
     * differ according the last operation invoked by the thread. <br>
     * <i>Note:</i> If the call to this method is not preceded by any operation
     * by this thread which return any default value, this method will return
     * <code>null</code>.
     * 
     * @return {@link Boolean}, {@link Boolean#TRUE} if the last operation with
     *         provided default value is returned the default value,
     *         {@link Boolean#FALSE} if the returned value were taken from the
     *         properties or <code>null</code> if the call to this method is not
     *         preceded by call to get method which return default value.
     */
    public Boolean isLastDefault() {
	return lastUsedDefault.get(Thread.currentThread());
    }

    /**
     * Set the last returned parameter according to that is it parameter
     * provided to the method or a value.
     * 
     * @param isDefault
     *            boolean, is the last returned property is the provided
     *            <code>defaultValue</code> parameter or not.
     */
    private void setLastDefault(final boolean isDefault) {
	lastUsedDefault.put(Thread.currentThread(), Boolean.valueOf(isDefault));
    }

    /**
     * Getter method for property.
     * 
     * @return the property
     */
    public Properties getProperty() {
	return property;
    }

    /**
     * Load the context from the properties file. First the properties are tried
     * to be loaded from the properties file set by user. If the file cannot be
     * found then the default property file name is used for this context. If
     * the loading fails again then it is tried to be loaded the internal
     * configuration file. If the loading fails again, the context is not loaded
     * and explicit call to {@link #load(InputStream)} should be made.
     */
    public AbstractContext() {
	this(false, LoadPolicy.INTERNAL_CONFIGURATION,
		LoadPolicy.EXTERNAL_CONFIGURATION, LoadPolicy.STREAM_INPUT);
    }

    /**
     * Load the context from the properties files. The properties are loaded in
     * order defined by the <code>policies</code> parameter. According each
     * value in the policies different type of configuration is loaded. The name
     * of configuration files are retrieved by the abstract methods. The user
     * can define its own input stream which can be accessed through
     * {@link #getStreamProperties()} method. If <code>stopOnSuccess</code> is
     * set on false all configurations will be loaded, otherwise only the first
     * successful load will be used.<br>
     * 
     * Note: If there are multiple equal policies in the <code>policies</code>,
     * the load will be executed multiple times. This can be used for providing
     * different properties which share the same policy, i.e. for every call to
     * method {@link #getStreamProperties()} different stream can be returned.
     * 
     * @param stopOnSuccess
     *            boolean, either to stop on first successful load or to try to
     *            load all
     * @param policies
     *            {@link LoadPolicy}, type of configurations which will be
     *            loaded
     */
    public AbstractContext(final boolean stopOnSuccess,
	    final LoadPolicy... policies) {
	InputStream input = null;
	String inputName = null;
	for (LoadPolicy policy : policies) {
	    try {
		switch (policy) {
		case EXTERNAL_CONFIGURATION:
		    inputName = getExternalPropertiesFile();
		    if (inputName == null) {
			continue;
		    }
		    input = new FileInputStream(inputName);
		    break;
		case INTERNAL_CONFIGURATION:
		    inputName = getInternalPropertiesFile();
		    if (inputName == null) {
			continue;
		    }
		    input = getClass().getClassLoader().getResourceAsStream(
			    inputName);
		    break;
		case STREAM_INPUT:
		    inputName = null;
		    input = getStreamProperties();
		    break;
		}
		if (input == null) {
		    addFailedLoad(new FailedLoadInfo(policy, inputName,
			    "Input is null")); //$NON-NLS-1$
		    continue;
		}
		load(input);
	    } catch (IOException e) {
		addFailedLoad(new FailedLoadInfo(policy, inputName, e));
		continue;
	    } finally {
		try {
		    if (input != null) {
			input.close();
		    }
		} catch (IOException e) {
		    // Ignore exception on close.
		}
	    }
	    if (stopOnSuccess) {
		break;
	    }
	}
    }

    /**
     * Retrieve the name of the default external file which should contains the
     * properties for the specific context.
     * 
     * @return {@link String}, name of the external file
     */
    public abstract String getExternalPropertiesFile();

    /**
     * Retrieve the name of the internal file which should contains the default
     * properties if the external file does not exists.
     * 
     * Note: The internal file is loaded with
     * getClass().getClassLoader().getResourceAsStream(String) method
     * 
     * @return {@link String}, name of the internal file
     * 
     */
    public abstract String getInternalPropertiesFile();

    /**
     * Retrieve the input stream which should contains the properties.
     * 
     * @return {@link InputStream}, the stream which contains the properties
     * @throws IOException
     *             thrown if there is an error while retrieving the stream
     */
    public abstract InputStream getStreamProperties() throws IOException;

    /**
     * Load context with properties.
     * 
     * @param fileInputStream
     *            {@link InputStream}, input stream
     * @throws IOException
     *             thrown if exception during loading occur
     */
    public void load(final InputStream fileInputStream) throws IOException {
	property.load(fileInputStream);
    }

    /**
     * Add information for failed load.
     * 
     * @param info
     *            {@link FailedLoadInfo}, information for failed load
     */
    private void addFailedLoad(final FailedLoadInfo info) {
	failedLoads.add(info);
    }

    /**
     * Getter method for failedLoads.
     * 
     * @return the failedLoads
     */
    public List<FailedLoadInfo> getFailedLoads() {
	return new LinkedList<FailedLoadInfo>(failedLoads);
    }

    /**
     * Clear the fails of loads.
     */
    public void clearFailedLoads() {
	failedLoads.clear();
    }

    /**
     * Concatenate part of keys. Between every part is added '.'(dot) character.
     * 
     * @param firstKey
     *            {@link String}, master key
     * @param otherKeys
     *            {@link String}, path of sub keys
     * @return {@link String}, concatenated key
     */
    public static String join(final String firstKey, final String... otherKeys) {
	StringBuilder result = new StringBuilder(firstKey);
	for (String subKey : otherKeys) {
	    result.append('.').append(subKey);
	}
	return result.toString();
    }

}
