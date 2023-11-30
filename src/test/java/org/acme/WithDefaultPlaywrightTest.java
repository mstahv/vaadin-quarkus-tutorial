package org.acme;

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