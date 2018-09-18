/*
* IBM Confidential
*
* OCO Source Materials
*
* WLP Copyright IBM Corp. 2017
*
* The source code for this program is not published or otherwise divested
* of its trade secrets, irrespective of what has been deposited with the
* U.S. Copyright Office.
*/
package jpa.provider.undead;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.SynchronizationType;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * A non-functional entity manager for tests that mostly returns null/empty values and doesn't actually persist anything.
 * Because we are only aiming to test the JPA container integration, and most entity manager methods would be invoked
 * by the application rather than the container, we shouldn't need to implement much on the entity manager itself.
 * In cases where something is specifically wanted for a test, it could be added, but we will not do so preemptively.
 */
public class ZombieEntityManager implements EntityManager {
    final ZombieEntityManagerFactory emf;
    volatile boolean open = true;
    final Map<String, Object> props;

    ZombieEntityManager(ZombieEntityManagerFactory emf, SynchronizationType syncType, Map<String, Object> props) {
        this.emf = emf;
        this.props = new TreeMap<String, Object>(emf.getProperties());
        if (props != null)
            this.props.putAll(props);
        if (syncType != null)
            this.props.put("Synchronization", syncType);
    }

    @Override
    public void clear() {}

    @Override
    public void close() {
        if (emf.info == null) // not container managed
            open = false;
        else
            ; // throw new IllegalStateException("container managed"); TODO JavaDoc says this is an error, but it causes an FFDC at runtime. Is there a bug?
    }

    @Override
    public boolean contains(Object entity) {
        return false;
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return null;
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return null;
    }

    @Override
    public Query createNamedQuery(String name) {
        return null;
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return null;
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return null;
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        return null;
    }

    @Override
    public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
        return null;
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return null;
    }

    @Override
    public Query createQuery(String qlString) {
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return null;
    }

    @Override
    public Query createQuery(@SuppressWarnings("rawtypes") CriteriaUpdate updateQuery) {
        return null;
    }

    @Override
    public Query createQuery(@SuppressWarnings("rawtypes") CriteriaDelete deleteQuery) {
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, @SuppressWarnings("rawtypes") Class... resultClasses) {
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return null;
    }

    @Override
    public void detach(Object entity) {}

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return null;
    }

    @Override
    public void flush() {}

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return null;
    }

    @Override
    public Object getDelegate() {
        return null;
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return null;
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return null;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        if (open)
            return emf;
        else
            throw new IllegalStateException();
    }

    @Override
    public FlushModeType getFlushMode() {
        return (FlushModeType) props.get("FlushMode");
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return null;
    }

    @Override
    public Metamodel getMetamodel() {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        return new TreeMap<String, Object>(props);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return null;
    }

    @Override
    public EntityTransaction getTransaction() {
        return null;
    }

    @Override
    public boolean isJoinedToTransaction() {
        try {
            return ((UserTransaction) InitialContext.doLookup("java:comp/UserTransaction")).getStatus() == Status.STATUS_ACTIVE
                   && SynchronizationType.SYNCHRONIZED.equals(props.get("Synchronization"));
        } catch (NamingException x) {
            return false;
        } catch (SystemException x) {
            return false;
        }
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void joinTransaction() {
        if (SynchronizationType.UNSYNCHRONIZED.equals(props.get("Synchronization")))
            throw new UnsupportedOperationException("Not implemented for UNSYNCHRONIZED");
        try {
            if (((UserTransaction) InitialContext.doLookup("java:comp/UserTransaction")).getStatus() != Status.STATUS_ACTIVE)
                throw new IllegalStateException();
        } catch (NamingException x) {
            throw (TransactionRequiredException) new TransactionRequiredException().initCause(x);
        } catch (SystemException x) {
            throw (TransactionRequiredException) new TransactionRequiredException().initCause(x);
        }
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {}

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {}

    @Override
    public <T> T merge(T entity) {
        return null;
    }

    @Override
    public void persist(Object entity) {}

    @Override
    public void refresh(Object entity) {}

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {}

    @Override
    public void refresh(Object entity, LockModeType lockMode) {}

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> props) {}

    @Override
    public void remove(Object entity) {}

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        props.put("FlushMode", flushMode);
    }

    @Override
    public void setProperty(String name, Object value) {
        props.put(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> ifc) {
        if (ifc.isInstance(this))
            return (T) this;
        throw new PersistenceException("Not supported for " + ifc);
    }
}