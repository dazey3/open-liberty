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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Default;
import javax.inject.Singleton;

@Default
@Singleton
public class LoggingServiceImpl implements LoggingService {

    private List<String> _messages = new ArrayList<String>();

    @Override
    public synchronized void log(String s) {
        System.out.println(s);
        _messages.add(s);
    }

    @Override
    public synchronized List<String> getAndClearMessages() {
        List<String> messages = _messages;
        _messages = new ArrayList<String>();
        return messages;
    }
}
