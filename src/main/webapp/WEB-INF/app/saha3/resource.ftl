[#ftl]
[#setting url_escaping_charset='UTF-8']

[#include "support/saha3_common.ftl"]

<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<title>SAHA3 - ${model} - ${instance.label}</title>
	[@header model/]
</head>

<body class="tundra">

	[@searchBar model lang/]
	
	<div style="margin:5px;">
		${uri}
		<br/>
		<div style="font-size:18pt;margin-top:1em;margin-left:5px;">
			[#if instance.types?size > 0]
				[#list instance.types as type]${type.label}[#if type_has_next], [/#if][/#list]: 
			[/#if]
			${instance.label}
		</div>
		<div style="margin-bottom:10px;margin-left:5px;">
			[#if !locked]
				<a href="editor.shtml?uri=${uri?url}&model=${model?url}" style="color:black">[edit]</a>
			[#else]
				<span style="color:red">locked</span>
			[/#if]
		</div>
		
		[@renderMap instance.propertyMapEntrySet instance.uri false /]
		[@properties model=model propertyMapEntrySet=instance.propertyMapEntrySet/]
		
		[#assign refCount = instance.inverseProperties?size]
		[#if refCount > 0]
			<div style="margin-top:1em;margin-bottom:5px;font-size:12pt;font-weight:bold;margin-left:5px;">
				References (${refCount})
			</div>
			<div id="references">
			[#list instance.sortedInverseProperties as p]
				<div>
					<a href="resource.shtml?uri=${p.valueUri?url}&model=${model?url}" 
					 onMouseOver="showResourceTooltip(this,'${p.valueUri}')" 
					 onMouseOut="hideResourceTooltip(this)">${p.valueLabel}</a> 
					(${p.valueTypeLabel}) <span class="inv_prop">&larr; ${p.label}</span>
				</div>
				[#if limitInverseProperties	&& p_index > 500]
				<div style="margin-top:10px;margin-bottom:30px;">
					<a href="resource.shtml?uri=${uri?url}&model=${model?url}&all" style="color:black;">
						[show remaining ${refCount-500} references]
					</a>
				</div>
				[#break/]
				[/#if]
			[/#list]
			</div>
		[/#if]
		
	</div>
	
</body>
</html>
