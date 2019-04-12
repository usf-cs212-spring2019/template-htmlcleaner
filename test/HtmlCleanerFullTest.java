import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class HtmlCleanerFullTest {

	public static void test(URL url, List<URL> urls, String text) {
		Assertions.assertTimeout(Duration.ofSeconds(30), () -> {
			HtmlCleaner cleaned = new HtmlCleaner(url);
			String actual = cleaned.text.strip();

			Assertions.assertEquals(urls, cleaned.urls);
			Assertions.assertEquals(text, actual);
		});
	}

	@Test
	public void testHello() throws MalformedURLException {
		URL url = new URL("https://www.cs.usfca.edu/~cs212/simple/hello.html");
		String expected = "Hello World!\n    Hello, world. Hello... World? HELLO WORLD!";

		test(url, Collections.emptyList(), expected);
	}

	@Test
	public void testGuten() throws MalformedURLException {
		URL url = new URL("https://www.cs.usfca.edu/~cs212/guten/");
		String expected = String.join("\n", List.of(
				"On the Origin of Species by Charles Darwin (1859)",
				"Leaves of Grass by Walt Whitman (1855)",
				"Great Expectations by Charles Dickens (1867)",
				"The Adventures of Sherlock Holmes by Arthur Conan Doyle (1892)",
				"Practical Grammar and Composition by Thomas Wood (1910)",
				"The Elements of Style by William Strunk"
		));

		test(url, HtmlCleanerLinkTest.getGutenURLs(), expected);
	}

	@Test
	public void testBirds() throws MalformedURLException {
		URL url = new URL("https://www.cs.usfca.edu/~cs212/birds/birds.html");
		String expected = String.join("\n", List.of(
				"Here is a list of birds:",
				"",
				"",
				"	albatross",
				"	blackbird",
				"	bluebird",
				"	cardinal",
				"	chickadee",
				"	crane",
				"	crow",
				"	cuckoo",
				"	dove",
				"	duck",
				"	eagle",
				"	egret",
				"	falcon",
				"	finch",
				"	goose",
				"	gull",
				"	hawk",
				"	heron",
				"	hummingbird",
				"	ibis",
				"	kingfisher",
				"	loon",
				"	magpie",
				"	mallard",
				"	meadowlark",
				"	mockingbird",
				"	nighthawk",
				"	osprey",
				"	owl",
				"	pelican",
				"	pheasant",
				"	pigeon",
				"	puffin",
				"	quail",
				"	raven",
				"	roadrunner",
				"	robin",
				"	sandpiper",
				"	sparrow",
				"	starling",
				"	stork",
				"	swallow",
				"	swan",
				"	tern",
				"	turkey",
				"	vulture",
				"	warbler",
				"	woodpecker",
				"	wren",
				"	yellowthroat",
				"",
				"",
				"",
				"Home"
		));

		test(url, HtmlCleanerLinkTest.getBirdURLs(), expected);
	}
}
