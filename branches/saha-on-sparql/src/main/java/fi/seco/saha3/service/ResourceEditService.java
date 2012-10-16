package fi.seco.saha3.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openjena.atlas.json.io.parser.JSONParser;
import org.openjena.atlas.json.io.parserjavacc.javacc.JSON_Parser;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.support.RequestContextUtils;

import fi.seco.saha3.infrastructure.OnkiWebService;
import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.model.ISahaProperty;
import fi.seco.saha3.model.SahaProject;
import fi.seco.saha3.model.UriLabel;
import fi.seco.semweb.util.FreeMarkerUtil;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;

/**
 * DWR-published class for modifying SAHA and HAKO data.
 * 
 */
public class ResourceEditService {

	private static final String OBJECT_PROPERTY_TEMPLATE = "saha3/standalone/objectProperty.ftl";
	private static final String LITERAL_PROPERTY_TEMPLATE = "saha3/standalone/literalProperty.ftl";
	private static final String LITERAL_PROPERTY_EDITOR_TEMPLATE = "saha3/standalone/literalPropertyEditor.ftl";
	private static final String EDITOR_PROPERTY_TABLE_TEMPLATE = "saha3/standalone/editorPropertyTable.ftl";
	private static final String PROPERTY_TABLE_TEMPLATE = "saha3/standalone/propertyTable.ftl";
	private static final String HAKO_PROPERTY_TABLE_TEMPLATE = "saha3/standalone/hakoPropertyTable.ftl";
	private static final String HAKO_TIMEMAP_EVENT_TEMPLATE = "saha3/standalone/hakoTimemapEvent.ftl";

	private final Logger log = Logger.getLogger(getClass());

	private SahaProjectRegistry sahaProjectRegistry;
	private Configuration configuration;
	private OnkiWebService onkiWebService;

	@Required
	public void setOnkiWebService(OnkiWebService onkiWebService) {
		this.onkiWebService = onkiWebService;
	}

	@Required
	public void setFreeMarkerConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
		this.sahaProjectRegistry = sahaProjectRegistry;
	}

	public String setObjectProperty(String model, String id, String s,
			String p, String o, HttpServletRequest request) {
		try {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.lock();

			Locale locale = RequestContextUtils.getLocale(request);

			SahaProject project = sahaProjectRegistry.getSahaProject(model);
			UriLabel object = project.addObjectProperty(s, p, o, locale);

			Map<String, Object> modelMap = buildMap(id, model, s, p, object);

			return FreeMarkerUtil.process(configuration,
					OBJECT_PROPERTY_TEMPLATE, modelMap);
		} finally {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.unlock();
		}
	}

	public String createInstance(String model, String type, String label,
			HttpServletRequest request) {
		try {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.lock();

			SahaProject project = sahaProjectRegistry.getSahaProject(model);
			return project.createResource(type, label);
		} finally {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.unlock();
		}
	}

	public String setLiteralProperty(String model, String id, String s,
			String p, String l, String lang) {
		try {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.lock();

			SahaProject project = sahaProjectRegistry.getSahaProject(model);
			UriLabel object = (lang == null) ? project.addLiteralProperty(s, p,
					l) : project.addLiteralProperty(s, p, l, lang);

			Map<String, Object> modelMap = buildMap(id, model, s, p, object);

			return FreeMarkerUtil.process(configuration,
					LITERAL_PROPERTY_TEMPLATE, modelMap);
		} finally {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.unlock();
		}
	}

	public String updateLiteralProperty(String model, String id, String s,
			String p, String l, String lang, String oldValueShaHex) {
		try {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.lock();

			SahaProject project = sahaProjectRegistry.getSahaProject(model);

			project.removeLiteralProperty(s, p, oldValueShaHex);
			UriLabel object = (lang == null) ? project.addLiteralProperty(s, p,
					l) : project.addLiteralProperty(s, p, l, lang);

			Map<String, Object> modelMap = buildMap(id, model, s, p, object);

			return FreeMarkerUtil.process(configuration,
					LITERAL_PROPERTY_TEMPLATE, modelMap);
		} finally {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.unlock();
		}
	}

	public boolean setMapProperty(String model, String s, String fc,
			String value) {

		try {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.lock();

			SahaProject project = sahaProjectRegistry.getSahaProject(model);
			project.setMapProperty(s, fc, value);

			return true;
		} finally {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.unlock();
		}
	}

	public String getLiteralPropertyEditor(String model, String id, String s,
			String p, String valueShaHex, HttpServletRequest request) {

		SahaProject project = sahaProjectRegistry.getSahaProject(model);
		Locale locale = RequestContextUtils.getLocale(request);

		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put("id", id);
		modelMap.put("model", model);
		modelMap.put("resourceUri", s);
		modelMap.put("inline", true);

		for (ISahaProperty property : project.getResource(s, locale)
				.getProperties())
			if (property.getValueShaHex().equals(valueShaHex))
				modelMap.put("property", property);

		if (!modelMap.containsKey("property"))
			throw new RuntimeException(
					"Trying to edit a non-existent literal value: " + s + " "
							+ p + " " + valueShaHex);

		return FreeMarkerUtil.process(configuration,
				LITERAL_PROPERTY_EDITOR_TEMPLATE, modelMap);
	}

	public boolean removeLiteralProperty(String model, String s, String p,
			String valueShaHex) {
		try {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.lock();

			SahaProject project = sahaProjectRegistry.getSahaProject(model);
			return project.removeLiteralProperty(s, p, valueShaHex);
		} finally {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.unlock();
		}
	}

	public boolean removeResource(String model, String uri) {
		try {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.lock();

			SahaProject project = sahaProjectRegistry.getSahaProject(model);
			return project.removeResource(uri);
		} finally {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.unlock();
		}
	}

	public boolean removeObjectProperty(String model, String s, String p,
			String o) {
		try {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.lock();

			SahaProject project = sahaProjectRegistry.getSahaProject(model);
			return project.removeObjectProperty(s, p, o);
		} finally {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.unlock();
		}
	}

	public boolean removeProperty(String model, String s, String p) {
		try {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.lock();

			SahaProject project = sahaProjectRegistry.getSahaProject(model);
			return project.removeProperty(s, p);
		} finally {
			this.sahaProjectRegistry.getLockForProject(model).writeLock()
					.unlock();
		}
	}

	private Map<String, Object> buildMap(String id, String model,
			String resourceUri, String propertyUri, UriLabel object) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put("id", id);
		modelMap.put("model", model);
		modelMap.put("resourceUri", resourceUri);
		modelMap.put("propertyUri", propertyUri);
		modelMap.put("propertyValueUri", object.getUri());
		modelMap.put("propertyValueLang", object.getLang());
		modelMap.put("propertyValueLabel", object.getLabel());
		modelMap.put("propertyValueShaHex", object.getLabelShaHex());
		return modelMap;
	}

	public String getEditorPropertyTable(String model, String resourceUri,
			String id, HttpServletRequest request) {

		Locale locale = RequestContextUtils.getLocale(request);
		SahaProject project = sahaProjectRegistry.getSahaProject(model);

		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put("model", model);
		modelMap.put("instance", project.getResource(resourceUri, locale));
		modelMap.put("editorId", id);

		return FreeMarkerUtil.process(configuration,
				EDITOR_PROPERTY_TABLE_TEMPLATE, modelMap);
	}

	public String getPropertyTable(String model, String resourceUri,
			HttpServletRequest request) {

		Locale locale = RequestContextUtils.getLocale(request);
		SahaProject project = sahaProjectRegistry.getSahaProject(model);

		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put("model", model);
		try {
			modelMap.put("gmaputil", BeansWrapper.getDefaultInstance()
					.getStaticModels()
					.get("fi.seco.semweb.util.GoogleMapsUtil"));
		} catch (TemplateModelException e) {
			e.printStackTrace();
		}
		modelMap.put("propertyMapEntrySet",
				project.getResource(resourceUri, locale)
						.getPropertyMapEntrySet());

		return FreeMarkerUtil.process(configuration, PROPERTY_TABLE_TEMPLATE,
				modelMap);
	}

	public String getHakoPropertyTable(String model, String resourceUri,
			HttpServletRequest request) {

		Locale locale = RequestContextUtils.getLocale(request);
		SahaProject project = sahaProjectRegistry.getSahaProject(model);

		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put("model", model);
		modelMap.put("propertyMapEntrySet",
				project.getResource(resourceUri, locale)
						.getPropertyMapEntrySet());

		return FreeMarkerUtil.process(configuration,
				HAKO_PROPERTY_TABLE_TEMPLATE, modelMap);
	}

	private JSONArray parseCoordinates(String rawString) throws JSONException {
		JSONArray coordArr = new JSONArray();
		for (String pair : rawString.split(" ")) {
			JSONObject tmp = new JSONObject();
			String latlong[] = pair.split(",");
			tmp.put("lat", latlong[0]);
			tmp.put("lon", latlong[1]);
			coordArr.put(tmp);
		}
		return coordArr;
	}

	private JSONObject parsePointCoordinates(String rawString)
			throws JSONException {
		JSONObject tmp = new JSONObject();
		for (String pair : rawString.split(" ")) {
			String latlong[] = pair.split(",");
			tmp.put("lat", latlong[0]);
			tmp.put("lon", latlong[1]);
		}
		return tmp;
	}

	public String getHakoTimemapEvents(String model, String pRequestUris,
			HttpServletRequest request) {
		Locale locale = RequestContextUtils.getLocale(request);
		SahaProject project = sahaProjectRegistry.getSahaProject(model);
		String jsonError = "{\"error\": \"error in JSON conversion\"}";
		JSONArray requestUris;
		try {
			requestUris = new JSONArray(pRequestUris);
		} catch (JSONException err) {
			return jsonError;
		}

		JSONObject result = new JSONObject();
		try {
			for (int i = 0; i < requestUris.length(); ++i) {
				JSONObject obj = new JSONObject();
				JSONObject tmpX = new JSONObject();
				String resourceUri = requestUris.getString(i);
				Set<Entry<UriLabel, Set<ISahaProperty>>> propertyMap = project
						.getResource(resourceUri, locale)
						.getPropertyMapEntrySet();

				for (Entry<UriLabel, Set<ISahaProperty>> key : propertyMap) {

					if (key.getKey()
							.getUri()
							.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
						for (ISahaProperty entry : key.getValue()) {
							obj.put("label", entry.getValueLabel());
						}
					} else if (key
							.getKey()
							.getUri()
							.equals("http://www.w3.org/2004/02/skos/core#prefLabel")) {
						for (ISahaProperty entry : key.getValue()) {
							obj.put("label", entry.getValueLabel());
						}
					} else if (key
							.getKey()
							.getUri()
							.equals("http://www.hatikka.fi/havainnot/date_collected")) {
						for (ISahaProperty entry : key.getValue()) {
							obj.put("time", entry.getValueLabel());
							obj.put("earliestStart", entry.getValueLabel()
									+ " 00:00");
							obj.put("earliestEnd", entry.getValueLabel()
									+ " 23:59");
						}
					} else if (key
							.getKey()
							.getUri()
							.equals("http://www.w3.org/2003/01/geo/wgs84_pos#lat")) {
						for (ISahaProperty entry : key.getValue()) {
							tmpX.put("latitude", entry.getValueLabel());
						}

					} else if (key
							.getKey()
							.getUri()
							.equals("http://www.w3.org/2003/01/geo/wgs84_pos#long")) {
						for (ISahaProperty entry : key.getValue()) {
							tmpX.put("longitude", entry.getValueLabel());
						}
					} else if (key.getKey().getUri()
							.equals("http://schema.onki.fi/poi#hasPolygon")) {
						for (ISahaProperty entry : key.getValue()) {
							obj.append("geo_polygons",
									parseCoordinates(entry.getValueLabel()));
						}
					} else if (key.getKey().getUri()
							.equals("http://schema.onki.fi/poi#hasPoint")) {
						for (ISahaProperty entry : key.getValue()) {
							obj.append(
									"geo_points",
									parsePointCoordinates(entry.getValueLabel()));
						}
					}
				}
				if (tmpX.has("latitude") && tmpX.has("longitude")) {
					JSONObject tmpY = new JSONObject();
					tmpY.put("latitude", tmpX.get("latitude"));
					tmpY.put("longitude", tmpX.get("longitude"));
					obj.append("geo_points", tmpY);
				}
				result.append("results", obj);
			}
		} catch (JSONException exception) {
			return jsonError;
		}

		return result.toString();
	}

	public String getExternalPropertyTable(String ontology, String resourceUri,
			HttpServletRequest request) {

		Locale locale = RequestContextUtils.getLocale(request);

		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put("model", ontology);
		try {
			modelMap.put("gmaputil", BeansWrapper.getDefaultInstance()
					.getStaticModels()
					.get("fi.seco.semweb.util.GoogleMapsUtil"));
		} catch (TemplateModelException e) {
			e.printStackTrace();
		}
		modelMap.put("propertyMapEntrySet",
				onkiWebService.getOnkiRepository(ontology)
						.getPropertyMapEntrySet(resourceUri, locale));

		return FreeMarkerUtil.process(configuration, PROPERTY_TABLE_TEMPLATE,
				modelMap);
	}

}
