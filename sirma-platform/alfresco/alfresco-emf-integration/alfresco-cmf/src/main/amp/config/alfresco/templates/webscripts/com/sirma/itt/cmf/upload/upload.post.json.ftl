<#escape x as jsonUtils.encodeJSONString(x)>
{
   "nodeRef": "${document.nodeRef}",
   "cm:content.mimetype": "${document.mimetype}",
   "cm:name": "${document.name}",
   "cm:title": "${document.title!""}",
   "site": "${document.getSiteShortName()!""}",
   "status":
   {
      "code": 200,
      "name": "OK",
      "description": "File uploaded successfully"
   }
}
</#escape>