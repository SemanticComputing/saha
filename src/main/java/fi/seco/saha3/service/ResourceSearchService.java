package fi.seco.saha3.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.support.RequestContextUtils;

import fi.seco.saha3.infrastructure.OnkiWebService;
import fi.seco.saha3.infrastructure.OnkiWebService.OnkiRepository;
import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.model.IModelReader;
import fi.seco.saha3.model.IResults;
import fi.seco.saha3.model.ISahaResource;
import fi.seco.saha3.model.UriLabel;
import fi.seco.saha3.model.configuration.PropertyConfig;
import fi.seco.saha3.model.configuration.RepositoryConfig;

/**
 * DWR-published class for SAHA autocompletion searches.
 * 
 */
public class ResourceSearchService implements Controller {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ResourceSearchService.class);

	private SahaProjectRegistry sahaProjectRegistry;
	private OnkiWebService onkiWebService;
	private final static int DEFAULT_MAX_RESULTS = 15;

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
		this.sahaProjectRegistry = sahaProjectRegistry;
	}

	public void setOnkiWebService(OnkiWebService onkiWebService) {
		this.onkiWebService = onkiWebService;
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String model = request.getParameter("model");
		if (model == null || model.isEmpty()) return null;

		IModelReader project = sahaProjectRegistry.getModelReader(model);
		Locale locale = RequestContextUtils.getLocale(request);

		String query = parseQuery(request.getParameter("name"));

		List<String> range = arrayToList(request.getParameterValues("r"));

		List<String> domain = new ArrayList<String>();
		String resourceUri = request.getParameter("resource");
		if (resourceUri != null && !resourceUri.isEmpty()) {
			ISahaResource resource = project.getResource(resourceUri, locale);
			for (UriLabel type : resource.getTypes())
				domain.add(type.getUri());
		}

		PropertyConfig propertyConfig = null;
		String propertyUri = request.getParameter("property");
		if (propertyUri != null && !propertyUri.isEmpty())
			propertyConfig = sahaProjectRegistry.getConfig(model).getPropertyConfig(propertyUri);

		JSONArray resultItems = new JSONArray();

		int maxResults = DEFAULT_MAX_RESULTS;

		if (query.endsWith("*")) {
			if (query.length() > 1) query = query.substring(0, query.length() - 1);

			if (query.endsWith("+")) {
				query = query.substring(0, query.length() - 1);
				maxResults = 1000;
			}

			Set<String> usedKeys = new HashSet<String>();
			// local search
			if (!(propertyConfig != null && propertyConfig.isDenyLocalReferences())) {
				IResults results = project.search(query, null, range, locale, maxResults);
				parseResult(resultItems, results, usedKeys, maxResults);
			}

			// onki search
			if (propertyConfig != null)
				for (RepositoryConfig config : propertyConfig.getRepositoryConfigs()) {
					String ontologyName = config.getSourceName();
					OnkiRepository repository = onkiWebService.getOnkiRepository(ontologyName);
					IResults results = repository.search(query, config.getParentRestrictions(), config.getTypeRestrictions(), locale, maxResults);
					parseResult(ontologyName, resultItems, results, usedKeys, maxResults);
				}

		} else resultItems.put(buildObject("", ""));

		JSONObject result = new JSONObject();
		result.put("identifier", "uri");
		result.put("items", resultItems);

		response.setContentType("text/plain;charset=UTF-8");
		response.getWriter().write(result.toString());

		return null;
	}

	private String parseQuery(String query) {
		return query == null ? "" : query;
	}

	private List<String> arrayToList(String[] types) {
		return types == null ? new ArrayList<String>() : Arrays.asList(types);
	}

	private String getSeparator(String ontologyName, int size, int maxResults) {
		return getSeparator("<em>results from </em><strong>" + ontologyName + "</strong><em> ontology</em> " + parseSizeInfo(size, maxResults));
	}

	private String getSeparator(String label) {
		return "<div style=\"margin:0;padding:0;padding-top:2px;border-top:thin solid grey;\">" + label + "</div>";
	}

	private String parseSizeInfo(int size, int maxResults) {
		if (size > maxResults) return "(showing top " + maxResults + " of " + size + " results)";
		return "";
	}

	private void parseResult(JSONArray array, IResults results, Set<String> usedKeys, int maxResults) throws JSONException {
		parseResult("", array, results, usedKeys, maxResults);
	}

	private void parseResult(String name, JSONArray array, IResults results, Set<String> usedKeys, int maxResults) throws JSONException {
		if (!name.isEmpty() && results.getSize() > 0)
			array.put(buildObject(name, getSeparator(name, results.getSize(), maxResults)));
		for (IResults.IResult result : results)
			if (usedKeys.add(result.getUri())) array.put(buildObject(result, name));
	}

	private JSONObject buildObject(IResults.IResult result, String name) throws JSONException {
		return buildObject(result.getUri(), result.getLabel(), result.getAltLabels(), name);
	}

	private JSONObject buildObject(String uri, String label, List<String> altLabels, String name) throws JSONException {
		if (!altLabels.isEmpty()) return buildObject(uri, parseTooltip(uri, name, label, parseAltLabels(altLabels)));
		return buildObject(uri, parseTooltip(uri, name, label, ""));
	}

	private String parseTooltip(String resourceUri, String ontology, String label, String altLabels) {
		if (ontology.isEmpty())
			return "<span onMouseOver=\"showResourceTooltip(this,'" + resourceUri + "')\" " + "onMouseOut=\"hideResourceTooltip(this)\">" + label + "</span>" + altLabels;
		return "<span onMouseOver=\"showExternalResourceTooltip(this,'" + ontology + "','" + resourceUri + "')\" " + "onMouseOut=\"hideResourceTooltip(this)\">" + label + "</span>" + altLabels;
	}

	private String parseAltLabels(List<String> altLabels) {
		StringBuilder buffer = new StringBuilder();
		for (String altLabel : altLabels)
			buffer.append("<div style=\"margin-left:5px;color:#666;\">&rarr;" + altLabel + "</div>");
		return buffer.toString();
	}

	private JSONObject buildObject(String uri, String name) throws JSONException {
		JSONObject object = new JSONObject();
		object.put("uri", uri);
		object.put("name", name);
		return object;
	}
}
