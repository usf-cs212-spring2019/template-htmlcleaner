import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("javadoc")
public class HtmlCleanerStripTest {

	public static final String format = "%nHTML:%n%s%n%nExpected:%n%s%n%nActual:%n%s%n%n";

	public static void test(String test, String expected, String actual) {
		Assertions.assertEquals(expected, actual, () -> String.format(format, test, expected, actual));
	}

	@Nested
	public class EntityTests {
		/**
		 * Tests "2010&ndash;2011". (View Javadoc to see rendering.)
		 */
		@Test
		public void testNamed() {
			String test = "2010&ndash;2011";
			String expected = "20102011";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "2010&#8211;2011". (View Javadoc to see rendering.)
		 */
		@Test
		public void testNumbered() {
			String test = "2010&#8211;2011";
			String expected = "20102011";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "2010&#x2013;2011". (View Javadoc to see rendering.)
		 */
		@Test
		public void testHexadecimal() {
			String test = "2010&#x2013;2011";
			String expected = "20102011";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "touche&#769;!". (View Javadoc to see rendering.)
		 */
		@Test
		public void testAccent1() {
			String test = "touche&#769;!";
			String expected = "touche!";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "touch&eacute;!". (View Javadoc to see rendering.)
		 */
		@Test
		public void testAccent2() {
			String test = "touch&eacute;!";
			String expected = "touch!";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "hello&mdash;good&dash;bye". (View Javadoc to see rendering.)
		 */
		@Test
		public void testMultiple() {
			String test = "hello&mdash;good&dash;bye";
			String expected = "hellogoodbye";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "hello & good-bye".
		 */
		@Test
		public void testAmpersand() {
			String test = "hello & good-bye";
			String expected = "hello & good-bye";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests "hello & good-bye;".
		 */
		@Test
		public void testAndSemicolon() {
			String test = "hello & good-bye;";
			String expected = "hello & good-bye;";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}
	}

	@Nested
	public class CommentTests {

		@Test
		public void testSimple() {
			String test = "<!-- hello -->";
			String expected = " ";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void testABC() {
			String test = "A<!-- B -->C";
			String expected = "A C";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void testNewLine() {
			String test = "A<!--\n B\n -->C";
			String expected = "A C";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void testTags() {
			String test = "A<!-- <b>B</b> -->C";
			String expected = "A C";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void testSlashes() {
			String test = "A<!-- B //-->C";
			String expected = "A C";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		@Test
		public void testMultiple() {
			String test = "A<!-- B -->C D<!-- E -->F";
			String expected = "A C D F";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}
	}

	@Nested
	public class TagTests {

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <b>hello</b> world!
		 * </pre>
		 */
		@Test
		public void testSimple() {
			String test = "<b>hello</b> world!";
			String expected = "hello world!";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <b>hello
		 * </b> world!
		 * </pre>
		 */
		@Test
		public void testSimpleNewLine() {
			String test = "<b>hello\n</b> world!";
			String expected = "hello\n world!";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <a
		 *  name=toc>table of contents</a>
		 * </pre>
		 */
		@Test
		public void testAttributeNewline() {
			String test = "<a \n name=toc>table of contents</a>";
			String expected = "table of contents";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <p>Hello, <strong>world</strong>!</p>
		 * </pre>
		 */
		@Test
		public void testNestedTags() {
			String test = "<p>Hello, <strong>world</strong>!</p>";
			String expected = "Hello, world!";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <p>Hello, <br/>world!</p>
		 * </pre>
		 */
		@Test
		public void testLineBreak() {
			String test = "<p>Hello, <br/>world!</p>";
			String expected = "Hello, world!";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}
	}

	@Nested
	public class ElementTests {
		@Test
		public void testStyle() {
			String test = "<style type=\"text/css\">body { font-size: 10pt; }</style>";
			String expected = " ";
			String actual = HtmlCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		@Test
		public void testStyleNewline1() {
			String test = "<style type=\"text/css\">\nbody { font-size: 10pt; }\n</style>";
			String expected = " ";
			String actual = HtmlCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		@Test
		public void testStyleNewline2() {
			String test = "<style \n type=\"text/css\">\nbody { font-size: 10pt; }\n</style>";
			String expected = " ";
			String actual = HtmlCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		@Test
		public void testMultiple() {
			String test = "a<test>b</test>c<test>d</test>e";
			String expected = "a c e";
			String actual = HtmlCleaner.stripElement(test, "test");

			test(test, expected, actual);
		}

		@Test
		public void testMixed() {
			String test = "<title>Hello</title><script>potato</script> world";
			String expected = "<title>Hello</title>  world";
			String actual = HtmlCleaner.stripElement(test, "script");

			test(test, expected, actual);
		}
	}

	@Nested
	public class CleanTests {

		@Test
		public void testNoHTML() {
			String test = "hello & good-bye;";
			String expected = "hello & good-bye;";
			String actual = HtmlCleaner.stripHtml(test);

			test(test, expected, actual);
		}

		@Test
		public void testOneLine() {
			String test = "<b>hello</p>&amp;<script>potato</script>world";
			String expected = "hello world";
			String actual = HtmlCleaner.stripHtml(test);

			test(test, expected, actual);
		}

		@Test
		public void testSimplePage() {
			StringBuilder html = new StringBuilder();
			html.append("<!DOCTYPE html>\n");
			html.append("<html>\n");
			html.append("<head>\n");
			html.append("    <meta charset=\"utf-8\">\n");
			html.append("    <script type=\"text/javascript\" src=\"d3.v3.js\"></script>\n");
			html.append("    <style type=\"text/css\">\n");
			html.append("    body {\n");
			html.append("        font-size: 10pt;\n");
			html.append("        font-family: sans-serif;\n");
			html.append("    }\n");
			html.append("    </style>\n");
			html.append("</head>\n");
			html.append("<body>\n");
			html.append("Hello, world! &copy;2013\n");
			html.append("</body>\n");
			html.append("</html>\n");

			String expected = "Hello, world! 2013";
			String actual = HtmlCleaner.stripHtml(html.toString());

			// note trim to remove blank lines
			test(html.toString(), expected, actual.strip());
		}

		@Test
		public void testPanagrams() throws IOException {
			Path htmlPath = Path.of("test", "pangrams.html");
			Path textPath = Path.of("test", "pangrams.txt");

			Assertions.assertTrue(Files.isReadable(htmlPath));
			Assertions.assertTrue(Files.isReadable(textPath));

			String html = Files.readString(htmlPath, StandardCharsets.UTF_8);
			String expected = Files.readString(textPath StandardCharsets.UTF_8);
			String actual = HtmlCleaner.stripHtml(html);

			test(html.toString(), expected, actual);
		}
	}
}
