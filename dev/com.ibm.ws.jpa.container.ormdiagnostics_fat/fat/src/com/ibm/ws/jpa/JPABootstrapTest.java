/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.jpa;

import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.ShrinkHelper;

import componenttest.annotation.Server;
import componenttest.annotation.TestServlet;
import componenttest.annotation.TestServlets;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.utils.FATServletClient;
import componenttest.topology.utils.PrivHelper;
import jpabootstrap.web.TestJPABootstrapServlet;

/**
 * Verify that the JPA Runtime Integration can parse the supported JPA Spec levels of persistence.xml.
 *
 */
@RunWith(FATRunner.class)
public class JPABootstrapTest extends FATServletClient {
    public static final String APP_NAME = "jpabootstrap";
    public static final String SERVLET = "TestJPABootstrap";

    @Server("JPABootstrapFATServer")
    @TestServlets({
                    @TestServlet(servlet = TestJPABootstrapServlet.class, path = APP_NAME + "_1.0/" + SERVLET),
                    @TestServlet(servlet = TestJPABootstrapServlet.class, path = APP_NAME + "_2.0/" + SERVLET),
                    @TestServlet(servlet = TestJPABootstrapServlet.class, path = APP_NAME + "_2.1/" + SERVLET),
                    @TestServlet(servlet = TestJPABootstrapServlet.class, path = APP_NAME + "_2.2/" + SERVLET)
    })
    public static LibertyServer server;

    @BeforeClass
    public static void setUp() throws Exception {
        PrivHelper.generateCustomPolicy(JPABootstrapTest.server, PrivHelper.JAXB_PERMISSION);
        createApplication(JPABootstrapTest.server, "1.0");
        createApplication(JPABootstrapTest.server, "2.0");
        createApplication(JPABootstrapTest.server, "2.1");
        createApplication(JPABootstrapTest.server, "2.2");
        JPABootstrapTest.server.startServer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        JPABootstrapTest.server.stopServer("CWWJP9991W");
    }

    @Test
    public void testJPA10Bootstrap() throws Exception {
        runTest(JPABootstrapTest.server, "1.0");
    }

    @Test
    public void testJPA20Bootstrap() throws Exception {
        runTest(JPABootstrapTest.server, "2.0");
    }

    @Test
    public void testJPA21Bootstrap() throws Exception {
        runTest(JPABootstrapTest.server, "2.1");
    }

    @Test
    public void testJPA22Bootstrap() throws Exception {
        runTest(JPABootstrapTest.server, "2.2");
    }

    private static void createApplication(LibertyServer server, String specLevel) throws Exception {
        final String resPath = "test-applications/" + APP_NAME + "/resources/jpa-" + specLevel + "/";

        WebArchive app = ShrinkWrap.create(WebArchive.class, APP_NAME + "_" + specLevel + ".war");
        app.addPackage("jpabootstrap.web");
        app.addPackage("jpabootstrap.entity");
        app.merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class).importDirectory(resPath).as(GenericArchive.class),
                  "/",
                  Filters.includeAll());
        ShrinkHelper.exportDropinAppToServer(server, app);
    }

    private void runTest(LibertyServer server, String spec) throws Exception {
        FATServletClient.runTest(server, APP_NAME + "_" + spec + "/TestJPABootstrap", "testPersistenceUnitBootstrap");
    }
}
