
package fi.helsinki.cs.seco.onki.service;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfOnkiHierarchyRelation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfOnkiHierarchyRelation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OnkiHierarchyRelation" type="{http://service.onki.seco.cs.helsinki.fi}OnkiHierarchyRelation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfOnkiHierarchyRelation", propOrder = {
    "onkiHierarchyRelation"
})
public class ArrayOfOnkiHierarchyRelation {

    @XmlElement(name = "OnkiHierarchyRelation", nillable = true)
    protected List<OnkiHierarchyRelation> onkiHierarchyRelation;

    /**
     * Gets the value of the onkiHierarchyRelation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the onkiHierarchyRelation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOnkiHierarchyRelation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OnkiHierarchyRelation }
     * 
     * 
     */
    public List<OnkiHierarchyRelation> getOnkiHierarchyRelation() {
        if (onkiHierarchyRelation == null) {
            onkiHierarchyRelation = new ArrayList<OnkiHierarchyRelation>();
        }
        return this.onkiHierarchyRelation;
    }

}
