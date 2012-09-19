package org.jboss.as.quickstarts.ejb.multi.server.app;

import javax.ejb.Remote;

@Remote
public interface AppOne {

    /**
     * Unsecured invocation, will return the name of application, principal and JBoss node.
     * 
     * @param text Simple text written to to the logfile to identify the invocation
     * @return app1[&lt;PrincipalName&gt;]@&lt;jboss.node.name&gt;
     */
    String invoke(String text);

    /**
     * @return The property of jboss.node.name, pattern &lt;host&gt;:&lt;server&gt;
     */
    String getJBossNodeName();
    
    /**
     * Secured invocation for Roles ( AppOne, Intern ).
     * See {@link #invoke(String)}
     * 
     * @param text Simple text written to to the logfile to identify the invocation
     * @return app1[&lt;PrincipalName&gt;]@&lt;jboss.node.name&gt;
     */
    String invokeSecured(String text);
}
