package programs;

import java.util.Locale;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;

public class IndexSaha3Project {

	public static void main(String[] args) throws Exception {
		SahaProjectRegistry r = new SahaProjectRegistry();
		r.setProjectBaseDirectory("/dump/saha3/");
		r.getSahaProject("kirjasampo").getLocalizedRootClasses(new Locale("fi"));
		r.close();
	}

}
