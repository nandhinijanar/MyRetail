package myretail.processor;

import myretail.dto.Price;
import myretail.dto.Product;
import myretail.exception.NotFoundException;
import myretail.exception.ValidationException;
import myretail.handler.RedskyClientHandler;
import myretail.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductProcessor {


    @Autowired
    RedskyClientHandler redskyClientHandler;

    @Autowired
    ProductRepository productRepository;

    Logger logger = LoggerFactory.getLogger(ProductProcessor.class);

    /**
     * TODO
     * @param productID
     * @return
     */
    public Product getProductDataForID(Integer productID) {

        validateProductID(productID);

        String productName = redskyClientHandler.getProductName(productID);
        Optional<Price> price = productRepository.findById(productID);
        if(price.isPresent()) {
            Product product = new Product(productID, productName, price.get());
            logger.debug("");
            return product;
        } else {
            throw new NotFoundException(String.format("Requested Product ID - %d not found in DB", productID));
        }
    }

    /**
     * TODO
     * @param productID
     * @param productInfo
     * @return
     */
    public Product updateProductPrice(Integer productID, Product productInfo) {

        validateProductIDForUpdate(productID, productInfo);
        productInfo.getCurrentPrice().setProductID(productID);
        Price newPrice = productRepository.save(productInfo.getCurrentPrice());
        productInfo.setCurrentPrice(newPrice);

        return productInfo;

    }

    private void validateProductIDForUpdate(Integer productID, Product productInfo) {
        validateProductID(productID);
        if(productInfo.getProductID().equals(productID)) {
            Optional<Price> price = productRepository.findById(productID);
            if(! price.isPresent()) {
                logger.error(String.format("Could not find Product ID %d in DB", productID));
                throw new NotFoundException(String.format("Resource not found for Product ID - %d", productID));
            }
        } else {
            logger.error(String.format("Path param Product ID %d doesn't match with Request payload %d", productID, productInfo.getProductID()));
            throw new ValidationException("Product ID in payload must be equal to requested 'id'");
        }
    }

    private void validateProductID(Integer productID) {
        if(productID == null || productID.intValue() < 1) {
            logger.error("Invalid Product ID - " + productID);
            throw new ValidationException("Product ID cannot be null or less than '1'");
        }
    }
}
