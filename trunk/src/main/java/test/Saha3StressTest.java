package test;

import java.util.Locale;
import java.util.Random;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.model.IResults;
import fi.seco.saha3.model.SahaProject;
import fi.seco.saha3.model.UriLabel;

public class Saha3StressTest {

	public static void main(String[] args) {
		SahaProjectRegistry r = new SahaProjectRegistry();
		r.setProjectBaseDirectory("/dump/saha3/");
		SahaProject p = r.getSahaProject("kirjasampo");
		new Saha3StressTest(p,10,50).start();
	}
	
	private class Worker extends Thread {
		private Random rnd = new Random();
		private String s = "http://stress.test.com/p123";
		private String p = "http://stress.test.com/label";
		private UriLabel value = new UriLabel("","testing...");
		@Override
		public void run() {
			for (int i=0;i<rounds;i++) {
				switch (rnd.nextInt(5)) {
					case 0: add(); break;
					case 1: remove(); break;
					case 2: get(); break;
					default: search(); break;
				}
			}
			clean();
			System.out.println(getId() + " - Done.");
		}
		private void add() {
			if (verbose) System.out.println(getId() + " - set");
			value = new UriLabel("","testing_"+rnd.nextDouble());
			project.addLiteralProperty(s,p,value.getLabel());
		}
		private void remove() {
			if (verbose) System.out.println(getId() + " - remove");
			// warnings are OK
			project.removeLiteralProperty(s,p,value.getLabelShaHex());
		}
		private void get() {
			if (verbose) System.out.println(getId() + " - get");
			project.getResource(s,new Locale("fi")).getPropertyMapEntrySet();
		}
		private void search() {
			if (verbose) System.out.println(getId() + " - search");
			IResults results = project.search("t",null,new Locale("fi"),10);
			for (IResults.IResult r : results) 
				System.out.println(r.getLabel());
		}
		private void clean() {
			project.removeResource(s);
		}
	}
	
	private SahaProject project;
	
	private int threads;
	private int rounds;
	
	public boolean verbose = true;
	
	public Saha3StressTest(SahaProject project, int threads, int rounds) {
		this.project = project;
		this.threads = threads;
		this.rounds = rounds;
	}
	
	public void start() {
		for (int i=0;i<threads;i++)
			new Worker().start();
	}
	
}
