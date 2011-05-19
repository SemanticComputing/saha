[#ftl]
[#setting url_escaping_charset='UTF-8']

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> 
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml"> 
<head> 
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <title>SAHA3 ${coordinateType} insertion</title>     
    <!-- mt08014 -->
    <script src="http://maps.google.com/maps/api/js?sensor=false" type="text/javascript"></script>
    <!-- demo.seco.tkk.fi -->
    [#--<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=ABQIAAAA61jIX_C9RqkE_idYRaanYRRkY9KrTsc54SNfat8hJ7dc0OtNkRRbtUgMZ_CdmFkdOhrBdQOioYYS2w" type="text/javascript"></script>--] 
  </head> 
  <body onunload="GUnload()"> 
<div> 
 
<div style="visibility: visible; float: right;"> 
<input class="button_outer button_inner" type="button" onclick="fetchConcept();" style="width: 135px; height: 25px;background: red none repeat scroll 0%; display: inline;  clear: right; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial;margin-bottom:25px" value="Save"/> 
 
<div id="coordsinfo" style="visibility: visible;">Coordinates:</div> 
<div id="coords">none
</div> 
</div> 
 	<div>[#if coordinateType == "polygon" || coordinateType == "route"]Right-click a node to remove it[/#if]</div>
    <div id="map" style="width: 450px; height: 750px"></div> 
 </div> 
    
 
 
 
 
 
 
<div id="message2" style="visibility: hidden"></div> 
<div id="message3" style="visibility: hidden"></div> 
<div id="message4" style="visibility: hidden"></div> 
 
 
 
 
 
<noscript>
	<b>JavaScript must be enabled in order for you to use Google Maps.</b>
</noscript> 
  
<script type="text/javascript"> 

var fc = '${coordinateType}';
var c = '${coordinate?string}';

var oldpoint;
var firsttime = true;
var markers = [];
var oldOverlay = null;
var coordinates = "";

var mapOptions = {
      zoom: 5,
      center: new google.maps.LatLng(65.5, 25.7),
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    
map = new google.maps.Map(document.getElementById("map"), mapOptions);

startitup();

function fetchConcept() {
	opener.setNewCoordinates(coordinates, fc);
	window.close();
}

function addPoint(point) {
	var newpoint = new google.maps.LatLng(point.lat(), point.lng());
	
	if (markers.length < 100) {
		var picIcon = new google.maps.MarkerImage(
			"http://demo.seco.tkk.fi/onkipaikka/images/selector.gif",
			new google.maps.Size(20, 20), 
			new google.maps.Point(0, 0),
			new google.maps.Point(10, 10),
			new google.maps.Size(20, 20));
		
		
		var markerpoint = new google.maps.Marker({
			position : newpoint,
			icon : picIcon,
			draggable : true,
			clickable : true,
			bouncy : false
		});
		
		markers[markers.length] = markerpoint;

		google.maps.event.addListener(markerpoint, "dragend", function() {
			draw();
		});
		google.maps.event.addListener(markerpoint, "rightclick", function() {
			removePoint(markerpoint);
		});
		
		markerpoint.setMap(map);
	}
	
	draw();
	return true;
}

function draw() {
	var coordinatesinfo = "";
	var bounds;
	coordinates = "";
	
	var bounds = new google.maps.LatLngBounds();
		
	if (markers.length > 0) {
		var points = [];
		for ( var i = 0; i < markers.length; i++) {
		
			var point = markers[i].getPosition()
			points.push(point);
			
			coordinatesinfo = coordinatesinfo + " <ul><li>WGS84lat: "
					+ point.lat().toString() + "<br>"
					+ " <li>WGS84lng: "
					+ point.lng().toString() + "</ul>";

			coordinates = coordinates
					+ point.lat().toString() + ","
					+ point.lng().toString() + " ";
					
			bounds.extend(point);
					
		}
		document.getElementById("coords").innerHTML = coordinatesinfo;
		
		var linesystem;
		if (fc == 'polygon')
		{
			points.push(markers[0].getPosition());
			linesystem = new google.maps.Polygon({
				paths : points,
				fillColor : "#C33045",
				strokeColor : "#C33045",
				strokeOpacity : 1,
				strokeWeight : 1,  
				fillOpacity : 0.35
			});
		}
		else if (fc == 'route')
			linesystem = new google.maps.Polyline({
				path : points,
				strokeColor : "#C33045",
				strokeWeight : 3
			});
			
		linesystem.setMap(map);
		
		if (oldOverlay) {
			oldOverlay.setMap(null);
		}
		oldOverlay = linesystem;
	}
	
	map.fitBounds(bounds);
		
	if (markers.length == 1) {
		map.setZoom(7);
	}

	return true;
}

function removePoint(marker) {	
	for (i in markers) {
		var current = markers[i];
		if (current == marker) {
			markers.splice(i, 1);
		}
	}
	
	marker.setMap(null);
	
	draw();
}

function startitup() {
	if (fc == "singlepoint" && !(c == 'false')) {
		createOld(decodeURIComponent(c));
	}
	else if ((fc == "polygon" || fc == "route") && !(c == 'false')) {
		createOldOverlay(decodeURIComponent(c));
	}
}

google.maps.event.addListener(map, "click", function(event) {
	
	var point = event.latLng
	
	[#if coordinateType == "polygon" || coordinateType == "route"]
		addPoint(point);
	[#else]
		updatecoordinates(point);
	[/#if]

});

function createOld(pointcand) {
	var pointarr = pointcand.split(',');

	var x = pointarr[0];
	var y = pointarr[1];

	var newpoint = new google.maps.LatLng(x, y);
	var markerpoint = new google.maps.Marker({
		position : newpoint, 
		draggable : true
	});
	
	var point = markerpoint.getPosition();
	
	coordinatesinfo = " <ul><li>WGS84lat: " + point.lat().toString() + "<br>"
			+ " <li>WGS84lng: " + point.lng().toString() + "</ul>";
	document.getElementById("coords").innerHTML = coordinatesinfo;
	firsttime = false;
	oldpoint = markerpoint;
	
	map.setCenter(newpoint);
	map.setZoom(8);
	markerpoint.setMap(map);

	updatecoordinates(newpoint);
}
function createOldOverlay(points) {

	var pointsarr = points.split(' ');
	
	for (var i in pointsarr) {
		var pointarr = pointsarr[i].split(',');
		if (pointarr.length == 2)
			addPoint(new google.maps.LatLng(pointarr[0], pointarr[1]));
	}
}

function updatecoordinates(point) {

	var newpoint = new google.maps.LatLng(point.lat(), point.lng());
	var markerpoint = new google.maps.Marker({
		position : newpoint,
		draggable : true
	});
	
	document.getElementById("message2").innerHTML = document
			.getElementById("message2").innerHTML
			+ "[" + point.lat().toString() + "," + point.lng().toString() + "];<br>";

	coordinates = point.lat().toString() + "," + point.lng().toString();
	
	coordinatesinfo = " <ul><li>WGS84lat: " + point.lat().toString() + "<br>"
			+ " <li>WGS84lng: " + point.lng().toString() + "</ul>";

	document.getElementById("coords").innerHTML = coordinatesinfo;

	google.maps.event.addListener(markerpoint, "dragend", function(event) {
		updatecoordinates(event.latlng);
	});
	
	markerpoint.setMap(map);

	if (!(firsttime)) {
		oldpoint.setMap(null)
		oldpoint = markerpoint;
	}
	oldpoint = markerpoint;

	firsttime = false;

}
</script> 
 
 
</body> 
 
</html> 
 
 
 
 
 
 