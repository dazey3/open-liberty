package jpa.ctr.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.SynchronizationType;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.extension.jpa.ExtendedPersistenceProvider;

import jpa.entity.Person;
import jpa.provider.undead.ZombiePersistenceProvider;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/JPAContainerTestServlet")
public class JPAContainerTestServlet extends HttpServlet {
    // Maximum number of nanoseconds to wait for an operation to finish.
    private static final long TIMEOUT_NS = TimeUnit.MINUTES.toNanos(2);

    @Resource
    UserTransaction tx;

    @Resource(name = "java:comp/env/jdbc/ds", lookup = "jdbc/ds")
    DataSource ds;

    @PersistenceContext(unitName = "PU_datasource")
    EntityManager em;

    @PersistenceContext(unitName = "PU_datasource", synchronization = SynchronizationType.UNSYNCHRONIZED)
    EntityManager emUnsync;

    @PersistenceContext(unitName = "PU_zombie")
    EntityManager emz;

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

    /**
     * Verify we can load one of the jpa-2.1 spec interfaces, as well
     * as an EclipseLink implementation class.
     */
    public void testLoadEclipseLinkPersistenceClass() throws Exception {
        Class.forName("javax.persistence.Persistence");
        Class.forName("org.eclipse.persistence.jpa.PersistenceProvider");
    }

    /**
     * Verify we can load one of the jpa-2.1 spec interfaces, as well
     * as a fake JPA provider class.
     */
    public void testLoadZombiePersistenceClass() throws Exception {
        Class.forName("javax.persistence.Persistence");
        Class.forName("jpa.provider.undead.ZombiePersistenceProvider");
    }

    /**
     * Verify that the Liberty EclipseLink service is not registered
     * when only jpaContainer-2.1 is enabled
     */
    public void testCantLoadLibertyEcl() throws Exception {
        // NOTE: This test is an ugly hack.  Don't ever do this in a real application.
        Class<?> LibertyCL = Thread.currentThread().getContextClassLoader().getClass();
        Class<?> FrameworkUtil = Class.forName("org.osgi.framework.FrameworkUtil", false, LibertyCL.getClassLoader());
        Object bundle = FrameworkUtil.getMethod("getBundle", Class.class).invoke(null, LibertyCL);
        Object bundleContext = bundle.getClass().getMethod("getBundleContext").invoke(bundle);
        Method getServiceReference = bundleContext.getClass().getMethod("getServiceReference", String.class);

        Object jpaSvcRef = getServiceReference.invoke(bundleContext, "com.ibm.ws.jpa.JPAProviderIntegration");

        if (jpaSvcRef.toString().contains("com.ibm.ws.jpa.container.eclipselink.EclipseLinkJPAProvider"))
            fail("Should not be able to get a service reference to Liberty JPA 2.1 service when only the jpaContainer-2.1 feature is enabled. \n" +
                 "Got service reference: " + jpaSvcRef);
    }

    /**
     * Verify that defaultPersistenceProvider under the jpa element takes precedence over providers found by bells.
     */
    public void testJPADefaultPersistenceProvider() throws Exception {
        Map<String, Object> emfProps = em.getEntityManagerFactory().getProperties();
        assertTrue("Entity manager properties should identify the provider as the one we defaulted via the jpa element " + emfProps,
                   Boolean.TRUE.equals(emfProps.get(ExtendedPersistenceProvider.class.getName())));

        tx.begin();

        assertTrue(em.isJoinedToTransaction());

        Person p = new Person();
        p.setName("James");
        em.persist(p);

        Person found = em.find(Person.class, "James");
        assertEquals("James", found.getName());

        tx.commit();
    }

    /**
     * Verify we can perform JSE-like usage of eclipselink by creating an emf and persisting an entity.
     */
    public void testJSEPersistenceUnit() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU_url");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Person p = new Person();
        p.setName("Alice");
        em.persist(p);
        em.getTransaction().commit();

        Person found = em.find(Person.class, "Alice");
        assertEquals("Alice", found.getName());

        em.close();
    }

    /**
     * Verify we can perform JSE-like usage by creating an entity manager factory.
     */
    public void testJSEZombiePersistenceUnit() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU_zombie");
        EntityManager em = emf.createEntityManager();

        Map<String, Object> props = em.getProperties();
        assertNotSame(PersistenceUnitTransactionType.JTA, props.get("TransactionType"));

        assertFalse(em.isJoinedToTransaction());

        tx.begin();

        assertFalse(em.isJoinedToTransaction());

        tx.commit();

        em.close();
    }

    /**
     * Verify we can perform JSE-like usage of eclipselink by creating an emf and persisting an entity.
     */
    public void testJSEDataSourcePersistenceUnit() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU_datasource");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Person p = new Person();
        p.setName("Bob");
        em.persist(p);
        em.getTransaction().commit();

        Person found = em.find(Person.class, "Bob");
        assertEquals("Bob", found.getName());

        em.close();
    }

    // Use EclipseLink MOXy to convert an entity to XML.
    public void testMOXy() throws Exception {
        Person p = new Person("Peter");
        JAXBContext jaxb = JAXBContextFactory.createContext(new Class[] { Person.class }, Collections.EMPTY_MAP);
        JAXBElement<Person> personElement = new JAXBElement<Person>(new QName("person"), Person.class, p);
        StringWriter writer = new StringWriter();
        jaxb.createMarshaller().marshal(personElement, writer);
        String s = writer.toString();
        assertTrue("XML should look like <person><name>Peter</name></person>. Instead: " + s,
                   s.matches(".*(<person>).*(<name>Peter</name>).*(</person>).*"));

        // Use MOXy extension to provide JAXB metadata without modifying the entity source code
        StringBuilder xmlMetadata = new StringBuilder()//
                        .append("<?xml version=\"1.0\"?>\n")//
                        .append("<xml-bindings xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence/oxm\" version=\"2.1\">\n")//
                        .append("  <java-types>\n")//
                        .append("    <java-type name=\"jpa.entity.Person\">\n")//
                        .append("      <xml-root-element/>\n")//
                        .append("      <xml-type prop-order=\"name\"/>\n")//
                        .append("      <java-attributes>\n")//
                        .append("        <xml-element java-attribute=\"name\" xml-path=\"@firstname\"/>\n")//
                        .append("      </java-attributes>\n")//
                        .append("    </java-type>\n")//
                        .append("  </java-types>\n")//
                        .append("</xml-bindings>\n");
        Map<String, Source> metadataMap = new HashMap<String, Source>();
        metadataMap.put("jpa.entity", new StreamSource(new StringReader(xmlMetadata.toString())));
        @SuppressWarnings("deprecation")
        Map<String, Object> properties = Collections.<String, Object> singletonMap(JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY, metadataMap);

        jaxb = JAXBContextFactory.createContext(new Class[] { Person.class }, properties);
        writer = new StringWriter();
        jaxb.createMarshaller().marshal(personElement, writer);
        s = writer.toString();
        assertTrue("XML should look like <person firstname=\"Peter\".... Instead: " + s,
                   s.contains("<person firstname=\"Peter\""));
    }

    public void testPersistenceContext() throws Exception {
        tx.begin();

        assertTrue(em.isJoinedToTransaction());

        Person p = new Person();
        p.setName("Charley");
        em.persist(p);

        Person found = em.find(Person.class, "Charley");
        assertEquals("Charley", found.getName());

        tx.commit();

        Map<String, Object> emfProps = em.getEntityManagerFactory().getProperties();
        assertTrue("Entity manager properties should identify the provider as the one we defaulted to via bells " + emfProps,
                   emfProps.containsKey("eclipselink.ddl-generation") && !emfProps.containsKey(ExtendedPersistenceProvider.class.getName()));
    }

    // This test uses an alternate JPA provider that is specified in persistence.xml instead of the default that is determined by the bells-1.0 feature
    public void testPersistenceUnitInfo() throws Exception {
        // Our fake persistence provider puts information from PersistenceUnitInfo into the properties so that we can test it here
        Map<String, Object> props = emz.getProperties();

        DataSource jtaDataSource = (DataSource) props.get("JtaDataSource");
        assertNotNull(jtaDataSource);

        @SuppressWarnings("unchecked")
        List<String> managedClassNames = (List<String>) props.get("ManagedClassNames");
        assertTrue("Entity class name should be found in " + managedClassNames, managedClassNames.contains(Person.class.getName()));

        assertNull(props.get("NonJtaDataSource"));
        assertEquals(ZombiePersistenceProvider.class.getName(), props.get("PersistenceProviderClassName"));
        assertEquals("PU_zombie", props.get("PersistenceUnitName"));
        assertEquals(SynchronizationType.SYNCHRONIZED, props.get("Synchronization"));
        assertEquals(PersistenceUnitTransactionType.JTA, props.get("TransactionType"));
        // assertEquals(ValidationMode.CALLBACK, props.get("ValidationMode")); TODO test this once we enable bean validation for jpaContainer-2.1?
    }

    // TODO Workaround for OSGi apps which don't seem to be supplied with @PersistenceContext SynchronizationType when entity manager is created.
    // This is not unique to jpaContainer-2.1 and also applies to jpa-2.1 - a defect will be opened for it, after which this can be removed.
    public void testPersistenceUnitInfoOSGiAppWorkaround() throws Exception {
        // Our fake persistence provider puts information from PersistenceUnitInfo into the properties so that we can test it here
        Map<String, Object> props = emz.getProperties();

        DataSource jtaDataSource = (DataSource) props.get("JtaDataSource");
        assertNotNull(jtaDataSource);

        @SuppressWarnings("unchecked")
        List<String> managedClassNames = (List<String>) props.get("ManagedClassNames");
        assertTrue("Entity class name should be found in " + managedClassNames, managedClassNames.contains(Person.class.getName()));

        assertNull(props.get("NonJtaDataSource"));
        assertEquals(ZombiePersistenceProvider.class.getName(), props.get("PersistenceProviderClassName"));
        assertEquals("PU_zombie", props.get("PersistenceUnitName"));
        assertNotSame(SynchronizationType.UNSYNCHRONIZED, props.get("Synchronization"));
        assertEquals(PersistenceUnitTransactionType.JTA, props.get("TransactionType"));
        // assertEquals(ValidationMode.CALLBACK, props.get("ValidationMode")); TODO test this once we enable bean validation for jpaContainer-2.1?
    }

    public void testSingleExecutionPersistentTimer() throws Exception {
        Object ejb = InitialContext.doLookup("java:global/jpaContainer/jpaContainer-ejb/JPAContainerTestEJB!jpa.ctr.ejb.JPAContainerTestEJB");
        ejb.getClass().getMethod("testSingleExecutionPersistentTimer", String.class).invoke(ejb, "George");

        Person found = null;
        for (long start = System.nanoTime(); found == null && System.nanoTime() - start < TIMEOUT_NS; Thread.sleep(200))
            found = em.find(Person.class, "George");

        assertNotNull(found);
        assertEquals("George", found.getName());
    }

    public void testUnsynchronized() throws Exception {
        tx.begin();

        assertFalse(emUnsync.isJoinedToTransaction());

        emUnsync.joinTransaction();

        assertTrue(emUnsync.isJoinedToTransaction());

        Person p = new Person();
        p.setName("Michael");
        emUnsync.persist(p);

        tx.commit();

        emUnsync.clear();
        Person found = emUnsync.find(Person.class, "Michael");
        assertEquals("Michael", found.getName());
    }
}
