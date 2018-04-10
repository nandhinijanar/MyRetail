package myretail.controller;

import io.swagger.annotations.*;
import myretail.dto.Product;
import myretail.exception.ClientServiceException;
import myretail.exception.ErrorDTO;
import myretail.exception.NotFoundException;
import myretail.exception.ValidationException;
import myretail.processor.ProductProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/MyRetail")
@Api(value = "MyRetail")
public class ProductController {

    Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    ProductProcessor productProcessor;

    /**
     * Return Product Information for the product ID specified in the url
     * @param productID
     * @return Product Information as JSON
     */
    @RequestMapping(value = "/v1/products/{id}", method = RequestMethod.GET ,produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the product name and price information for product ID")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "Product ID for which the Product Information is requested",dataType = "integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Validation Error - Bad request"),
            @ApiResponse(code = 404, message = "Product Information not found for given Product ID"),
            @ApiResponse(code = 503, message = "Client Service is unavailable")
    })
    public Product getProductData(@PathVariable("id") Integer productID) {

        return productProcessor.getProductDataForID(productID);
    }

    /**
     * Modify the Price information of a product using Product ID and Request JSON
     * @param productID
     * @param productRequest
     * @return Updated Product Information
     */
    @RequestMapping(value = "/v1/products/{id}", method = RequestMethod.PUT ,produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Modify Product price information for product ID")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "Product ID for which the Product Information is requested",dataType = "integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Validation Error - Bad request"),
            @ApiResponse(code = 404, message = "Product Information not found for given Product ID")
    })
    public Product updateProductPrice(@PathVariable("id") Integer productID, @Valid @RequestBody Product productRequest) {

        return productProcessor.updateProductPrice(productID, productRequest);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException e) {
        logger.error(String.format("Not Found Exception in ProductController - %s", e.getMessage()));
        return e.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ErrorDTO> handleClientServiceException(ClientServiceException e) {
        logger.error(String.format("ClientServiceException in ProductController - %s", e.getMessage()));
        return new ResponseEntity<ErrorDTO>(new ErrorDTO(e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> handleValidationException(ValidationException e) {
        logger.error(String.format("ValidationException in ProductController - %s", e.getMessage()));
        return new ResponseEntity<ErrorDTO>(new ErrorDTO(e.getMessage()), HttpStatus.BAD_REQUEST );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> handleValidationException(MethodArgumentNotValidException e) {
        StringBuilder builder = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            builder.append(error.getField()).append(" ").append(error.getDefaultMessage()).append(". ");
        });
        logger.error(String.format("MethodArgumentNotValidException in ProductController - %s", builder.toString()));
        return new ResponseEntity<ErrorDTO>(new ErrorDTO(builder.toString()), HttpStatus.BAD_REQUEST);
    }

}