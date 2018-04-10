package myretail.processor;

import myretail.dto.Price;
import myretail.dto.Product;
import myretail.exception.ClientServiceException;
import myretail.exception.NotFoundException;
import myretail.exception.ValidationException;
import myretail.handler.RedskyClientHandler;
import myretail.repository.ProductRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class ProductProcessorTest {

    @InjectMocks
    ProductProcessor productProcessor = new ProductProcessor();

    @Mock
    RedskyClientHandler redskyClientHandler;

    @Mock
    ProductRepository productRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testGetProductDataForID_Success() throws IOException {
        Integer productID = 13860428;
        when(redskyClientHandler.getProductName(Mockito.anyInt())).thenReturn("The Big Lebowski (Blu-ray)");
        when(productRepository.findById(productID)).thenReturn(Optional.of(new Price(13860428, BigDecimal.valueOf(11.42), "USD")));
        Product product = productProcessor.getProductDataForID(productID);
        assertNotNull(product);
        assertEquals(productID, product.getProductID());
        assertNotNull(product.getName());
        assertEquals("The Big Lebowski (Blu-ray)", product.getName());
        assertNotNull(product.getCurrentPrice());
        assertEquals(BigDecimal.valueOf(11.42),product.getCurrentPrice().getPrice());
        assertEquals("USD", product.getCurrentPrice().getCurrencyCode());
    }

    @Test(expected = NotFoundException.class)
    public void testGetProductDataForID_NotFoundInRedsky() throws IOException {
        Integer productID = 13860428;
        when(redskyClientHandler.getProductName(Mockito.anyInt())).thenThrow(new NotFoundException("Product Information is unavailable for Product ID - 13860428"));
        try {
            Product product = productProcessor.getProductDataForID(productID);
        } catch (NotFoundException e) {
            assertEquals("Product Information is unavailable for Product ID - 13860428", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ClientServiceException.class)
    public void testGetProductDataForID_RedskyClientException() throws IOException {
        Integer productID = 13860428;
        when(redskyClientHandler.getProductName(Mockito.anyInt())).thenThrow(new ClientServiceException("Exception - Redsky Client returned status : 404"));
        try {
            Product product = productProcessor.getProductDataForID(productID);
        }  catch(ClientServiceException e) {
            assertEquals("Exception - Redsky Client returned status : 404", e.getMessage());
            throw e;
        }
    }

    @Test(expected = NotFoundException.class)
    public void testGetProductDataForID_NotFoundInDB() throws IOException {
        Integer productID = 13860428;
        when(redskyClientHandler.getProductName(Mockito.anyInt())).thenReturn("The Big Lebowski (Blu-ray)");
        when(productRepository.findById(productID)).thenReturn(Optional.empty());
        try {
            Product product = productProcessor.getProductDataForID(productID);
        }  catch(ClientServiceException e) {
            assertEquals("Requested Product ID - 13860428 not found in DB", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testValidateProductID() {
        Integer productID = 13860428;
        try {
            ReflectionTestUtils.invokeMethod(productProcessor, "validateProductID", productID);
        } catch(ValidationException e) {
            fail("No exception expected");
        }
    }

    @Test(expected = ValidationException.class)
    public void testValidateProductID_null() {
        Integer productID = null;
        try {
            ReflectionTestUtils.invokeMethod(productProcessor, "validateProductID", productID);
        } catch(ValidationException e) {
            assertEquals("Product ID cannot be null or less than '1'", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void testValidateProductID_zero() {
        Integer productID = 0;
        try {
            ReflectionTestUtils.invokeMethod(productProcessor, "validateProductID", productID);
        } catch(ValidationException e) {
            assertEquals("Product ID cannot be null or less than '1'", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testValidateProductIDForUpdate_Success() {
        Integer productID = 13860428;
        Product product = new Product(productID, "", new Price(productID, BigDecimal.valueOf(15.00), "USD"));
        when(productRepository.findById(productID)).thenReturn(Optional.of(new Price(productID, BigDecimal.valueOf(11.42), "USD")));
        try {
            ReflectionTestUtils.invokeMethod(productProcessor, "validateProductIDForUpdate", productID, product);
        } catch (NotFoundException | ValidationException e) {
            fail("Unexpected Exception - " + e.getMessage());
        }
    }

    @Test(expected = ValidationException.class)
    public void testValidateProductIDForUpdate_differentIDs() {
        Integer productID = 13860428;
        Product product = new Product(1234, "", new Price(productID, BigDecimal.valueOf(15.00), "USD"));
        try {
            ReflectionTestUtils.invokeMethod(productProcessor, "validateProductIDForUpdate", productID, product);
        } catch (NotFoundException | ValidationException e) {
            assertEquals("Product ID in payload must be equal to requested 'id'", e.getMessage());
            throw e;
        }
    }

    @Test(expected = NotFoundException.class)
    public void testValidateProductIDForUpdate_NotFound() {
        Integer productID = 13860428;
        Product product = new Product(productID, "", new Price(productID, BigDecimal.valueOf(15.00), "USD"));
        when(productRepository.findById(productID)).thenReturn(Optional.empty());
        try {
            ReflectionTestUtils.invokeMethod(productProcessor, "validateProductIDForUpdate", productID, product);
        } catch (NotFoundException | ValidationException e) {
            assertEquals("Resource not found for Product ID - 13860428", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testUpdateProductPrice_Success() {
        Integer productID = 13860428;
        Product product = new Product(productID, "", new Price(productID, BigDecimal.valueOf(15.00), "USD"));
        when(productRepository.findById(productID)).thenReturn(Optional.of(new Price(productID, BigDecimal.valueOf(11.42), "USD")));
        when(productRepository.save(product.getCurrentPrice())).thenReturn(product.getCurrentPrice());
        Product updatedProduct = productProcessor.updateProductPrice(productID, product);
        assertEquals(product, updatedProduct);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateProductPrice_NotFound() {
        Integer productID = 13860428;
        Product product = new Product(productID, "", new Price(productID, BigDecimal.valueOf(15.00), "USD"));
        when(productRepository.findById(productID)).thenReturn(Optional.empty());
        when(productRepository.save(product.getCurrentPrice())).thenReturn(product.getCurrentPrice());
        try {
            productProcessor.updateProductPrice(productID, product);
        } catch (NotFoundException | ValidationException e) {
            assertEquals("Resource not found for Product ID - 13860428", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void testUpdateProductPrice_differentIDs() {
        Integer productID = 13860428;
        Product product = new Product(1234, "", new Price(productID, BigDecimal.valueOf(15.00), "USD"));
        try {
            productProcessor.updateProductPrice(productID, product);
        } catch (NotFoundException | ValidationException e) {
            assertEquals("Product ID in payload must be equal to requested 'id'", e.getMessage());
            throw e;
        }
    }

}
