package org.jboss.as.quickstarts.ejb.multi.server;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.as.quickstarts.ejb.multi.server.app.MainApp;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

public class Client {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        // suppress output of client messages
        Logger.getLogger("org.jboss").setLevel(Level.OFF);
        Logger.getLogger("org.xnio").setLevel(Level.OFF);
        
        Properties p = new Properties();
        p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
        p.put("remote.connections", "one");
        p.put("remote.connection.one.port", "4447");
        p.put("remote.connection.one.host", "localhost");
        p.put("remote.connection.one.username", "quickuser");
        p.put("remote.connection.one.password", "quick-123");

        EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(p);
        ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
        EJBClientContext.setSelector(selector);

        Properties props = new Properties();
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        InitialContext context = new InitialContext(props);

        final String rcal = "ejb:appmain/ejb//MainAppBean!" + MainApp.class.getName();
        final MainApp remote = (MainApp) context.lookup(rcal);
        final String result = remote.invokeAll("Client call at "+new Date());

        System.out.println("InvokeAll succeed: "+result);
    }

}
