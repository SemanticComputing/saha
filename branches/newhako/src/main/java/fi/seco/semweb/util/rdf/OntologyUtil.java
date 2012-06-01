package fi.seco.semweb.util.rdf;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

public class OntologyUtil {

	public static Set<Resource> getInverseTransitiveClosure(Model m,Resource r, Property p) {
		Set<Resource> ret = new HashSet<Resource>();
		ret.add(r);
		addInverseTransitiveClosure(m, r, p, ret);
		return ret;
	}

	public static void addInverseTransitiveClosure(Model m,Resource r, Property p, Set<Resource> set) {
		for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null,p,r))) if (!set.contains(s.getSubject())) {
			set.add(s.getSubject());
			addInverseTransitiveClosure(m,s.getSubject(),p,set);
		}
	}

	public static void addTransitiveClosure(Model m,Resource r, Property p, Set<Resource> set) {
		for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(r,p,(RDFNode)null))) if (s.getObject().isURIResource() && !set.contains(s.getResource())) {
			set.add(s.getResource());
			addTransitiveClosure(m,s.getResource(),p,set);
		}
	}

	public static Set<Resource> getMetaClassesFrom(Model input) {
		Set<Resource> metaclasses = new HashSet<Resource>();
		metaclasses.add(OWL.Class);
		metaclasses.add(RDFS.Class);
//		metaclasses.add(SKOS.Concept);
		addInverseTransitiveClosure(input, OWL.Class, RDFS.subClassOf, metaclasses);
		addInverseTransitiveClosure(input, RDFS.Class, RDFS.subClassOf, metaclasses);
//		addInverseTransitiveClosure(input, SKOS.Concept, RDFS.subClassOf, metaclasses);
		return metaclasses;
	}

	public static Set<Resource> getMetaPropertiesFrom(Model input) {
		Set<Resource> metaclasses = new HashSet<Resource>();
		metaclasses.add(OWL.SymmetricProperty);
		metaclasses.add(OWL.TransitiveProperty);
		metaclasses.add(OWL.FunctionalProperty);
		metaclasses.add(OWL.InverseFunctionalProperty);
		metaclasses.add(OWL.ObjectProperty);
		metaclasses.add(OWL.DatatypeProperty);
		metaclasses.add(RDF.Property);
		addInverseTransitiveClosure(input, OWL.SymmetricProperty, RDFS.subPropertyOf, metaclasses);
		addInverseTransitiveClosure(input, OWL.TransitiveProperty, RDFS.subPropertyOf, metaclasses);
		addInverseTransitiveClosure(input, OWL.FunctionalProperty, RDFS.subPropertyOf, metaclasses);
		addInverseTransitiveClosure(input, OWL.InverseFunctionalProperty, RDFS.subPropertyOf, metaclasses);
		addInverseTransitiveClosure(input, OWL.ObjectProperty, RDFS.subPropertyOf, metaclasses);
		addInverseTransitiveClosure(input, OWL.DatatypeProperty, RDFS.subPropertyOf, metaclasses);
		addInverseTransitiveClosure(input, RDF.Property, RDFS.subPropertyOf, metaclasses);
		return metaclasses;
	}

	public static Set<Resource> getPropertiesFrom(Model input) {
		Set<Resource> metaclasses = getMetaPropertiesFrom(input);
		Set<Resource> props = new HashSet<Resource>();
		for (Resource r : metaclasses) for (Resource r2 : new IteratorToIIterableIterator<Resource>(input.listResourcesWithProperty(RDF.type,r))) props.add(r2);
		metaclasses.addAll(props);
		return metaclasses;
	}

	public static Set<Resource> getMetaObjectsFrom(Model input) {
		Set<Resource> metaclasses = new HashSet<Resource>();
		metaclasses.addAll(getMetaClassesFrom(input));
		metaclasses.addAll(getMetaPropertiesFrom(input));
		metaclasses.add(RDFS.Resource);
		return metaclasses;
	}

	public static Set<Resource> getSchemaObjectsFrom(Model input) {
		Set<Resource> metaclasses = new HashSet<Resource>();
		metaclasses.addAll(getMetaClassesFrom(input));
		metaclasses.addAll(getPropertiesFrom(input));
		return metaclasses;
	}

	public static Set<Resource> getTransitiveClosure(Model m, Resource r, Property p) {
		Set<Resource> ret = new HashSet<Resource>();
		ret.add(r);
		addTransitiveClosure(m, r, p, ret);
		return ret;
	}
}
