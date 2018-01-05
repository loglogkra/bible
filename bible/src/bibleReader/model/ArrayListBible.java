package bibleReader.model;

import java.util.ArrayList;

/**
 * A class that stores a version of the Bible.
 * 
 * @author Chuck Cusack (Provided the interface). Modified February 9, 2015.
 * @author Logan (provided the implementation)
 */
public class ArrayListBible implements Bible {

	// The Fields
	private String version;
	private String title;
	private ArrayList<Verse> theVerses;

	/**
	 * Create a new Bible with the given verses.
	 * 
	 * @param version
	 *            the version of the Bible (e.g. ESV, KJV, ASV, NIV).
	 * @param verses
	 *            All of the verses of this version of the Bible.
	 */
	public ArrayListBible(VerseList verses) {
		version = verses.getVersion();
		title = verses.getDescription();
		theVerses = new ArrayList<Verse>(verses);

		// Add dummy book.
		Verse dummy = new Verse(new Reference(
				BookOfBible.getBookOfBible("dummy"), 1, 1), "dummy");
		theVerses.add(dummy);
	}

	@Override
	public int getNumberOfVerses() {
		return theVerses.size();
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
		for (Verse verse : theVerses) {
			if (verse.getReference().equals(ref))
				return true;
		}
		return false;
	}

	@Override
	public String getVerseText(Reference r) {
		for (Verse verse : theVerses) {
			if (verse.getReference().equals(r))
				return verse.getText();
		}
		return null;
	}

	@Override
	public Verse getVerse(Reference r) {
		for (Verse verse : theVerses) {
			if (verse.getReference().equals(r))
				return verse;
		}
		return null;
	}

	@Override
	public Verse getVerse(BookOfBible book, int chapter, int verse) {
		Reference r = new Reference(book, chapter, verse);
		return getVerse(r);
	}

	// ---------------------------------------------------------------------------------------------
	// The following part of this class should be implemented for stage 4.
	// See the Bible interface for the documentation of these methods.
	// Do not over think these methods. All three should be pretty
	// straightforward to implement.
	// For Stage 8 (give or take a 1 or 2) you will re-implement them so they
	// work better.
	// At that stage you will create another class to facilitate searching and
	// use it here.
	// (Essentially these two methods will be delegate methods.)
	// ---------------------------------------------------------------------------------------------

	@Override
	public VerseList getAllVerses() {
		// Remove the dummy book.
		theVerses.remove(theVerses.size() - 1);
		VerseList verseListToReturn = new VerseList(version, title);
		verseListToReturn.addAll(theVerses);
		theVerses
				.add(new Verse(new Reference(BookOfBible.Dummy, 1, 1), "dummy"));
		return verseListToReturn;
	}

	@Override
	public VerseList getVersesContaining(String phrase) {
		phrase = phrase.toLowerCase();

		VerseList verseListToReturn = new VerseList(version, title);

		// First check that the string is valid, ie make sure it is not empty.
		if (phrase.length() < 1)
			return verseListToReturn;

		// Find verses that contain this string and add them to the VerseList.
		for (Verse verse : theVerses) {
			if (verse.getText().toLowerCase().contains(phrase)) {
				verseListToReturn.add(verse);
			}
		}
		return verseListToReturn;
	}

	@Override
	public ReferenceList getReferencesContaining(String phrase) {
		ReferenceList referenceListToReturn = new ReferenceList();
		phrase = phrase.toLowerCase();
		// First check that the string is valid, ie make sure it is not empty.
		if (phrase.length() < 1)
			return referenceListToReturn;

		// Find verses that contain this string and add them to the VerseList.
		for (Verse verse : theVerses) {
			if (verse.getText().toLowerCase().contains(phrase)) {
				referenceListToReturn.add(verse.getReference());
			}
		}
		return referenceListToReturn;
	}

	// ---------------------------------------------------------------------------------------------
	// The following part of this class should be implemented for Stage 7.
	//
	// HINT: Do not reinvent the wheel. Some of these methods can be implemented
	// by looking up
	// one or two things and calling another method to do the bulk of the work.
	// ---------------------------------------------------------------------------------------------

	@Override
	public VerseList getVerses(ReferenceList references) {
		VerseList list = new VerseList(getVersion(), "Arbitrary list of Verses");
		for (Reference r : references) {
			list.add(getVerse(r));
		}
		return list;
	}

	@Override
	public int getLastVerseNumber(BookOfBible book, int chapter) {
		int count = 0;
		boolean counting = false;
		for (Verse v : theVerses) {
			Reference r = v.getReference();
			if (book == r.getBookOfBible() && chapter == r.getChapter()) {
				counting = true;
			}
			if (counting) {
				if (!(book == r.getBookOfBible() && chapter == r.getChapter()))
					return count;
				else
					count++;
			}

		}
		return -1;

	}

	@Override
	public int getLastChapterNumber(BookOfBible book) {
		for (int i = 0; i < theVerses.size(); i++) {
			if (book == theVerses.get(i).getReference().getBookOfBible()) {
				if (theVerses.get(i + 1).getReference().getBookOfBible() != book) {
					return theVerses.get(i).getReference().getChapter();
				}
			}
		}
		return -1;
	}

	@Override
	public ReferenceList getReferencesInclusive(Reference firstVerse,
			Reference lastVerse) {

		// Make sure that this is a valid search before searching
		if (firstVerse == null || lastVerse == null
				|| firstVerse.getBookOfBible() == null
				|| lastVerse.getBookOfBible() == null)
			return new ReferenceList();

		if (firstVerse.compareTo(lastVerse) > 0) {
			return new ReferenceList();
		}
		// Create the reference list we want to return.
		ReferenceList list = new ReferenceList();

		boolean recording = false;

		for (Verse v : theVerses) {
			Reference r = v.getReference();

			if (r.equals(firstVerse))
				recording = true;

			// If this reference equals the last verse, we should return the
			// list.
			// Last verse was already added outside of the loop,
			// so it does not need to be added.
			else if (r.equals(lastVerse)) {
				list.add(r);
				return list;
			}

			if (recording)
				list.add(r);
		}
		// If the last verse was never hit, we will an empty reference list
		return new ReferenceList();

	}

	@Override
	public ReferenceList getReferencesExclusive(Reference firstVerse,
			Reference lastVerse) {

		// Make sure that this is a valid search before searching
		if (firstVerse == null || lastVerse == null
				|| firstVerse.getBookOfBible() == null
				|| lastVerse.getBookOfBible() == null)
			return new ReferenceList();

		if (firstVerse.compareTo(lastVerse) > 0) {
			return new ReferenceList();
		}
		// Create the reference list we want to return.
		ReferenceList list = new ReferenceList();

		boolean recording = false;

		for (Verse v : theVerses) {

			// If this reference equals the first reference, we need to start
			// recording.
			if (v.getReference().equals(firstVerse))
				recording = true;

			else if (v.getReference().equals(lastVerse))
				return list;

			if (recording)
				list.add(v.getReference());
		}
		// If the last verse was never hit, we will an empty reference list
		return new ReferenceList();

	}

	@Override
	public ReferenceList getReferencesForBook(BookOfBible book) {
		if (book == null)
			return new ReferenceList();

		boolean recording = false;
		ReferenceList list = new ReferenceList();

		for (Verse v : theVerses) {
			Reference r = v.getReference();
			BookOfBible b = r.getBookOfBible();

			if (b == book && !recording) {
				recording = true;
			} else if (recording && b != book) {
				return list;
			}
			if (recording) {
				list.add(r);
			}
		}
		// If the book was never found, then the list will be empty.
		return list;
	}

	@Override
	public ReferenceList getReferencesForChapter(BookOfBible book, int chapter) {
		if (book == null)
			return new ReferenceList();

		return getReferencesInclusive(new Reference(book, chapter, 1),
				new Reference(book, chapter, getLastVerseNumber(book, chapter)));
	}

	@Override
	public ReferenceList getReferencesForChapters(BookOfBible book,
			int chapter1, int chapter2) {

		ReferenceList list = new ReferenceList();

		for (int i = chapter1; i <= chapter2; i++) {
			ReferenceList l = getReferencesForChapter(book, i);

			if (l.isEmpty())
				return new ReferenceList();
			else
				list.addAll(l);
		}

		return list;
	}

	@Override
	public ReferenceList getReferencesForPassage(BookOfBible book, int chapter,
			int verse1, int verse2) {
		Reference start = new Reference(book, chapter, verse1);
		Reference end = new Reference(book, chapter, verse2);
		return getReferencesInclusive(start, end);

	}

	@Override
	public ReferenceList getReferencesForPassage(BookOfBible book,
			int chapter1, int verse1, int chapter2, int verse2) {
		Reference start = new Reference(book, chapter1, verse1);
		Reference end = new Reference(book, chapter2, verse2);

		return getReferencesInclusive(start, end);
	}

	@Override
	public VerseList getVersesInclusive(Reference firstVerse,
			Reference lastVerse) {
		VerseList list = new VerseList(version, title);

		if (firstVerse.compareTo(lastVerse) > 0)
			return list;

		if (!isValid(firstVerse) || !isValid(lastVerse))
			return list;

		int index1 = 0;
		int size = theVerses.size();
		int originalSize = size;
		int searchIndex = size / 2;
		int compareToIndex;

		boolean keepGoing = true;

		while (keepGoing) {
			compareToIndex = theVerses.get(searchIndex).getReference()
					.compareTo(firstVerse);

			if (compareToIndex > 0) {
				// The size should be cut in half.
				size = size / 2;
				// Create the new search index.
				if (size == 0)
					searchIndex--;
				else
					searchIndex = searchIndex - (size / 2);
			} else if (compareToIndex < 0) {
				// The size should be cut in half
				size = size / 2;
				// Create the new search index.
				if (size == 0)
					searchIndex++;
				else
					searchIndex = searchIndex - (size / 2);
			} else if (compareToIndex == 0) {
				index1 = searchIndex;
				keepGoing = false;
			} else
				keepGoing = true;
		}
		for (int i = index1; i < originalSize; i++) {
			Verse v = theVerses.get(i);
			if (v.getReference().equals(lastVerse)) {
				list.add(v);
				return list;
			} else {
				list.add(v);
			}
		}
		return list;

	}

	@Override
	public VerseList getVersesExclusive(Reference firstVerse,
			Reference lastVerse) {
		VerseList list = new VerseList(version, title);

		if (firstVerse.compareTo(lastVerse) > 0)
			return list;

		if (!isValid(firstVerse) || !isValid(lastVerse))
			return list;

		int index1 = 0;

		int size = theVerses.size();
		int originalSize = size;
		int searchIndex = size / 2;
		int compareToIndex;

		boolean keepGoing = true;

		while (keepGoing) {
			// See if the verse we want is on the left or right side of the
			// size;
			compareToIndex = theVerses.get(searchIndex).getReference()
					.compareTo(firstVerse);

			// If this is positive, then the reference of the search index is
			// after the first verse ie. first verse comes first.
			// this means that we can elimate the second half
			if (compareToIndex > 0) {
				// The size should be cut in half.
				size = size / 2;
				// Create the new search index.
				if (size == 0)
					searchIndex--;
				else
					searchIndex = searchIndex - (size / 2);
			}
			// This one is harder. The reference of the search index is
			// before the first verse.
			// this means we can elimate the first half.
			else if (compareToIndex < 0) {
				// The size should be cut in half
				size = size / 2;
				// Create the new search index.
				if (size == 0)
					searchIndex++;
				else
					searchIndex = searchIndex - (size / 2);
			} else if (compareToIndex == 0) {
				index1 = searchIndex;
				keepGoing = false;
			} else
				keepGoing = true;
		}
		for (int i = index1; i < originalSize; i++) {
			Verse v = theVerses.get(i);
			if (v.getReference().equals(lastVerse)) {
				return list;
			} else {
				list.add(v);
			}
		}
		return list;
	}

	@Override
	public VerseList getBook(BookOfBible book) {

		if (book == null)
			return new VerseList(version, title);
		boolean recording = false;

		VerseList list = new VerseList(version, title);

		for (Verse v : theVerses) {
			Reference r = v.getReference();
			BookOfBible b = r.getBookOfBible();

			if (b == book && !recording)
				recording = true;

			else if (recording && b != book)
				return list;

			if (recording)
				list.add(v);
		}
		// If the book was never found, then the list will be empty.
		return list;
	}

	@Override
	public VerseList getChapter(BookOfBible book, int chapter) {
		VerseList list = new VerseList(version, title);
		boolean record = false;
		for (int i = 0; i < theVerses.size(); i++) {
			Verse v = theVerses.get(i);
			Reference r = v.getReference();
			int c = r.getChapter();
			if (r.getBookOfBible() == book && c == chapter)
				record = true;
			if (record && c != chapter)
				return list;
			if (record)
				list.add(v);
		}
		return new VerseList(version, title);
	}

	@Override
	public VerseList getChapters(BookOfBible book, int chapter1, int chapter2) {
		VerseList list = new VerseList(version, title);
		for (int i = chapter1; i <= chapter2; i++) {
			VerseList l = getChapter(book, i);
			// If any of the chapters are not valid, then the passage search
			if (l.isEmpty())
				return new VerseList(version, title);
			else
				list.addAll(l);
		}
		return list;
	}

	@Override
	public VerseList getPassage(BookOfBible book, int chapter, int verse1,
			int verse2) {
		Reference start = new Reference(book, chapter, verse1);
		Reference end = new Reference(book, chapter, verse2);
		return getVersesInclusive(start, end);
	}

	@Override
	public VerseList getPassage(BookOfBible book, int chapter1, int verse1,
			int chapter2, int verse2) {
		Reference start = new Reference(book, chapter1, verse1);
		Reference end = new Reference(book, chapter2, verse2);
		return getVersesInclusive(start, end);
	}
}
