package jpa.ctr.integration.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;

import jpa.ctr.ejb.JPAIntegrationSFEJB;
import jpa.entity.BeanValEntity;
import jpa.entity.BeanValXMLEntity;
import jpa.entity.LoggingService;
import jpa.entity.Widget;
import jpa.entity.XMLEntity;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/JPAContainerIntTestServlet")
public class JPAIntegrationTestServlet extends HttpServlet {

    @Resource
    UserTransaction tx;

    @EJB(beanName = "JPAContainerSFEJB")
    JPAIntegrationSFEJB extendedEJB;

    @Inject
    // used for checking callbacks to entity listener
    private LoggingService logger;

    @PersistenceContext(unitName = "TestCDI")
    private EntityManager emCDI;

    @PersistenceContext(unitName = "TestBVal")
    private EntityManager emBVal;

    @PersistenceContext(unitName = "TestXML")
    private EntityManager emXML;

    @PersistenceContext(unitName = "TestBValXML")
    private EntityManager emBValXML;

    @PersistenceUnit(unitName = "TestXML")
    EntityManagerFactory emfJTA;

    @PersistenceUnit(unitName = "TestResourceLocal")
    EntityManagerFactory emfRL;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("BEGIN " + request.getRequestURL() + '?' + request.getQueryString());

        PrintWriter writer = response.getWriter();
        String method = request.getParameter("testMethod");
        if (method != null && method.length() > 0) {
            try {
                try {
                    Method mthd = getClass().getMethod(method, HttpServletRequest.class, HttpServletResponse.class);
                    mthd.invoke(this, request, response);
                } catch (NoSuchMethodException nsme) {
                    Method mthd = getClass().getMethod(method, (Class<?>[]) null);
                    mthd.invoke(this);
                }

                writer.println("SUCCESS");
            } catch (Throwable t) {
                if (t instanceof InvocationTargetException) {
                    t = t.getCause();
                }

                System.out.println("ERROR: " + t);
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                System.err.print(sw);

                writer.println("ERROR: Caught exception attempting to call test method " + method + " on servlet " + getClass().getName());
                t.printStackTrace(writer);
            }
        } else {
            System.out.println("ERROR: expected testMethod parameter");
            writer.println("ERROR: expected testMethod parameter");
        }

        writer.flush();
        writer.close();

        System.out.println("END");
    }

    //Test to ensure that CDI container integration is functioning with web servlets
    public void testCDIWeb() throws Exception {

        // Clear messages
        getEntityListenerMessages();
        insert(tx, "circle", "A round widget", emCDI);

        // Expect to see PrePersist, then PostPersist.  There may be a PostConstruct
        // which should be ignored.
        List<String> listenerMessages = getEntityListenerMessages();
        assertNotNull(listenerMessages);

        int indexOfPrePersist = -1;
        int indexOfPostPersist = -1;
        int index = 0;
        for (String s : listenerMessages) {

            if (s.contains("prePersist")) {
                indexOfPrePersist = index;
            }
            if (s.contains("postPersist")) {
                indexOfPostPersist = index;
            }

            index++;
        }
        assertNotSame("prePersist was not called", -1, indexOfPrePersist);
        assertNotSame("postPersist was not called", -1, indexOfPostPersist);
        assertTrue("prePersist did not occur before postPersist", indexOfPostPersist > indexOfPrePersist);
        assertTrue("prePersist message: " + listenerMessages.get(indexOfPrePersist), listenerMessages.get(indexOfPrePersist).contains("name=circle"));
        assertTrue("postPersist message: " + listenerMessages.get(indexOfPostPersist), listenerMessages.get(indexOfPostPersist).contains("name=circle"));

    }

    //Test to ensure that CDI container integration is functioning with ejb
    public void testCDIEJB() throws Exception {
        Object ejb = InitialContext.doLookup("java:global/jpaIntegration/jpaIntegration-ejb/JPAIntegrationCDIEJB!jpa.ctr.ejb.JPAIntegrationCDIEJB");
        ejb.getClass().getMethod("getEntityListenerMessages").invoke(ejb);
        ejb.getClass().getMethod("insert", String.class, String.class).invoke(ejb, "circle", "A round widget");
        @SuppressWarnings("unchecked")
        List<String> listenerMessages = (List<String>) ejb.getClass().getMethod("getEntityListenerMessages").invoke(ejb);
        assertNotNull(listenerMessages);

        int indexOfPrePersist = -1;
        int indexOfPostPersist = -1;
        int index = 0;
        for (String s : listenerMessages) {

            if (s.contains("prePersist")) {
                indexOfPrePersist = index;
            }
            if (s.contains("postPersist")) {
                indexOfPostPersist = index;
            }

            index++;
        }
        assertNotSame("prePersist was not called", -1, indexOfPrePersist);
        assertNotSame("postPersist was not called", -1, indexOfPostPersist);
        assertTrue("prePersist did not occur before postPersist", indexOfPostPersist > indexOfPrePersist);
        assertTrue("prePersist message: " + listenerMessages.get(indexOfPrePersist), listenerMessages.get(indexOfPrePersist).contains("name=circle"));
        assertTrue("postPersist message: " + listenerMessages.get(indexOfPostPersist), listenerMessages.get(indexOfPostPersist).contains("name=circle"));

    }

    //Test to ensure bean validation is working with annotations
    public void testBeanValidationAnno() throws Exception {
        try {

            emBVal.clear();
            tx.begin();
            BeanValEntity entity = new BeanValEntity();

            entity.setId(1);
            entity.setName(null);

            try {
                emBVal.persist(entity);
                tx.commit();

                throw new Exception("Validation did not occur: null name should not be allowed");
            } catch (ConstraintViolationException cve) {

                assertEquals("ValEntity.name is null",
                             cve.getConstraintViolations().iterator().next().getMessage());
            }
        } finally {
            if (tx.getStatus() != Status.STATUS_NO_TRANSACTION && tx.getStatus() != Status.STATUS_UNKNOWN) {
                tx.rollback();
            }
        }

        try {

            emBVal.clear();
            tx.begin();
            BeanValEntity entity1 = new BeanValEntity();

            entity1.setId(1);
            entity1.setName("John");

            emBVal.persist(entity1);
            tx.commit();

        } finally {
            if (tx.getStatus() != Status.STATUS_NO_TRANSACTION && tx.getStatus() != Status.STATUS_UNKNOWN) {
                tx.rollback();
            }
        }

    }

    //Test to ensure bean validation is working with XML (and contained managed transactions)
    public void testBeanValidationXML() throws Exception {
        try {

            emBValXML.clear();
            tx.begin();
            BeanValXMLEntity entity = new BeanValXMLEntity();

            entity.setId(1);
            entity.setName(null);

            try {
                emBValXML.persist(entity);
                tx.commit();

                throw new Exception("Validation did not occur: null name should not be allowed");
            } catch (ConstraintViolationException cve) {

                assertEquals("BeanValXMLEntity.name is null",
                             cve.getConstraintViolations().iterator().next().getMessage());
            }
        } finally {
            if (tx.getStatus() != Status.STATUS_NO_TRANSACTION && tx.getStatus() != Status.STATUS_UNKNOWN) {
                tx.rollback();
            }
        }

        try {

            emBValXML.clear();
            tx.begin();
            BeanValXMLEntity entity1 = new BeanValXMLEntity();

            entity1.setId(1);
            entity1.setName("John");

            emBValXML.persist(entity1);
            tx.commit();

        } finally {
            if (tx.getStatus() != Status.STATUS_NO_TRANSACTION && tx.getStatus() != Status.STATUS_UNKNOWN) {
                tx.rollback();
            }
        }

    }

    //Test to ensure transactions are working with xml entities
    public void testTransactionWebXML() throws Exception {
        // Clear persistence context
        emXML.clear();

        XMLEntity newEntity = new XMLEntity();
        newEntity.setId(1);
        newEntity.setStrData("Some String Data");

        // 2) Persist the new entity to the database
        tx.begin();
        emXML.persist(newEntity);
        tx.commit();

        // 3) Verify the entity was saved to the database
        emXML.clear();

        // Begin a new transaction, to ensure the entity returned by find is managed
        // by the persistence context in all environments, including CM-TS.
        tx.begin();

        XMLEntity findEntity = emXML.find(XMLEntity.class, 1);
        assertNotNull("Assert that the find operation did not return null", findEntity);
        assertNotSame(
                      "Assert find did not return the original object",
                      newEntity, findEntity);
        assertTrue(
                   "Assert entity returned by find is managed by the persistence context.",
                   emXML.contains(findEntity));
        assertEquals(
                     "Assert that the entity's id is 1",
                     1, findEntity.getId());
        assertEquals(
                     "Assert that the entity's strData field is \"Some String Data\"",
                     "Some String Data", findEntity.getStrData());

        // 4) Update the entity
        findEntity.setStrData("New Data");
        tx.commit();

        // 5) Verify the entity update was saved to the database
        emXML.clear();
        tx.begin();
        XMLEntity findUpdatedEntity = emXML.find(XMLEntity.class, 1);
        assertNotNull("Assert that the find operation did not return null", findUpdatedEntity);
        assertNotSame(
                      "Assert find did not return the original object",
                      newEntity, findUpdatedEntity);
        assertNotSame(
                      "Assert find did not return the first object returned by find",
                      findEntity, findUpdatedEntity);
        assertTrue(
                   "Assert entity returned by find is managed by the persistence context.",
                   emXML.contains(findUpdatedEntity));
        assertEquals(
                     "Assert that the entity's id is 1",
                     1, findUpdatedEntity.getId());
        assertEquals(
                     "Assert that the entity's strData field is \"New Data\"",
                     "New Data", findUpdatedEntity.getStrData());

        // 6) Delete the entity from the database
        emXML.remove(findUpdatedEntity);
        tx.commit();

        // 7) Verify the entity remove was successful
        emXML.clear();
        XMLEntity findRemovedEntity = emXML.find(XMLEntity.class, 1);
        assertNull("Assert that the find operation did return null", findRemovedEntity);
    }

    //Test to ensure transactions are working with application managed JTA
    public void testTransactionWebJTA() throws Exception {
        // Clear persistence context
        EntityManager emJTA = emfJTA.createEntityManager();

        emJTA.clear();

        XMLEntity newEntity = new XMLEntity();
        newEntity.setId(1);
        newEntity.setStrData("Some String Data");

        // 2) Persist the new entity to the database
        tx.begin();
        emJTA.joinTransaction();
        emJTA.persist(newEntity);
        tx.commit();

        // 3) Verify the entity was saved to the database
        emJTA.clear();

        // Begin a new transaction, to ensure the entity returned by find is managed
        // by the persistence context in all environments, including CM-TS.
        tx.begin();
        emJTA.joinTransaction();
        XMLEntity findEntity = emJTA.find(XMLEntity.class, 1);
        assertNotNull("Assert that the find operation did not return null", findEntity);
        assertNotSame(
                      "Assert find did not return the original object",
                      newEntity, findEntity);
        assertTrue(
                   "Assert entity returned by find is managed by the persistence context.",
                   emJTA.contains(findEntity));
        assertEquals(
                     "Assert that the entity's id is 1",
                     1, findEntity.getId());
        assertEquals(
                     "Assert that the entity's strData field is \"Some String Data\"",
                     "Some String Data", findEntity.getStrData());

        // 4) Update the entity
        findEntity.setStrData("New Data");
        tx.commit();

        // 5) Verify the entity update was saved to the database
        emJTA.clear();
        tx.begin();
        emJTA.joinTransaction();
        XMLEntity findUpdatedEntity = emJTA.find(XMLEntity.class, 1);
        assertNotNull("Assert that the find operation did not return null", findUpdatedEntity);
        assertNotSame(
                      "Assert find did not return the original object",
                      newEntity, findUpdatedEntity);
        assertNotSame(
                      "Assert find did not return the first object returned by find",
                      findEntity, findUpdatedEntity);
        assertTrue(
                   "Assert entity returned by find is managed by the persistence context.",
                   emJTA.contains(findUpdatedEntity));
        assertEquals(
                     "Assert that the entity's id is 1",
                     1, findUpdatedEntity.getId());
        assertEquals(
                     "Assert that the entity's strData field is \"New Data\"",
                     "New Data", findUpdatedEntity.getStrData());

        // 6) Delete the entity from the database
        emJTA.remove(findUpdatedEntity);
        tx.commit();

        // 7) Verify the entity remove was successful
        emJTA.clear();
        XMLEntity findRemovedEntity = emJTA.find(XMLEntity.class, 1);
        assertNull("Assert that the find operation did return null", findRemovedEntity);
        emJTA.close();
    }

    //Test to ensure transactions are working with application managed resource local
    public void testTransactionWebRL() throws Exception {
        // Clear persistence context
        EntityManager emRL = emfRL.createEntityManager();

        emRL.clear();

        XMLEntity newEntity = new XMLEntity();
        newEntity.setId(1);
        newEntity.setStrData("Some String Data");

        // 2) Persist the new entity to the database
        EntityTransaction t = emRL.getTransaction();
        t.begin();
        emRL.persist(newEntity);
        t.commit();

        // 3) Verify the entity was saved to the database
        emRL.clear();

        // Begin a new transaction, to ensure the entity returned by find is managed
        // by the persistence context in all environments, including CM-TS.
        t.begin();
        XMLEntity findEntity = emRL.find(XMLEntity.class, 1);
        assertNotNull("Assert that the find operation did not return null", findEntity);
        assertNotSame(
                      "Assert find did not return the original object",
                      newEntity, findEntity);
        assertTrue(
                   "Assert entity returned by find is managed by the persistence context.",
                   emRL.contains(findEntity));
        assertEquals(
                     "Assert that the entity's id is 1",
                     1, findEntity.getId());
        assertEquals(
                     "Assert that the entity's strData field is \"Some String Data\"",
                     "Some String Data", findEntity.getStrData());

        // 4) Update the entity
        findEntity.setStrData("New Data");
        t.commit();

        // 5) Verify the entity update was saved to the database
        emRL.clear();
        t.begin();
        XMLEntity findUpdatedEntity = emRL.find(XMLEntity.class, 1);
        assertNotNull("Assert that the find operation did not return null", findUpdatedEntity);
        assertNotSame(
                      "Assert find did not return the original object",
                      newEntity, findUpdatedEntity);
        assertNotSame(
                      "Assert find did not return the first object returned by find",
                      findEntity, findUpdatedEntity);
        assertTrue(
                   "Assert entity returned by find is managed by the persistence context.",
                   emRL.contains(findUpdatedEntity));
        assertEquals(
                     "Assert that the entity's id is 1",
                     1, findUpdatedEntity.getId());
        assertEquals(
                     "Assert that the entity's strData field is \"New Data\"",
                     "New Data", findUpdatedEntity.getStrData());

        // 6) Delete the entity from the database
        emRL.remove(findUpdatedEntity);
        t.commit();

        // 7) Verify the entity remove was successful
        emRL.clear();
        XMLEntity findRemovedEntity = emRL.find(XMLEntity.class, 1);
        assertNull("Assert that the find operation did return null", findRemovedEntity);
        emRL.close();
    }

    //Test to ensure transactions are working with container managed extended
    public void testTransactionEJBEx() throws Exception {
        try {
            XMLEntity newEntity = new XMLEntity();
            newEntity.setId(1);
            newEntity.setStrData("Some String Data");

            // 2) Persist the new entity to the database
            extendedEJB.clear();
            extendedEJB.insert(newEntity);

            // 3) Verify the entity was saved to the database
            extendedEJB.clear();

            XMLEntity findEntity = extendedEJB.find(1);

            assertNotNull("Assert that the find operation did not return null", findEntity);
            assertNotSame(
                          "Assert find did not return the original object",
                          newEntity, findEntity);
            assertTrue(
                       "Assert entity returned by find is managed by the persistence context.",
                       extendedEJB.contains(findEntity));
            assertEquals(
                         "Assert that the entity's id is 1",
                         1, findEntity.getId());
            assertEquals(
                         "Assert that the entity's strData field is \"Some String Data\"",
                         "Some String Data", findEntity.getStrData());

            // 4) Update the entity
            extendedEJB.update(1, "New Data");

            // 5) Verify the entity update was saved to the database
            extendedEJB.clear();

            XMLEntity findUpdatedEntity = extendedEJB.find(1);
            assertNotNull("Assert that the find operation did not return null", findUpdatedEntity);
            assertNotSame(
                          "Assert find did not return the original object",
                          newEntity, findUpdatedEntity);
            assertNotSame(
                          "Assert find did not return the first object returned by find",
                          findEntity, findUpdatedEntity);
            assertTrue(
                       "Assert entity returned by find is managed by the persistence context.",
                       extendedEJB.contains(findUpdatedEntity));
            assertEquals(
                         "Assert that the entity's id is 1",
                         1, findUpdatedEntity.getId());
            assertEquals(
                         "Assert that the entity's strData field is \"New Data\"",
                         "New Data", findUpdatedEntity.getStrData());

            // 6) Delete the entity from the database
            extendedEJB.delete(findUpdatedEntity);

            // 7) Verify the entity remove was successful
            extendedEJB.clear();
            XMLEntity findRemovedEntity = extendedEJB.find(1);
            assertNull("Assert that the find operation did return null", findRemovedEntity);
        } finally {
            extendedEJB.remove();
        }
    }

    private void insert(UserTransaction t, String name, String description, EntityManager e) throws Exception {
        try {
            t.begin();
            Widget w = new Widget();
            w.setName(name);
            w.setDescription(description);
            e.persist(w);
        } finally {
            t.commit();
        }
    }

    public List<String> getEntityListenerMessages() {
        return logger.getAndClearMessages();
    }
}
