import org.junit.jupiter.api.Nested;

@SuppressWarnings("javadoc")
public class HtmlCleanerTest {

	@Nested
	public class NestedLinkTests extends HtmlCleanerLinkTest {

	}

	@Nested
	public class NestedStripTests extends HtmlCleanerStripTest {

	}

	@Nested
	public class NestedFetchTests extends HtmlFetcherTest {

	}

	@Nested
	public class NestedCleanerTest extends HtmlCleanerFullTest {

	}
}

