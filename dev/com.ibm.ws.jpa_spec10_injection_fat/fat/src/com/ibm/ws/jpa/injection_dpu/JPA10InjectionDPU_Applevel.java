/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.jpa.injection_dpu;

import java.util.HashSet;
import java.util.Set;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.testcontainers.containers.JdbcDatabaseContainer;

import com.ibm.websphere.simplicity.ShrinkHelper;
import com.ibm.websphere.simplicity.config.Application;
import com.ibm.websphere.simplicity.config.ClassloaderElement;
import com.ibm.websphere.simplicity.config.ConfigElementList;
import com.ibm.websphere.simplicity.config.ServerConfiguration;
import com.ibm.ws.jpa.FATSuite;
import com.ibm.ws.jpa.JPAFATServletClient;
import com.ibm.ws.jpa.fvt.injectiondpu.ejb.applevel.web.InjectionDPUEJBAppLevelTestServlet;
import com.ibm.ws.jpa.fvt.injectiondpu.web.applevel.jta.AppLevelJTADPUFieldInjectionServlet;
import com.ibm.ws.jpa.fvt.injectiondpu.web.applevel.jta.AppLevelJTADPUMethodInjectionServlet;
import com.ibm.ws.jpa.fvt.injectiondpu.web.applevel.jta.InjectionDPUServlet;
import com.ibm.ws.jpa.fvt.injectiondpu.web.applevel.rl.AppLevelRLDPUFieldInjectionServlet;
import com.ibm.ws.jpa.fvt.injectiondpu.web.applevel.rl.AppLevelRLDPUMethodInjectionServlet;

import componenttest.annotation.Server;
import componenttest.annotation.TestServlet;
import componenttest.annotation.TestServlets;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.custom.junit.runner.Mode;
import componenttest.custom.junit.runner.Mode.TestMode;
import componenttest.topology.database.container.DatabaseContainerType;
import componenttest.topology.database.container.DatabaseContainerUtil;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.utils.PrivHelper;

@RunWith(FATRunner.class)
@Mode(TestMode.FULL)
public class JPA10InjectionDPU_Applevel extends JPAFATServletClient {
    private final static String RESOURCE_ROOT = "test-applications/injection_dpu/";
    private final static String applicationName = "InjectionDPUAppLevel";

    private final static Set<String> dropSet = new HashSet<String>();
    private final static Set<String> createSet = new HashSet<String>();

    private static long timestart = 0;

    static {
        dropSet.add("JPA10_INJECTION_DPU_DROP_${dbvendor}.ddl");
        createSet.add("JPA10_INJECTION_DPU_CREATE_${dbvendor}.ddl");
    }

    @Server("JPAServer")
    @TestServlets({
                    @TestServlet(servlet = InjectionDPUServlet.class, path = "injectiondpu_appjta" + "/" + "InjectionDPUServlet"),
                    @TestServlet(servlet = AppLevelJTADPUFieldInjectionServlet.class, path = "injectiondpu_appjta" + "/" + "AppLevelJTADPUFieldInjectionServlet"),
                    @TestServlet(servlet = AppLevelJTADPUMethodInjectionServlet.class, path = "injectiondpu_appjta" + "/" + "AppLevelJTADPUMethodInjectionServlet"),

                    @TestServlet(servlet = com.ibm.ws.jpa.fvt.injectiondpu.web.applevel.rl.InjectionDPUServlet.class, path = "injectiondpu_apprl" + "/" + "InjectionDPUServlet"),
                    @TestServlet(servlet = AppLevelRLDPUFieldInjectionServlet.class, path = "injectiondpu_apprl" + "/" + "AppLevelRLDPUFieldInjectionServlet"),
                    @TestServlet(servlet = AppLevelRLDPUMethodInjectionServlet.class, path = "injectiondpu_apprl" + "/" + "AppLevelRLDPUMethodInjectionServlet"),

                    @TestServlet(servlet = InjectionDPUEJBAppLevelTestServlet.class, path = "applvlejbexecutor" + "/" + "InjectionDPUEJBAppLevelTestServlet")
    })
    public static LibertyServer server;

    public static final JdbcDatabaseContainer<?> testContainer = FATSuite.testContainer;

    @BeforeClass
    public static void setUp() throws Exception {
        int appStartTimeout = server.getAppStartTimeout();
        if (appStartTimeout < (120 * 1000)) {
            server.setAppStartTimeout(120 * 1000);
        }

        int configUpdateTimeout = server.getConfigUpdateTimeout();
        if (configUpdateTimeout < (120 * 1000)) {
            server.setConfigUpdateTimeout(120 * 1000);
        }

        PrivHelper.generateCustomPolicy(server, FATSuite.JAXB_PERMS);
        bannerStart(JPA10InjectionDPU_Applevel.class);
        timestart = System.currentTimeMillis();

        server.addEnvVar("repeat_phase", FATSuite.repeatPhase);

        //Get driver name
        server.addEnvVar("DB_DRIVER", DatabaseContainerType.valueOf(testContainer).getDriverName());

        //Setup server DataSource properties
        DatabaseContainerUtil.setupDataSourceProperties(server, testContainer);

        server.startServer();

        setupDatabaseApplication(server, RESOURCE_ROOT + "ddl/");

        final Set<String> ddlSet = new HashSet<String>();

        ddlSet.clear();
        for (String ddlName : dropSet) {
            ddlSet.add(ddlName.replace("${dbvendor}", getDbVendor().name()));
        }
        executeDDL(server, ddlSet, true);

        ddlSet.clear();
        for (String ddlName : createSet) {
            ddlSet.add(ddlName.replace("${dbvendor}", getDbVendor().name()));
        }
        executeDDL(server, ddlSet, false);

        setupTestApplication();
    }

    /*
     * Construct InjectionDPUAppLevel.ear
     */
    private static void setupTestApplication() throws Exception {
        final JavaArchive testApiJar = buildTestAPIJar();

        // InjectionDPUAppLevelJTAEJB.jar
        final JavaArchive ejbjar1 = ShrinkWrap.create(JavaArchive.class, "InjectionDPUAppLevelJTAEJB.jar");
        ejbjar1.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.entities");
        ejbjar1.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.testlogic");
        ejbjar1.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.ejb.applevel.jta");
        ShrinkHelper.addDirectory(ejbjar1, RESOURCE_ROOT + "/apps/InjectionDPUAppLevel.ear/InjectionDPUAppLevelJTAEJB.jar");

        // InjectionDPUAppLevelRLEJB.jar
        final JavaArchive ejbjar2 = ShrinkWrap.create(JavaArchive.class, "InjectionDPUAppLevelRLEJB.jar");
        ejbjar2.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.entities");
        ejbjar2.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.testlogic");
        ejbjar2.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.ejb.applevel.rl");
        ShrinkHelper.addDirectory(ejbjar2, RESOURCE_ROOT + "/apps/InjectionDPUAppLevel.ear/InjectionDPUAppLevelRLEJB.jar");

        // InjectionDPUAppLevelJTA.war
        final WebArchive webApp1 = ShrinkWrap.create(WebArchive.class, "InjectionDPUAppLevelJTA.war");
        webApp1.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.entities");
        webApp1.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.testlogic");
        webApp1.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.web.applevel.jta");
        ShrinkHelper.addDirectory(webApp1, RESOURCE_ROOT + "/apps/InjectionDPUAppLevel.ear/InjectionDPUAppLevelJTA.war");

        // InjectionDPUAppLevelRL.war
        final WebArchive webApp2 = ShrinkWrap.create(WebArchive.class, "InjectionDPUAppLevelRL.war");
        webApp2.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.entities");
        webApp2.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.testlogic");
        webApp2.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.web.applevel.rl");
        ShrinkHelper.addDirectory(webApp2, RESOURCE_ROOT + "/apps/InjectionDPUAppLevel.ear/InjectionDPUAppLevelRL.war");

        // EJBExecutor.war
        final WebArchive webApp3 = ShrinkWrap.create(WebArchive.class, "EJBExecutor.war");
        webApp3.addPackages(true, "com.ibm.ws.jpa.fvt.injectiondpu.ejb.applevel.web");
        ShrinkHelper.addDirectory(webApp3, RESOURCE_ROOT + "/apps/InjectionDPUAppLevel.ear/EJBExecutor.war");

        final EnterpriseArchive app = ShrinkWrap.create(EnterpriseArchive.class, "InjectionDPUAppLevel.ear");
        app.addAsModule(ejbjar1);
        app.addAsModule(ejbjar2);
        app.addAsModule(webApp1);
        app.addAsModule(webApp2);
        app.addAsModule(webApp3);
        app.addAsLibrary(testApiJar);
        ShrinkHelper.addDirectory(app, RESOURCE_ROOT + "/apps/InjectionDPUAppLevel.ear", new org.jboss.shrinkwrap.api.Filter<ArchivePath>() {
            @Override
            public boolean include(ArchivePath arg0) {
                if (arg0.get().startsWith("/META-INF/")) {
                    return true;
                }
                return false;
            }

        });

        ShrinkHelper.exportToServer(server, "apps", app);

        Application appRecord = new Application();
        appRecord.setLocation(applicationName + ".ear");
        appRecord.setName(applicationName);

        if (FATSuite.repeatPhase != null && FATSuite.repeatPhase.contains("hibernate")) {
            ConfigElementList<ClassloaderElement> cel = appRecord.getClassloaders();
            ClassloaderElement loader = new ClassloaderElement();
            loader.getCommonLibraryRefs().add("HibernateLib");
            cel.add(loader);
        }

        server.setMarkToEndOfLog();
        ServerConfiguration sc = server.getServerConfiguration();
        sc.getApplications().add(appRecord);
        server.updateServerConfiguration(sc);
        server.saveServerConfiguration();

        HashSet<String> appNamesSet = new HashSet<String>();
        appNamesSet.add(applicationName);
        server.waitForConfigUpdateInLogUsingMark(appNamesSet, "");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            // Clean up database
            try {
                final Set<String> ddlSet = new HashSet<String>();
                for (String ddlName : dropSet) {
                    ddlSet.add(ddlName.replace("${dbvendor}", getDbVendor().name()));
                }
                executeDDL(server, ddlSet, true);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            server.stopServer("CWWJP9991W", // From Eclipselink drop-and-create tables option
                              "WTRN0074E: Exception caught from before_completion synchronization operation" // RuntimeException test, expected
            );
        } finally {
            try {
                ServerConfiguration sc = server.getServerConfiguration();
                sc.getApplications().clear();
                server.updateServerConfiguration(sc);
                server.saveServerConfiguration();

                server.deleteFileFromLibertyServerRoot("apps/" + applicationName + ".ear");
                server.deleteFileFromLibertyServerRoot("apps/DatabaseManagement.war");
            } catch (Throwable t) {
                t.printStackTrace();
            }
            bannerEnd(JPA10InjectionDPU_Applevel.class, timestart);
        }
    }
}
