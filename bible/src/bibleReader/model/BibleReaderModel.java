package bibleReader.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The model of the Bible Reader. It stores the Bibles and has methods for
 * searching for verses based on words or references.
 * 
 * @author cusack, Logan
 */
public class BibleReaderModel implements MultiBibleModel {

	private ArrayList<Bible> theBibles;
	private HashMap<Bible, Concordance> hm;
	public static final String number = "\\s*(\\d+)\\s*";

	public static Pattern bookPattern = Pattern
			.compile("\\s*((?:1|2|3|I|II|III)\\s*\\w+|(?:\\s*[a-zA-Z]+)+)\\s*(.*)");

	// ---------------------------------------------------------------------------------------------------------
	// Two examples of patterns that are valid. More are needed.
	// This one matches things like "3:4-7:3"
	public static Pattern cvcvPattern = Pattern.compile(number + ":" + number
			+ "-" + number + ":" + number);
	// This one matches things like "3-5"
	public static Pattern ccPattern = Pattern.compile(number + "-" + number);
	// This one matches single chapters
	public static Pattern cPattern = Pattern.compile(number);
	// This one matches Book 12 : 1 - 4
	public static Pattern cvvPattern = Pattern.compile(number + ":" + number
			+ "-" + number);
	// This one matches the format of a single verse
	public static Pattern vPattern = Pattern.compile(number + ":" + number);
	// This one matches "chaper-chapter : verse"
	public static Pattern ccvPattern = Pattern.compile(number + "-" + number
			+ ":" + number);

	/**
	 * Default constructor. You probably need to instantiate objects and do
	 * other assorted things to set up the model.
	 */
	public BibleReaderModel() {
		theBibles = new ArrayList<Bible>();
		hm = new HashMap<Bible, Concordance>();
		for (Bible bible : theBibles) {
			Concordance c = BibleFactory.createConcordance(bible);
			hm.put(bible, c);
		}
	}

	@Override
	public String[] getVersions() {
		String[] versions = new String[theBibles.size()];
		for (int i = 0; i < versions.length; i++) {
			versions[i] = theBibles.get(i).getVersion();
		}
		Arrays.sort(versions);
		return versions;
	}

	@Override
	public int getNumberOfVersions() {
		return getVersions().length;
	}

	@Override
	public void addBible(Bible bible) {
		if (bible != null) {
			hm.put(bible, hm.get(bible));
			theBibles.add(bible);
		}
	}

	@Override
	public Bible getBible(String version) {
		for (Bible bible : theBibles) {
			if (bible.getVersion().equals(version)) {
				return bible;
			}
		}
		return null;
	}

	@Override
	public VerseList getVerses(String version, ReferenceList references) {
		Bible bible = getBible(version);
		VerseList vl = new VerseList(version, bible.getTitle());
		vl.addAll(bible.getVerses(references));
		return vl;

	}

	// TODO Implement me: Stage 7

	@Override
	public String getText(String version, Reference reference) {
		Bible bible = getBible(version);
		if (bible != null) {
			String text = bible.getVerseText(reference);
			if (text != null)
				return text;
		}
		return "";
	}

	@Override
	public ReferenceList getReferencesContaining(String words) {
		TreeSet<Reference> list = new TreeSet<Reference>();
		for (Bible bible : theBibles) {
			list.addAll(bible.getReferencesContaining(words));
		}
		
		return new ReferenceList(list);
	}

	@Override
	public ReferenceList getReferencesContainingWord(String word) {
		TreeSet<Reference> list = new TreeSet<Reference>();
		if (!word.equals("")) {
			for (Concordance c : hm.values()) {
				list.addAll(c.getReferencesContaining(word));
			}
		}
		return new ReferenceList(list);
	}

	@Override
	public ReferenceList getReferencesContainingAllWords(String words) {
		return new ReferenceList();
	}

	@Override
	public ReferenceList getReferencesContainingAllWordsAndPhrases(String words) {
		return new ReferenceList();
	}

	@Override
	public ReferenceList getReferencesForPassage(String reference) {

		String theRest = null;
		String book = null;
		int chapter1, chapter2, verse1, verse2;

		// First, split the input into the book and the rest, if possible.
		Matcher m = bookPattern.matcher(reference);

		// Now see if it matches.
		if (m.matches()) {
			// It matches. Good.
			book = m.group(1);
			theRest = m.group(2);
			// Now we need to parse theRest to see what format it is.
			// Notice that I have omitted some of the cases.
			// You should think about whether or not the order the
			// possibilities occurs matters if you use this and add more cases.

			// "Book"
			if (theRest.length() == 0) {
				// It looks like they want a whole book.
				// So now you need to do something about it.

				// Return the a reference list of an entire book

				return getBookReferences(BookOfBible.getBookOfBible(book));

				// Book 1 : 1 - 2 : 2 - Works
			} else if ((m = cvcvPattern.matcher(theRest)).matches()) {
				chapter1 = Integer.parseInt(m.group(1));
				verse1 = Integer.parseInt(m.group(2));
				chapter2 = Integer.parseInt(m.group(3));
				verse2 = Integer.parseInt(m.group(4));
				// They want something of the form book
				// chapter1:verse1-chapter2:verse2
				// So now you need to do something about it.

				return getPassageReferences(BookOfBible.getBookOfBible(book),
						chapter1, verse1, chapter2, verse2);

				// Single chapter
			} else if ((m = ccPattern.matcher(theRest)).matches()) {
				chapter1 = Integer.parseInt(m.group(1));
				chapter2 = Integer.parseInt(m.group(2));
				// They want something of the form book chapter1-chapter2
				// So now you need to do something about it.
				return getChapterReferences(BookOfBible.getBookOfBible(book),
						chapter1, chapter2);

			} else if ((m = cPattern.matcher(theRest)).matches()) {
				chapter1 = Integer.parseInt(m.group(1));
				// They want something of the form book chapter1-chapter2
				// So now you need to do something about it.
				return getChapterReferences(BookOfBible.getBookOfBible(book),
						chapter1);
			} else if ((m = cvvPattern.matcher(theRest)).matches()) {
				chapter1 = Integer.parseInt(m.group(1));
				verse1 = Integer.parseInt(m.group(2));
				verse2 = Integer.parseInt(m.group(3));

				return getPassageReferences(BookOfBible.getBookOfBible(book),
						chapter1, verse1, verse2);

			} else if ((m = vPattern.matcher(theRest)).matches()) {
				chapter1 = Integer.parseInt(m.group(1));
				verse1 = Integer.parseInt(m.group(2));

				return getVerseReferences(BookOfBible.getBookOfBible(book),
						chapter1, verse1);
			} else if ((m = ccvPattern.matcher(theRest)).matches()) {
				chapter1 = Integer.parseInt(m.group(1));
				chapter2 = Integer.parseInt(m.group(2));
				verse2 = Integer.parseInt(m.group(3));

				return getPassageReferences(BookOfBible.getBookOfBible(book),
						chapter1, 1, chapter2, verse2);
			}

			else {
				// They want something else that I haven't taken into account
				// yet

				// For test purposes
				return new ReferenceList();
			}
		} else {
			// It doesn't match the overall format of "BOOK Stuff".

			return new ReferenceList();
		}
	}

	// -----------------------------------------------------------------------------
	// The methods below are for use by the getReferencesForPassage method
	// above.
	// After it parses the input string it will call one of these.
	//
	// These methods should be somewhat easy to implement. They are kind of
	// delegate
	// methods in that they call a method on the Bible class to do most of the
	// work.
	// However, they need to do so for every version of the Bible stored in the
	// model.
	// and combine the results.
	//
	// Once you implement one of these, the rest of them should be fairly
	// straightforward.
	// Think before you code, get one to work, and then implement the rest based
	// on
	// that one.
	//
	// These methods should not notify the observers. There may be times when we
	// want to call them and not notify. Since they are private methods, we
	// should
	// have the right to do that.
	//
	// These methods also shouldn't set the query/result type since we presume
	// one
	// of the public methods was called and hopefully that method did so.
	// -----------------------------------------------------------------------------

	@Override
	public ReferenceList getVerseReferences(BookOfBible book, int chapter,
			int verse) {
		TreeSet<Reference> referenceSet = new TreeSet<Reference>();

		for (Bible bible : theBibles) {
			Verse v = bible.getVerse(book, chapter, verse);
			if (v != null)
				referenceSet.add(v.getReference());

		}

		return new ReferenceList(referenceSet);
	}

	@Override
	public ReferenceList getPassageReferences(Reference startVerse,
			Reference endVerse) {

		TreeSet<Reference> referenceSet = new TreeSet<Reference>();

		for (Bible bible : theBibles) {
			referenceSet.addAll(bible.getReferencesInclusive(startVerse,
					endVerse));
		}

		return new ReferenceList(referenceSet);
	}

	@Override
	public ReferenceList getBookReferences(BookOfBible book) {
		TreeSet<Reference> referenceSet = new TreeSet<Reference>();

		for (Bible bible : theBibles) {
			referenceSet.addAll(bible.getReferencesForBook(book));
		}

		return new ReferenceList(referenceSet);
	}

	@Override
	public ReferenceList getChapterReferences(BookOfBible book, int chapter) {
		TreeSet<Reference> referenceSet = new TreeSet<Reference>();

		for (Bible bible : theBibles) {
			referenceSet.addAll(bible.getReferencesForChapter(book, chapter));
		}

		return new ReferenceList(referenceSet);
	}

	@Override
	public ReferenceList getChapterReferences(BookOfBible book, int chapter1,
			int chapter2) {
		// TODO Implement me: Stage 7
		TreeSet<Reference> referenceSet = new TreeSet<Reference>();

		for (Bible bible : theBibles) {
			referenceSet.addAll(bible.getReferencesForChapters(book, chapter1,
					chapter2));
		}

		return new ReferenceList(referenceSet);
	}

	@Override
	public ReferenceList getPassageReferences(BookOfBible book, int chapter,
			int verse1, int verse2) {

		TreeSet<Reference> referenceSet = new TreeSet<Reference>();

		for (Bible bible : theBibles) {
			referenceSet.addAll(bible.getReferencesForPassage(book, chapter,
					verse1, verse2));
		}

		return new ReferenceList(referenceSet);
	}

	@Override
	public ReferenceList getPassageReferences(BookOfBible book, int chapter1,
			int verse1, int chapter2, int verse2) {
		// TODO Implement me: Stage 7
		TreeSet<Reference> referenceSet = new TreeSet<Reference>();

		for (Bible bible : theBibles) {
			referenceSet.addAll(bible.getReferencesForPassage(book, chapter1,
					verse1, chapter2, verse2));
		}

		return new ReferenceList(referenceSet);
	}

	public static ArrayList<String> extractWords(String text) {
		text = text.toLowerCase();
		text = text.replaceAll("(<sup>[,\\w]*?</sup>|'s|`s|&#\\w*;)", "");
		text = text.replaceAll(",", "");
		String[] words = text.split("\\W+");
		ArrayList<String> toReturn = new ArrayList<String>(Arrays.asList(words));
		toReturn.remove("");
		return toReturn;
	}
}