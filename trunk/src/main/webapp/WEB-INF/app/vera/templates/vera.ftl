[#ftl]
[#setting url_escaping_charset='UTF-8']


<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<head>
[#--
These must be included as templates so they are available both
locally (on the classpath) and on the server through the same logic.
--]
[#include "veraScripts.ftl" /]
[#include "veraCSS.ftl" /]
<title>Vera - Validation Results - ${veraVersion}</title>
</head>
<body>

<div class="outset">

<div class="headline">Validation results</div><br/>
<br/>
[#list schemaListItems as name]
<br/>${name}
[/#list]


<div align="right"><a class="link" href="vera.shtml">New validation</a></div>

</div>
<div>
[@processCategory category=item.root recursionLevel = 1 /]

[#macro processCategory category recursionLevel]
	<ul class="level" [#if recursionLevel > 6]style="display:none;"[/#if]>
		[#if category.key?exists]
			<li class="outset vcenter" style="font-size:${18 - recursionLevel * 1.3}pt[#if recursionLevel < 4];margin-top:${15 / recursionLevel}pt[/#if];">			    
				[#assign itemCount=category.caseCount/]
				<span class="link" onclick="javascript:toggleList(this)" >[#if recursionLevel > 5][+][#else][-][/#if]</span>
				<strong style="margin-top:1em;">${category.key}</strong> [#if itemCount > 0](${itemCount} case[#if itemCount > 1]s[/#if])[/#if]												
			</li>
		[/#if]
		
		[#if category.itemCount > 0]
		<li [#if recursionLevel > 5]style="display:none;"[/#if]class="item">
			[#list category.itemSortingIterator as case]
				<div class="item">${case}</div><br/>
			[/#list]			
		</li>
		[/#if]
		
		[#list category.categorySortingIterator as subCategory]
			[@processCategory category=subCategory recursionLevel = recursionLevel + 1/]
		[/#list]
	</ul>
[/#macro]


</div>
</body>
</html>