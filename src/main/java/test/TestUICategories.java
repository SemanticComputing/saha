package test;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;

import com.hp.hpl.jena.vocabulary.RDF;

import fi.seco.saha3.index.category.UICategoryNode;
import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.model.SahaProject;

public class TestUICategories {
	public static void main(String[] args) throws Exception {
		SahaProjectRegistry r = new SahaProjectRegistry();
		r.setProjectBaseDirectory("/dump/saha3/");
		
		SahaProject p = r.getSahaProject("kirjasampo");
		
		for (int i=0;i<10;i++) {
			long startTime = System.currentTimeMillis();
			p.getUICategories(
						Arrays.asList("http://www.yso.fi/onto/kaunokki#yteos"), 
						Arrays.asList(RDF.type.getURI(),"http://www.yso.fi/onto/kaunokki#teema"), 
						new Locale("fi"));
			System.out.println(System.currentTimeMillis() - startTime);
		}	
		
		r.close();
	}
	
	public static<K,V extends UICategoryNode> void printUICategories(Map<K,SortedSet<V>> categories) {
		for (Map.Entry<K,SortedSet<V>> entry : categories.entrySet()) {
			System.out.println(entry.getKey());
			printUIValues("\t",entry.getValue());
		}
	}
	
	private static<K,V extends UICategoryNode> void printUIValues(String indent, SortedSet<V> values) {
		for (V value : values) {
			if (value.getRecursiveItemCount() > 0) {
				System.out.println(indent + value);
				printUIValues(indent + "\t",value.getChildren());
			}	
		}	
	}
}
