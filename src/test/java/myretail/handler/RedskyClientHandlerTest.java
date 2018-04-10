package myretail.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import myretail.dto.MyRetailProperties;
import myretail.exception.ClientServiceException;
import myretail.exception.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


public class RedskyClientHandlerTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    MyRetailProperties myRetailProperties;

    @InjectMocks
    RedskyClientHandler redskyClientHandler;

    Integer productID = 13860428;
    String url = "http://redsky.target.com/v2/pdp/tcin/13860428?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics";

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        when(myRetailProperties.getRedskyClientEndpoint()).
                thenReturn("http://redsky.target.com/v2/pdp/tcin/{0}?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics");
    }

    @Test
    public void testGetProductName_Success() throws IOException {

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockRedskyClientResponse("redskyResponse_Success.json"), HttpStatus.OK);
        when(restTemplate.getForEntity(url, Map.class)).thenReturn(responseEntity);
        String name = redskyClientHandler.getProductName(productID);
        assertNotNull(name);
        assertEquals("The Big Lebowski (Blu-ray)", name);
    }

    @Test(expected = NotFoundException.class)
    public void testGetProductName_NotFound() throws IOException {
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockRedskyClientResponse("redskyResponse_EmptyResponse.json"), HttpStatus.OK);
        when(restTemplate.getForEntity(url, Map.class)).thenReturn(responseEntity);
        try {
            redskyClientHandler.getProductName(productID);
        } catch (NotFoundException e) {
            assertEquals("Product Information is unavailable for Product ID - 13860428", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ClientServiceException.class)
    public void testGetProductName_ClientException() {
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(url, Map.class)).thenReturn(responseEntity);
        try {
            redskyClientHandler.getProductName(productID);
        } catch (ClientServiceException e) {
            assertEquals("Exception - Redsky Client returned status : 404", e.getMessage());
            throw e;
        }
    }

    private Map<String, Map> mockRedskyClientResponse(String fileName) throws IOException {

        File file = new File("src/test/resources/" + fileName);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, Map.class);

    }
}
