# Cheat Sheet to Run Through Vaadin Tutorial using Quarkus

The official [Vaadin Tutorial](https://vaadin.com/docs/latest/tutorial) runs using Spring Boot. Anybody who have maintained multiplatform libraries knows why we only want to provide a single version of the tutorial: the maintenance requires a ton of work, even for the Spring Boot version.

But in case you happen to be allergic to Spring Boot or just a fan of Quarkus, no worries. [Vaadin runs perfectly on Quarkus](https://vaadin.com/docs/latest/integrations/quarkus) as well and in case you know the platform better than Spring Boot, it might actually be better for you to learn the basics of Vaadin development on Quarkus.

Most of the tutorial is actually exactly the same, at least at the level of principles, but there are some differences in the project setup, libraries you use for repository pattern, security and so on. I run through our tutorial, but used a Quarkus based stack. My notes of differences ended up to be this *cheat sheet to run through the Vaadin Tutorial using Quarkus* 😎

## Project Setup

A bit of more work here as we don't have a ready-made starting point with the backend code like we do for Spring Boot. You can do all things in this part, or take a shortcut to my start point and [download that project as zip file](https://github.com/mstahv/vaadin-quarkus-tutorial/archive/refs/tags/startingpoint.zip), but then the Vaadin version is fixed to what it happened to be today (`24.2.4`).

Let's now go with the step by step preparation of our project

Create a Quarkus project in a typical way and add some essential dependencies. The second call shows you the recently added Vaadin Quarkus extension. You can try that on your existing Quarkus project as well 😉

```shell
    quarkus create && cd code-with-quarkus

    quarkus ext add vaadin
    quarkus ext add io.quarkus:quarkus-hibernate-orm-panache
    quarkus ext add io.quarkus:quarkus-jdbc-h2
    quarkus ext add io.quarkus:quarkus-hibernate-validator
```

Download [the Spring version](https://start.vaadin.com/dl?preset=flow-crm-tutorial&preset=partial-latest) and extract it as we'll steal some stuff from it. 

First copy the data package from Spring Boot version and make mechanic conversion to Panache repositories and CDI beans. You'll find the right spots marked red in your IDE or you can cheat a bit and [steal my solutions](https://github.com/mstahv/vaadin-quarkus-tutorial/tree/c8747c543dbfd495b01c736d8e3e5905ad6eec6b/src/main/java/org/acme/data).

Add `@Column(name="last_name")` and `@Column(name="first_name")` annotations to those fields in the Contact entity so the script imports properly (apparently different naming defaults than in Spring Data JPA and using autogenerated schema).

Then copy still following things to your Quarkus project:

 * The src/main/resources/data.sql and name it as import.sql
 * src/main/resources/META-INF/resources
 * frontend/themes
 * ListView.java

And then add following configuration class to define the theme in use (this is done in Application.java in Spring version):
```java
@Theme(value = "flowcrmtutorial")
public class VaadinAppConfig implements AppShellConfigurator {
}
```
Optionally you can remove the Quarkus generated example, the Greeting REST example etc. Now the project ought to be ready to start the actual tutorial. Spin up the app in development mode and in http://localhost:8080/ you should see an empty view to wait for your Vaadin code:

```shell
mvn quarkus:dev
```

## The basic part

Now you can continue most of the official tutorial pretty much as is all the way until the `Login And Authentication` part. Just note that instead of Spring Beans, use CDI beans and that there are various differences between Spring Data and [Panache](https://quarkus.io/guides/hibernate-orm-panache). Essentially if the tutorial says `Autowired`, you say `Inject`.

Another gotcha is when building the Dashboard view. Adding Vaadin extension as we did in the Project Setup, only adds the core OSS dependencies. Add `com.vaadin:vaadin-charts-flow` dependency to you pom.xml (with version or with `com.vaadin:vaadin-bom` to dependency management part). Alternatively you can add the whole `com.vaadin:vaadin` package with all the things.

## Login and Authentication

Naturally, Spring Security is not an option with Quarkus. In this example we'll use basic [form based authentication](https://quarkus.io/guides/security-authentication-mechanisms) and a ["user" table in the database via JPA](https://quarkus.io/guides/security-jpa). This is bit more production ready setup than in the official Spring Boot version that just holds users in memory. Note that with Quarkus it is [rather trivial to set up a Keycloak with OIDC & Vaadin](https://vaadin.com/blog/openid-connect-authentication-vaadin-an-integration-example-using-quarkus), so I suggest to look primarily to that for even trivial real world scenarios. But with this approach we are closer to the in memory form based login done in the official Vaadin tutorial.

Add extension/dependency:
```shell
    quarkus extension add security-jpa
 ```

Configure form based security in application.properties like this:

    quarkus.http.auth.form.enabled=true
    quarkus.http.auth.form.landing-page=/
    quarkus.http.auth.form.error-page=/login?error
    quarkus.http.auth.form.username-parameter=username
    quarkus.http.auth.form.password-parameter=password

Add ViewAccesChecker (not added automatically like with Spring integration):

```java
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.auth.ViewAccessChecker;
import jakarta.enterprise.event.Observes;

public class ViewAccessCheckerInit {
private final ViewAccessChecker viewAccessChecker;

    public ViewAccessCheckerInit() {
        viewAccessChecker = new ViewAccessChecker();
        viewAccessChecker.setLoginView("/login");
    }

    public void serviceInit(@Observes ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.getSource().addUIInitListener(
                uiInitEvent -> uiInitEvent.getUI().addBeforeEnterListener(viewAccessChecker)
        );
    }
}
```
User entity (active record pattern) to store users in the DB.

```java
package org.acme.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;

@Entity
@Table(name = "test_user")
@UserDefinition
public class User extends PanacheEntity {
@Username
public String username;
@Password
public String password;
@Roles
public String role;

    /**
     * Adds a new user to the database
     * @param username the username
     * @param password the unencrypted password (it will be encrypted with bcrypt)
     * @param role the comma-separated roles
     */
    public static void add(String username, String password, String role) { 
        User user = new User();
        user.username = username;
        user.password = BcryptUtil.bcryptHash(password);
        user.role = role;
        user.persist();
    }
}
```
And then fill in test users (or do similar with encrypted pws to import.sql):

```java
package org.acme.data.service;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.acme.data.entity.User;

@Singleton
public class Startup {
    @Transactional
    public void loadUsers(@Observes StartupEvent evt) {
        // reset and load all test users
        User.deleteAll();
        User.add("alice", "alice", "admin");
        User.add("bob", "bob", "user");
    }
}
```

Now the Quarkus specific basics are in and you can continue with following exceptions:

In LoginView.java, configure the form post URL in use:

```java
login.setAction("/j_security_check");
```

SecurityService.java impl is slightly different to the Spring version:

```java
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
```

## Installable PWA

I skipped this part as well, but there should be nothing platform specific. I tend to consider this an advanced topic anyways, and you should look into this only if you really need installable web apps (usually essential in case you target mobile devices!).

## Unit & Integration Tests

I have to admit that I skipped this part in rush, but the principles ought to be very much the same. A suggestion that I would these days make also for Spring developers would be to use the handy `UI Unit Testing` from TestBench or Karibu Testing. Makes testing easier, on all platforms.

## End-to-End Testing

Did this part in a bit different way. I used my (current) favorite for browser automation, Playwright. There is a handy community extension for Quarkus. Follow [these instructions](https://docs.quarkiverse.io/quarkus-playwright/dev/) and you are up and running in a minute.

This is how I smoke tested that the app starts, one can log in and the ListView opens up properly (asserting the "Add Contact" button appears).
```java
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@QuarkusTest
@WithPlaywright
public class WithDefaultPlaywrightTest {

    @InjectPlaywright
    BrowserContext context;

    @TestHTTPResource("/")
    URL index;

    @Test
    public void testIndex() {
        final Page page = context.newPage();
        Response response = page.navigate(index.toString());
        Assertions.assertEquals("OK", response.statusText());

        page.waitForLoadState();

        page.getByLabel("Username").fill("bob");
        page.locator("input[name='password']").fill("bob");
        page.locator("vaadin-button").getByText("Log in").click();

        page.waitForLoadState();

        assertThat(page.getByText("Add contact")).isVisible();

    }
}
```

## Deployment

An essential step is to [add a production profile](https://vaadin.com/docs/latest/production/production-build#enabling-production-builds) to do optimized front-end build for the client bundle.

And make sure you use that when building your container image, e.g.
```shell 
./mvnw clean install -Pproduction -DskipTests=True -Dquarkus.container-image.build=true
```

I skipped tests above in the container build as my Playwright setup only works with development mode (would need a bit of additional configure to work on both).

Then it is only about connect to a proper DB (this one uses H2 for trivial development) and push the built container image to your favourite cloud. For example the Azure `az containerapp` approach used in the official tutorial should work fine!
