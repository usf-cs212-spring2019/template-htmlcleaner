
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class HtmlCleanerLinkTest {

	@Nested
	public class SingleURLTests {

		public void testValid(String link, String html) throws MalformedURLException {
			URL base = new URL("http://www.example.com");
			URL expected = new URL(link);
			ArrayList<URL> actual = HtmlCleaner.listLinks(base, html);

			String debug = String.format("%nHTML:%n%s%n%n", html);
			Assertions.assertEquals(expected, actual.get(0), debug);
		}

		public void testInvalid(String html) throws MalformedURLException {
			URL base = new URL("http://www.example.com");
			ArrayList<URL> actual = HtmlCleaner.listLinks(base, html);

			String debug = String.format("%nHTML:%n%s%n%nLinks:%n%s%n", html, actual);
			Assertions.assertEquals(0, actual.size(), debug);
		}

		@Test
		public void testSimple() throws MalformedURLException {
			String link = "http://www.usfca.edu/";
			String html = "<a href=\"http://www.usfca.edu/\">";
			testValid(link, html);
		}

		@Test
		public void testFragment() throws MalformedURLException {
			String link = "http://docs.python.org/library/string.html?highlight=string";
			String html = "<a href=\"http://docs.python.org/library/string.html?highlight=string#module-string\">";
			testValid(link, html);
		}

		@Test
		public void testUppercase() throws MalformedURLException {
			String link = "HTTP://WWW.USFCA.EDU";
			String html = "<A HREF=\"HTTP://WWW.USFCA.EDU\">";
			testValid(link, html);
		}

		@Test
		public void testMixedCase() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<A hREf=\"http://www.usfca.edu\">";
			testValid(link, html);
		}

		@Test
		public void testSpaces() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<a href = \"http://www.usfca.edu\" >";
			testValid(link, html);
		}

		@Test
		public void testOneNewline() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<a href = \n \"http://www.usfca.edu\">";
			testValid(link, html);
		}

		@Test
		public void testManyNewlines() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<a\n\nhref\n=\n\"http://www.usfca.edu\"\n>";
			testValid(link, html);
		}

		@Test
		public void testSnippet() throws MalformedURLException {
			String link = "http://www.usfca.edu";
			String html = "<p><a href=\"http://www.usfca.edu\">USFCA</a> is in San Francisco.</p>";
			testValid(link, html);
		}

		@Test
		public void testRelative() throws MalformedURLException {
			String link = "http://www.example.com/index.html";
			String html = "<a href=\"index.html\">";
			testValid(link, html);
		}

		@Test
		public void testHREFLast() throws MalformedURLException {
			String link = "http://www.example.com/index.html";
			String html = "<a name=\"home\" href=\"index.html\">";
			testValid(link, html);
		}

		@Test
		public void testHREFFirst() throws MalformedURLException {
			String link = "http://www.example.com/index.html";
			String html = "<a href=\"index.html\" class=\"primary\">";
			testValid(link, html);
		}

		@Test
		public void testMultipleAttributes() throws MalformedURLException {
			String link = "http://www.example.com/index.html";
			String html = "<a name=\"home\" target=\"_top\" href=\"index.html\" id=\"home\" accesskey=\"A\">";
			testValid(link, html);
		}

		@Test
		public void testNoHREF() throws MalformedURLException {
			String html = "<a name = \"home\">";
			testInvalid(html);
		}

		@Test
		public void testNoAnchor() throws MalformedURLException {
			String html = "<h1>Home</h1>";
			testInvalid(html);
		}

		@Test
		public void testMixedNoHREF() throws MalformedURLException {
			String html = "<a name=href>The href = \"link\" attribute is useful.</a>";
			testInvalid(html);
		}

		@Test
		public void testLinkTag() throws MalformedURLException {
			String html = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">";
			testInvalid(html);
		}

		@Test
		public void testNoTag() throws MalformedURLException {
			String html = "<p>The a href=\"http://www.google.com\" attribute is often used in HTML.</p>";
			testInvalid(html);
		}
	}

	@Nested
	public class MultipleLinkTest {
		@Test
		public void testMultiple() throws MalformedURLException {
			String html = String.join("", List.of(
					"<h1><a name=\"about\">About</a></h1>\n",
					"<p>The <a class=\"primary\" href=\"index.html\">Department of ",
					"Computer Science</a> offers an undergraduate and graduate degree at ",
					"<a href=\"http://www.usfca.edu\">University of San Francisco</a>.</p>\n",
					"<p>Find out more about those degrees at <a href=\"https://www.usfca.edu/",
					"catalog/undergraduate/arts-sciences/computer-science\">https://www.usfca.edu/",
					"catalog/undergraduate/arts-sciences/computer-science</a>.</p>"
			));

			List<URL> expected = List.of(
					new URL("https://www.cs.usfca.edu/index.html"),
					new URL("http://www.usfca.edu"),
					new URL("https://www.usfca.edu/catalog/undergraduate/arts-sciences/computer-science")
			);

			URL base = new URL("https://www.cs.usfca.edu/");
			ArrayList<URL> actual = HtmlCleaner.listLinks(base, html);

			String debug = String.format("%nHTML:%n%s%n%n", html);
			Assertions.assertEquals(expected, actual, debug);
		}
	}

	@Nested
	public class RemoteLinkTest {
		/*
		 * Do not run these tests until you are passing the others! You risk
		 * being rate-limited or banned from the web server if you access it too
		 * frequently while testing.
		 *
		 * These are the same seed URLs you will use in project 4, so getting the
		 * parsing correct now will help tremendously!
		 */

		public void testRemote(String url, List<URL> expected) throws MalformedURLException {
			URL base = new URL(url);

			Assertions.assertTimeout(Duration.ofSeconds(30), () -> {
				HtmlCleaner cleaned = new HtmlCleaner(base);
				String html = cleaned.html;
				List<URL> actual = cleaned.urls;

				Assertions.assertEquals(expected, actual, () -> {
					StringBuffer debug = new StringBuffer("\nLinks (Expected, Actual):\n");

					for (int i = 0; i < Math.max(expected.size(), actual.size()); i++) {
						debug.append(i + ":\t");

						if (i < expected.size() && i < actual.size() && expected.get(i).equals(actual.get(i))) {
							debug.append("OKAY\t ");
							debug.append(expected.get(i));
						}
						else {
							debug.append("ERROR\t");
							debug.append(i < expected.size() ? expected.get(i) : "null");
							debug.append(",\t");
							debug.append(i < actual.size() ? actual.get(i) : "null");
						}

						debug.append("\n");
					}

					debug.append("\nHTML:\n");
					debug.append(html);
					debug.append("\n");

					return debug.toString();
				});
			});
		}

		@Test
		public void testHello() throws MalformedURLException {
			ArrayList<URL> expected = new ArrayList<>();
			testRemote("https://www.cs.usfca.edu/~cs212/simple/hello.html", expected);
		}

		@Test
		public void testSimple() throws MalformedURLException {
			List<URL> expected = List.of(
					new URL("https://www.cs.usfca.edu/~cs212/simple/a/b/c/subdir.html"),
					new URL("https://www.cs.usfca.edu/~cs212/simple/capital_extension.HTML"),
					new URL("https://www.cs.usfca.edu/~cs212/simple/double_extension.html.txt"),
					new URL("https://www.cs.usfca.edu/~cs212/simple/empty.html"),
					new URL("https://www.cs.usfca.edu/~cs212/simple/hello.html"),
					new URL("https://www.cs.usfca.edu/~cs212/simple/mixed_case.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/simple/no_extension"),
					new URL("https://www.cs.usfca.edu/~cs212/simple/position.html"),
					new URL("https://www.cs.usfca.edu/~cs212/simple/symbols.html"));

			testRemote("https://www.cs.usfca.edu/~cs212/simple/index.html", expected);
		}

		@Test
		public void testBirds() throws MalformedURLException {
			List<URL> expected = getBirdURLs();
			testRemote("https://www.cs.usfca.edu/~cs212/birds/birds.html", expected);
		}

		@Test
		public void testRecurse() throws MalformedURLException {
			List<URL> expected = List.of(new URL("https://www.cs.usfca.edu/~cs212/recurse/link02.html"));
			testRemote("https://www.cs.usfca.edu/~cs212/recurse/link01.html", expected);
		}

		@Test
		public void testGuten1400() throws MalformedURLException {
			List<URL> expected = List.of(
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0012.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0037.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/pip.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0072.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0082.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0132.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0189.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0223.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0242.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0245.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0279.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0295.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0335.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0348.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0393.jpg"),
					new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/images/0399.jpg")
			);

			testRemote("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm", expected);
		}

		@Test
		public void testGutenberg() throws MalformedURLException {
			List<URL> expected = getGutenURLs();
			testRemote("https://www.cs.usfca.edu/~cs212/guten/index.html", expected);
		}

		@Test
		public void testNumpy() throws MalformedURLException {
			List<URL> expected = List.of(
					new URL("https://www.cs.usfca.edu/~cs212/numpy/index.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/genindex.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/setting-up.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/contents.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/contents.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/setting-up.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/index.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/reference/index.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/setting-up.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/quickstart.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/basics.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/misc.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/numpy-for-matlab-users.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/building.html"),
					new URL("https://www.cs.usfca.edu/~cs212/numpy/user/c-info.html"),
					new URL("http://sphinx.pocoo.org/"));

			testRemote("https://www.cs.usfca.edu/~cs212/numpy/user/index.html", expected);
		}
	}

	public static final List<URL> getBirdURLs() throws MalformedURLException {
		return List.of(
				new URL("https://www.cs.usfca.edu/~cs212/birds/albatross.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/blackbird.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/bluebird.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/cardinal.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/chickadee.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/crane.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/crow.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/cuckoo.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/dove.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/duck.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/eagle.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/egret.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/falcon.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/finch.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/goose.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/gull.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/hawk.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/heron.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/hummingbird.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/ibis.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/kingfisher.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/loon.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/magpie.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/mallard.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/meadowlark.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/mockingbird.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/nighthawk.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/osprey.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/owl.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/pelican.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/pheasant.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/pigeon.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/puffin.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/quail.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/raven.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/roadrunner.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/robin.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/sandpiper.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/sparrow.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/starling.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/stork.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/swallow.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/swan.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/tern.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/turkey.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/vulture.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/warbler.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/woodpecker.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/wren.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/yellowthroat.html"),
				new URL("https://www.cs.usfca.edu/~cs212/birds/birds.html"));
	}

	public static List<URL> getGutenURLs() throws MalformedURLException {
		return List.of(
				new URL("https://www.cs.usfca.edu/~cs212/guten/1228-h/1228-h.htm"),
				new URL("https://www.cs.usfca.edu/~cs212/guten/1322-h/1322-h.htm"),
				new URL("https://www.cs.usfca.edu/~cs212/guten/1400-h/1400-h.htm"),
				new URL("https://www.cs.usfca.edu/~cs212/guten/1661-h/1661-h.htm"),
				new URL("https://www.cs.usfca.edu/~cs212/guten/22577-h/22577-h.htm"),
				new URL("https://www.cs.usfca.edu/~cs212/guten/37134-h/37134-h.htm"));
	}
}
