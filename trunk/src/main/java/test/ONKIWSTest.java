package test;

import java.util.Locale;
import fi.seco.saha3.infrastructure.OnkiWebService;
import fi.seco.saha3.infrastructure.OnkiWebService.OnkiRepository;
import fi.seco.saha3.model.ISahaProperty;

public class ONKIWSTest {

	public static void main(String[] args) {
		OnkiWebService onki = new OnkiWebService();
		onki.setAccessKey("51d3cdaa81331c9117d77f443a2c5404");
		
//		HttpTransportPipe.dump=true;
		
		OnkiRepository repository = onki.getOnkiRepository("http://demo.seco.tkk.fi/kulsa/remoting/OnkiAutocompletion?wsdl");
		
		//OnkiRepository repository = onki.getOnkiRepository("koko");
		// http://www.yso.fi/onto/koko/p35295
		
//		System.out.println("getLabel");
//		System.out.println(repository.getLabel("http://www.yso.fi/onto/suo/A0020_10342733",new Locale("fi")));
		
		System.out.println("getProperties");
		for (ISahaProperty p : repository.getProperties("http://www.yso.fi/onto/suo/A0020_10342733",new Locale("fi")))
			System.out.println(p.getLabel() + " [" + p.getUri() + "]: " + 
					p.getValueLabel() + " [" + p.getValueUri() + "]");
	}
	
}
