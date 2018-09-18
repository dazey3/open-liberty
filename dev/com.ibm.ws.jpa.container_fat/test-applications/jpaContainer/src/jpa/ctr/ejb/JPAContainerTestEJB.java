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
package jpa.ctr.ejb;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import jpa.entity.Person;

@Stateless
public class JPAContainerTestEJB {
    @Resource(name = "java:comp/env/jdbc/ds", lookup = "jdbc/ds")
    private DataSource ds;

    @PersistenceContext(unitName = "PU_datasource")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    @Timeout
    public void runTimer(Timer timer) {
        String name = (String) timer.getInfo();
        System.out.println("Timer will insert " + name);

        Person p = new Person();
        p.setName(name);
        em.persist(p);
    }

    /**
     * Schedule a timer that runs once after 100ms.
     * When the timer runs, it will insert the specified name into the database via JPA.
     */
    public Timer testSingleExecutionPersistentTimer(String name) {
        TimerConfig timerConfig = new TimerConfig(name, true);
        TimerService timerService = sessionContext.getTimerService();
        return timerService.createSingleActionTimer(100, timerConfig);
    }
}
