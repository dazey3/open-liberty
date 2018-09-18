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

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

/**
 * A minimal persistence provider for testing that cannot actually persistent anything.
 */
public class ZombiePersistenceProvider implements PersistenceProvider {
    @SuppressWarnings("unchecked")
    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
        return new ZombieEntityManagerFactory(info, map);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityManagerFactory createEntityManagerFactory(String emName, @SuppressWarnings("rawtypes") Map map) {
        return new ZombieEntityManagerFactory(null, map);
    }

    @Override
    public void generateSchema(PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {}

    @Override
    public boolean generateSchema(String puName, @SuppressWarnings("rawtypes") Map map) {
        return false;
    }

    @Override
    public ProviderUtil getProviderUtil() {
        throw new UnsupportedOperationException();
    }
}
