
package fi.helsinki.cs.seco.onki.service;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfOnkiQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfOnkiQueryResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OnkiQueryResult" type="{http://service.onki.seco.cs.helsinki.fi}OnkiQueryResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfOnkiQueryResult", propOrder = {
    "onkiQueryResult"
})
public class ArrayOfOnkiQueryResult {

    @XmlElement(name = "OnkiQueryResult", nillable = true)
    protected List<OnkiQueryResult> onkiQueryResult;

    /**
     * Gets the value of the onkiQueryResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the onkiQueryResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOnkiQueryResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OnkiQueryResult }
     * 
     * 
     */
    public List<OnkiQueryResult> getOnkiQueryResult() {
        if (onkiQueryResult == null) {
            onkiQueryResult = new ArrayList<OnkiQueryResult>();
        }
        return this.onkiQueryResult;
    }

}
