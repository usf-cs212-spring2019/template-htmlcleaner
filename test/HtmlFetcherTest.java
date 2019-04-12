import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// DO NOT OVER-CALL THESE TESTS OR YOU CAN BE BLOCKED FROM THE WEB SERVER!

@SuppressWarnings("javadoc")
public class HtmlFetcherTest {

	public static final Duration TIMEOUT = Duration.ofSeconds(30);

	@Nested
	public class HTMLTypeTests {

		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/no_extension",
				"https://www.cs.usfca.edu/~cs212/simple/double_extension.html.txt"
		})
		public void testNotHTML(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeout(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertFalse(HtmlFetcher.isHTML(headers));
			});
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/",
				"https://www.cs.usfca.edu/~cs212/simple/empty.html",
				"https://www.cs.usfca.edu/~cs212/birds/falcon.html",
				"https://www.cs.usfca.edu/~cs212/redirect/nowhere"
		})
		public void testIsHTML(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeout(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertTrue(HtmlFetcher.isHTML(headers));
			});
		}
	}

	@Nested
	public class StatusCodeTests {

		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/no_extension",
				"https://www.cs.usfca.edu/~cs212/simple/double_extension.html.txt",
				"https://www.cs.usfca.edu/~cs212/birds/yellowthroat.html"
		})
		public void test200(String link) throws IOException {
			test(link, 200);
		}

		@Test
		public void test404() throws IOException {
			String link = "https://www.cs.usfca.edu/~cs212/redirect/nowhere";
			test(link, 404);
		}

		@Test
		public void test410() throws IOException {
			String link = "https://www.cs.usfca.edu/~cs212/redirect/gone";
			test(link, 410);
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/redirect/loop1",
				"https://www.cs.usfca.edu/~cs212/redirect/loop2",
				"https://www.cs.usfca.edu/~cs212/redirect/one",
				"https://www.cs.usfca.edu/~cs212/redirect/two"
		})
		public void testRedirect(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);


			Assertions.assertTimeout(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertTrue(HtmlFetcher.isRedirect(headers));
			});
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/no_extension",
				"https://www.cs.usfca.edu/~cs212/redirect/nowhere",
				"https://www.cs.usfca.edu/~cs212/redirect/gone"
		})
		public void testNotRedirect(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeout(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertFalse(HtmlFetcher.isRedirect(headers));
			});
		}

		public void test(String link, int code) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeout(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				int actual = HtmlFetcher.getStatusCode(headers);
				Assertions.assertEquals(code, actual);
			});
		}
	}

	@Nested
	public class FetchHTMLTests {

		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/no_extension",
				"https://www.cs.usfca.edu/~cs212/simple/double_extension.html.txt",
				"https://www.cs.usfca.edu/~cs212/redirect/nowhere"
		})
		public void testNotValidHTML(String url) throws IOException {
			Assertions.assertTimeout(TIMEOUT, () -> {
				String html = HtmlFetcher.fetchHTML(url);
				Assertions.assertNull(html);
			});
		}

		@Test
		public void testValidHTML() throws IOException {
			String link = "https://www.cs.usfca.edu/~cs212/birds/yellowthroat.html";

			Assertions.assertTimeout(TIMEOUT, () -> {
				String html = HtmlFetcher.fetchHTML(link);
				Assertions.assertNotNull(html);

				Path hello = Paths.get("test", "yellowthroat.html");
				List<String> lines = Files.readAllLines(hello);
				String expected = String.join(System.lineSeparator(), lines);
				Assertions.assertEquals(expected, html);
			});
		}

		@ParameterizedTest
		@ValueSource(ints = { -1, 0, 1, 2 })
		public void testUnsuccessfulRedirect(int redirects) throws IOException {
			String one = "https://www.cs.usfca.edu/~cs212/redirect/one";

			Assertions.assertTimeout(TIMEOUT, () -> {
				String html = HtmlFetcher.fetchHTML(one, redirects);
				Assertions.assertNull(html);
			});
		}

		@ParameterizedTest
		@ValueSource(ints = { 3, 4 })
		public void testSuccessfulRedirect(int redirects) throws IOException {
			String one = "https://www.cs.usfca.edu/~cs212/redirect/one";

			Assertions.assertTimeout(TIMEOUT, () -> {
				String html = HtmlFetcher.fetchHTML(one, redirects);
				Assertions.assertNotNull(html);

				Path hello = Paths.get("test", "hello.html");
				List<String> lines = Files.readAllLines(hello);
				String expected = String.join(System.lineSeparator(), lines);
				Assertions.assertEquals(expected, html);
			});
		}
	}
}
