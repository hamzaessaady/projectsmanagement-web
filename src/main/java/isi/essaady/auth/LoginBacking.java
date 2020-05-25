package isi.essaady.auth;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.annotation.ManagedProperty;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@SuppressWarnings("cdi-ambiguous-dependency")
@Named("loginBacking")
@ViewScoped
public class LoginBacking  implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@NotNull
	@Size(min=4, message="Username must be at least 4 characters.")
	private String username;
	
	@NotNull
	@Size(min=6, message="Password must be at least 6 characters.")
	private String password;
	
	@Inject
	private SecurityContext securityContext;
	@Inject
	private ExternalContext externalContext;
	@Inject
	private FacesContext facesContext;
	
	@Inject
	private Flash flash;
	@Inject @ManagedProperty("#{param.new}")
	private boolean isNew;
	
	public LoginBacking() {
		super();
	}
	
	/*
	 * login() method
	 * TODO: JAVA DOC
	 * */
	public void login() throws IOException {
		switch (continueAuthentication()) {
			case SEND_CONTINUE:
				facesContext.responseComplete();
				break;
			case SEND_FAILURE:
				facesContext.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Login failed : Username and/or password are incorrect !", null));
				break;
			case SUCCESS:
				flash.setKeepMessages(true);
				facesContext.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Login succeed", null));
				externalContext.redirect(
						externalContext.getRequestContextPath() + "/views/dashboard.xhtml");
				break;
			case NOT_DONE:
				break;
		}
	}
	
	/*
	 * continueAuthentication() method
	 * TODO: JAVA DOC
	 * */
	private AuthenticationStatus continueAuthentication(){
        UsernamePasswordCredential credentials = 
        		new UsernamePasswordCredential(username, password);
        return securityContext.authenticate(
        		(HttpServletRequest)externalContext.getRequest(), 
                (HttpServletResponse)externalContext.getResponse(), 
                AuthenticationParameters.withParams()
                						.newAuthentication(isNew)
                						.credential(credentials));
    }
	
	/* Default methods*/
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
