
package fi.helsinki.cs.seco.onki.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OnkiQueryResultsMetadata complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OnkiQueryResultsMetadata">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="containingHitsAmount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="moreHits" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="moreHitsAmount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="totalHitsAmount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OnkiQueryResultsMetadata", propOrder = {
    "containingHitsAmount",
    "moreHits",
    "moreHitsAmount",
    "totalHitsAmount"
})
public class OnkiQueryResultsMetadata {

    protected Integer containingHitsAmount;
    protected Boolean moreHits;
    protected Integer moreHitsAmount;
    protected Integer totalHitsAmount;

    /**
     * Gets the value of the containingHitsAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getContainingHitsAmount() {
        return containingHitsAmount;
    }

    /**
     * Sets the value of the containingHitsAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setContainingHitsAmount(Integer value) {
        this.containingHitsAmount = value;
    }

    /**
     * Gets the value of the moreHits property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMoreHits() {
        return moreHits;
    }

    /**
     * Sets the value of the moreHits property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMoreHits(Boolean value) {
        this.moreHits = value;
    }

    /**
     * Gets the value of the moreHitsAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMoreHitsAmount() {
        return moreHitsAmount;
    }

    /**
     * Sets the value of the moreHitsAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMoreHitsAmount(Integer value) {
        this.moreHitsAmount = value;
    }

    /**
     * Gets the value of the totalHitsAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalHitsAmount() {
        return totalHitsAmount;
    }

    /**
     * Sets the value of the totalHitsAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalHitsAmount(Integer value) {
        this.totalHitsAmount = value;
    }

}
