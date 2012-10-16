[#ftl]
[#setting url_escaping_charset='UTF-8']

<script type="text/javascript">
function toggleList(element) {
	if (element.innerHTML == '[-]')
		element.innerHTML = '[+]';
	else
		element.innerHTML = '[-]';
	toggleDisplaySiblings(element.parentNode);
}

function toggleDisplaySiblings(element) {
	x = element.nextSibling;
	while (x != null) {
		if (x.nodeType == 1) { /* is element */
			if (x.style.display != 'none')
				x.style.display = 'none';
			else if (x.tagName == 'LI')
				x.style.display = 'list-item';
			else
				x.style.display = 'block';
		}
		x = x.nextSibling;
	}
}
</script>