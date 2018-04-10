package myretail.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO to store Current price of the Product and mapping to DB
 */
@Table(value = "product_price")
public class Price {

    @JsonIgnore
    @PrimaryKey
    @Column(value = "productid")
    private Integer productID;

    @Column(value = "price")
    @JsonProperty("value")
    @NotNull
    private BigDecimal price;

    @JsonProperty("currency_code")
    @Column(value = "currencycode")
    @NotNull
    private String currencyCode;

    public Price() {
    }

    public Price(Integer productID, BigDecimal price, String currencyCode) {
        this.productID = productID;
        this.price = price;
        this.currencyCode = currencyCode;
    }

    public Integer getProductID() {
        return productID;
    }

    public void setProductID(Integer productID) {
        this.productID = productID;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price1 = (Price) o;
        return Objects.equals(productID, price1.productID) &&
                Objects.equals(price, price1.price) &&
                Objects.equals(currencyCode, price1.currencyCode);
    }

    @Override
    public int hashCode() {

        return Objects.hash(productID, price, currencyCode);
    }

    @Override
    public String toString() {
        return "Price{" +
                "productID=" + productID +
                ", price=" + price +
                ", currencyCode='" + currencyCode + '\'' +
                '}';
    }
}

