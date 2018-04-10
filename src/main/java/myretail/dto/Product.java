package myretail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * DTO for the Product Information
 */
public class Product {

    @JsonProperty("id")
    @NotNull
    @Min(value = 1, message = "should be greater than zero")
    private Integer productID;

    private String name;

    @JsonProperty("current_price")
    @Valid
    private Price currentPrice;

    public Product() {
    }

    public Product(Integer productID, String name, Price currentPrice) {
        this.productID = productID;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    public Integer getProductID() {
        return productID;
    }

    public void setProductID(Integer productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Price getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Price currentPrice) {
        this.currentPrice = currentPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productID, product.productID) &&
                Objects.equals(name, product.name) &&
                Objects.equals(currentPrice, product.currentPrice);
    }

    @Override
    public int hashCode() {

        return Objects.hash(productID, name, currentPrice);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productID=" + productID +
                ", name='" + name + '\'' +
                ", currentPrice=" + currentPrice +
                '}';
    }
}
