package com.ibm.ws.jpaContainer.fat;

import static com.ibm.ws.jpaContainer.fat.FATSuite.JEE_APP;
import static com.ibm.ws.jpaContainer.fat.FATSuite.server;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.utils.FATServletClient;

@RunWith(FATRunner.class)
public class JPAContainerTest extends FATServletClient {
    public static final String SERVLET_NAME = "JPAContainerTestServlet";

    @BeforeClass
    public static void setUp() throws Exception {
        server.addInstalledAppForValidation(JEE_APP);
        server.startServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stopServer();
    }

    /**
     * Make an HTTP request and receive the response as a JSON object.
     *
     * @param reqMethod GET/POST/DELETE
     * @param appName application name
     * @param path path beyond the application name
     * @param json optional JSON object to send
     * @param expectedResponseCode response code that must be received
     * @return the response as a JSON object if successful, otherwise null
     */
    private String http(String reqMethod, String appName, String path, String json, int expectedResponseCode) throws Exception {
        URL url = new URL("http://" + server.getHostname() + ':' + server.getHttpDefaultPort() + '/' + appName + '/' + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestMethod(reqMethod);
            con.setRequestProperty("Accept", "application/json");

            if (json != null) {
                con.setRequestProperty("Content-Type", "application/json");
                OutputStream out = con.getOutputStream();
                out.write(json.getBytes("UTF-8"));
                out.close();
            }

            int responseCode = con.getResponseCode();
            if (responseCode != expectedResponseCode)
                throw new Exception("Unexpected response (See HTTP_* constant values on HttpURLConnection): " + responseCode);

            if (responseCode / 100 == 2) { // response codes in the 200s mean success
                StringBuilder response = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                while (in.ready())
                    response.append(in.readLine()).append('\n');
                in.close();
                return response.toString();
            } else
                return null;
        } finally {
            con.disconnect();
        }
    }

    private void runTest(String appName) throws Exception {
        runTest(server, appName + '/' + SERVLET_NAME, testName);
    }

    @Test
    public void testLoadEclipseLinkPersistenceClass() throws Exception {
        runTest(JEE_APP);
    }

    @Test
    public void testLoadZombiePersistenceClass() throws Exception {
        runTest(JEE_APP);
    }

    @Test
    public void testCantLoadLibertyEcl() throws Exception {
        runTest(JEE_APP);
    }

    @Test
    public void testJPARS() throws Exception {
        String response;
        response = http("POST", JEE_APP, "persistence/v2.0/PU_datasource/entity/Person", "{ \"name\": \"Jason\" }", HttpURLConnection.HTTP_OK);
        response = http("POST", JEE_APP, "persistence/v2.0/PU_datasource/entity/Person", "{ \"name\": \"Jacob\" }", HttpURLConnection.HTTP_OK);
        response = http("POST", JEE_APP, "persistence/v2.0/PU_datasource/entity/Person", "{ \"name\": \"John\" }", HttpURLConnection.HTTP_OK);

        response = http("GET", JEE_APP, "persistence/v2.0/PU_datasource/entity/Person/Jason", null, HttpURLConnection.HTTP_OK);
        if (!response.contains("\"name\":\"Jason\""))
            throw new Exception("Unexpected response when querying for first entity: " + response);

        response = http("DELETE", JEE_APP, "persistence/v2.0/PU_datasource/entity/Person/Jason", null, HttpURLConnection.HTTP_OK);
        response = http("GET", JEE_APP, "persistence/v2.0/PU_datasource/entity/Person/Jason", null, HttpURLConnection.HTTP_BAD_REQUEST); // doesn't exist anymore

        response = http("GET", JEE_APP, "persistence/v2.0/PU_datasource/entity/Person/Jacob", null, HttpURLConnection.HTTP_OK);
        if (!response.contains("\"name\":\"Jacob\""))
            throw new Exception("Unexpected response when querying for second entity: " + response);
    }

    @Test
    public void testJSEPersistenceUnit() throws Exception {
        runTest(JEE_APP);
    }

    @Test
    public void testJSEDataSourcePersistenceUnit() throws Exception {
        runTest(JEE_APP);
    }

    @Test
    public void testMOXy() throws Exception {
        runTest(JEE_APP);
    }

    @Test
    public void testPersistenceContext() throws Exception {
        runTest(JEE_APP);
    }

    @Test
    public void testPersistenceUnitInfo() throws Exception {
        runTest(JEE_APP);
    }

    /**
     * Verify there is exactly one occurrence of the message indicating the third party JPA provider version information.
     */
    @Test
    public void testThirdPartyProviderMessage() throws Exception {
        assertEquals(1, server.findStringsInFileInLibertyServerRoot(".*CWWJP0053I.* 2.6.4.v20160829-44060b6", "logs/messages.log").size());
    }

    @Test
    public void testUnsynchronized() throws Exception {
        runTest(JEE_APP);
    }
}
