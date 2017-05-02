<#if mode=="attach">
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "nodeRef": "${document.nodeRef}",
   "cm:content.mimetype": "${document.mimetype}",
   "version": "${version!"1.0"}",
   "cm:name": "${document.name}",
   "cm:title": "${document.properties.title!""}",
   "site": "${document.getSiteShortName()!""}",
   "status":
   {
      "code": 200,
      "name": "OK",
      "description": "File uploaded successfully"
   }
}
</#escape>
<#elseif mode=="dettach">
{
  <#if deleted??> "deleted": "${deleted}",
   "status":
   {
      "code": 200,
      "name": "OK",
      "description": "File deleted successfully"
   }
   <#else>
   "status":
   {
      "code": 404,
      "name": "Fail",
      "description": "File not found"
   }
   </#if>
}

<#elseif mode="version" || mode="move"|| mode="copy" || mode="revert" >
<#import "../metadata.lib.ftl" as itemLib />
<@itemLib.resultJSON results=results />

<#elseif mode=="lock" || mode=="unlock" >
<#import "../results.lib.ftl" as resultJSONLib />
<@resultJSONLib.resultJSON results=results />

</#if>