package myretail.controller;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import myretail.dto.MyRetailProperties;
import myretail.dto.Product;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MyRetailProperties myRetailProperties;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    public static final String CREATE_KEYSPACE_QUERY  = "CREATE KEYSPACE IF NOT EXISTS testKeySpace WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '3' };";

    @BeforeClass
    public static void startEmbeddedCassandra() throws InterruptedException, IOException, TTransportException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        Cluster cluster = Cluster.builder()
                .addContactPoints("127.0.0.1").withPort(9142).build();
        Session session = cluster.connect();
        session.execute(CREATE_KEYSPACE_QUERY);
        session.execute("USE testKeySpace;");
        session.execute("CREATE TABLE testKeySpace.product_price(productID int, price decimal, currencyCode text, primary key(productID));");
        //Insert data to use for GET endpoint
        session.execute("INSERT INTO testKeySpace.product_price(productID, price, currencyCode) VALUES (13860428, 13.49, 'USD');");
        //Insert data to use for PUT endpoint
        session.execute("INSERT INTO testKeySpace.product_price(productID, price, currencyCode) VALUES (15117729, 12.00, 'USD');");
        session.execute("INSERT INTO testKeySpace.product_price(productID, price, currencyCode) VALUES (16483589, 52.78, 'USD');");
        session.execute("INSERT INTO testKeySpace.product_price(productID, price, currencyCode) VALUES (16696652, 79.00, 'USD');");
        session.execute("INSERT INTO testKeySpace.product_price(productID, price, currencyCode) VALUES (16752456, 278.33, 'USD');");
    }

    @AfterClass
    public static void stopEmbeddedCassandra() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Before
    public void setUp() {
        int wireMockPort = wireMockRule.port();
        when(myRetailProperties.getRedskyClientEndpoint()).
                thenReturn("http://localhost:" + wireMockPort +
                        "/v2/pdp/tcin/{0}?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics");
    }

    @Test
    public void testGetProductData_Success() throws Exception {
        stubFor(get(urlPathMatching("/v2/pdp/tcin/([0-9]*)")).
                willReturn(aResponse().withStatus(200).withHeader("content-type", MediaType.APPLICATION_JSON_VALUE).
                        withBody(readJsonFile("redskyResponse_Success.json"))));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/MyRetail/v1/products/13860428")).
                andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value())).andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        Product product = mapper.readValue(response, Product.class);
        assertNotNull(product);
        assertEquals(Integer.valueOf(13860428), product.getProductID());
        assertEquals("The Big Lebowski (Blu-ray)", product.getName());
        assertEquals(BigDecimal.valueOf(13.49), product.getCurrentPrice().getPrice());
        assertEquals("USD", product.getCurrentPrice().getCurrencyCode());

    }

    @Test
    public void testGetProductData_NotFound() throws Exception {
        stubFor(get(urlPathMatching("/v2/pdp/tcin/([0-9]*)")).
                willReturn(aResponse().withStatus(200).withHeader("content-type", MediaType.APPLICATION_JSON_VALUE).
                        withBody(readJsonFile("redskyResponse_EmptyResponse.json"))));

         mockMvc.perform(MockMvcRequestBuilders.get("/MyRetail/v1/products/13860428")).
                 andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void testGetProductData_BadRequest() throws Exception {
         mockMvc.perform(MockMvcRequestBuilders.get("/MyRetail/v1/products/0")).
                andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testUpdateProductPrice_Success() throws Exception {
        MockHttpServletRequestBuilder mockBuilder = MockMvcRequestBuilders.put("/MyRetail/v1/products/15117729");
        mockBuilder.contentType(MediaType.APPLICATION_JSON_VALUE);
        mockBuilder.content(readJsonFile("ModifyProductPriceRequest_Success.json"));
        MvcResult mvcResult = mockMvc.perform(mockBuilder).andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value())).andReturn();
    }

    @Test
    public void testUpdateProductPrice_BadRequest() throws Exception {
        MockHttpServletRequestBuilder mockBuilder = MockMvcRequestBuilders.put("/MyRetail/v1/products/15117729");
        mockBuilder.contentType(MediaType.APPLICATION_JSON_VALUE);
        mockBuilder.content(readJsonFile("ModifyProductPriceRequest_BadRequest.json"));
        mockMvc.perform(mockBuilder).andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testUpdateProductPrice_differentIDs() throws Exception {
        MockHttpServletRequestBuilder mockBuilder = MockMvcRequestBuilders.put("/MyRetail/v1/products/1234");
        mockBuilder.contentType(MediaType.APPLICATION_JSON_VALUE);
        mockBuilder.content(readJsonFile("ModifyProductPriceRequest_Success.json"));
        mockMvc.perform(mockBuilder).andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void testUpdateProductPrice_NotFound() throws Exception {
        MockHttpServletRequestBuilder mockBuilder = MockMvcRequestBuilders.put("/MyRetail/v1/products/1234");
        mockBuilder.contentType(MediaType.APPLICATION_JSON_VALUE);
        mockBuilder.content(readJsonFile("ModifyProductPriceRequest_NotFound.json"));
        mockMvc.perform(mockBuilder).andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()));
    }

    private String readJsonFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/"+fileName)));
    }


}
