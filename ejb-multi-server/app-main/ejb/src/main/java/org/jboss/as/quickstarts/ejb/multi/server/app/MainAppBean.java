package org.jboss.as.quickstarts.ejb.multi.server.app;

import java.security.Principal;
import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/**
 * <p>The main bean called by the standalone client.</p>
 * <p>The sub applications, deployed in different servers are called direct or via indirect naming
 * to hide the lookup name and use a configured name via comp/env environment.</p>
 *  
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
@Stateless
public class MainAppBean implements MainApp {
    private static final Logger LOGGER = Logger.getLogger(MainAppBean.class);
    @Resource SessionContext context;
    private InitialContext iCtx;

    /**
     * Initialize an store the context for the EJB invocation.
     */
    @PostConstruct
    public void init() {
        try {
            final Hashtable<String, String> p = new Hashtable<String, String>();
            p.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
            // create the InitialContext
            this.iCtx = new InitialContext(p);
        } catch (NamingException e) {
            throw new RuntimeException("Could not initialize context", e);
        }
    }

    @Override
    public String getJBossNodeName() {
        return System.getProperty("jboss.node.name");
    }

    @Override
    public String invokeAll(String text) {
        Principal caller = context.getCallerPrincipal();
        LOGGER.info("["+caller.getName()+"] "+text);
        final StringBuilder result = new StringBuilder("MainApp["+caller.getName()+"]@" + System.getProperty("jboss.node.name"));
        
        // Call AppOne with the direct ejb: naming
        try {
            result.append("  >  [ "+ invokeAppOne(text));
        }catch(Exception e) {
            LOGGER.error("Could not invoke AppOne",e);
        }
        
        String lookup = "";
        // Call AppTwo with the direct ejb: naming
        try {
            lookup = "ejb:apptwo/ejb//AppTwoBean!" + AppTwo.class.getName();
            result.append(" > "+invokeAppTwo(lookup, text));
            LOGGER.info("Invoke '"+lookup+" OK");
        }catch(Exception e) {
            LOGGER.error("Could not invoke apptwo '"+lookup+"'",e);
        }
        
        // Call AppTwo by using the local alias configured in META-INF/ejb-jar.xml
        try {
            lookup = "java:comp/env/AppTwoAlias";
            result.append(" ; "+invokeAppTwo(lookup, text));
            LOGGER.info("Invoke '"+lookup+" OK");
        }catch(Exception e) {
            LOGGER.error("Could not invoke apptwo '"+lookup+"'",e);
        }

        result.append(" ]");
        
        return result.toString();
    }

    /**
     * The application one can only be called with the standard naming, there is no alias.
     * 
     * @param text Simple text for logging in the target servers logfile
     * @return A text with server details for demonstration
     */
    private String invokeAppOne(String text) {
        try {
            final AppOne bean = (AppOne) iCtx.lookup("ejb:appone/ejb//AppOneBean!" + AppOne.class.getName());

            // invoke on the bean
            final String appOneResult = bean.invoke(text);

            LOGGER.info("AppOne return : " + appOneResult);
            return appOneResult;
        } catch (NamingException e) {
            throw new RuntimeException("Could not invoke appOne", e);
        }
    }


    /**
     * The application one can be called
     * <ul><li>with the standard naming <i>ejb:apptwo/ejb//AppTwoBean!org.jboss.as.quickstarts.ejb.multi.server.app.AppTwo</i></li>
     * <li>the alias provided by the server configuration <i>java:global/AliasAppTwo</i> <b>this is not recommended</b></li>
     * <li>the local alias provided by the ejb-jar.xml configuration <i>java:comp/env/AppTwoAlias</i></li>
     * </ul>
     * 
     * @param text Simple text for logging in the target servers logfile
     * @return A text with server details for demonstration
     */
    private String invokeAppTwo(String lookup, String text) throws NamingException {
        final AppTwo bean = (AppTwo) iCtx.lookup(lookup);

        // invoke on the bean
        final String appTwoResult = bean.invoke(text);

        LOGGER.info("AppTwo return : " + appTwoResult);
        return appTwoResult;
    }
}
