package Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import bibleReader.BibleIO;
import bibleReader.model.BookOfBible;
import bibleReader.model.Reference;
import bibleReader.model.ReferenceList;
import bibleReader.model.Verse;
import bibleReader.model.VerseList;

/**
 * Tests for the methods of Bible that are related to passage lookup methods.
 * Many of the tests perform the same lookup twice--once using a method that
 * takes in details about the passage (e.g. Reference, Book, Chapter, etc.), and
 * once using the method that takes a String. This should help with debugging
 * since if the former passes but the latter fails, then it narrows down where
 * the problem is.
 * 
 * @author Chuck Cusack, February 12, 2013.
 */
// Notice that this class extends one from Stage 7.
public class Stage09ModelPassageTest extends Stage07ModelPassageTest {

	// Need to rewrite this method so that the versions has 3 versions instead
	// of just 1
	// that Stage 7 had.
	@BeforeClass
	public static void readFile() {
		versions = new String[] { "kjv.atv", "asv.xmv", "esv.atv" };
		verseListArray = new VerseList[versions.length];
		references = new ReferenceList[versions.length];
		for (int i = 0; i < versions.length; i++) {
			File file = new File(versions[i]);
			verseListArray[i] = BibleIO.readBible(file);
			references[i] = new ReferenceList();
			for (Verse v : verseListArray[i]) {
				references[i].add(v.getReference());
			}
		}
		TreeSet<Reference> combined = new TreeSet<Reference>();
		for (ReferenceList rl : references) {
			combined.addAll(rl);
		}
		allRefs = new ReferenceList(combined);
	}

	@Test(timeout = 5000)
	public void testVerseOnlyInESV() {
		// One verse is only in ESV
		Reference refOnlyInESV = new Reference(BookOfBible.John3, 1, 15);

		// See if we can properly get the first chapter of 3 John even though
		// two versions don't have a 15th verse.
		ReferenceList results = model.getReferencesForPassage("3 John 1");
		assertEquals(15, results.size());

		// Make sure the getText method returns the correct thing in both cases.
		assertEquals(
				"Peace be to you. The friends greet you. Greet the friends, <sup>p</sup>every one of them.",
				model.getText("ESV", refOnlyInESV));
		assertEquals("", model.getText("KJV", refOnlyInESV));
		assertEquals("", model.getText("ASV", refOnlyInESV));

		// Now try the getVerses method. There should be 15 results, but the
		// last one
		// should be null for KJV and ASV. We will just check that the first 14
		// are not null and the last one is.
		VerseList verses = model.getVerses("KJV", results);
		assertEquals(15, verses.size());
		for (int i = 0; i < 14; i++) {
			assertNotNull(verses.get(i));
		}
		assertNull(verses.get(14));

		verses = model.getVerses("ASV", results);

	}

	@Test(timeout = 5000)
	public void testVersesMisingInESV() {
		// Try a passage with a missing verse in the middle.
		// (The ESV doesn't have 9:44 or 9:46).
		ReferenceList refs = model.getReferencesForPassage("Mark 9:43-46");
		assertEquals(4, refs.size());
		for (int i = 0; i < refs.size(); i++) {
			// Just double checking the verse.
			assertEquals(43 + i, refs.get(i).getVerse());
		}
	}

	@Test(timeout = 5000)
	public void testVersesMisingInESV2() {
		// Several verses are in both KJV and ASV, but not ESV.
		ReferenceList refsNotInESV = new ReferenceList();
		refsNotInESV.add(new Reference(BookOfBible.Matthew, 12, 47));
		refsNotInESV.add(new Reference(BookOfBible.Matthew, 17, 21));
		refsNotInESV.add(new Reference(BookOfBible.Matthew, 18, 11));
		refsNotInESV.add(new Reference(BookOfBible.Matthew, 23, 14));
		refsNotInESV.add(new Reference(BookOfBible.Mark, 7, 16));
		refsNotInESV.add(new Reference(BookOfBible.Mark, 9, 44));
		refsNotInESV.add(new Reference(BookOfBible.Mark, 9, 46));
		refsNotInESV.add(new Reference(BookOfBible.Mark, 11, 26));
		refsNotInESV.add(new Reference(BookOfBible.Mark, 15, 28));
		refsNotInESV.add(new Reference(BookOfBible.Luke, 17, 36));
		refsNotInESV.add(new Reference(BookOfBible.Luke, 23, 17));
		refsNotInESV.add(new Reference(BookOfBible.John, 5, 4));
		refsNotInESV.add(new Reference(BookOfBible.Acts, 8, 37));
		refsNotInESV.add(new Reference(BookOfBible.Acts, 15, 34));
		refsNotInESV.add(new Reference(BookOfBible.Acts, 24, 7));
		refsNotInESV.add(new Reference(BookOfBible.Acts, 28, 29));
		refsNotInESV.add(new Reference(BookOfBible.Romans, 16, 24));

		// Just a quick check that the ASV has results for all of these.
		// We could be more specific and check contents and also check
		// KJV, but if everything else passes, everything is probably fine.
		VerseList verseList = model.getVerses("ASV", refsNotInESV);
		assertEquals(17, verseList.size());
		for (int i = 0; i < verseList.size(); i++) {
			assertNotNull(verseList.get(i));
		}

		// getVerses should return a bunch of nulls for ESV
		verseList = model.getVerses("ESV", refsNotInESV);
		assertEquals(17, verseList.size());
		for (int i = 0; i < verseList.size(); i++) {
			assertNull(verseList.get(i));
		}
	}
}
