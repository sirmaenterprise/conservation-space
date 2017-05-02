[
<#list tenants as tenant>
{
     "tenantDomain": "${tenant.tenantDomain}",
     "enabled": "${tenant.enabled?string}",
     "contentRoot": "${tenant.rootContentStoreDir!""}"
}<#if tenant_has_next>,</#if>
</#list>
]