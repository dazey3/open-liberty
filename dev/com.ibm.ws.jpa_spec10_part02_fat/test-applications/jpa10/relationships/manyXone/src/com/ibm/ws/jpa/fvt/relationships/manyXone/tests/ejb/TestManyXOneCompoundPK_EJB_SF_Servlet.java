/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.jpa.fvt.relationships.manyXone.tests.ejb;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.servlet.annotation.WebServlet;

import org.junit.Test;

import com.ibm.ws.jpa.fvt.relationships.manyXone.entities.compoundpk.annotation.EmbedIDMOEntityA;
import com.ibm.ws.jpa.fvt.relationships.manyXone.entities.compoundpk.annotation.EmbedIDMOEntityB;
import com.ibm.ws.jpa.fvt.relationships.manyXone.entities.compoundpk.annotation.IDClassMOEntityA;
import com.ibm.ws.jpa.fvt.relationships.manyXone.entities.compoundpk.annotation.IDClassMOEntityB;
import com.ibm.ws.jpa.fvt.relationships.manyXone.entities.compoundpk.xml.XMLEmbedIDMOEntityA;
import com.ibm.ws.jpa.fvt.relationships.manyXone.entities.compoundpk.xml.XMLEmbedIDMOEntityB;
import com.ibm.ws.jpa.fvt.relationships.manyXone.entities.compoundpk.xml.XMLIDClassMOEntityA;
import com.ibm.ws.jpa.fvt.relationships.manyXone.entities.compoundpk.xml.XMLIDClassMOEntityB;
import com.ibm.ws.jpa.fvt.relationships.manyXone.testlogic.ManyXOneCompoundPKTestLogic;
import com.ibm.ws.testtooling.testinfo.JPAPersistenceContext;
import com.ibm.ws.testtooling.testinfo.JPAPersistenceContext.PersistenceContextType;
import com.ibm.ws.testtooling.testinfo.JPAPersistenceContext.PersistenceInjectionType;
import com.ibm.ws.testtooling.vehicle.web.EJBTestVehicleServlet;

@WebServlet(urlPatterns = "/TestManyXOneCompoundPK_EJB_SF_Servlet")
public class TestManyXOneCompoundPK_EJB_SF_Servlet extends EJBTestVehicleServlet {
    private static final long serialVersionUID = 1L;

    @PostConstruct
    private void initFAT() {
        testClassName = ManyXOneCompoundPKTestLogic.class.getName();
        ejbJNDIName = "ejb/ManyXOneSFEJB";

        jpaPctxMap.put("test-jpa-resource-amjta",
                       new JPAPersistenceContext("test-jpa-resource-amjta", PersistenceContextType.APPLICATION_MANAGED_JTA, PersistenceInjectionType.JNDI, "java:comp/env/jpa/ManyXOne_CompoundPK_AMJTA"));
        jpaPctxMap.put("test-jpa-resource-amrl",
                       new JPAPersistenceContext("test-jpa-resource-amrl", PersistenceContextType.APPLICATION_MANAGED_RL, PersistenceInjectionType.JNDI, "java:comp/env/jpa/ManyXOne_CompoundPK_AMRL"));
        jpaPctxMap.put("test-jpa-resource-cmts",
                       new JPAPersistenceContext("test-jpa-resource-cmts", PersistenceContextType.CONTAINER_MANAGED_TS, PersistenceInjectionType.JNDI, "java:comp/env/jpa/ManyXOne_CompoundPK_CMTS"));
    }

    /*
     * Verify that the JPA provider can manage ManyXOne relationships where the
     * entity on the inverse side of the relationship has a compound (Embeddable/ID Class) primary key.
     * Entities and IdClass are defined in annotation.
     *
     * Test Strategy:
     *
     * 1) Start a new transaction
     * 2) Create ICompoundPKManyXOneEntityA(id=1)
     * Create ICompoundPKManyXOneEntityB(id=1)
     * Set ICompoundPKManyXOneEntityA(id=1) to reference ICompoundPKManyXOneEntityB(id=1) in
     * a ManyXOne relationship
     * 3) Commit the transaction
     * 4) Clear the persistence context
     * 5) Find ICompoundPKManyXOneEntityA(id=1), access ICompoundPKManyXOneEntityB(id=1) from
     * ICompoundPKManyXOneEntityA(id=1)'s ManyXOne relationship field.
     *
     * Test passes if the relationship properly references ICompoundPKManyXOneEntityB(id=1)
     */

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_Ano_AMJTA_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_Ano_AMJTA_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-amjta";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", EmbedIDMOEntityA.class);
        properties.put("EntityBName", EmbedIDMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_XML_AMJTA_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_XML_AMJTA_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-amjta";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", XMLEmbedIDMOEntityA.class);
        properties.put("EntityBName", XMLEmbedIDMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_Ano_AMRL_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_Ano_AMRL_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-amrl";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", EmbedIDMOEntityA.class);
        properties.put("EntityBName", EmbedIDMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_XML_AMRL_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_XML_AMRL_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-amrl";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", XMLEmbedIDMOEntityA.class);
        properties.put("EntityBName", XMLEmbedIDMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_Ano_CMTS_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_Ano_CMTS_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-cmts";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", EmbedIDMOEntityA.class);
        properties.put("EntityBName", EmbedIDMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_XML_CMTS_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_Embeddable_001_XML_CMTS_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-cmts";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", XMLEmbedIDMOEntityA.class);
        properties.put("EntityBName", XMLEmbedIDMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_Ano_AMJTA_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_Ano_AMJTA_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-amjta";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", IDClassMOEntityA.class);
        properties.put("EntityBName", IDClassMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_XML_AMJTA_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_XML_AMJTA_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-amjta";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", XMLIDClassMOEntityA.class);
        properties.put("EntityBName", XMLIDClassMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_Ano_AMRL_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_Ano_AMRL_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-amrl";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", IDClassMOEntityA.class);
        properties.put("EntityBName", IDClassMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_XML_AMRL_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_XML_AMRL_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-amrl";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", XMLIDClassMOEntityA.class);
        properties.put("EntityBName", XMLIDClassMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_Ano_CMTS_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_Ano_CMTS_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-cmts";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", IDClassMOEntityA.class);
        properties.put("EntityBName", IDClassMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }

    @Test
    public void jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_XML_CMTS_EJB_SF() throws Exception {
        final String testName = "jpa10_Relationships_ManyXOne_CompoundPK_IDClass_001_XML_CMTS_EJB_SF";
        final String testMethod = "testManyXOneCompoundPK001";
        final String testResource = "test-jpa-resource-cmts";

        HashMap<String, java.io.Serializable> properties = new HashMap<String, java.io.Serializable>();
        properties.put("EntityAName", XMLIDClassMOEntityA.class);
        properties.put("EntityBName", XMLIDClassMOEntityB.class);

        executeDDL("JPA10_MANYXONE_DELETE_${dbvendor}.ddl");
        executeTest(testName, testMethod, testResource, properties);
    }
}
