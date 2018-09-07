[
<#list jsons as json> 
${json}<#if json_index < jsons?size - 1>,</#if>
</#list>
]