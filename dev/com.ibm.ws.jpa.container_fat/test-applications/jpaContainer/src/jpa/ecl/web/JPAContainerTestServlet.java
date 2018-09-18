package jpa.ecl.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.SynchronizationType;
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

import jpa.entity.Person;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/JPAContainerTestServlet")
public class JPAContainerTestServlet extends HttpServlet {

    @Resource
    UserTransaction tx;

    @Resource(name = "java:comp/env/jdbc/ds", lookup = "jdbc/ds")
    DataSource ds;

    @PersistenceContext(unitName = "ECL_datasource")
    EntityManager em;

    @PersistenceContext(unitName = "ECL_datasource", synchronization = SynchronizationType.UNSYNCHRONIZED)
    EntityManager emUnsync;

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
     * Verify we can perform JSE-like usage of eclipselink by creating an emf and persisting an entity.
     */
    public void testJSEPersistenceUnit() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ECL_url");
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
     * Verify we can perform JSE-like usage of eclipselink by creating an emf and persisting an entity.
     */
    public void testJSEDataSourcePersistenceUnit() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ECL_datasource");
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
