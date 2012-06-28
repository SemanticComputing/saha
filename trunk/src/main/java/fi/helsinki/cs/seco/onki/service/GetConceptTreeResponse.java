
package fi.helsinki.cs.seco.onki.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="out" type="{http://service.onki.seco.cs.helsinki.fi}ArrayOfOnkiHierarchyRelation"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "out"
})
@XmlRootElement(name = "getConceptTreeResponse")
public class GetConceptTreeResponse {

    @XmlElement(required = true, nillable = true)
    protected ArrayOfOnkiHierarchyRelation out;

    /**
     * Gets the value of the out property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfOnkiHierarchyRelation }
     *     
     */
    public ArrayOfOnkiHierarchyRelation getOut() {
        return out;
    }

    /**
     * Sets the value of the out property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfOnkiHierarchyRelation }
     *     
     */
    public void setOut(ArrayOfOnkiHierarchyRelation value) {
        this.out = value;
    }

}
