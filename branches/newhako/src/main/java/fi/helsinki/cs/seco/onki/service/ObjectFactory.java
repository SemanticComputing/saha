
package fi.helsinki.cs.seco.onki.service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fi.helsinki.cs.seco.onki.service package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _OnkiHierarchyRelationConceptUri_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "conceptUri");
    private final static QName _OnkiHierarchyRelationParentUri_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "parentUri");
    private final static QName _OnkiHierarchyRelationLabel_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "label");
    private final static QName _OnkiQueryResultsResults_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "results");
    private final static QName _OnkiQueryResultsMetadata_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "metadata");
    private final static QName _StatementUri_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "uri");
    private final static QName _StatementPredicateUri_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "predicateUri");
    private final static QName _StatementLang_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "lang");
    private final static QName _StatementPredicateLabel_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "predicateLabel");
    private final static QName _StatementValue_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "value");
    private final static QName _OnkiQueryResultTitle_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "title");
    private final static QName _OnkiQueryResultAltLabel_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "altLabel");
    private final static QName _OnkiQueryResultSerkki_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "serkki");
    private final static QName _OnkiQueryResultNamespacePrefix_QNAME = new QName("http://service.onki.seco.cs.helsinki.fi", "namespacePrefix");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fi.helsinki.cs.seco.onki.service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OnkiHierarchyRelation }
     * 
     */
    public OnkiHierarchyRelation createOnkiHierarchyRelation() {
        return new OnkiHierarchyRelation();
    }

    /**
     * Create an instance of {@link ArrayOfOnkiHierarchyRelation }
     * 
     */
    public ArrayOfOnkiHierarchyRelation createArrayOfOnkiHierarchyRelation() {
        return new ArrayOfOnkiHierarchyRelation();
    }

    /**
     * Create an instance of {@link ArrayOfString }
     * 
     */
    public ArrayOfString createArrayOfString() {
        return new ArrayOfString();
    }

    /**
     * Create an instance of {@link GetAvailableLanguagesResponse }
     * 
     */
    public GetAvailableLanguagesResponse createGetAvailableLanguagesResponse() {
        return new GetAvailableLanguagesResponse();
    }

    /**
     * Create an instance of {@link GetLabel }
     * 
     */
    public GetLabel createGetLabel() {
        return new GetLabel();
    }

    /**
     * Create an instance of {@link GetConceptTree }
     * 
     */
    public GetConceptTree createGetConceptTree() {
        return new GetConceptTree();
    }

    /**
     * Create an instance of {@link GetConceptTreeResponse }
     * 
     */
    public GetConceptTreeResponse createGetConceptTreeResponse() {
        return new GetConceptTreeResponse();
    }

    /**
     * Create an instance of {@link GetAvailableTypeUris }
     * 
     */
    public GetAvailableTypeUris createGetAvailableTypeUris() {
        return new GetAvailableTypeUris();
    }

    /**
     * Create an instance of {@link OnkiQueryResultsMetadata }
     * 
     */
    public OnkiQueryResultsMetadata createOnkiQueryResultsMetadata() {
        return new OnkiQueryResultsMetadata();
    }

    /**
     * Create an instance of {@link ExpandQueryResponse }
     * 
     */
    public ExpandQueryResponse createExpandQueryResponse() {
        return new ExpandQueryResponse();
    }

    /**
     * Create an instance of {@link ArrayOfStatement }
     * 
     */
    public ArrayOfStatement createArrayOfStatement() {
        return new ArrayOfStatement();
    }

    /**
     * Create an instance of {@link ExpandQuery }
     * 
     */
    public ExpandQuery createExpandQuery() {
        return new ExpandQuery();
    }

    /**
     * Create an instance of {@link GetPropertiesResponse }
     * 
     */
    public GetPropertiesResponse createGetPropertiesResponse() {
        return new GetPropertiesResponse();
    }

    /**
     * Create an instance of {@link GetAvailableTypeUrisResponse }
     * 
     */
    public GetAvailableTypeUrisResponse createGetAvailableTypeUrisResponse() {
        return new GetAvailableTypeUrisResponse();
    }

    /**
     * Create an instance of {@link GetProperties }
     * 
     */
    public GetProperties createGetProperties() {
        return new GetProperties();
    }

    /**
     * Create an instance of {@link OnkiQueryResults }
     * 
     */
    public OnkiQueryResults createOnkiQueryResults() {
        return new OnkiQueryResults();
    }

    /**
     * Create an instance of {@link Search }
     * 
     */
    public Search createSearch() {
        return new Search();
    }

    /**
     * Create an instance of {@link Statement }
     * 
     */
    public Statement createStatement() {
        return new Statement();
    }

    /**
     * Create an instance of {@link SearchResponse }
     * 
     */
    public SearchResponse createSearchResponse() {
        return new SearchResponse();
    }

    /**
     * Create an instance of {@link OnkiQueryResult }
     * 
     */
    public OnkiQueryResult createOnkiQueryResult() {
        return new OnkiQueryResult();
    }

    /**
     * Create an instance of {@link GetAvailableLanguages }
     * 
     */
    public GetAvailableLanguages createGetAvailableLanguages() {
        return new GetAvailableLanguages();
    }

    /**
     * Create an instance of {@link ArrayOfOnkiQueryResult }
     * 
     */
    public ArrayOfOnkiQueryResult createArrayOfOnkiQueryResult() {
        return new ArrayOfOnkiQueryResult();
    }

    /**
     * Create an instance of {@link GetLabelResponse }
     * 
     */
    public GetLabelResponse createGetLabelResponse() {
        return new GetLabelResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "conceptUri", scope = OnkiHierarchyRelation.class)
    public JAXBElement<String> createOnkiHierarchyRelationConceptUri(String value) {
        return new JAXBElement<String>(_OnkiHierarchyRelationConceptUri_QNAME, String.class, OnkiHierarchyRelation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "parentUri", scope = OnkiHierarchyRelation.class)
    public JAXBElement<String> createOnkiHierarchyRelationParentUri(String value) {
        return new JAXBElement<String>(_OnkiHierarchyRelationParentUri_QNAME, String.class, OnkiHierarchyRelation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "label", scope = OnkiHierarchyRelation.class)
    public JAXBElement<String> createOnkiHierarchyRelationLabel(String value) {
        return new JAXBElement<String>(_OnkiHierarchyRelationLabel_QNAME, String.class, OnkiHierarchyRelation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfOnkiQueryResult }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "results", scope = OnkiQueryResults.class)
    public JAXBElement<ArrayOfOnkiQueryResult> createOnkiQueryResultsResults(ArrayOfOnkiQueryResult value) {
        return new JAXBElement<ArrayOfOnkiQueryResult>(_OnkiQueryResultsResults_QNAME, ArrayOfOnkiQueryResult.class, OnkiQueryResults.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OnkiQueryResultsMetadata }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "metadata", scope = OnkiQueryResults.class)
    public JAXBElement<OnkiQueryResultsMetadata> createOnkiQueryResultsMetadata(OnkiQueryResultsMetadata value) {
        return new JAXBElement<OnkiQueryResultsMetadata>(_OnkiQueryResultsMetadata_QNAME, OnkiQueryResultsMetadata.class, OnkiQueryResults.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "uri", scope = Statement.class)
    public JAXBElement<String> createStatementUri(String value) {
        return new JAXBElement<String>(_StatementUri_QNAME, String.class, Statement.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "predicateUri", scope = Statement.class)
    public JAXBElement<String> createStatementPredicateUri(String value) {
        return new JAXBElement<String>(_StatementPredicateUri_QNAME, String.class, Statement.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "lang", scope = Statement.class)
    public JAXBElement<String> createStatementLang(String value) {
        return new JAXBElement<String>(_StatementLang_QNAME, String.class, Statement.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "predicateLabel", scope = Statement.class)
    public JAXBElement<String> createStatementPredicateLabel(String value) {
        return new JAXBElement<String>(_StatementPredicateLabel_QNAME, String.class, Statement.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "value", scope = Statement.class)
    public JAXBElement<String> createStatementValue(String value) {
        return new JAXBElement<String>(_StatementValue_QNAME, String.class, Statement.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "label", scope = Statement.class)
    public JAXBElement<String> createStatementLabel(String value) {
        return new JAXBElement<String>(_OnkiHierarchyRelationLabel_QNAME, String.class, Statement.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "title", scope = OnkiQueryResult.class)
    public JAXBElement<String> createOnkiQueryResultTitle(String value) {
        return new JAXBElement<String>(_OnkiQueryResultTitle_QNAME, String.class, OnkiQueryResult.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "uri", scope = OnkiQueryResult.class)
    public JAXBElement<String> createOnkiQueryResultUri(String value) {
        return new JAXBElement<String>(_StatementUri_QNAME, String.class, OnkiQueryResult.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "altLabel", scope = OnkiQueryResult.class)
    public JAXBElement<String> createOnkiQueryResultAltLabel(String value) {
        return new JAXBElement<String>(_OnkiQueryResultAltLabel_QNAME, String.class, OnkiQueryResult.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "serkki", scope = OnkiQueryResult.class)
    public JAXBElement<String> createOnkiQueryResultSerkki(String value) {
        return new JAXBElement<String>(_OnkiQueryResultSerkki_QNAME, String.class, OnkiQueryResult.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.onki.seco.cs.helsinki.fi", name = "namespacePrefix", scope = OnkiQueryResult.class)
    public JAXBElement<String> createOnkiQueryResultNamespacePrefix(String value) {
        return new JAXBElement<String>(_OnkiQueryResultNamespacePrefix_QNAME, String.class, OnkiQueryResult.class, value);
    }

}
