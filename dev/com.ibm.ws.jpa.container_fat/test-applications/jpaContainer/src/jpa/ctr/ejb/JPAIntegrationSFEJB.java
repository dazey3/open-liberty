package jpa.ctr.ejb;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.UserTransaction;

import jpa.entity.XMLEntity;

@Stateful(name = "JPAIntegrationSFEJB")
@TransactionManagement(javax.ejb.TransactionManagementType.BEAN)
public class JPAIntegrationSFEJB {

    @Resource
    private SessionContext sessionContext;

    @PersistenceContext(unitName = "TestXML", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Resource
    protected UserTransaction tx;

    public EntityManager getEntityManager() {
        return em;
    }

    public XMLEntity find(int i) {
        return em.find(XMLEntity.class, i);
    }

    public void clear() {
        em.clear();
    }

    public void insert(XMLEntity x) throws Exception {
        try {
            tx.begin();
            em.persist(x);
        } finally {
            tx.commit();
        }
    }

    public void delete(XMLEntity x) throws Exception {
        try {
            tx.begin();
            em.remove(x);
        } finally {
            tx.commit();
        }
    }

    public void update(Integer i, String s) throws Exception {
        try {
            tx.begin();
            XMLEntity x = find(i);
            x.setStrData(s);
        } finally {
            tx.commit();
        }
    }

    public boolean contains(XMLEntity x) {
        return em.contains(x);
    }

    @Remove
    public void remove() {}

}
