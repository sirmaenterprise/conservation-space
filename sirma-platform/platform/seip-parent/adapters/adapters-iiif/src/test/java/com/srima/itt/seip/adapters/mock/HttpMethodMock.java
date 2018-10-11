package com.srima.itt.seip.adapters.mock;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.auth.AuthState;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * Mock for the HttpMethod class. Method for setting the input stream is available.
 *
 * @author Nikolay Ch
 */
public class HttpMethodMock implements HttpMethod {
	private InputStream inputStream = null;

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public HostConfiguration getHostConfiguration() {
		return null;
	}

	@Override
	public void setPath(String path) {
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public URI getURI() throws URIException {
		return null;
	}

	@Override
	public void setURI(URI uri) throws URIException {

	}

	@Override
	public void setStrictMode(boolean strictMode) {
	}

	@Override
	public boolean isStrictMode() {
		return false;
	}

	@Override
	public void setRequestHeader(String headerName, String headerValue) {
	}

	@Override
	public void setRequestHeader(Header header) {
	}

	@Override
	public void addRequestHeader(String headerName, String headerValue) {
	}

	@Override
	public void addRequestHeader(Header header) {
	}

	@Override
	public Header getRequestHeader(String headerName) {
		return null;
	}

	@Override
	public void removeRequestHeader(String headerName) {
	}

	@Override
	public void removeRequestHeader(Header header) {
	}

	@Override
	public boolean getFollowRedirects() {
		return false;
	}

	@Override
	public void setFollowRedirects(boolean followRedirects) {
	}

	@Override
	public void setQueryString(String queryString) {
	}

	@Override
	public void setQueryString(NameValuePair[] params) {
	}

	@Override
	public String getQueryString() {
		return null;
	}

	@Override
	public Header[] getRequestHeaders() {
		return null;
	}

	@Override
	public Header[] getRequestHeaders(String headerName) {
		return null;
	}

	@Override
	public boolean validate() {
		return false;
	}

	@Override
	public int getStatusCode() {
		return 0;
	}

	@Override
	public String getStatusText() {
		return null;
	}

	@Override
	public Header[] getResponseHeaders() {
		return null;
	}

	@Override
	public Header getResponseHeader(String headerName) {
		return null;
	}

	@Override
	public Header[] getResponseHeaders(String headerName) {
		return null;
	}

	@Override
	public Header[] getResponseFooters() {
		return null;
	}

	@Override
	public Header getResponseFooter(String footerName) {
		return null;
	}

	@Override
	public byte[] getResponseBody() throws IOException {
		return null;
	}

	@Override
	public String getResponseBodyAsString() throws IOException {
		return null;
	}

	@Override
	public InputStream getResponseBodyAsStream() throws IOException {
		return inputStream;
	}

	@Override
	public boolean hasBeenUsed() {
		return false;
	}

	@Override
	public int execute(HttpState state, HttpConnection connection) throws HttpException, IOException {
		return 0;
	}

	@Override
	public void abort() {
	}

	@Override
	public void recycle() {

	}

	@Override
	public void releaseConnection() {

	}

	@Override
	public void addResponseFooter(Header footer) {
	}

	@Override
	public StatusLine getStatusLine() {
		return null;
	}

	@Override
	public boolean getDoAuthentication() {
		return false;
	}

	@Override
	public void setDoAuthentication(boolean doAuthentication) {
	}

	@Override
	public HttpMethodParams getParams() {
		return null;
	}

	@Override
	public void setParams(HttpMethodParams params) {
	}

	@Override
	public AuthState getHostAuthState() {
		return null;
	}

	@Override
	public AuthState getProxyAuthState() {
		return null;
	}

	@Override
	public boolean isRequestSent() {
		return false;
	}

}
