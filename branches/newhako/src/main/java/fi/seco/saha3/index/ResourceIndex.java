package fi.seco.saha3.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.semweb.util.ImmutableLString;
import fi.seco.semweb.util.LRUCache;
import fi.seco.semweb.util.LString;
import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

/**
 * The main class to control the Lucene index which is used in many read and
 * write operations (alongside the simple TDB RDF graph index) to speed things
 * up.
 *
 */
public class ResourceIndex {

	protected class Lock {
		private boolean lock;
		private final TimerTask lockTimeoutTask;

		private Lock() {
			this.lock = true;
			this.lockTimeoutTask = new TimerTask() {
				@Override
				public void run() {
					log.warn("Lock timeout: releasing index read lock.");
					releaseLock();
				}
			};
			timer.schedule(lockTimeoutTask, TimeUnit.SECONDS.toMillis(20));
		}

		public synchronized void releaseLock() {
			if (lock) {
				lockTimeoutTask.cancel();
				releaseReadLock();
				lock = false;
			}
		}
	}

	public final static String UBER_FIELD_NAME = "term";
	public final static String TYPE_FIELD_NAME = "type";
	public final static String LABEL_FIELD_NAME = "label";
	public final static String URI_FIELD = "_uri";

	private final Set<String> stopWords = new HashSet<String>();
	private final Analyzer analyzer = new StandardLatinFilterAnalyzer(stopWords);

	private final Logger log = Logger.getLogger(getClass());

	private final Set<Property> acceptedLabels = new HashSet<Property>(Arrays.asList(RDFS.label, ResourceFactory.createProperty("http://www.w3.org/2008/05/skos#prefLabel"), ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"), ResourceFactory.createProperty("http://www.seco.tkk.fi/applications/saha#conceptLabel")));

	private final Set<Locale> acceptedLocales = new HashSet<Locale>(Arrays.asList(new Locale("fi"), new Locale("sv"), new Locale("en")));

	private final Model model;

	private int readLock = 0;
	private boolean writeLock = false;

	private final Timer timer = new Timer();

	private LRUCache<String, ImmutableLString> labelCache;
	private LRUCache<String, String[]> typeCache;
	private SubjectObjectCache subClassOfCache;

	private final Directory directory;
	private IndexWriter indexWriter;
	private IndexReader indexReader;
	private IndexSearcher indexSearcher;

	private final int indexMergeFactor = 4;
	private int cacheSize = 50000;

	public ResourceIndex(Model model, String path, boolean ramIndex) {
		BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
		this.model = model;
		this.directory = openDirectory(path, ramIndex);
		if (ramIndex) this.cacheSize = 5000;
		initLabelProperties();
		initCaches();
		initIndex();
	}

	private Directory openDirectory(String path, boolean ramIndex) {
		if (ramIndex) return new RAMDirectory();
		try {
			return FSDirectory.open(new File(getIndexPath(path)));
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}

	private String getIndexPath(String projectPath) {
		return projectPath.endsWith("/") ? projectPath + "index" : projectPath + "/index";
	}

	private void initLabelProperties() {
		for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(null, RDF.type, OWL.DatatypeProperty))) {
			String uri = s.getSubject().getURI();
			if (uri != null && uri.endsWith("prefLabel")) acceptedLabels.add(ResourceFactory.createProperty(uri));
		}
	}

	private void initCaches() {
		subClassOfCache = new SubjectObjectCache(model, RDFS.subClassOf);
		labelCache = new LRUCache<String, ImmutableLString>(cacheSize);
		typeCache = new LRUCache<String, String[]>(cacheSize);
	}

	private void initIndex() {
		log.info("Loading index from: " + directory);
		try {
			openWriter();
			openSearcher();
			reindexIfEmpty();
		} catch (IOException e) {
			log.error("", e);
		}
	}

	private void openWriter() throws IOException {
		if (indexWriter != null) indexWriter.close();
		indexWriter = new IndexWriter(directory, getAnalyzer(), createIndex(directory), IndexWriter.MaxFieldLength.UNLIMITED);
		indexWriter.setMergeFactor(indexMergeFactor);
	}

	private boolean createIndex(Directory dir) {
		if (dir instanceof FSDirectory) return !((FSDirectory) dir).getFile().exists();
		return true;
	}

	private void openSearcher() throws IOException {
		if (indexReader == null || indexSearcher == null) {
			indexReader = IndexReader.open(directory, true);
			indexSearcher = new IndexSearcher(indexReader);
		} else {
			IndexReader newIndexReader = indexReader.reopen();
			if (newIndexReader != indexReader) {
				indexReader.close();
				indexReader = newIndexReader;
				indexSearcher.close();
				indexSearcher = new IndexSearcher(indexReader);
			}
		}
	}

	private void reindexIfEmpty() throws IOException {
		if (indexWriter.numDocs() == 0) reindex();
	}

	// acquire read lock for searcher (released through lockObject.releaseLock())
	protected synchronized Lock acquireLock() {
		acquireReadLock();
		return new Lock();
	}

	private synchronized void acquireReadLock() {
		while (writeLock)
			try {
				wait();
			} catch (InterruptedException e) {
				log.error("", e);
				return;
			}
		readLock++;
	}

	private synchronized void releaseReadLock() {
		readLock--;
		notifyAll();
	}

	private synchronized void acquireWriteLock() {
		while (readLock > 0 || writeLock)
			try {
				wait();
			} catch (InterruptedException e) {
				log.error("", e);
				return;
			}
		writeLock = true;
	}

	private synchronized void releaseWriteLock() {
		writeLock = false;
		notifyAll();
	}

	public Set<Locale> getAcceptedLocales() {
		return acceptedLocales;
	}

	public Set<Property> getAcceptedLabels() {
		return acceptedLabels;
	}

	protected Analyzer getAnalyzer() {
		return analyzer;
	}

	// should be called only within read/write locked blocks
	protected IndexSearcher getSearcher() {
		return indexSearcher;
	}

	// should be called only within read/write locked blocks
	protected IndexReader getReader() {
		return indexReader;
	}

	public synchronized void clear() {
		acquireWriteLock();
		initCaches();
		try {
			indexWriter.deleteAll();
			indexWriter.commit();
			openSearcher();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			releaseWriteLock();
		}
	}

	public synchronized void close() {
		try {
			timer.cancel();
			indexReader.close();
			indexWriter.close();
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public void reindex() {
		acquireWriteLock();

		log.info("Indexing...");
		long start = System.currentTimeMillis();

		initCaches();

		try {
			indexWriter.setMergeFactor(15);

			Set<String> indexed = new HashSet<String>();
			for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements())) {
				Resource subject = s.getSubject();
				if (subject.isURIResource() && !indexed.contains(subject.getURI())) {
					indexWriter.addDocument(buildDocument(subject));
					indexed.add(subject.getURI());
				}
			}
			log.info("Optimizing...");
			indexWriter.optimize();
			indexWriter.commit();
			log.info("Indexed " + indexWriter.numDocs() + " documents in " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds.");

			indexWriter.setMergeFactor(indexMergeFactor);
			openSearcher();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			releaseWriteLock();
		}
	}

	public void indexResource(Set<String> uris) {
		if (!uris.isEmpty()) indexResource(uris.toArray(new String[uris.size()]));
	}

	public void indexResource(String... uris) {
		acquireWriteLock();
		try {
			for (String uri : uris) {
				indexWriter.deleteDocuments(new Term("uri", uri));
				removeFromCache(uri);
				Document d = buildDocument(uri);
				if (d != null) indexWriter.addDocument(d);
			}
			indexWriter.commit();
			openSearcher();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			releaseWriteLock();
		}
	}

	public void optimize() {
		acquireWriteLock();
		try {
			indexWriter.optimize();
			indexWriter.commit();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			releaseWriteLock();
		}
	}

	public void clearSubClassOfCache() {
		subClassOfCache.clear();
	}

	private void removeFromCache(String uri) {
		labelCache.remove(uri);
		typeCache.remove(uri);
	}

	protected ImmutableLString getLabel(String uri) {
		if (!labelCache.containsKey(uri)) labelCache.put(uri, getLabelFromIndex(uri));
		return labelCache.get(uri);
	}

	private ImmutableLString getLabelFromIndex(String uri) {
		Document document = getDocument(uri);
		LString llabel = new LString();
		if (document != null) {
			String[] labels = document.getValues(LABEL_FIELD_NAME);
			if (labels != null) for (String label : labels)
				llabel.add(label);
			for (Locale locale : acceptedLocales) {
				String label = document.get(LABEL_FIELD_NAME + "_" + locale.getLanguage());
				if (label != null) llabel.add(locale, label);
			}
		}
		if (llabel.size() == 0) llabel.add(getLocalName(uri));
		return new ImmutableLString(llabel);
	}

	protected String[] getTypeUris(String uri) {
		if (!typeCache.containsKey(uri)) typeCache.put(uri, getTypeUrisFromIndex(uri));
		return typeCache.get(uri);
	}

	private String[] getTypeUrisFromIndex(String uri) {
		Document document = getDocument(uri);
		if (document != null) {
			String[] typeValues = document.getValues(TYPE_FIELD_NAME);
			if (typeValues != null) return typeValues;
		}
		return new String[0];
	}

	protected String[] getTransitiveTypeUris(String uri) {
		Set<String> transitiveTypeUris = new HashSet<String>();
		for (String typeUri : getTypeUris(uri))
			transitiveTypeUris.addAll(getAllAncestors(typeUri));
		return transitiveTypeUris.toArray(new String[transitiveTypeUris.size()]);
	}

	protected String[] getAncestors(String uri) {
		return subClassOfCache.getObjects(uri);
	}

	protected Set<String> getAllAncestors(String uri) {
		return subClassOfCache.getTransitiveObjects(uri);
	}

	protected Set<String> getAllDescendants(String uri) {
		return subClassOfCache.getTransitiveSubjects(uri);
	}

	private Document getDocument(String uri) {
		acquireReadLock();
		TermQuery query = new TermQuery(new Term("uri", uri));
		try {
			Searcher searcher = getSearcher();
			TopDocs topDocs = searcher.search(query, 1);
			if (topDocs.totalHits > 1)
				log.warn("URI '" + uri + "' is ambiguous: index has " + topDocs.totalHits + " documents for the URI.");
			ScoreDoc[] hits = topDocs.scoreDocs;
			if (hits.length > 0) return searcher.doc(hits[0].doc);
		} catch (IOException e) {
			log.error("", e);
		} finally {
			releaseReadLock();
		}
		return null;
	}

	private Document buildDocument(String uri) {
		return buildDocument(model.createResource(uri));
	}

	private Document buildDocument(Resource r) {
		Document doc = new Document();
		addLabels(doc, r);
		addTypes(doc, r);
		addProperties(doc, r);
		if (doc.getFields().isEmpty()) return null;
		doc.add(new Field("uri", r.getURI(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}

	private void addLabels(Document doc, Resource r) {
		ImmutableLString label = getLabelForIndexing(r.getURI());
		if (label.size() > 0) {
			for (Locale locale : acceptedLocales) {
				String llabel = label.get(locale);
				if (llabel != null)
					doc.add(new Field(LABEL_FIELD_NAME + "_" + locale.getLanguage(), llabel, Field.Store.YES, Field.Index.NOT_ANALYZED));
				else doc.add(new Field(LABEL_FIELD_NAME + "_" + locale.getLanguage(), label.getMatchAny(locale), Field.Store.YES, Field.Index.NOT_ANALYZED));
			}
			for (String l : label.getAll())
				doc.add(new Field(LABEL_FIELD_NAME, l, Field.Store.YES, Field.Index.ANALYZED));
		}
	}

	private ImmutableLString getLabelForIndexing(String uri) {
		if (!labelCache.containsKey(uri)) labelCache.put(uri, getLabelFromModel(uri));
		return labelCache.get(uri);
	}

	private ImmutableLString getLabelFromModel(String uri) {
		LString llabel = new LString();
		for (Property p : acceptedLabels)
			llabel.addAll(model.listStatements(model.createResource(uri), p, (RDFNode) null));
		if (llabel.size() == 0) llabel.add(getLocalName(uri));
		return new ImmutableLString(llabel);
	}

	private void addTypes(Document doc, Resource r) {
		for (String type : getTypesForIndexing(r.getURI()))
			doc.add(new Field(TYPE_FIELD_NAME, type, Field.Store.YES, Field.Index.NOT_ANALYZED));
	}

	private String[] getTypesForIndexing(String uri) {
		if (!typeCache.containsKey(uri)) typeCache.put(uri, getTypesFromModel(uri));
		return typeCache.get(uri);
	}

	private String[] getTypesFromModel(String uri) {
		List<String> types = new ArrayList<String>();
		for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(model.createResource(uri), RDF.type, (RDFNode) null)))
			if (s.getObject().isURIResource()) types.add(s.getResource().getURI());
		return types.toArray(new String[types.size()]);
	}

	private void addProperties(Document doc, Resource r) {
		for (Statement s : new IteratorToIIterableIterator<Statement>(r.listProperties()))
			addValue(doc, s.getPredicate().getURI(), s.getObject());
	}

	private void addValue(Document doc, String propertyUri, RDFNode value) {
		if (value.isURIResource())
			for (String ancestor : subClassOfCache.getTransitiveObjects(((Resource) value).getURI()))
				addResource(doc, propertyUri, ancestor);
		else if (value.isLiteral()) addLiteral(doc, propertyUri, ((Literal) value).getString());
	}

	private void addResource(Document doc, String propertyUri, String valueUri) {
		doc.add(new Field(propertyUri + URI_FIELD, valueUri, Field.Store.NO, Field.Index.NOT_ANALYZED));
		for (String label : getLabelForIndexing(valueUri).getAll())
			addLiteral(doc, propertyUri, label);
	}

	private void addLiteral(Document doc, String propertyUri, String literalValue) {
		doc.add(new Field(propertyUri, literalValue, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field(UBER_FIELD_NAME, literalValue, Field.Store.NO, Field.Index.ANALYZED));
	}

	private static String getLocalName(String uri) {
		if (uri == null) return null;
		int index = uri.lastIndexOf("#");
		if (index == -1) index = uri.lastIndexOf("/");
		if (index == -1) return uri;
		return uri.substring(index + 1);
	}

}