package fi.seco.saha3.model.configuration;

import java.util.ArrayList;
import java.util.Collection;

public class RepositoryConfig {
	
	private String sourceName;
	private Collection<String> typeRestrictions = new ArrayList<String>();
	private Collection<String> parentRestrictions = new ArrayList<String>();
	
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public Collection<String> getTypeRestrictions() {
		return typeRestrictions;
	}
	public void setTypeRestrictions(Collection<String> typeRestrictions) {
		this.typeRestrictions = typeRestrictions;
	}
	public Collection<String> getParentRestrictions() {
		return parentRestrictions;
	}
	public void setParentRestrictions(Collection<String> parentRestrictions) {
		this.parentRestrictions = parentRestrictions;
	}
	
}
