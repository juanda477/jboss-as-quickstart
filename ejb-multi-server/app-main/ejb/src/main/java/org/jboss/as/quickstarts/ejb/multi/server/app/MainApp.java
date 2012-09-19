package org.jboss.as.quickstarts.ejb.multi.server.app;

import javax.ejb.Remote;

@Remote
public interface MainApp {

    /**
     * Invoke the sub applications with the recommended lookup names.
     * Return informations about the called beans.
     * 
     * @param text This text will be logged in the main-application
     * @return A simple text representation of the call stack and the destination servers
     */
    String invokeAll(String text);

    String getJBossNodeName();

}
