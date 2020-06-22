package com.sirmaenterprise.sep.jms.api;

/**
 * Responses returned from the {@link MessageReceiver} that indicate what happened with the message
 * receiving and consuming proccess.
 * 
 * @author nvelkov
 */
public enum MessageReceiverResponse {
	FAILED_RECEIVING, FAILED_CONSUMING, SUCCESS, NO_OP;
}