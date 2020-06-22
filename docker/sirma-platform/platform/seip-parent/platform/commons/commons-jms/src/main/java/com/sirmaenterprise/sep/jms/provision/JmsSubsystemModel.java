package com.sirmaenterprise.sep.jms.provision;

/**
 * A POJO containing jms system configurations that can be used when provisioning the jms system to
 * the current application server.
 *
 * @author nvelkov
 */
public class JmsSubsystemModel {

	private boolean jmxManagementEnabled = true;
	private boolean persistenceEnabled = true;
	private String persistenceStoreLocation;
	private boolean securityEnabled = false;
	private int journalFileSize = 102400;
	private int httpBatchDelay = 50;
	private int remoteConnectorPort = 61616;
	private int remoteAcceptorPort = 5445;
	private String topicsClientId = "sep-core";

	public String getTopicsClientId() {
		return topicsClientId;
	}

	public void setTopicsClientId(String topicsClientId) {
		this.topicsClientId = topicsClientId;
	}

	public boolean isJmxManagementEnabled() {
		return jmxManagementEnabled;
	}

	public void setJmxManagementEnabled(boolean jmxManagementEnabled) {
		this.jmxManagementEnabled = jmxManagementEnabled;
	}

	public boolean isSecurityEnabled() {
		return securityEnabled;
	}

	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}

	public int getJournalFileSize() {
		return journalFileSize;
	}

	public void setJournalFileSize(int journalFileSize) {
		this.journalFileSize = journalFileSize;
	}

	public int getRemoteConnectorPort() {
		return remoteConnectorPort;
	}

	public void setRemoteConnectorPort(int remoteConnectorPort) {
		this.remoteConnectorPort = remoteConnectorPort;
	}

	public int getRemoteAcceptorPort() {
		return remoteAcceptorPort;
	}

	public void setRemoteAcceptorPort(int remoteAcceptorPort) {
		this.remoteAcceptorPort = remoteAcceptorPort;
	}

	public boolean isPersistenceEnabled() {
		return persistenceEnabled;
	}

	public void setPersistenceEnabled(boolean persistenceEnabled) {
		this.persistenceEnabled = persistenceEnabled;
	}

	public int getHttpBatchDelay() {
		return httpBatchDelay;
	}

	public void setHttpBatchDelay(int httpBatchDelay) {
		this.httpBatchDelay = httpBatchDelay;
	}

	public String getPersistenceStoreLocation() {
		return persistenceStoreLocation;
	}

	public void setPersistenceStoreLocation(String persistenceStoreLocation) {
		this.persistenceStoreLocation = persistenceStoreLocation;
	}
}
