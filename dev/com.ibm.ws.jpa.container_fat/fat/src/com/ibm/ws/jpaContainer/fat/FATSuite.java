package com.ibm.ws.jpaContainer.fat;

import java.io.File;
import java.io.FilenameFilter;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.websphere.simplicity.ShrinkHelper;

import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;

@RunWith(Suite.class)
@SuiteClasses({ JPAContainerTest.class,
                JPADefaultProviderTest.class,
                JPAOSGiTest.class,
                JPAIntegrationTest.class
})
public class FATSuite {
    static final LibertyServer server = LibertyServerFactory.getLibertyServer("com.ibm.ws.jpa.container.fat");
    static final String JEE_APP = "jpaContainer";

    @BeforeClass
    public static void beforeSuite() throws Exception {
        // Locate the JPA-RS file
        File jpars = new File("test-applications/resources").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("org.eclipse.persistence.jpars_");
            }
        })[0];

        // Create a normal Java EE application and export to server
        WebArchive app = ShrinkWrap.create(WebArchive.class, JEE_APP + ".war")//
                        .addPackages(true, "jpa.ctr.web")// web module
                        .addPackages(true, "jpa.entity")// entities
                        .addPackages(true, "jpa.provider.undead")// fake persistence provider
                        .addPackages(true, "org.extension.jpa")// fake persistence provider that extends EclipseLink
                        .addAsLibrary(jpars)//
                        .addAsWebInfResource(new File("test-applications/resources/persistence.xml"),
                                             "classes/META-INF/persistence.xml");
        ShrinkHelper.exportAppToServer(server, app);
    }
}