<#assign workingCopyLabel = " " + message("coci_service.working_copy_label")>

<#macro dateFormat date=""><#if date?is_date>${xmldate(date)}</#if></#macro>

<#macro itemJSON item>
   <#escape x as jsonUtils.encodeJSONString(x)>
      <#assign node = item.node>
      <#assign version = "1.0">
      <#if node.hasAspect("cm:versionable") && node.versionHistory?size != 0><#assign version = node.versionHistory[0].versionLabel></#if>
      <#if item.createdBy??>
         <#assign createdBy = item.createdBy.displayName>
         <#assign createdByUser = item.createdBy.userName>
      <#else>
         <#assign createdBy="" createdByUser="">
      </#if>
      <#if item.modifiedBy??>
         <#assign modifiedBy = item.modifiedBy.displayName>
         <#assign modifiedByUser = item.modifiedBy.userName>
      <#else>
         <#assign modifiedBy="" modifiedByUser="">
      </#if>
      <#if item.lockedBy??>
         <#assign lockedBy = item.lockedBy.displayName>
         <#assign lockedByUser = item.lockedBy.userName>
      <#else>
         <#assign lockedBy="" lockedByUser="">
      </#if>
      <#assign tags><#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
   "nodeRef": "${node.nodeRef}",
   "nodeType": "${shortQName(node.type)}",
   "type": "${item.type}",
   "mimetype": "${node.mimetype!""}",
   "isFolder": <#if item.linkedNode??>${item.linkedNode.isContainer?string}<#else>${node.isContainer?string}</#if>,
   "isLink": ${(item.isLink!false)?string},
<#if item.linkedNode??>
   "linkedNodeRef": "${item.linkedNode.nodeRef?string}",
</#if>
   "fileName": "<#if item.linkedNode??>${item.linkedNode.name}<#else>${node.name}</#if>",
   "displayName": "<#if item.linkedNode??>${item.linkedNode.name}<#elseif node.hasAspect("cm:workingcopy")>${node.name?replace(workingCopyLabel, "")}<#else>${node.name}</#if>",
   "status": "<#list item.status?keys as s><#if item.status[s]?is_boolean && item.status[s] == true>${s}<#if s_has_next>,</#if></#if></#list>",
   "title": "${node.properties.title!""}",
   "description": "${node.properties.description!""}",
   "author": "${node.properties.author!""}",
   "createdOn": "<@dateFormat node.properties.created />",
   "createdBy": "${createdBy}",
   "createdByUser": "${createdByUser}",
   "modifiedOn": "<@dateFormat node.properties.modified />",
   "modifiedBy": "${modifiedBy}",
   "modifiedByUser": "${modifiedByUser}",
   "lockedBy": "${lockedBy}",
   "lockedByUser": "${lockedByUser}",
   <#if node.getIsLocked()>
   "lockedOwner": "${node.properties["cm:lockOwner"]}",
   </#if>
   "size": "${node.size?c}",
   "version": "${version}",
   "contentUrl": "api/node/content/${node.storeType}/${node.storeId}/${node.id}/${node.name?url}",
   "webdavUrl": "${node.webdavUrl}",
   "actionSet": "${item.actionSet}",
   "tags": <#noescape>[${tags}]</#noescape>,
   <#if node.hasAspect("cm:generalclassifiable")>
   "categories": [<#list node.properties.categories![] as c>["${c.name}", "${c.displayPath?replace("/categories/General","")}"]<#if c_has_next>,</#if></#list>],
   </#if>
   <#if item.activeWorkflows??>"activeWorkflows": "<#list item.activeWorkflows as aw>${aw}<#if aw_has_next>,</#if></#list>",</#if>
   <#if item.isFavourite??>"isFavourite": ${item.isFavourite?string},</#if>
   "permissions":
   {
      "inherited": ${node.inheritsPermissions?string},
      "roles":
      [
      <#list node.fullPermissions as permission>
         "${permission?string}"<#if permission_has_next>,</#if>
      </#list>
      ],
      "userAccess":
      {
      <#list item.actionPermissions?keys as actionPerm>
         <#if item.actionPermissions[actionPerm]?is_boolean>
         "${actionPerm?string}": ${item.actionPermissions[actionPerm]?string}<#if actionPerm_has_next>,</#if>
         </#if>
      </#list>
      }
   },
   <#if item.custom??>"custom": <#noescape>${item.custom}</#noescape>,</#if>
   "actionLabels":
   {
<#if item.actionLabels??>
   <#list item.actionLabels?keys as actionLabel>
      "${actionLabel?string}": "${item.actionLabels[actionLabel]}"<#if actionLabel_has_next>,</#if>
   </#list>
</#if>
   }
   </#escape>
</#macro>
