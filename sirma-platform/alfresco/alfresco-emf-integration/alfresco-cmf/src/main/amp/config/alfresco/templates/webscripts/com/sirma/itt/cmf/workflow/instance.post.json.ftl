<#if mode=="transition" || mode=="update">
<#import "workflow.lib.ftl" as workflowLib />
{
data: [
<#list currentTasks as task>
<@workflowLib.taskJSON task=task /><#if task_has_next>,</#if></#list>
]
}
<#elseif mode=="start">
<#import "workflow.lib.ftl" as workflowLib />
{
   workflow: <@workflowLib.workflowInstanceJSON workflowInstance=workflowInstance />,
   tasks: {
data: [
<#list currentTasks as task>
<@workflowLib.taskJSON task=task /><#if task_has_next>,</#if></#list>
]
}
}
<#elseif mode=="starttask">
<#import "workflow.lib.ftl" as workflowLib />
{
   tasks: {
data: [
<#list currentTasks as task>
<@workflowLib.taskJSON task=task /><#if task_has_next>,</#if></#list>
]
}
}
<#elseif mode=="cancel">
<#import "workflow.lib.ftl" as workflowLib />
   <@workflowLib.workflowInstanceJSON workflowInstance=workflowInstance />
</#if>