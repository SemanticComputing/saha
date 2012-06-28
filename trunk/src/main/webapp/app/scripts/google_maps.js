var map;
var markersArray = new Array();
var otherOverlayArray = new Array();


function initialize(clat, clng) {
	var myLatlng = new google.maps.LatLng(clat, clng);
	var myOptions = {
			zoom : 8,
			center : myLatlng,
			mapTypeId : google.maps.MapTypeId.ROADMAP
	};

	map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	showOverlays(clat, clng);
}

//Shows any overlays currently in the array
function showOverlays() {
	var maxLat = "";
	var maxLng = "";
	var minLat = "";
	var minLng = "";
	if (markersArray.length > 0) {
		//alert("markers!");
		var i = 0;
		for (i = 0; i < markersArray.length; i++) {
			var marker = markersArray[i];
			marker.setMap(map);
			var latlng = marker.getPosition(); 
			lat = latlng.lat();
			lng = latlng.lng();
			if (maxLat.length == 0 && maxLng.length == 0 && minLat.length == 0 && minLng.length == 0) {
				maxLat = lat;
				maxLng = lng;
				minLat = lat;
				minLng = lng;
			} 
			else {
				if (maxLat < lat) {
					maxLat = lat;
				}
				if (minLat > lat) {
					minLat = lat;
				}
				if (maxLng < lng) {
					maxLng = lng;
				}
				if (minLng > lng) {
					minLng = lng;
				}
			}
		}
		map.fitBounds(new google.maps.LatLngBounds(new google.maps.LatLng(minLat, minLng), new google.maps.LatLng(maxLat, maxLng)));
	}
	if (otherOverlayArray.length > 0) {
		var i = 0;
		for (i = 0; i < otherOverlayArray.length; i++) {
			var overlay = otherOverlayArray[i];
			overlay.setMap(map);
		}
	}
}
function addOverlay(overlay) {
	otherOverlayArray.push(overlay);
}

function createAndAddPolygon(coords, label, infoText, color) {
	var coordinateArray = coords.trim().split(' ');
	if (coordinateArray.length > 0) {
		var coordinates = [];

		var flat = 0;
		var flng = 0;
		var llat = 0;
		var llng = 0;
		var sumLat = "";
		var sumLng = "";
		var correction = 1;
		for ( var i in coordinateArray) {
			var coordinate = coordinateArray[i].split(',');
			
			llat = coordinate[0];
			llng = coordinate[1];
			var point = new google.maps.LatLng(llat, llng);
			var lat = point.lat();
			var lng = point.lng();
			coordinates.push(point);
			if (i == 0) {
				flat = lat;
				flng = lng;
				sumLat = lat;
				sumLng = lng;
				
			} else if (i == coordinateArray.length-1) {
				if (flat != lat || flng != lng) {
					sumLat = sumLat + lat;
					sumLng = sumLng + lng;
					var point = new google.maps.LatLng(flat, flng);
					coordinates.push(point);
					correction = 0;
				}
			} else {
				sumLat = sumLat + lat;
				sumLng = sumLng + lng;
			}
		}
		var clat = sumLat/(coordinateArray.length-correction);
		var clng = sumLng/(coordinateArray.length-correction);
		
		createAndAddMarker2(clat+","+clng, label, infoText, color);
		
		var polygon = new google.maps.Polygon({
		    paths: coordinates,
		    strokeColor: "#"+color,
		    strokeOpacity: 0.8,
		    strokeWeight: 2,
		    fillColor: "#"+color,
		    fillOpacity: 0.35
		  });
		
		addOverlay(polygon);
	}
}

function createAndAddRoute(coords, label, infoText, color) {
	var coordinateArray = coords.trim().split(' ');
	if (coordinateArray.length > 0) {
		var coordinates = [];
		
		var llat = 0;
		var llng = 0;
		var sumLat = "";
		var sumLng = "";
		
		for ( var i in coordinateArray) {
			var coordinate = coordinateArray[i].split(',');
			
			llat = coordinate[0];
			llng = coordinate[1];
			var point = new google.maps.LatLng(llat, llng);
			var lat = point.lat();
			var lng = point.lng();
			coordinates.push(point);
			if (i == 0) {
				sumLat = lat;
				sumLng = lng;
				
			} else {
				sumLat = sumLat + lat;
				sumLng = sumLng + lng;
			}
			
		}
		var clat = sumLat/(coordinateArray.length);
		var clng = sumLng/(coordinateArray.length);
		createAndAddMarker2(clat+","+clng, label, infoText, color);
		var route =  new google.maps.Polyline({
		    path: coordinates,
		    strokeColor: "#" + color,
		    strokeOpacity: 0.8,
		    strokeWeight: 2
		  });
		
		addOverlay(route);
	}
}

function createAndAddMarker(lat, lng, label, infoText, color) {
	var latlng = new google.maps.LatLng(lat, lng);
	var marker = createMarker(latlng, label, infoText, color);
	markersArray.push(marker);
}

function createAndAddMarker2(coords, label, infoText, color) {
	var coordinates = coords.split(',');
	
	lat = coordinates[0];
	lng = coordinates[1];
	
	var latlng = new google.maps.LatLng(lat, lng);
	var marker = createMarker(latlng, label, infoText, color);
	markersArray.push(marker);
}

function createMarker(point, label, infoText, color) {
	var image = "http://thydzik.com/thydzikGoogleMap/markerlink.php?text="
			+ label + "&color=" + color;


	var marker = new google.maps.Marker({
	      position: point,
	      map: map,
	      icon: image
	  });
	
	var infowindow = new google.maps.InfoWindow({
	    content: infoText
	});

	google.maps.event.addListener(marker, 'click', function() {
		infowindow.open(map,marker);
	});
	return marker;
}

//Removes the overlays from the map, but keeps them in the array
function clearOverlays() {
	if (markersArray) for (i in markersArray) {
		markersArray[i].setMap(null);
	}
}

//Deletes all markers in the array by removing references to them
function deleteOverlays() {
	clearOverlays();
	if (markersArray) {
		markersArray.length = 0;
	}
}