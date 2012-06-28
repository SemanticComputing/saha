package fi.seco.semweb.util;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.seco.semweb.util.iterator.AMappingIterator;
import fi.seco.semweb.util.iterator.EmptyIterator;
import fi.seco.semweb.util.iterator.IIterableIterator;

/* use this as:

import static fi.seco.semweb.util.JenaModelHelper.p;
import static fi.seco.semweb.util.JenaModelHelper.r;

 */

public class JenaHelper {

	private static Log log = LogFactory.getLog(JenaHelper.class);

	public static Property p(String uri) {
		return ResourceFactory.createProperty(PrefixMapping.Standard.expandPrefix(uri));
	}

	public static Resource r(String uri) {
		return ResourceFactory.createResource(PrefixMapping.Standard.expandPrefix(uri));
	}

	public static Property p(Model model, String uri) {
		return model.createProperty(model.expandPrefix(uri));
	}

	public static OntProperty op(OntModel model, String uri) {
		return model.createOntProperty(model.expandPrefix(uri));
	}

	public static Resource r(Model model, String uri) {
		return model.createResource(model.expandPrefix(uri));
	}

	public static Resource r(Model model) {
		return model.createResource();
	}

	public static OntResource or(OntModel model, Resource r) {
		return model.getOntResource(r);
	}

	public static OntResource or(OntModel model, String uri) {
		return model.createOntResource(model.expandPrefix(uri));
	}

	public static Resource i(Model model, Resource classResource) {
		Resource r = model.createResource();
		r.addProperty(RDF.type,classResource);
		return r;
	}

	public static Resource i(Model model, String classUri) {
		return i(model,r(model,classUri));
	}

	public static Resource i(Model model, String uri, String classUri) {
		Resource r = r(model,uri);
		r.addProperty(RDF.type,r(model,classUri));
		return r;
	}

	@SuppressWarnings("unchecked")
	public static Iterator<RDFNode> getListItems(Resource resource, Property p) {
		try {
			return ((RDFList) resource.getProperty(p).getResource().as(RDFList.class)).iterator();
		} catch (Exception e) {
			log.error("JenaHelper.getListItems() called with non-list. This shouldn't happen, but returning an empty iterator anyway..");
			return EmptyIterator.SHARED_INSTANCE;
		}
	}

	public static Individual oi(OntModel model, Resource classResource) {
		return model.createIndividual(classResource);
	}

	public static Individual oi(OntModel model, String uri, Resource classResource) {
		return model.createIndividual(model.expandPrefix(uri),classResource);
	}

	public static Individual oi(OntModel model, String uri, String classUri) {
		return model.createIndividual(model.expandPrefix(uri),r(model,classUri));
	}

	public static OntClass oc(OntModel model, String uri) {
		return model.createClass(model.expandPrefix(uri));
	}

	public static String getLangProperty(Resource r, Property p, String lang) {
		StmtIterator sti = r.listProperties(p);
		while (sti.hasNext()) {
			Statement st = sti.nextStatement();
			if (lang.equalsIgnoreCase(st.getLanguage())) return st.getString();
		}
		return null;
	}

	public static String getPropertyAsString(Resource r, String p) {
		return getPropertyAsString(r,p(r.getModel(),p));
	}

	public static String getPropertyAsString(Model m, String r, String p) {
		return getPropertyAsString(r(m,r),p(m,p));
	}

	public static String getPropertyAsString(Model m, String r, Property p) {
		return getPropertyAsString(r(m,r),p);
	}

	public static String getPropertyAsString(Resource r, Property p) {
		RDFNode tmp = getPropertyAsObject(r,p);
		return (tmp instanceof Literal) ? ((Literal)tmp).getString() : null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends RDFNode> T getPropertyAsObject(Resource resource, String p) {
		Statement tmp = getProperty(resource,p);
		return (T) (tmp!=null ? tmp.getObject() : null);
	}

	public static Statement getProperty(Resource resource, String p) {
		  return resource.getProperty(p(resource.getModel(),p));
	}

	public static Resource getPropertyAsResource(Resource resource, Property p) {
	   Statement tmp=resource.getProperty(p);
	   return tmp!=null ? tmp.getResource() : null;
	}

	public static RDFNode getPropertyAsObject(Resource resource, Property p) {
		   Statement tmp=resource.getProperty(p);
		   return tmp!=null ? tmp.getObject() : null;
	}

	public static IIterableIterator<RDFNode> getPropertiesAsObjects(Resource resource, String p) {
		return getPropertiesAsObjects(resource,p(resource.getModel(),p));
	}

	public static IIterableIterator<RDFNode> getPropertiesAsObjects(Resource resource, Property p) {
		return new AMappingIterator<Statement,RDFNode>(resource.listProperties(p)) {
			@Override
			public RDFNode map(Statement src) {
				return src.getObject();
			}
		};
	}

	public static IIterableIterator<Resource> getPropertiesAsResources(Resource resource, Property p) {
		return new AMappingIterator<Statement,Resource>(resource.listProperties(p)) {
			@Override
			public Resource map(Statement src) {
				return (Resource) src.getObject();
			}
		};
	}

	public static IIterableIterator<Resource> getPropertiesAsResources(Resource resource, String p) {
		return getPropertiesAsResources(resource, p(resource.getModel(),p));
	}

}
