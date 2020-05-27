package isi.essaady.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.SecurityContext;


/*
 * Resource : The Definitive Guide to JSF in Java EE 8 by Bauke Scholtz, Arjan Tijms.
 * 
 * */


@SuppressWarnings("cdi-ambiguous-dependency")
@Named
@ApplicationScoped
public class Security {
	
	@Inject
	private SecurityContext securityContext;
	
	/*
     * hasAccessToWebResource() method
     * TODO: JAVA DOC
     * 
     * */
	public boolean hasAccessToWebResource(String resource) {
		return securityContext.hasAccessToWebResource(resource, "GET");
	}
	
}