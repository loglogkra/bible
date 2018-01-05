package bibleReader.model;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A class that stores a version of the Bible.
 * 
 * @author Chuck Cusack (Provided the interface)
 * @author Logan (provided the implementation)
 */
public class TreeMapBible implements Bible {

	// The Fields
	private String version;
	private String title;
	private TreeMap<Reference, String> theVerses;

	/**
	 * Create a new Bible with the given verses.
	 * 
	 * @param version
	 *            the version of the Bible (e.g. ESV, KJV, ASV, NIV).
	 * @param verses
	 *            All of the verses of this version of the Bible.
	 */
	public TreeMapBible(VerseList verses) {
		theVerses = new TreeMap<Reference, String>();
		version = verses.getVersion();
		title = verses.getDescription();
		// Loop through the verselist and add all of the verses
		for (Verse verse : verses) {
			theVerses.put(verse.getReference(), verse.getText());
		}
		// Add the dummy book to the end.
		theVerses.put(new Reference(BookOfBible.Dummy, 1, 1), "dummy");
	}

	@Override
	public int getNumberOfVerses() {
		return theVerses.size() - 1;
	}

	@Override
	public VerseList getAllVerses() {
		// Remove the dummy book.
		theVerses.remove(theVerses.lastKey());
		VerseList listToReturn = new VerseList(version, title);
		for (Reference ref : theVerses.keySet()) {
			listToReturn.add(new Verse(ref, theVerses.get(ref)));
		}
		// Add back the dummy book.
		theVerses.put(new Reference(BookOfBible.Dummy, 1, 1), "dummy");
		return listToReturn;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public boolean isValid(Reference ref) {
		if (ref.getBookOfBible() == BookOfBible.Dummy)
			return false;
		return theVerses.containsKey(ref);

	}

	@Override
	public String getVerseText(Reference r) {
		return theVerses.get(r);
	}

	@Override
	public Verse getVerse(Reference r) {
		if (isValid(r)) {
			return new Verse(r, theVerses.get(r));
		}
		return null;
	}

	@Override
	public Verse getVerse(BookOfBible book, int chapter, int verse) {
		Reference ref = new Reference(book, chapter, verse);
		return getVerse(ref);
	}

	@Override
	public VerseList getVersesContaining(String phrase) {
		VerseList list = new VerseList(version,title);
		phrase = phrase.toLowerCase();
		if (phrase.isEmpty()) {
			return list;
		}
		for (Reference ref : theVerses.keySet()) {
			String verseText = theVerses.get(ref);
			if (verseText.toLowerCase().contains(phrase))
				list.add(new Verse(ref, verseText));
		}
		return list;
	}

	@Override
	public ReferenceList getReferencesContaining(String phrase) {
		ReferenceList list = new ReferenceList();
		phrase = phrase.toLowerCase();
		if (phrase.isEmpty()) {
			return list;
		}
		for (Reference ref : theVerses.keySet()) {
			if (theVerses.get(ref).toLowerCase().contains(phrase))
				list.add(ref);
		}
		return list;
	}

	@Override
	public VerseList getVerses(ReferenceList references) {
		VerseList list = new VerseList(version, "Arbitrary list of Verses");
		for (Reference ref : references) {
			if (ref.getBookOfBible() == null)
				list.add(null);
			else {
				String verseText = theVerses.get(ref);
				if (verseText == null)
					list.add(null);
				else
					list.add(new Verse(ref, verseText));
			}
		}
		return list;
	}

	@Override
	public int getLastVerseNumber(BookOfBible book, int chapter) {
		Reference startRef = new Reference(book, chapter, 1);
		if (isValid(startRef)) {
			if (isValid(new Reference(book, chapter + 1, 1))) {
				SortedMap<Reference, String> subMap = theVerses.subMap(
						startRef, new Reference(book, chapter + 1, 1));
				return subMap.lastKey().getVerse();
			} else {
				SortedMap<Reference, String> subMap = theVerses.subMap(
						startRef, new Reference(BookOfBible.nextBook(book), 1,
								1));
				return subMap.lastKey().getVerse();
			}
		}
		return -1;
	}

	@Override
	public int getLastChapterNumber(BookOfBible book) {
		Reference startRef = new Reference(book, 1, 1);
		if (isValid(startRef)) {
			SortedMap<Reference, String> subMap = theVerses.subMap(startRef,
					new Reference(BookOfBible.nextBook(book), 1, 1));
			return subMap.lastKey().getChapter();
		}
		return -1;
	}

	@Override
	public ReferenceList getReferencesInclusive(Reference firstVerse,
			Reference lastVerse) {
		return getReferenceList(true, firstVerse, lastVerse);
	}

	@Override
	public ReferenceList getReferencesExclusive(Reference firstVerse,
			Reference lastVerse) {
		return getReferenceList(false, firstVerse, lastVerse);
	}

	@Override
	public ReferenceList getReferencesForBook(BookOfBible book) {
		if (book == null)
			return new ReferenceList();
		Reference startRef = new Reference(book, 1, 1);
		Reference endRef = new Reference(BookOfBible.nextBook(book), 1, 1);
		return getReferenceList(false, startRef, endRef);
	}

	@Override
	public ReferenceList getReferencesForChapter(BookOfBible book, int chapter) {
		if (book == null)
			return new ReferenceList();
		Reference startRef = new Reference(book, chapter, 1);
		Reference endRef = new Reference(book, chapter, getLastVerseNumber(
				book, chapter));
		return getReferenceList(true, startRef, endRef);
	}

	@Override
	public ReferenceList getReferencesForChapters(BookOfBible book,
			int chapter1, int chapter2) {
		if (book == null)
			return new ReferenceList();
		Reference startRef = new Reference(book, chapter1, 1);
		Reference endRef = new Reference(book, chapter2, getLastVerseNumber(
				book, chapter2));
		return getReferenceList(true, startRef, endRef);
	}

	@Override
	public ReferenceList getReferencesForPassage(BookOfBible book, int chapter,
			int verse1, int verse2) {
		if (book == null)
			return new ReferenceList();
		Reference startRef = new Reference(book, chapter, verse1);
		Reference endRef = new Reference(book, chapter, verse2);
		return getReferenceList(true, startRef, endRef);
	}

	@Override
	public ReferenceList getReferencesForPassage(BookOfBible book,
			int chapter1, int verse1, int chapter2, int verse2) {
		if (book == null)
			return new ReferenceList();
		Reference startRef = new Reference(book, chapter1, verse1);
		Reference endRef = new Reference(book, chapter2, verse2);
		return getReferenceList(true, startRef, endRef);
	}

	@Override
	public VerseList getVersesInclusive(Reference firstVerse,
			Reference lastVerse) {
		return getVerseList(true, firstVerse, lastVerse);
	}

	@Override
	public VerseList getVersesExclusive(Reference firstVerse,
			Reference lastVerse) {
		return getVerseList(false, firstVerse, lastVerse);
	}

	@Override
	public VerseList getBook(BookOfBible book) {
		if (book == null)
			return new VerseList(version, title);
		Reference startRef = new Reference(book, 1, 1);
		Reference endRef = new Reference(BookOfBible.nextBook(book), 1, 1);
		return getVerseList(false, startRef, endRef);
	}

	@Override
	public VerseList getChapter(BookOfBible book, int chapter) {
		if (book == null)
			return new VerseList(version, title);
		Reference startRef = new Reference(book, chapter, 1);
		Reference endRef = new Reference(book, chapter, getLastVerseNumber(
				book, chapter));
		return getVerseList(true, startRef, endRef);
	}

	@Override
	public VerseList getChapters(BookOfBible book, int chapter1, int chapter2) {
		if (book == null)
			return new VerseList(version, title);
		Reference startRef = new Reference(book, chapter1, 1);
		Reference endRef = new Reference(book, chapter2, getLastVerseNumber(
				book, chapter2));
		return getVerseList(true, startRef, endRef);
	}

	@Override
	public VerseList getPassage(BookOfBible book, int chapter, int verse1,
			int verse2) {
		if (book == null)
			return new VerseList(version, title);
		Reference startRef = new Reference(book, chapter, verse1);
		Reference endRef = new Reference(book, chapter, verse2);
		return getVerseList(true, startRef, endRef);
	}

	@Override
	public VerseList getPassage(BookOfBible book, int chapter1, int verse1,
			int chapter2, int verse2) {
		if (book == null)
			return new VerseList(version, title);
		Reference startRef = new Reference(book, chapter1, verse1);
		Reference endRef = new Reference(book, chapter2, verse2);
		return getVerseList(true, startRef, endRef);
	}

	/**
	 * Returns a referencelist of references between reference start and end.
	 * 
	 * @param inclusive
	 *            True to include end reference.
	 * @param start
	 *            The reference to start with. (inclusive)
	 * @param end
	 *            The reference to end with.
	 * @return The referencelist of references in between.
	 */
	private ReferenceList getReferenceList(boolean inclusive, Reference start,
			Reference end) {
		ReferenceList list = new ReferenceList();
		if (isValid(start) && start.compareTo(end) < 0) {
			SortedMap<Reference, String> subMap;
			if (inclusive) {
				subMap = theVerses.subMap(start, true, end, true);
			} else {
				subMap = theVerses.subMap(start, end);
			}
			for (Reference ref : subMap.keySet()) {
				list.add(ref);
			}
		}
		return list;
	}

	/**
	 * Returns a verselist of verses between reference start and end.
	 * 
	 * @param inclusive
	 *            True to include end reference.
	 * @param start
	 *            The reference to start with. (inclusive)
	 * @param end
	 *            The reference to end with.
	 * @return The VerseList of verses in between.
	 */
	private VerseList getVerseList(boolean inclusive, Reference start,
			Reference end) {
		VerseList list = new VerseList(version, title);
		if (isValid(start) && start.compareTo(end) < 0) {
			SortedMap<Reference, String> subMap;
			if (inclusive) {
				subMap = theVerses.subMap(start, true, end, true);
			} else {
				subMap = theVerses.subMap(start, end);
			}
			for (Reference ref : subMap.keySet()) {
				list.add(new Verse(ref, subMap.get(ref)));
			}
		}
		return list;
	}

}

