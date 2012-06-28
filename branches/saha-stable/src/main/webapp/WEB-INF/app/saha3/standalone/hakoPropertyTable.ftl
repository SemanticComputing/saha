[#ftl]
[#setting url_escaping_charset='UTF-8']

[@properties model propertyMapEntrySet/]

[#macro properties model propertyMapEntrySet]
	<table style="border-collapse:collapse;font-size:11pt;margin-top:5px;margin-bottom:5px;">
	[#list propertyMapEntrySet as entry]
		[#if entry.value?size > 0]
			[#assign property = entry.value?first]
			[#if !property.config.hidden]
			<tr>
				<td style="padding-right:10px;max-width:150px;color:#333;vertical-align:top;"><em>${entry.key.label}</em></td>
				<td style="vertical-align:top;">
					[#list entry.value as property]
						[#if property.literal]
							[#assign label = property.valueLabel]
							[#if label?ends_with(".jpg") || label?ends_with(".jpeg") || label?ends_with(".png") || label?ends_with(".gif")]
								<div style="margin:5px;">
								[#if label?starts_with("http://") && !label?starts_with("http://demo.seco.tkk.fi/")]
									<img src="${label}" style="max-width:150px;max-height:150px;margin-right:5px;border:thin solid black;"/>
									${label}
								[#else]
									<img src="../service/pics/?name=${label}&model=${model}" 
									style="max-width:150px;max-height:150px;margin-right:5px;border:thin solid black;"/>
									${label}
								[/#if]
								</div>
							[#else]
								[#if property.config.localized && property.valueLang?length > 0]
									<span style="color:grey">(${property.valueLang})</span>
								[/#if]
								<span style="white-space:pre-wrap;">${label}</span>[#if property_has_next], [/#if]
							[/#if]
						[#else]
							${property.valueLabel}[#if property_has_next], [/#if]
						[/#if]
					[/#list]
				</td>
			</tr>
			[/#if]
		[/#if]
	[/#list]
	</table>
[/#macro]