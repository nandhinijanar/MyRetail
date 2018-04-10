package myretail.handler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import myretail.dto.MyRetailProperties;
import myretail.exception.ClientServiceException;
import myretail.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

@Component
public class RedskyClientHandler {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MyRetailProperties myRetailProperties;

    /**
     * TODO
     * @param productID
     * @return
     */
    public String getProductName(Integer productID) {

        try {
            ResponseEntity<Map> productResponse =
                    restTemplate.getForEntity(MessageFormat.format(myRetailProperties.getRedskyClientEndpoint(), String.valueOf(productID)), Map.class);

            if (productResponse.getStatusCode().equals(HttpStatus.OK)) {
                Map<String, Map> productInfo = productResponse.getBody();
                if (CollectionUtils.isEmpty(productInfo)) {
                    throw new NotFoundException("Product Information is unavailable for Product ID - " + productID);
                } else {
                    Map itemMap = (Map) productInfo.get("product").get("item");
                    if (CollectionUtils.isEmpty(itemMap)) {
                        throw new NotFoundException("Product Information is unavailable for Product ID - " + productID);
                    } else {
                        if ((itemMap.get("tcin") != null) && (Integer.valueOf((String) itemMap.get("tcin")).equals(productID))) {
                            Map productDescription = (Map) itemMap.get("product_description");
                            if (!CollectionUtils.isEmpty(productDescription)) {
                                return String.valueOf(productDescription.get("title"));
                            }
                        }
                    }
                }
            } else {
                throw new ClientServiceException(String.format("Exception - Redsky Client returned status : %d", productResponse.getStatusCode().value()));
            }
        } catch (HttpClientErrorException e) {
            throw new ClientServiceException(String.format("Exception in calling Redsky Client - %s", e.getMessage()));
        }

        return null;
    }

}
