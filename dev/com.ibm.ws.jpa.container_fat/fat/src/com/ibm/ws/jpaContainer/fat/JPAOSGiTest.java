package com.ibm.ws.jpaContainer.fat;

import java.io.File;
import java.io.FilenameFilter;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
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
public class JPAOSGiTest extends FATServletClient {
    private static final String ECL_JPA_OSGI = "eclJpaOSGi";
    private static final String ZOM_JPA_OSGI = "zomJpaOSGi";

    public static final String SERVLET_NAME = "JPAContainerTestServlet";

    private static final LibertyServer server = LibertyServerFactory.getLibertyServer("com.ibm.ws.jpa.container.fat.osgi");

    private void runTest(String appName) throws Exception {
        runTest(server, appName + '/' + SERVLET_NAME, testName);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        // Install user feature
        server.copyFileToLibertyInstallRoot("usr/extension/lib/features/", "features/thirdPartyJPAProviders-1.0.mf");

        // Fake JPA provider bundle
        server.copyFileToLibertyInstallRoot("usr/extension/lib/", "bundles/jpa.provider.undead.zombie_1.0.0.jar");

        // EclipseLink bundles
        server.copyFileToLibertyInstallRoot("usr/extension/lib/", "bundles/jpa.provider.thirdparty.eclipselink_1.0.0.jar");
        File[] bundles = new File("publish/shared/resources/ecl").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("org.eclipse.persistence.");
            }
        });
        for (File bundle : bundles)
            server.copyFileToLibertyInstallRoot("usr/extension/lib/", "../../publish/shared/resources/ecl/" + bundle.getName());

        // Create an OSGi application and export to server
        WebArchive eclJAR = ShrinkWrap.create(WebArchive.class, "ECLJPAOSGi.jar")//
                        .addPackages(true, "jpa.ecl.web")// web module
                        .addPackages(true, "jpa.entity")// entities
                        .addAsManifestResource(new File("test-applications/resources/jpaOSGiApp.eba/ECLJPAOSGi.jar/META-INF/MANIFEST.MF"))//
                        .addAsManifestResource(new File("test-applications/resources/ecl-persistence.xml"),
                                               "persistence.xml");

        WebArchive zomJAR = ShrinkWrap.create(WebArchive.class, "ZOMJPAOSGi.jar")//
                        .addPackages(true, "jpa.ctr.web")// web module
                        .addPackages(true, "jpa.entity")// entities
                        .addAsManifestResource(new File("test-applications/resources/jpaOSGiApp.eba/ZOMJPAOSGi.jar/META-INF/MANIFEST.MF"))//
                        .addAsManifestResource(new File("test-applications/resources/persistence.xml"),
                                               "persistence.xml");

        EnterpriseArchive app = ShrinkWrap.create(EnterpriseArchive.class, "jpaOSGiApp.eba")//
                        .addAsModule(eclJAR)//
                        .addAsModule(zomJAR)//
                        .addAsManifestResource(new File("test-applications/resources/jpaOSGiApp.eba/META-INF/APPLICATION.MF"));
        ShrinkHelper.exportAppToServer(server, app);

        server.addInstalledAppForValidation("jpaOSGiApp");
        server.startServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stopServer();

        // Remove the user extension added during the build process.
        server.deleteDirectoryFromLibertyInstallRoot("usr/extension/");
    }

    @Test
    public void testCantLoadLibertyEcl() throws Exception {
        runTest(ECL_JPA_OSGI);
    }

    // @Test // TODO enable if aries adds support for multiple persistence providers
    public void testJSEDataSourcePersistenceUnit() throws Exception {
        runTest(ECL_JPA_OSGI);
    }

    // @Test // TODO enable if aries adds support for multiple persistence providers
    public void testJSEPersistenceUnit() throws Exception {
        runTest(ECL_JPA_OSGI);
    }

    @Test
    public void testJSEZombiePersistenceUnit() throws Exception {
        runTest(ZOM_JPA_OSGI);
    }

    @Test
    public void testLoadEclipseLinkPersistenceClass() throws Exception {
        runTest(ECL_JPA_OSGI);
    }

    @Test
    public void testLoadZombiePersistenceClass() throws Exception {
        runTest(ZOM_JPA_OSGI);
    }

    @Test
    public void testMOXy() throws Exception {
        runTest(ECL_JPA_OSGI);
    }

    @Test
    public void testPersistenceContext() throws Exception {
        runTest(ECL_JPA_OSGI);
    }

    @Test // TODO remove workaround once following TODO is addressed
    public void testPersistenceUnitInfoOSGiAppWorkaround() throws Exception {
        runTest(ZOM_JPA_OSGI);
    }

    // @Test // TODO can't expect @PersistenceContext with UNSYNCHRONIZED to work for OSGi apps with
    // jpaContainer-2.1 until it gets fixed for jpa-2.1 where it is also busted
    public void testUnsynchronized() throws Exception {
        runTest(ECL_JPA_OSGI);
    }
}
