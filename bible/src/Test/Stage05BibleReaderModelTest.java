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
import bibleReader.model.BibleFactory;
import bibleReader.model.BibleReaderModel;
import bibleReader.model.BookOfBible;
import bibleReader.model.Reference;
import bibleReader.model.ReferenceList;
import bibleReader.model.Verse;
import bibleReader.model.VerseList;

/**
 * Tests for the Search capabilities of the BibleReaderModel class. These tests assume BibleIO is working an can read in
 * the kjv.atv file.
 * 
 * @author Chuck Cusack, January, 2013
 */
public class Stage05BibleReaderModelTest  {
	private static VerseList	versesFromFile;
	private BibleReaderModel	model;

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

		Bible testBible = BibleFactory.createBible(copyOfVerseList);
		model = new BibleReaderModel();
		model.addBible(testBible);
	}

	@Test
	public void testGetVersions() {
		String[] versions = model.getVersions();
		assertEquals(1, versions.length);
		assertEquals("KJV", versions[0]);
	}

	@Test
	public void testGetNumberVersions() {
		assertEquals(1, model.getNumberOfVersions());
	}

	@Test
	public void testSearchNoResults() {
		ReferenceList results = model.getReferencesContaining("three wise men");
		assertEquals(0, results.size());
		results = model.getReferencesContaining("trinity");
		assertEquals(0, results.size());
		results = model.getReferencesContaining("neo");
		assertEquals(0, results.size());
	}

	@Test
	public void testSearchOneResult() {
		ReferenceList results = model.getReferencesContaining("the fig tree shall not blossom");
		assertEquals(1, results.size());
		assertEquals(new Reference(BookOfBible.Habakkuk, 3, 17), results.get(0));
	}

	@Test
	public void testGetReferenceContainingWithFewResults() {
		ReferenceList results = model.getReferencesContaining("Melchizedek");
		assertEquals(2, results.size());
		assertEquals(new Reference(BookOfBible.Genesis, 14, 18), results.get(0));
		assertEquals(new Reference(BookOfBible.Psalms, 110, 4), results.get(1));

		results = model.getReferencesContaining("god SO loved");
		assertEquals(2, results.size());
		Reference jhn3_16 = new Reference(BookOfBible.John, 3, 16);
		Reference firstJohn4_11 = new Reference(BookOfBible.John1, 4, 11);
		assertTrue(results.contains(jhn3_16));
		assertTrue(results.contains(firstJohn4_11));

		// Second test
		results = model.getReferencesContaining("Christians");
		assertEquals(1, results.size());
		Reference act11_26 = new Reference(BookOfBible.Acts, 11, 26);
		assertEquals(act11_26, results.get(0));
	}

	@Test
	public void testGetVerseContainingWithManyResults() {
		// One that occurs 47 times, but change the case of the search string
		ReferenceList verseResults = model.getReferencesContaining("son oF GoD");
		assertEquals(47, verseResults.size());

		verseResults = model.getReferencesContaining("righteousness");
		// Biblegateway.com gets 291 results. If you don't pass this test, talk to me ASAP!
		assertEquals(307, verseResults.size());

		// Should get 511 verses for the word "three".
		// We'll test 3 known results--the first, last, and one in the middle.
		verseResults = model.getReferencesContaining("three");
		assertEquals(511, verseResults.size());
		assertEquals(new Reference(BookOfBible.Genesis, 5, 22), verseResults.get(0));
		assertEquals(new Reference(BookOfBible.Joshua, 13, 30), verseResults.get(126));
		assertEquals(new Reference(BookOfBible.Revelation, 21, 13), verseResults.get(510));
	}

	@Test
	public void testGetVerseContainingWithPartialWords() {
		// This should match eaten as well as beaten, so it should return 143 results.
		ReferenceList verseResults = model.getReferencesContaining("eaten");
		assertEquals(143, verseResults.size());
	}

	@Test
	public void testExtremeSearches() {
		// Empty string should return no results.
		ReferenceList verseResults = model.getReferencesContaining("");
		assertEquals(0, verseResults.size());

		// Something that occurs a lot, like "the".
		// Of course, this isn't the number of verses containing the word the,
		// but the string "the", so it also matches verses with "then", etc.
		// Occurs in 28000 verses. How weird is that. Exactly 28,000?
		// Do it with both search methods.
		verseResults = model.getReferencesContaining("the");
		assertEquals(28000, verseResults.size());

		// Space occurs in every verse. That is annoying.
		// Searches for ".", ",". etc. will be similar.
		// For now we won't worry about filtering these.
		// Our next version will take care of it.
		verseResults = model.getReferencesContaining(" ");
		assertEquals(31102, verseResults.size());
	}
}
