package fi.seco.saha3.model;

import java.util.Locale;

import org.apache.commons.codec.digest.DigestUtils;

import com.hp.hpl.jena.rdf.model.Literal;

/**
 * A class used to combine the URI and label of a resource to a single object.
 * 
 */
public class UriLabel implements Comparable<UriLabel> {

	private final String uri;
	private final String lang;
	private final String label;

	public UriLabel() {
		this.uri = "";
		this.lang = "";
		this.label = "";
	}

	public UriLabel(String uri, String label) {
		this.uri = uri;
		this.lang = "";
		this.label = label;
	}

	public UriLabel(String uri, Locale locale, String label) {
		this.uri = uri;
		this.lang = (locale != null) ? locale.getLanguage() : "";
		this.label = label;
	}

	public UriLabel(String uri, String lang, String label) {
		this.uri = uri;
		this.lang = lang;
		this.label = label;
	}

	public UriLabel(Literal literal) {
		this.uri = "";
		this.lang = literal.getLanguage();
		this.label = literal.getString();
	}

	public String getUri() {
		return uri;
	}

	public String getLang() {
		return lang;
	}

	public String getLabel() {
		return label;
	}

	public String getLabelShaHex() {
		return DigestUtils.sha1Hex(getLabel());
	}

	@Override
	public int compareTo(UriLabel o) {
		int c = String.CASE_INSENSITIVE_ORDER.compare(getLabel(), o.getLabel());
		return c != 0 ? c : getUri().compareTo(o.getUri());
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o.getClass().equals(getClass())) return ((UriLabel) o).getUri().equals(getUri());
		return false;
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}

}
