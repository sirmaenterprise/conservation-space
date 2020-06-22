package com.sirma.sep.content.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;


/**
 * Content service that provides information for the content.
 *
 * @author NikolayCh
 */
@Path("/content")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class ContentRestService {

	@Inject
	private InstanceContentService instanceContentService;

	/**
	 * Maps and fetches content ids with their content info.
	 *
	 * @param contentIds
	 * 			  the collection with the content ids
	 * @return the mapped contentId with contentInfo
	 */
	@POST
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	@Path("/info")
	@Transactional
	public Map<String, ContentInfo> fetchContentInfo(Collection<String> contentIds) {
		return mapContentInfo(contentIds);
	}

	/**
	 * Fetches the content info by given content id.
	 *
	 * @param contentId
	 * 			the id of the content
	 * @return the content info
	 */
	@GET
	@Transactional
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	@Path("/{"+JsonKeys.ID+"}/info")
	public ContentInfo fetchContentInfo(@PathParam(JsonKeys.ID) String contentId){
		return instanceContentService.getContent(contentId, null);
	}

	private Map<String, ContentInfo> mapContentInfo(Collection<String> contentIds) {
		Map<String, ContentInfo> mappedContentInfo = new HashMap<>();
		
		for(String contentId : contentIds){
			ContentInfo contentInfo = instanceContentService.getContent(contentId, null);
			if(contentInfo.exists()){
				mappedContentInfo.put(contentId, contentInfo);
			}
		}
		return mappedContentInfo;
	}
	
}
