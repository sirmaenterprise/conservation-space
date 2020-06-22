/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.sirma.itt.emf.solr.services.query;

import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;

/**
 * The SuggestRequest is extension of the Query to support execution of suggest queries.
 */
public class SuggestRequest extends QueryRequest {

	/** serialVersionUID. */
	private static final long serialVersionUID = 5286444617794108027L;

	/**
	 * Instantiates a new suggest request.
	 */
	public SuggestRequest() {
		super();
	}

	/**
	 * Instantiates a new suggest request.
	 *
	 * @param params
	 *            the solr parameters for the request
	 */
	public SuggestRequest(SolrParams params) {
		super(params);
	}

	/**
	 * Instantiates a new suggest request.
	 *
	 * @param params
	 *            the solr parameters for the request
	 * @param method
	 *            the method to execute
	 */
	public SuggestRequest(SolrParams params, METHOD method) {
		super(params, method);
	}

	/**
	 * Use the params 'QT' parameter if it exists.
	 *
	 * @return the path
	 */
	@Override
	public String getPath() {
		String qt = getParams() == null ? null : getParams().get(CommonParams.QT);
		if (qt == null) {
			qt = super.getPath();
		}
		if (qt != null && qt.startsWith("/")) {
			return qt;
		}
		return "/suggest";
	}

}
