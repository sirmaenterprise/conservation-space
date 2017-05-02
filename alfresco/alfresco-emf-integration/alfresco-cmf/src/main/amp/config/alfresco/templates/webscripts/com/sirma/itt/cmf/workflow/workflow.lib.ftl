<#-- Renders a task instance. -->
<#macro taskJSON task detailed=false>
      {
         "id": "${task.id}",
         "definitionId": "${task.definitionId}",
         "url": "${task.url}",
         "name": "${task.name?string?j_string}",
         "title": "${task.title?string?j_string!""}",
         "description": "${task.description?string?j_string!""}",
         "state": "${task.state}",
         "path": "${task.path}",
         "isPooled": ${task.isPooled?string},
         "isEditable": ${task.isEditable?string},
         "isReassignable": ${task.isReassignable?string},
         "isClaimable": ${task.isClaimable?string},
         "isReleasable": ${task.isReleasable?string},
         "outcome": <#if task.outcome??>"${task.outcome}"<#else>null</#if>,
         "owner": <#if task.owner??>"${task.owner.userName}"<#else>null</#if>,
         "nodeRef": <#if task.nodeRef??>"${task.nodeRef}"<#else>null</#if>,
         "properties":
         <@propertiesJSON properties=task.properties /> <#-- ,
         "propertyLabels":
         <@propertyLabelsJSON propertyLabels=task.propertyLabels  /> --> <#if task.workflowInstance??>,
         "workflowInstance": <#if task.workflowInstance??>
         <@workflowInstanceJSON workflowInstance=task.workflowInstance/><#else>{}</#if><#if detailed>,
         "definition":
         {
            "id": "${task.definition.id}",
            "url": "${task.definition.url}",
            "type":
            {
               "name": "${shortQName(task.definition.type.name?string)}",
               "title": "${task.definition.type.title!""}",
               "description": "${task.definition.type.description!""}",
               "url": "${task.definition.type.url}"
            },
            "node":
            {
               "name": "${task.definition.node.name}",
               "title": "${task.definition.node.title!""}",
               "description": "${task.definition.node.description!""}",
               "isTaskNode": ${task.definition.node.isTaskNode?string},
               "transitions":
               [
                  <#list task.definition.node.transitions as transition>
                  {
                     "id": "${transition.id}",
                     "title": "${transition.title!""}",
                     "description": "${transition.description!""}",
                     "isDefault": ${transition.isDefault?string},
                     "isHidden": ${transition.isHidden?string}
                  }
                  <#if transition_has_next>,</#if>
                  </#list>
               ]
            }
         }
         </#if></#if>
      }
</#macro>

<#-- Renders a map of properties -->
<#macro propertiesJSON properties>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#list properties?keys as key>
		"${key}":<#if properties[key]??><#assign val=properties[key]><#if val?is_boolean == true>${val?string}<#elseif val?is_number == true> ${val?c}<#elseif val?is_sequence>[<#list val as element>"${element?string}"<#if (element_has_next)>,</#if></#list>]<#else>"${shortQName(val?string)}"</#if><#else>null</#if><#if (key_has_next)>,</#if>
</#list>
}
</#escape>
</#macro>

<#-- Renders a map of property labels -->
<#macro propertyLabelsJSON propertyLabels>
<#escape x as jsonUtils.encodeJSONString(x)>
{
<#list propertyLabels?keys as key>
		"${key}":<#if propertyLabels[key]??>"${propertyLabels[key]}"<#else>null</#if><#if (key_has_next)>,</#if>
</#list>
}
</#escape>
</#macro>

<#-- Renders a workflow instance. -->
<#macro workflowInstanceJSON workflowInstance detailed=false>
<#escape x as jsonUtils.encodeJSONString(x)>
{
		"id": "${workflowInstance.id}",
		"url": "${workflowInstance.url}",
		"name": "${workflowInstance.name}",
		"title": "${workflowInstance.title!""}",
		"description": "${workflowInstance.description!""}",
		"isActive": ${workflowInstance.isActive?string},
		"startDate": "${workflowInstance.startDate}",
		"priority": <#if workflowInstance.priority??>${workflowInstance.priority?c}<#else>2</#if>,
		"message": <#if workflowInstance.message?? && workflowInstance.message?length &gt; 0>"${workflowInstance.message}"<#else>null</#if>,
		<#-- "dueDate": <#if workflowInstance.dueDate??>"${workflowInstance.dueDate}"<#else>null</#if>,-->
		"context": <#if workflowInstance.context??>"${workflowInstance.context}"<#else>null</#if>,
		"package": <#if workflowInstance.package??>"${workflowInstance.package}"<#else>null</#if>,
		"initiator": <#if workflowInstance.initiator??> "${workflowInstance.initiator.userName}",
		<#--{
				"userName": "${workflowInstance.initiator.userName}"<#if workflowInstance.initiator.firstName??>,
				"firstName": "${workflowInstance.initiator.firstName}"</#if><#if workflowInstance.initiator.lastName??>,
				"lastName": "${workflowInstance.initiator.lastName}"</#if><#if workflowInstance.initiator.avatarUrl??>,
				"avatarUrl": "${workflowInstance.initiator.avatarUrl}"</#if>
		},-->
		<#else>
		null,
		</#if>
		<#if workflowInstance.properties??>
		"properties":
		<@propertiesJSON properties=workflowInstance.properties />, </#if>
		"definitionUrl": "${workflowInstance.definitionUrl}"<#if detailed>,
		"diagramUrl": <#if workflowInstance.diagramUrl??>"${workflowInstance.diagramUrl}"<#else>null</#if>,
		"startTaskInstanceId": "${workflowInstance.startTaskInstanceId}",
		"definition":
		<@workflowDefinitionJSON workflowDefinition=workflowInstance.definition detailed=true/>
		<#if workflowInstance.tasks??>,
		"tasks":
		[
				<#list workflowInstance.tasks as task>
				<@taskJSON task=task/>
				<#if task_has_next>,</#if>
				</#list>
		]
		</#if>
		</#if>
}
</#escape>
</#macro>

<#-- Renders a workflow definition. -->
<#macro workflowDefinitionJSON workflowDefinition detailed=false>
<#escape x as jsonUtils.encodeJSONString(x)>
      {
         "id" : "${workflowDefinition.id}",
         "url": "${workflowDefinition.url}",
         "name": "${workflowDefinition.name}",
         "title": "${workflowDefinition.title!""}",
         "description": "${workflowDefinition.description!""}",
         "version": "${workflowDefinition.version}"
         <#if detailed>,
         "startTaskDefinitionUrl": "${workflowDefinition.startTaskDefinitionUrl}",
         "startTaskDefinitionType": "${shortQName(workflowDefinition.startTaskDefinitionType)}",
         "taskDefinitions":
         [
            <#list workflowDefinition.taskDefinitions as taskDefinition>
            {
               "url": "${taskDefinition.url}",
               "type": "${shortQName(taskDefinition.type)}"
            }
            <#if taskDefinition_has_next>,</#if>
            </#list>
         ]
         </#if>
      }
</#escape>
</#macro>
