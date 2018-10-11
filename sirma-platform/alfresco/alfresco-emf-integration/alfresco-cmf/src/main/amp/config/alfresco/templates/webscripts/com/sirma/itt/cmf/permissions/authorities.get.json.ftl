<#-- list / search / groups -->
<#if mode=="findcontained">
<#import "authority.lib.ftl" as authorityLib/>
<#import "generic-paged-results.lib.ftl" as genericPaging />
{
	"data": [
		<#list groups as group>
			<@authorityLib.authorityJSON authority=group />
			<#if group_has_next>,</#if>
		</#list>
	]

   <@genericPaging.pagingJSON />
}
<#elseif mode=="finduser">
   <#import "person.lib.ftl" as personLib/>
   <#import "generic-paged-results.lib.ftl" as genericPaging />
	{
	"people" : [
		<#list peoplelist as person>
			<@personLib.personJSON person=person/>
			<#if person_has_next>,</#if>
		</#list>
	]
	}
</#if>