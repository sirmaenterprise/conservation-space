{
<#if tenant??>
         "tenantDomain": "${tenant.tenantDomain}",
         "enabled": "${tenant.enabled?string}",
         "contentRoot": "${tenant.rootContentStoreDir!""}"
</#if>
}
