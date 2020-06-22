package com.sirma.itt.seip.adapters.remote;

import java.io.Serializable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.IntegerPair;

/**
 * The class contains all the information necessary for the connection with a ftp server.
 *
 * @author Nikolay Ch
 */
public class FTPConfiguration implements Serializable {
	private static final long serialVersionUID = 6987067081751507901L;
	/** The host of the server. */
	@Tag(1)
	private String hostname;
	/** The port on which the server will receive. */
	@Tag(2)
	private int port;
	/** The username. */
	@Tag(3)
	private String username;
	/** The password. */
	@Tag(4)
	private String password;
	/** The path where the file will be stored. */
	@Tag(5)
	private String remoteDir;
	/** Keeps whether a secure connection is necessary. */
	@Tag(6)
	private boolean isSecured;
	/**
	 * Controls if active module should be used for file transfer.
	 * <p>
	 * In active mode during file transfers the server calls the client on one of the configured active ports. In
	 * passive mode the client calls the server.
	 */
	@Tag(7)
	private boolean useActiveMode = true;
	/**
	 * The port range that will be allowed for incoming connections to the client
	 */
	@Tag(8)
	private IntegerPair activeModePorts = IntegerPair.EMPTY_RANGE;

	/** The max allowed connections to the given FTP server. 0 or less than zero means unlimited. */
	@Tag(9)
	private int maxAllowedConnections;
	
	/**
	 * Enable or disable verification that the remote host taking part
	 * of a data connection is the same as the host to which the control
	 * connection is attached.
	 */
	@Tag(10)
	private boolean remoteVerificationEnabled;

	/**
	 * Defines the transfer buffer to use as optimization for the file transfer
	 */
	@Tag(11)
	private int bufferSize = 1048576;

	/**
	 * Instantiates the class.
	 */
	public FTPConfiguration() {
		// The parameters can be initialized easily by their setters.
	}

	/**
	 * Instantiates the class.
	 *
	 * @param hostname
	 *            the hostname of the image server
	 * @param port
	 *            the port on which the server will receive the images
	 * @param username
	 *            the username of the client
	 * @param password
	 *            the password of the client
	 * @param remoteDir
	 *            the remote dir where the files will be stored
	 * @param isSecured
	 *            is secured connection needed
	 */
	public FTPConfiguration(String hostname, int port, String username, String password, String remoteDir,
			boolean isSecured) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.isSecured = isSecured;
		this.remoteDir = correctRemoteDir(remoteDir);
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSecured() {
		return isSecured;
	}

	public void setSecured(boolean isSecured) {
		this.isSecured = isSecured;
	}

	public String getRemoteDir() {
		return remoteDir;
	}

	/**
	 * Sets the remote dir. The method makes sure the path given path starts and ends with path separator '/'.
	 *
	 * @param remoteDir
	 *            the new remote dir
	 */
	public void setRemoteDir(String remoteDir) {
		this.remoteDir = correctRemoteDir(remoteDir);
	}

	/**
	 * Makes sure the path starts and ends with path separator
	 *
	 * @param remoteDir
	 *            the remote dir
	 * @return the string
	 */
	private static String correctRemoteDir(String remoteDir) {
		String dirPath = remoteDir;
		if (dirPath != null) {
			if (!dirPath.startsWith("/")) {
				dirPath = "/" + dirPath;
			}
			if (!dirPath.endsWith("/")) {
				dirPath += "/";
			}
		}
		return dirPath;
	}

	/**
	 * Sets the use active mode.
	 * <p>
	 * In active mode during file transfers the server calls the client on one of the configured active ports. In
	 * passive mode the client calls the server.
	 *
	 * @param useActiveMode
	 *            the new use active mode
	 */
	public void setUseActiveMode(boolean useActiveMode) {
		this.useActiveMode = useActiveMode;
	}

	/**
	 * Gets the use active mode.
	 * <p>
	 * In active mode during file transfers the server calls the client on one of the configured active ports. In
	 * passive mode the client calls the server.
	 *
	 * @return the use active mode
	 */
	public boolean isUseActiveMode() {
		return useActiveMode;
	}

	/**
	 * Gets the active mode ports.
	 *
	 * @return the active mode ports
	 */
	public IntegerPair getActiveModePorts() {
		return activeModePorts;
	}

	/**
	 * Sets the active port range.
	 *
	 * @param activeModePorts
	 *            the new active port range
	 */
	public void setActivePortRange(IntegerPair activeModePorts) {
		this.activeModePorts = activeModePorts;
	}

	/**
	 * Gets the max allowed connections.
	 *
	 * @return the max allowed connections
	 */
	public int getMaxAllowedConnections() {
		return maxAllowedConnections;
	}

	/**
	 * Sets the max allowed connections. The will be no limit if set zero or less than zero the
	 *
	 * @param maxAllowedConnections
	 *            the new max allowed connections
	 */
	public void setMaxAllowedConnections(int maxAllowedConnections) {
		this.maxAllowedConnections = maxAllowedConnections;
	}

	public boolean isRemoteVerificationEnabled() {
		return remoteVerificationEnabled;
	}

	public void setRemoteVerificationEnabled(boolean remoteVerificationEnabled) {
		this.remoteVerificationEnabled = remoteVerificationEnabled;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
