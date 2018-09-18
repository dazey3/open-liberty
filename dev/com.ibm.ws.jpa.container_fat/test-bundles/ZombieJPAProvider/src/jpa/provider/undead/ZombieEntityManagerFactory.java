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

import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

/**
 * A minimal entity manager factory for tests that rejects most operations and doesn't actually persist anything.
 */
public class ZombieEntityManagerFactory implements EntityManagerFactory {
    final PersistenceUnitInfo info;
    volatile boolean open = true;
    private final Map<String, Object> props = new TreeMap<String, Object>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    ZombieEntityManagerFactory(PersistenceUnitInfo info, Map<String, Object> map) {
        this.info = info;
        if (map != null)
            props.putAll(map);
        if (info != null) {
            // Add PersistenceUnitInfo to the properties so that we can test values from the application
            props.putAll((Map) info.getProperties());
            props.put("JarFileUrls", info.getJarFileUrls());
            props.put("JtaDataSource", info.getJtaDataSource());
            props.put("ManagedClassNames", info.getManagedClassNames());
            props.put("MappingFileNames", info.getMappingFileNames());
            props.put("NonJtaDataSource", info.getNonJtaDataSource());
            props.put("PersistenceProviderClassName", info.getPersistenceProviderClassName());
            props.put("PersistenceUnitName", info.getPersistenceUnitName());
            props.put("PersistenceUnitRootUrl", info.getPersistenceUnitRootUrl());
            props.put("SharedCacheMode", info.getSharedCacheMode());
            props.put("TransactionType", info.getTransactionType());
            props.put("ValidationMode", info.getValidationMode());
        }
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addNamedQuery(String name, Query query) {}

    @Override
    public void close() {
        if (open)
            open = false;
        else
            throw new IllegalStateException();
    }

    @Override
    public EntityManager createEntityManager() {
        return createEntityManager(null, null);
    }

    @Override
    public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
        return createEntityManager(null, map);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType syncType) {
        return createEntityManager(syncType, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityManager createEntityManager(SynchronizationType syncType, @SuppressWarnings("rawtypes") Map map) {
        if (open)
            return new ZombieEntityManager(this, syncType, map);
        else
            throw new IllegalStateException();
    }

    @Override
    public Cache getCache() {
        return null;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Metamodel getMetamodel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getProperties() {
        if (open)
            return props;
        else
            throw new IllegalStateException();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> ifc) {
        if (ifc.isInstance(this))
            return (T) this;
        throw new PersistenceException("Not supported for " + ifc);
    }
}