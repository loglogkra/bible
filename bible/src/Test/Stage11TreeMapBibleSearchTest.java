package Test;

// If you organize imports, the following import might be removed and you will
// not be able to find certain methods. If you can't find something, copy the
// commented import statement below, paste a copy, and remove the comments.
// Keep this commented one in case you organize imports multiple times.
//
// import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bibleReader.BibleIO;
import bibleReader.model.Bible;
import bibleReader.model.BookOfBible;
import bibleReader.model.Reference;
import bibleReader.model.ReferenceList;
import bibleReader.model.TreeMapBible;
import bibleReader.model.Verse;
import bibleReader.model.VerseList;

/**
 * Tests for the Search capabilities of the Bible class. These tests assume
 * BibleIO is working an can read in the kjv.atv file.
 * 
 * @author Chuck Cusack, January, 2013
 */
public class Stage11TreeMapBibleSearchTest {
	private Bible testBible;
	private static VerseList versesFromFile;

	@BeforeClass
	public static void readFile() {
		// Our tests will be based on the KJV version for now.
		File file = new File("kjv.atv");
		// We read the file here so it isn't done before every test.
		versesFromFile = BibleIO.readBible(file);
	}

	@Before
	public void setUp() throws Exception {
		// Make a shallow copy of the verses.
		ArrayList<Verse> copyOfList = new ArrayList<Verse>(versesFromFile);
		// Now make a copy of the VerseList
		VerseList copyOfVerseList = new VerseList(versesFromFile.getVersion(), versesFromFile.getDescription(),
				copyOfList);

		testBible = new TreeMapBible(copyOfVerseList);
	}

	@Test(timeout = 500)
	public void testGetReferenceContainingWithNoResults() {
		ReferenceList refResults = testBible.getReferencesContaining("three wise men");
		assertEquals(0, refResults.size());
		refResults = testBible.getReferencesContaining("trinity");
		assertEquals(0, refResults.size());
		refResults = testBible.getReferencesContaining("neo"); // Get it?
		assertEquals(0, refResults.size());
	}

	@Test(timeout = 250)
	public void testGetReferenceContainingWithOneResult() {
		ReferenceList refResults = testBible.getReferencesContaining("the fig tree shall not blossom");
		assertEquals(1, refResults.size());
		assertEquals(new Reference(BookOfBible.Habakkuk, 3, 17), refResults.get(0));
	}

	@Test(timeout = 250)
	public void testGetReferenceContainingWithFewResults() {
		ReferenceList refResults = testBible.getReferencesContaining("Melchizedek");
		assertEquals(2, refResults.size());
		assertTrue(refResults.contains(new Reference(BookOfBible.Genesis, 14, 18)));
		assertTrue(refResults.contains(new Reference(BookOfBible.Psalms, 110, 4)));
	}

	@Test(timeout = 250)
	public void testGetVerseContainingWithFewResults() {
		// First Test
		VerseList verseResults = testBible.getVersesContaining("god SO loved");
		assertEquals(2, verseResults.size());
		Verse jhn3_16 = new Verse(BookOfBible.John, 3, 16, "For God so loved the world, "
				+ "that he gave his only begotten Son, that whosoever believeth in him should not perish, "
				+ "but have everlasting life.");
		Verse firstJohn4_11 = new Verse(BookOfBible.John1, 4, 11,
				"Beloved, if God so loved us, we ought also to love one another.");
		assertTrue(verseResults.contains(jhn3_16));
		assertTrue(verseResults.contains(firstJohn4_11));

		// Second test
		verseResults = testBible.getVersesContaining("Christians");
		assertEquals(1, verseResults.size());
		Verse act11_26 = new Verse(BookOfBible.Acts, 11, 26, "And when he had found him, "
				+ "he brought him unto Antioch. And it came to pass, that a whole year they assembled themselves "
				+ "with the church, and taught much people. And the disciples were called Christians first in Antioch.");
		assertEquals(act11_26, verseResults.get(0));
	}

	@Test(timeout = 500)
	public void testGetVerseContainingWithManyResults() {
		// One that occurs 47 times, but change the case of the search string
		VerseList verseResults = testBible.getVersesContaining("son oF GoD");
		assertEquals(47, verseResults.size());

		verseResults = testBible.getVersesContaining("righteousness");
		assertEquals(307, verseResults.size());

		// Should get 511 verses for the word "three".
		// We'll test 3 known results--the first, last, and one in the middle.
		verseResults = testBible.getVersesContaining("three");
		assertEquals(511, verseResults.size());
		assertEquals(new Reference(BookOfBible.Genesis, 5, 22), verseResults.get(0).getReference());
		assertEquals(new Reference(BookOfBible.Joshua, 13, 30), verseResults.get(126).getReference());
		assertEquals(new Reference(BookOfBible.Revelation, 21, 13), verseResults.get(510).getReference());

		ReferenceList refResults = testBible.getReferencesContaining("three");
		assertEquals(511, refResults.size());
	}

	@Test(timeout = 250)
	public void testGetVerseContainingWithPartialWords() {
		// This should match eaten as well as beaten, so it should return 143
		// results.
		VerseList verseResults = testBible.getVersesContaining("eaten");
		assertEquals(143, verseResults.size());
	}

	@Test(timeout = 250)
	public void testGetVersesContaining_EmptyString() {
		VerseList verseResults = testBible.getVersesContaining("");
		assertEquals(0, verseResults.size());
	}

	@Test(timeout = 250)
	public void testGetReferencesContaining_EmptyString() {
		ReferenceList refResults = testBible.getReferencesContaining("");
		assertEquals(0, refResults.size());
	}

	@Test(timeout = 250)
	public void testGetVersesContaining_The() {
		VerseList verseResults = testBible.getVersesContaining("the");
		assertEquals(28000, verseResults.size());
		verseResults = testBible.getVersesContaining("THE");
		assertEquals(28000, verseResults.size());
	}

	@Test(timeout = 250)
	public void testGetReferencesContaining_The() {
		ReferenceList refResults = testBible.getReferencesContaining("the");
		assertEquals(28000, refResults.size());
	}

	@Test(timeout = 250)
	public void testGetVersesContaining_Space() {
		// Space occurs in every verse. That is annoying.
		// Searches for ".", ",". etc. will be similar.
		// For now we won't worry about filtering these.
		// Our next version will take care of it.
		VerseList verseResults = testBible.getVersesContaining(" ");
		assertEquals(31102, verseResults.size());
	}

	@Test(timeout = 250)
	public void testGetReferencesContaining_Space() {
		ReferenceList refResults = testBible.getReferencesContaining(" ");
		assertEquals(31102, refResults.size());
	}
}
