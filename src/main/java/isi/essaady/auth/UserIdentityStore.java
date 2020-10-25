package isi.essaady.auth;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;

import isi.essaady.ejbs.UserBean;
import isi.essaady.entities.User;

/*
 * Resource : The Definitive Guide to JSF in Java EE 8 by Bauke Scholtz, Arjan Tijms.
 * [MODIFIED]
 * 
 * */
@ApplicationScoped
public class UserIdentityStore implements IdentityStore {
	
	@EJB
	private UserBean userBean;
	
	@Override
	public CredentialValidationResult validate(Credential credential) {
		UsernamePasswordCredential login = (UsernamePasswordCredential) credential;
		String username = login.getCaller();
		String password = login.getPasswordAsString();
		Optional<User> optionalUser = userBean.findByUsernamePassword(username, password);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			return new CredentialValidationResult(
					user.getUsername(),
					new LinkedHashSet<String>(
							Arrays.asList(user.getUserGroup(),user.getRole())
					));
		} else {
			return CredentialValidationResult.INVALID_RESULT;
		}
	}
}
