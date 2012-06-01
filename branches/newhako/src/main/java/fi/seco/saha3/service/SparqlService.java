package fi.seco.saha3.service;

import java.net.URL;

import org.joseki.vocabulary.JosekiSchemaBase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.NamedModelAssembler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.util.IOUtils;

/**
 * Service class exposing data from SAHA projects as separate SPARQL endpoints. 
 * 
 */
public class SparqlService extends NamedModelAssembler implements InitializingBean {

	private static final Resource jenaServiceModel = ResourceFactory.createResource("http://www.seco.tkk.fi/onto/assembler/JenaServiceModel");

	private SahaProjectRegistry projectRegistry;

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry projectRegistry) {
		this.projectRegistry = projectRegistry;
	}

	@Override
	public Model openEmptyModel(Assembler a, Resource root, Mode mode) {
		return getModel(getModelName(root));
	}

	private Model getModel(String modelName) {
		return projectRegistry.getModel(modelName);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assembler.general.implementWith(jenaServiceModel, this);

		Model josekiConfig = ModelFactory.createDefaultModel();
		josekiConfig.read(this.getClass().getResourceAsStream("/joseki-config-skeleton.ttl"), null, "TTL");

		String NS = JosekiSchemaBase.NS;

		Resource josekiService = josekiConfig.createResource(NS + "Service");
		Resource josekiSPARQLProcessor = josekiConfig.createResource(NS + "ProcessorSPARQL_FixedDS");
		Resource josekiSPARULProcessor = josekiConfig.createResource(NS + "ProcessorSPARQLUpdate");
		Resource jaDataset = josekiConfig.createResource("http://jena.hpl.hp.com/2005/11/Assembler#RDFDataset");
		Property jaDefGraph = josekiConfig.createProperty("http://jena.hpl.hp.com/2005/11/Assembler#defaultGraph");
		Property jaModelName = josekiConfig.createProperty("http://jena.hpl.hp.com/2005/11/Assembler#modelName");

		int serviceNumber = 0;
		for (String modelName : projectRegistry.getAllProjects()) {
			Resource sparqlService = josekiConfig.createResource("#sparqlService" + ++serviceNumber);
			Resource sparulService = josekiConfig.createResource("#sparulService" + serviceNumber);
			Resource data = josekiConfig.createResource("http://demo.seco.tkk.fi/saha/joseki_service/" + serviceNumber);

			josekiConfig.add(sparqlService, RDF.type, josekiService);
			josekiConfig.add(sparqlService, RDFS.label, "SPARQL");
			josekiConfig.add(sparqlService, JosekiSchemaBase.serviceRef, "service/data/" + modelName + "/sparql");
			josekiConfig.add(sparqlService, JosekiSchemaBase.dataset, data);
			josekiConfig.add(sparqlService, JosekiSchemaBase.processor, josekiSPARQLProcessor);

			josekiConfig.add(sparulService, RDF.type, josekiService);
			josekiConfig.add(sparulService, RDFS.label, "SPARUL");
			josekiConfig.add(sparulService, JosekiSchemaBase.serviceRef, "service/data/" + modelName + "/sparul");
			josekiConfig.add(sparulService, JosekiSchemaBase.dataset, data);
			josekiConfig.add(sparulService, JosekiSchemaBase.processor, josekiSPARULProcessor);

			Resource defaultGraph = josekiConfig.createResource();
			josekiConfig.add(defaultGraph, RDF.type, jenaServiceModel);
			josekiConfig.add(defaultGraph, jaModelName, modelName);
			josekiConfig.add(defaultGraph, RDFS.label, modelName);

			josekiConfig.add(data, RDF.type, jaDataset);
			josekiConfig.add(data, RDFS.label, "Smetana: " + modelName);
			josekiConfig.add(data, jaDefGraph, defaultGraph);
		}

		URL target = this.getClass().getResource("/");
		IOUtils.writeRDFFile(josekiConfig, target.getPath() + "joseki-config.ttl", "TTL");
	}

}
