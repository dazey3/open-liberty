package com.ibm.ws.jpaContainer.fat;

import static com.ibm.ws.jpaContainer.fat.FATSuite.JEE_APP;

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
public class JPADefaultProviderTest extends FATServletClient {
    public static final String SERVLET_NAME = "JPAContainerTestServlet";
    private static final LibertyServer server = LibertyServerFactory.getLibertyServer("com.ibm.ws.jpa.container.fat.ejb");

    @BeforeClass
    public static void setUp() throws Exception {
        // Create a normal Java EE application and export to server
        WebArchive war = ShrinkWrap.create(WebArchive.class, JEE_APP + ".war")//
                        .addPackages(true, "jpa.ctr.web");// web module

        JavaArchive lib = ShrinkWrap.create(JavaArchive.class, "persistence.jar")//
                        .addPackages(true, "jpa.entity")// entities
                        .addPackages(true, "jpa.provider.undead")// fake persistence provider
                        .addPackages(true, "org.extension.jpa")// fake persistence provider that extends EclipseLink
                        .addAsManifestResource(new File("test-applications/resources/app-persistence.xml"), "persistence.xml");

        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, JEE_APP + "-ejb.jar")//
                        .addPackages(true, "jpa.ctr.ejb");// ejb module

        EnterpriseArchive app = ShrinkWrap.create(EnterpriseArchive.class, JEE_APP + ".ear");
        app.addAsModule(war);
        app.addAsLibrary(lib);
        app.addAsModule(ejbJar);

        ShrinkHelper.exportAppToServer(server, app);

        server.addInstalledAppForValidation(JEE_APP);
        server.startServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stopServer();
    }

    private void runTest(String appName) throws Exception {
        runTest(server, appName + '/' + SERVLET_NAME, testName);
    }

    // Verify that jpa defaultPersistenceProvider takes precedence over JPA providers found by bells
    @Test
    public void testJPADefaultPersistenceProvider() throws Exception {
        runTest(JEE_APP);
    }

    // Verify that persistence.xml <provider> takes precedence over jpa defaultPersistenceProvider
    @Test
    public void testPersistenceUnitInfo() throws Exception {
        runTest(JEE_APP);
    }

    // Verify that persistence service can be used alongside configured JPA providers for the JPA container.
    // Persistence service uses a hidden internal version of EclipseLink.  In this case, we schedule a
    // persistent EJB timer which is built on persistent executor and persistence service.  When the
    // persistent EJB timer runs, we have it insert a new entity which the test case can later check for.
    @Test
    public void testSingleExecutionPersistentTimer() throws Exception {
        runTest(JEE_APP);
    }
}
