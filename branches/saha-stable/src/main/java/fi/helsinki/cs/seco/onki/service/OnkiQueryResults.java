
package fi.helsinki.cs.seco.onki.service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OnkiQueryResults complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OnkiQueryResults">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="metadata" type="{http://service.onki.seco.cs.helsinki.fi}OnkiQueryResultsMetadata" minOccurs="0"/>
 *         &lt;element name="results" type="{http://service.onki.seco.cs.helsinki.fi}ArrayOfOnkiQueryResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnkiQueryResults", propOrder = {
    "metadata",
    "results"
})
public class OnkiQueryResults {

    @XmlElementRef(name = "metadata", namespace = "http://service.onki.seco.cs.helsinki.fi", type = JAXBElement.class)
    protected JAXBElement<OnkiQueryResultsMetadata> metadata;
    @XmlElementRef(name = "results", namespace = "http://service.onki.seco.cs.helsinki.fi", type = JAXBElement.class)
    protected JAXBElement<ArrayOfOnkiQueryResult> results;

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link OnkiQueryResultsMetadata }{@code >}
     *     
     */
    public JAXBElement<OnkiQueryResultsMetadata> getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link OnkiQueryResultsMetadata }{@code >}
     *     
     */
    public void setMetadata(JAXBElement<OnkiQueryResultsMetadata> value) {
        this.metadata = ((JAXBElement<OnkiQueryResultsMetadata> ) value);
    }

    /**
     * Gets the value of the results property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfOnkiQueryResult }{@code >}
     *     
     */
    public JAXBElement<ArrayOfOnkiQueryResult> getResults() {
        return results;
    }

    /**
     * Sets the value of the results property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfOnkiQueryResult }{@code >}
     *     
     */
    public void setResults(JAXBElement<ArrayOfOnkiQueryResult> value) {
        this.results = ((JAXBElement<ArrayOfOnkiQueryResult> ) value);
    }

}
