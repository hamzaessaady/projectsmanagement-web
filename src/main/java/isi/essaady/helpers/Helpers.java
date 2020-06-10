package isi.essaady.helpers;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class Helpers {
	
	public Helpers() {}
	
	/**
     * Displays the Faces message. This is just a helper method
     * 
     * @param severity	The FacesMessage sevirity constant.
     * @param summary	Message summary.
     * @param detail	Message detail.
     */
    public static void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

}
