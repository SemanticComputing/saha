[#ftl]
[#setting url_escaping_charset='UTF-8']

<head>
<style>
	.image_frame {
		width: 400px;
		height: 300px;
		border: thin solid black;
	}
	#info {
		font-size: 10pt;
		color: grey;
		padding: 2px;
		margin-top: 10px;
	}
</style>
<script>
	var imageCounter = 0;
	
	function uploadImage() {
		imageCounter++;
		
		var image = document.getElementById("file");
		
		// prepare new request and target iframe
		var newElement = document.createElement('div');
		newElement.innerHTML =
			'<iframe class="image_frame" id="frame_' + imageCounter + '" name="frame_' + imageCounter + '" src=""></iframe>' +
			'<form id="form" target="frame_' + imageCounter +  
				'" action="service/upload" enctype="multipart/form-data" method="post"></form>';
		document.getElementById("uploadedImages").appendChild(newElement);
		
		// do request (load image)
		var form = document.getElementById("form");
		form.appendChild(image);
		form.submit();
		
		// cleanup and restore input
		form.removeChild(image);
		newElement.removeChild(form);
		document.getElementById("input").appendChild(image);
   }
</script>
</head>

<html>
	<body>
		<div id="uploadedImages"></div>
		
		<div id="info">
			Select image from disk and click Upload.
		</div>
		<table style="border-collapse:collapse;">
			<tr>
				<td id="input"><input name="file" id="file" type="file"></td>
				<td><button onclick="javascript:uploadImage()">Upload</button></td>
			</tr>
		</table>
	
	</body>
</html>