package com.ibm.ws.jpaContainer.fat;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.ShrinkHelper;

import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;
import componenttest.topology.utils.FATServletClient;

@RunWith(FATRunner.class)
public class JPAIntegrationTest extends FATServletClient {
    public static final String SERVLET_NAME = "JPAContainerIntTestServlet";
    static final String APP = "jpaIntegration";
    private static final LibertyServer server = LibertyServerFactory.getLibertyServer("com.ibm.ws.jpa.container.fat.integration");

    @BeforeClass
    public static void setUp() throws Exception {

        WebArchive war = ShrinkWrap.create(WebArchive.class, APP + ".war")//
                        .addPackages(true, "jpa.entity")// entities
                        .addPackages(true, "jpa.ctr.integration.web")//
                        .addAsWebInfResource(new File("test-applications/resources/validation.xml"), "validation.xml") //
                        .addAsWebInfResource(new File("test-applications/resources/basic-constraints.xml"), "validation/basic-constraints.xml"); //

        JavaArchive lib = ShrinkWrap.create(JavaArchive.class, "persistence.jar")//
                        .addPackages(true, "jpa.entity")// entities
                        .addAsManifestResource(new File("test-applications/resources/integration-persistence.xml"), "persistence.xml") //
                        .addAsManifestResource(new File("test-applications/resources/beans.xml"), "beans.xml") //
                        .addAsManifestResource(new File("test-applications/resources/orm.xml"), "orm.xml");

        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, APP + "-ejb.jar")//
                        .addPackages(true, "jpa.ctr.ejb");// ejb module
        EnterpriseArchive app = ShrinkWrap.create(EnterpriseArchive.class, APP + ".ear");
        app.addAsModule(war);
        app.addAsLibrary(lib);
        app.addAsModule(ejbJar);
        ShrinkHelper.exportAppToServer(server, app);

        server.addInstalledAppForValidation(APP);
        server.startServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stopServer();
    }

    private void runTest(String appName) throws Exception {
        runTest(server, appName + '/' + SERVLET_NAME, testName);
    }

    @Test
    public void testCDIWeb() throws Exception {
        runTest(APP);
    }

    @Test
    public void testCDIEJB() throws Exception {
        runTest(APP);
    }

    @Test
    public void testBeanValidationAnno() throws Exception {
        runTest(APP);
    }

    //@Test //TODO: eclipselink bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=487889
    public void testBeanValidationXML() throws Exception {
        runTest(APP);
    }

    @Test
    public void testTransactionWebXML() throws Exception {
        runTest(APP);
    }

    @Test
    public void testTransactionWebJTA() throws Exception {
        runTest(APP);
    }

    @Test
    public void testTransactionWebRL() throws Exception {
        runTest(APP);
    }

    @Test
    public void testTransactionEJBEx() throws Exception {
        runTest(APP);
    }
}
