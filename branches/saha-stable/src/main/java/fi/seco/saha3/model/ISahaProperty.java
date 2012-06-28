package fi.seco.saha3.model;

import java.util.List;
import java.util.Set;

import fi.seco.saha3.factory.TreeBuilder;
import fi.seco.saha3.model.configuration.PropertyConfig;

public interface ISahaProperty extends Comparable<ISahaProperty> {

	public String getUri();
	public String getLabel();
	public String getComment();
	
	public boolean isLiteral();
	
	public String getValueUri();
	public String getValueLang();
	public String getValueLabel();
	public String getValueShaHex();
	public String getValueDatatypeUri();
	
	public String getValueTypeUri();
	public String getValueTypeLabel();
	
	public Set<String> getRange();
	public List<UriLabel> getRangeUriLabel();
	public Set<TreeBuilder.UITreeNode> getRangeTree();
	
	public PropertyConfig getConfig();
	
}
