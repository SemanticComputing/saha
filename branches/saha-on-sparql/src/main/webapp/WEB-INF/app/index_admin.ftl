[#ftl]
[#setting url_escaping_charset='UTF-8']
<html>
<body>

<h1>Open indexes</h1>

<h3><a href="index_admin.shtml">refresh</a></h3>

<table style="width:400px;">
	<tr>
		<td style="width:100px"><strong>name</strong></td>
		<td><strong>status</strong></td>
	</tr>
[#list indexes?keys as indexName]
	<tr>
		<td style="width:100px">${indexName}</td>
		<td>
			[#assign status = indexes[indexName]]
			[#if status == 'indexing']
				<span style="color:red">Indexing...</span>
			[#else]
				<span>ready, size: ${status}</span> [<a href="index_admin.shtml?model=${indexName}&index=true">reindex</a>]
			[/#if]
		</td>
	</tr>
[/#list]
</table>

</body>
</html>