package fi.seco.saha3.service;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.hp.hpl.jena.rdf.model.Model;

import fi.helsinki.cs.seco.onki.service.ArrayOfOnkiQueryResult;
import fi.helsinki.cs.seco.onki.service.ArrayOfStatement;
import fi.helsinki.cs.seco.onki.service.ArrayOfString;
import fi.helsinki.cs.seco.onki.service.GetLabel;
import fi.helsinki.cs.seco.onki.service.GetLabelResponse;
import fi.helsinki.cs.seco.onki.service.GetProperties;
import fi.helsinki.cs.seco.onki.service.GetPropertiesResponse;
import fi.helsinki.cs.seco.onki.service.ObjectFactory;
import fi.helsinki.cs.seco.onki.service.OnkiQueryResult;
import fi.helsinki.cs.seco.onki.service.OnkiQueryResults;
import fi.helsinki.cs.seco.onki.service.OnkiQueryResultsMetadata;
import fi.helsinki.cs.seco.onki.service.Search;
import fi.helsinki.cs.seco.onki.service.SearchResponse;
import fi.helsinki.cs.seco.onki.service.Statement;
import fi.seco.saha3.model.IResults;
import fi.seco.saha3.model.IResults.IResult;
import fi.seco.saha3.model.ISahaProperty;
import fi.seco.saha3.model.ISahaResource;
import fi.seco.saha3.model.SahaProject;

@Endpoint
public class OnkiService {

	private static final Logger log = LoggerFactory.getLogger(OnkiService.class);

	private Model m;
	private SahaProject spr;

	@Required
	public void setModel(Model m) {
		this.m = m;
	}

	@Required
	public void setSahaProject(SahaProject spr) {
		this.spr = spr;
	}

	private static final ObjectFactory of = new ObjectFactory();

	@PayloadRoot(namespace = "http://service.onki.seco.cs.helsinki.fi", localPart = "getLabel")
	@ResponsePayload
	public GetLabelResponse getLabel(@RequestPayload GetLabel gl) {
		GetLabelResponse ret = of.createGetLabelResponse();
		OnkiQueryResult r = of.createOnkiQueryResult();
		Locale l = null;
		if (gl.getIn1() != null) l = new Locale(gl.getIn1());
		r.setTitle(of.createOnkiQueryResultTitle(spr.getResource(gl.getIn0(), l).getLabel()));
		r.setUri(of.createOnkiQueryResultUri(gl.getIn0()));
		ret.setOut(r);
		return ret;
	}

	@PayloadRoot(namespace = "http://service.onki.seco.cs.helsinki.fi", localPart = "search")
	@ResponsePayload
	public SearchResponse search(@RequestPayload Search s) {
		String inquery = s.getIn0();
		Locale lang = null;
		if (s.getIn1() != null) lang = new Locale(s.getIn1());
		Integer maxHits = s.getIn2();
		ArrayOfString types = s.getIn3();
		ArrayOfString parents = s.getIn4();
		ArrayOfString groups = s.getIn5();
		if (maxHits == null) maxHits = 50;
		OnkiQueryResults r = of.createOnkiQueryResults();
		OnkiQueryResultsMetadata m = of.createOnkiQueryResultsMetadata();
		ArrayOfOnkiQueryResult res = of.createArrayOfOnkiQueryResult();
		List<OnkiQueryResult> res2 = res.getOnkiQueryResult();
		IResults qr = spr.search(inquery, parents != null ? parents.getString() : null, types != null ? types.getString() : null, lang, maxHits);
		for (IResult qres : qr) {
			OnkiQueryResult oqr = of.createOnkiQueryResult();
			oqr.setAltLabel(of.createOnkiQueryResultAltLabel(qres.getAltLabel()));
			oqr.setTitle(of.createOnkiQueryResultTitle(qres.getLabel()));
			oqr.setSerkki(of.createOnkiQueryResultTitle(qres.getLabel()));
			oqr.setUri(of.createOnkiQueryResultUri(qres.getUri()));
			int pl = qres.getUri().lastIndexOf('#');
			if (pl == -1) pl = qres.getUri().lastIndexOf('/');
			if (pl != -1)
				oqr.setNamespacePrefix(of.createOnkiQueryResultNamespacePrefix(this.m.getNsURIPrefix(qres.getUri().substring(0, pl + 1))));
			res2.add(oqr);
		}
		/*		Collections.sort(res2, new Comparator<OnkiQueryResult>() {
					@Override
					public int compare(OnkiQueryResult o1, OnkiQueryResult o2) {
						String s1 = o1.getAltLabel().getValue();
						if (s1 == null) s1 = o1.getTitle().getValue();
						String s2 = o2.getAltLabel().getValue();
						if (s2 == null) s2 = o2.getTitle().getValue();
						if (s1 == null) {
							if (s2 == null) return 0;
							return 1;
						}
						if (s2 == null) return -1;
						return s1.compareTo(s2);
					}

				}); */
		m.setTotalHitsAmount(qr.getSize());
		int mha = qr.getSize() - res2.size();
		m.setMoreHitsAmount(mha);
		m.setMoreHits(mha > 0);
		r.setMetadata(of.createOnkiQueryResultsMetadata(m));
		r.setResults(of.createOnkiQueryResultsResults(res));
		SearchResponse sr = of.createSearchResponse();
		sr.setOut(r);
		return sr;
	}

	@PayloadRoot(namespace = "http://service.onki.seco.cs.helsinki.fi", localPart = "getProperties")
	@ResponsePayload
	public GetPropertiesResponse getProperties(@RequestPayload GetProperties req) {
		String uri = req.getIn0();
		ArrayOfString queriedProperties = req.getIn1();
		Locale lang = null;
		if (req.getIn2() != null) lang = new Locale(req.getIn2());
		ArrayOfStatement tret = of.createArrayOfStatement();
		List<Statement> ret = tret.getStatement();
		ISahaResource r = spr.getResource(uri, lang);
		for (ISahaProperty p : r.getProperties()) {
			Statement t = of.createStatement();
			t.setPredicateUri(of.createStatementPredicateUri(p.getUri()));
			t.setPredicateLabel(of.createStatementPredicateLabel(p.getLabel()));
			if (p.isLiteral()) {
				t.setValue(of.createStatementValue(p.getValueLabel()));
				t.setLang(of.createStatementLang(p.getValueLang()));
			} else {
				t.setUri(of.createStatementUri(p.getValueUri()));
				t.setLabel(of.createStatementLabel(p.getValueLabel()));
			}
			ret.add(t);
		}
		GetPropertiesResponse gpr = of.createGetPropertiesResponse();
		gpr.setOut(tret);
		return gpr;
	}

}
