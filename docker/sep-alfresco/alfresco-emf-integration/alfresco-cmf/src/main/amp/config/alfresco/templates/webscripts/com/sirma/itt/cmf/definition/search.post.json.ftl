<#if propertiesMap??>
<#import "../metadata.lib.ftl" as itemLib />
<@itemLib.resultJSON results=results />
<#else>
<#import "../results.lib.ftl" as itemLib />
<@itemLib.resultJSON results=results />
</#if>