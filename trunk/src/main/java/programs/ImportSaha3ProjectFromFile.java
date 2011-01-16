package programs;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;

public class ImportSaha3ProjectFromFile {

	public static void main(String[] args) throws Exception {
		SahaProjectRegistry r = new SahaProjectRegistry();
		r.setProjectBaseDirectory("/common/group/finnonto/saha3/");
		r.importModelFromFile("/common/scratch/biografiat/tuomas_saha_elamankerrat_100923_1226043.ttl","elamakerrat");
		r.close();
	}
	
}
