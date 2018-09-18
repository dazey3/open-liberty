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
package org.extension.jpa;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

/**
 * Extension of the EclipseLink persistence provider
 */
public class ExtendedPersistenceProvider extends org.eclipse.persistence.jpa.PersistenceProvider {
    @SuppressWarnings("unchecked")
    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
        map.put("eclipselink.target-server", "WebSphere_Liberty");
        map.put(ExtendedPersistenceProvider.class.getName(), true);
        return super.createContainerEntityManagerFactory(info, map);
    }
}
