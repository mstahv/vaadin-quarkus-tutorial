package org.acme;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinResponse;
import io.quarkus.security.runtime.SecurityIdentityAssociation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.Cookie;


@RequestScoped
public class SecurityService {

    private final SecurityIdentityAssociation sia;

    public SecurityService(SecurityIdentityAssociation sia) {
        this.sia = sia;
    }

    public String getAuthenticatedUser() {
        return sia.getIdentity().getPrincipal().getName();
    }

    public void logout() {
        // Note, with websocket communication, this must be done
        // with a RequestHandler
        Cookie cookie = new Cookie("quarkus-credential", "; Max-Age=0;path=/");
        VaadinResponse.getCurrent().addCookie(cookie);
        UI.getCurrent().getPage().setLocation("/");
    }
}