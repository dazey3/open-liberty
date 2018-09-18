package jpa.ctr.ejb;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import jpa.entity.LoggingService;
import jpa.entity.Widget;

@Stateless(name = "JPAIntegrationCDIEJB")
@TransactionManagement(javax.ejb.TransactionManagementType.BEAN)
public class JPAIntegrationCDIEJB {

    @Resource
    protected UserTransaction tx;

    @Resource
    protected EJBContext ejbCtx;

    protected String beanName = "";

    @PostConstruct
    protected void postConstruct() {
        try {
            beanName = (String) ejbCtx.lookup("beanName");
            System.out.println("Bean self identifying with identity \"" + beanName + "\".");
        } catch (Throwable t) {
            // Swallow.  Defining a bean name is not required by the test framework.
        }
    }

    @PreDestroy
    protected void preDestroy() {

    }

    public String getEnvDefinedBeanName() {
        return beanName;
    }

    @Inject
    // used for checking callbacks to entity listener
    private LoggingService logger;

    @PersistenceContext(unitName = "TestCDI")
    private EntityManager em;

    public List<String> getEntityListenerMessages() {
        return logger.getAndClearMessages();
    }

    public void insert(String name, String description) throws Exception {
        try {
            tx.begin();
            Widget w = new Widget();
            w.setName(name);
            w.setDescription(description);
            em.persist(w);
        } finally {
            tx.commit();
        }
    }
}
