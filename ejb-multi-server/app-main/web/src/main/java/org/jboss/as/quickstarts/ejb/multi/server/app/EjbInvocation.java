package org.jboss.as.quickstarts.ejb.multi.server.app;

public class EjbInvocation {

    private String text;
    private String result;
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getResult() {
        return result;
    }
    void setResult(String result) {
        this.result = result;
    }
}
