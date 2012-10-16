package fi.seco.saha3.web.control;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.model.IModelEditor;
import fi.seco.saha3.model.IModelReader;
import fi.seco.saha3.model.ISahaProperty;
import fi.seco.saha3.model.configuration.IConfigService;
import fi.seco.saha3.util.SAHA3;

/**
 * Controller for adding and editing map information (points, routes and areas)
 * in SAHA.
 * 
 */
public class MapEditController extends ASahaController {
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response, IModelReader reader,
			IModelEditor editor, IConfigService config, Locale locale, ModelAndView mav) throws Exception {
		String uri = request.getParameter("uri");
		String type = request.getParameter("fc");
		if (type == null) type = "singlepoint";

		String lat = null;
		String lng = null;
		String polygon = null;
		String route = null;

		for (ISahaProperty property : reader.getResource(uri, locale).getProperties()) {
			String propertyUri = property.getUri();
			if (type.equals("singlepoint") && propertyUri.equals(SAHA3.WGS84_LAT))
				lat = property.getValueLabel();
			else if (type.equals("singlepoint") && propertyUri.equals(SAHA3.WGS84_LONG))
				lng = property.getValueLabel();
			else if (type.equals("polygon") && propertyUri.equals(SAHA3.POLYGON_URI))
				polygon = property.getValueLabel();
			else if (type.equals("route") && propertyUri.equals(SAHA3.ROUTE_URI)) route = property.getValueLabel();
		}

		String coordinate = null;
		if (type.equals("singlepoint") && lat != null && lng != null)
			coordinate = lat + "," + lng;
		else if (type.equals("polygon") && polygon != null)
			coordinate = polygon;
		else if (type.equals("route") && route != null) coordinate = route;

		mav.addObject("coordinate", ((coordinate == null) ? "false" : coordinate));
		mav.addObject("coordinateType", ((type == null) ? "singlepoint" : type));

		return mav;
	}

}
