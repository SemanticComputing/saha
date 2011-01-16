package test;

import java.net.URL;
import java.util.concurrent.Executors;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import com.sun.xml.internal.ws.developer.JAXWSProperties;

import fi.helsinki.cs.seco.onki.service.ArrayOfString;
import fi.helsinki.cs.seco.onki.service.IOnkiQuery;
import fi.helsinki.cs.seco.onki.service.IOnkiQueryPortType;
import fi.helsinki.cs.seco.onki.service.OnkiQueryResult;
import fi.helsinki.cs.seco.onki.service.OnkiQueryResults;

@SuppressWarnings("restriction")
public class WSTest {

    public static void main(String[] args) throws Exception {
		
//		URL url = new URL("http://demo.seco.tkk.fi/kulsa/remoting/OnkiAutocompletion?wsdl");
		URL url = new URL("http://www.yso.fi/onkiwebservice/wsdl/?o=tero&k=51d3cdaa81331c9117d77f443a2c5404");
		
		IOnkiQuery oq = new IOnkiQuery(url,new QName("http://service.onki.seco.cs.helsinki.fi","IOnkiQuery"));
		oq.setExecutor(Executors.newCachedThreadPool());
		
		IOnkiQueryPortType pt = oq.getOnkiQuery();
		((BindingProvider)pt).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT,10000);

		System.out.println("...");
		
		ArrayOfString a = pt.getAvailableLanguages();
		for (String s : a.getString())
			System.out.println(s);
		
		OnkiQueryResults rs = pt.search("a","fi",10,null,null,null);
		for (OnkiQueryResult oqr : rs.getResults().getValue().getOnkiQueryResult()) {
			System.out.println(oqr.getTitle().getValue() + " " + oqr.getUri().getValue());
			Thread.sleep(100);
		}
		
//		System.out.println(">>" + pt.getLabel("http://www.lingvoj.org/lang/fil","fi").getTitle().getValue());
		
	}
	
}
