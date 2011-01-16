package fi.seco.saha3.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface IResults extends Iterable<IResults.IResult> {

	public interface IResult {
		public String getUri();
		public String getLabel();
		public String getAltLabel();
		public List<String> getAltLabels();
	}
	
	public class Result extends UriLabel implements IResult {
		private List<String> altLabels = new ArrayList<String>();
		public Result(String uri, String label) {
			super(uri,label);
		}
		public Result(String uri, String label, String altLabel) {
			super(uri,label);
			if (altLabel != null)
				altLabels.add(altLabel);
		}
		public Result(String uri, String label, List<String> altLabels) {
			super(uri,label);
			this.altLabels = altLabels;
		}
		public String getAltLabel() {
			return altLabels.isEmpty() ? "" : altLabels.get(0);
		}
		public List<String> getAltLabels() {
			return altLabels;
		}
	}
	
	public int getSize();
	public Iterator<IResult> iterator();
	
}
