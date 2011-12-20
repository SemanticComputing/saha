package fi.seco.saha3.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.index.ResourceIndex;
import fi.seco.saha3.index.ResourceIndexSearcher;
import fi.seco.saha3.infrastructure.OnkiWebService;
import fi.seco.saha3.infrastructure.OnkiWebService.OnkiRepository;
import fi.seco.saha3.model.configuration.ConfigService;
import fi.seco.saha3.model.configuration.RepositoryConfig;
import fi.seco.saha3.util.SAHA3;
import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

/**
 * Class used for write operations performed in SAHA. Keeps both the RDF
 * data model and the Lucene index consistent with each other.
 * 
 */
public class ModelEditor implements IModelEditor {

	private final Logger log = Logger.getLogger(getClass());

	private Model model;
	private ConfigService config;
	private ResourceIndex index;
	private ResourceIndexSearcher searcher;
	private OnkiWebService onkiWebService;
	private boolean allowEditing = true;

	private final Set<String> knownLanguages = new HashSet<String>(Arrays.asList(Locale.getISOLanguages()));

	public static final String WGS84_LAT = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
	public static final String WGS84_LONG = "http://www.w3.org/2003/01/geo/wgs84_pos#long";
	public static final String POLYGON_URI = "http://www.yso.fi/onto/sapo/hasPolygon";
	public static final String ROUTE_URI = "http://www.yso.fi/onto/sapo/hasRoute";

	private boolean listenerDisabled = false;

	@Required
	public void setModel(final Model model) {
		this.model = model;
		model.register(new ModelChangedListener() {

			@Override
			public void removedStatements(Model m) {
				removedStatements(m.listStatements());
			}

			@Override
			public void removedStatements(StmtIterator statements) {
				updateChanged(new IteratorToIIterableIterator<Statement>(statements));
			}

			@Override
			public void removedStatements(List<Statement> statements) {
				updateChanged(statements);
			}

			@Override
			public void removedStatements(Statement[] statements) {
				updateChanged(Arrays.asList(statements));
			}

			private void updateChanged(Iterable<Statement> sts) {
				if (!listenerDisabled) {
					Set<String> affected = new HashSet<String>();
					Set<String> affectedSubjects = new HashSet<String>();
					for (Statement st : sts)
						if (!SAHA3.dateModified.equals(st.getPredicate())) {
							affected.add(st.getSubject().getURI());
							affectedSubjects.add(st.getSubject().getURI());
							if (st.getPredicate().equals(RDFS.subClassOf)) clearSubClassOfCache();
							if (index.getAcceptedLabels().contains(st.getPredicate()))
								for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(null, null, st.getSubject())))
									if (s.getSubject().isURIResource()) affected.add(s.getSubject().getURI());
						}
					for (String r : affectedSubjects)
						updateModifiedTimestamp(r);
					index.indexResource(affected);
				}
			}

			@Override
			public void removedStatement(Statement s) {
				updateChanged(Collections.singleton(s));
			}

			@Override
			public void notifyEvent(Model m, Object event) {}

			@Override
			public void addedStatements(Model m) {
				addedStatements(m.listStatements());
			}

			@Override
			public void addedStatements(StmtIterator statements) {
				updateChanged(new IteratorToIIterableIterator<Statement>(statements));
			}

			@Override
			public void addedStatements(List<Statement> statements) {
				updateChanged(statements);
			}

			@Override
			public void addedStatements(Statement[] statements) {
				updateChanged(Arrays.asList(statements));
			}

			@Override
			public void addedStatement(Statement s) {
				updateChanged(Collections.singleton(s));
			}
		});
	}

	@Required
	public void setConfig(ConfigService config) {
		this.config = config;
	}

	@Required
	public void setIndex(ResourceIndex index) {
		this.index = index;
	}

	@Required
	public void setSearcher(ResourceIndexSearcher searcher) {
		this.searcher = searcher;
	}

	@Required
	public void setOnkiWebService(OnkiWebService onkiWebService) {
		this.onkiWebService = onkiWebService;
	}

	public void setAllowEditing(boolean allowEditing) {
		this.allowEditing = allowEditing;
	}

	@Override
	public synchronized UriLabel addObjectProperty(String s, String p, String o, Locale locale) {
		if (allowEditing) {
			checkAndAddExternalResources(p, o, locale);
			model.add(model.createResource(s), model.createProperty(p), model.createResource(o));
		}
		return new UriLabel(o, searcher.getLabel(o, locale));
	}

	private void checkAndAddExternalResources(String propertyUri, String objectUri, Locale locale) {
		if (onkiWebService != null && !model.containsResource(model.createResource(objectUri)))
			for (RepositoryConfig repositoryConfig : config.getPropertyConfig(propertyUri).getRepositoryConfigs()) {
				OnkiRepository onkiRepository = onkiWebService.getOnkiRepository(repositoryConfig.getSourceName());
				createResource(objectUri, null, null);

				for (ISahaProperty property : onkiRepository.getProperties(objectUri, locale))
					if ((property.getUri().equals(WGS84_LAT) || property.getUri().equals(WGS84_LONG) || property.getUri().equals(POLYGON_URI) || property.getUri().equals(ROUTE_URI)) && property.isLiteral())
						addLiteralProperty(objectUri, property.getUri(), property.getValueLabel());

				Set<Locale> locales = new HashSet<Locale>(index.getAcceptedLocales());
				locales.add(locale);
				for (Locale l : locales) {
					String label = onkiRepository.getLabel(objectUri, l);
					if (!label.equals(objectUri))
						addLiteralProperty(objectUri, config.getLabelProperty(), label, l.getLanguage());
				}
			}
	}

	@Override
	public synchronized boolean removeObjectProperty(String s, String p, String o) {
		if (allowEditing) {
			model.remove(model.createResource(s), model.createProperty(p), model.createResource(o));
			return true;
		}
		return false;
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l) {
		return addLiteralProperty(s, p, model.createLiteral(l));
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l, String lang) {
		lang = lang.trim();
		return knownLanguages.contains(lang) ? addLiteralProperty(s, p, model.createLiteral(l, lang)) : addLiteralProperty(s, p, model.createLiteral(l));
	}

	private UriLabel addLiteralProperty(String s, String p, Literal l) {
		if (allowEditing) {
			Resource subject = model.createResource(s);
			Property predicate = model.createProperty(p);
			model.add(subject, predicate, l);
		}
		return new UriLabel("", l.getLanguage(), l.getString());
	}

	@Override
	public synchronized boolean addModel(Model m) {
		if (allowEditing) {
			model.add(m);
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean readModel(InputStream in, String lang) {
		if (allowEditing) {
			listenerDisabled = true;
			model.read(in, "", lang);
			listenerDisabled = false;

    		// Re-add internal configuration from model and remove the handled triples
			config.addConfigFromModel(model);

			clearSubClassOfCache();
			index.clear();
			index.reindex();
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean removeProperty(String s, String p) {
		if (allowEditing) {
			Resource subject = model.createResource(s);
			Property predicate = model.createProperty(p);
			model.removeAll(subject, predicate, null);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeLiteralProperty(String s, String p, String valueShaHex) {
		for (Statement statement : new IteratorToIIterableIterator<Statement>(model.createResource(s).listProperties(model.createProperty(p))))
			if (statement.getObject().isLiteral())
				if (DigestUtils.shaHex(statement.getObject().toString()).equals(valueShaHex))
					return removeLiteralProperty(s, p, statement.getLiteral());
		log.warn("Failed to remove literal property. s: " + s + " p: " + p + " valueShaHex: " + valueShaHex);
		return false;
	}

	private synchronized boolean removeLiteralProperty(String s, String p, Literal l) {
		if (allowEditing) {
			Resource subject = model.createResource(s);
			Property predicate = model.createProperty(p);
			model.remove(subject, predicate, l);
			return true;
		}
		return false;
	}

	@Override
	public String createResource(String type, String label) {
        return createResource(generateRandomUri(config.getNamespace()),type,label);
	}

	@Override
	public synchronized String createResource(String uri, String type, String label) {
		if (allowEditing) {
			Resource resource = model.createResource(uri);
			if (type != null && !type.isEmpty()) model.add(resource, RDF.type, model.createResource(type));
			if (label != null && !label.isEmpty())
				model.add(resource, model.createProperty(config.getLabelProperty()), label);
			addCreatedTimestamp(uri);
		}
		return uri;
	}

    public static String generateRandomUri(String namespace) {
        return namespace + "u" + UUID.randomUUID().toString();
	}

	@Override
	public synchronized boolean removeResource(String uri) {
		if (allowEditing) {
			List<Statement> trash = new ArrayList<Statement>();
			Set<String> modified = new HashSet<String>();
			for (Statement s : new IteratorToIIterableIterator<Statement>(model.createResource(uri).listProperties()))
				trash.add(s);
			for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(null, null, model.createResource(uri)))) {
				if (s.getSubject().isURIResource()) modified.add(s.getSubject().getURI());
				trash.add(s);
			}
			model.remove(trash);
			return true;
		}
		return false;
	}

	public boolean setMapProperty(String s, String fc, String value) {
		boolean success = false;

		removeProperty(s, WGS84_LAT);
		removeProperty(s, WGS84_LONG);
		removeProperty(s, POLYGON_URI);
		removeProperty(s, ROUTE_URI);

		// Remove existing
		if (fc == null || value == null || fc.isEmpty() || value.isEmpty()) return true;

		if (fc.equals("singlepoint")) {
			String[] parts = value.split(",");
			addLiteralProperty(s, WGS84_LAT, parts[0]);
			addLiteralProperty(s, WGS84_LONG, parts[1]);
			success = true;
		} else if (fc.equals("polygon")) {
			addLiteralProperty(s, POLYGON_URI, value);
			success = true;
		} else if (fc.equals("route")) {
			addLiteralProperty(s, ROUTE_URI, value);
			success = true;
		}

		return success;
	}

	private void addCreatedTimestamp(String uri) {
		model.add(model.createLiteralStatement(model.createResource(uri), SAHA3.dateCreated, model.createTypedLiteral(Calendar.getInstance())));
	}

	private void updateModifiedTimestamp(String uri) {
		Resource r = model.createResource(uri);
		model.removeAll(r, SAHA3.dateModified, null);
		model.add(r, SAHA3.dateModified, model.createTypedLiteral(Calendar.getInstance()));
	}

	private void clearSubClassOfCache() {
		index.clearSubClassOfCache();
	}

	@Override
	public synchronized boolean clear() {
		if (allowEditing) {
			listenerDisabled = true;
			model.removeAll();
			listenerDisabled = false;
			index.clear();
			index.reindex(); // init empty index
			return true;
		}
		return false;
	}

	public boolean reindexFromModel() {
		if (allowEditing) {
			index.clear();
			index.reindex();
			return true;
		}
		return false;
	}
}
