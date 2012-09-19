package org.jboss.as.quickstarts.ejb.multi.server.app;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

/**
 * <p>Simple bean with methods to get the node name of the server and log messages.
 * One method is annotated with a security role. The security-domain is declared within the 
 * deployment descriptor jboss-ejb3.xml instead of using the annotation.</p>
 * <p>If the security-domain is removed the secured method can be invoked from every user.
 * The shown principal user is 'anonymous' instead of the original logged in user (AS 7.1, 7.2 and EAP6.0)</p>
 * 
 * <p>The EJB is marked as clustered by using the xml deployment descriptor, see <code>jboss-ejb3.xml</code></p>
 *  
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
@Stateless
public class AppOneBean implements AppOne {
    private static final Logger LOGGER = Logger.getLogger(AppOneBean.class);
    
    @Resource SessionContext context;
    
    @Override
    public String getJBossNodeName() {
        return System.getProperty("jboss.node.name");
    }
    @Override
    public String invoke(String text) {
        Principal caller = context.getCallerPrincipal();
        LOGGER.info("["+caller.getName()+"] "+text);
        return "app1["+caller.getName()+"]@"+getJBossNodeName();
    }
    
    @Override
    @RolesAllowed({"AppOne","Intern"})
    public String invokeSecured(String text) {
        Principal caller = context.getCallerPrincipal();
        LOGGER.info("Secured invocation ["+caller.getName()+"] "+text);
        LOGGER.info("Is in Role AppOne="+context.isCallerInRole("AppOne")+" Intern="+context.isCallerInRole("Intern"));
        return "app1["+caller.getName()+"]@"+getJBossNodeName();
    }
}
