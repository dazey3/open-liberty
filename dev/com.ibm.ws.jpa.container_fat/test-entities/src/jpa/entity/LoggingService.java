/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WLP Copyright IBM Corp. 2014
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package jpa.entity;

import java.util.List;

public interface LoggingService {

    public void log(String s);

    public List<String> getAndClearMessages();
}
