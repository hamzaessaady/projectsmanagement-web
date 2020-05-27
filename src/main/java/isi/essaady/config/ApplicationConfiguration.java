package isi.essaady.config;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.annotation.FacesConfig;
import javax.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;

@CustomFormAuthenticationMechanismDefinition(
		loginToContinue = @LoginToContinue(
				loginPage = "/index.xhtml",
				errorPage = "" ))

@FacesConfig( version = FacesConfig.Version.JSF_2_3 )
@ApplicationScoped
public class ApplicationConfiguration {
    
}