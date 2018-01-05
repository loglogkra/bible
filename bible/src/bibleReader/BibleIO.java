package bibleReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import bibleReader.model.Bible;
import bibleReader.model.BookOfBible;
import bibleReader.model.Reference;
import bibleReader.model.Verse;
import bibleReader.model.VerseList;

/**
 * A utility class that has useful methods to read/write Bibles and Verses.
 * 
 * @author cusack, Logan
 */
public class BibleIO {

	/**
	 * Read in a file and create a Bible object from it and return it.
	 * 
	 * @param bibleFile
	 * @return
	 */
	public static VerseList readBible(File bibleFile) { // Get the extension of
														// the file
		String name = bibleFile.getName();
		String extension = name.substring(name.lastIndexOf('.') + 1,
				name.length());

		// Call the read method based on the file type.
		if ("atv".equals(extension.toLowerCase())) {
			return readATV(bibleFile);
		} else if ("xmv".equals(extension.toLowerCase())) {
			return readXMV(bibleFile);
		} else {
			return null;
		}
	}

	/**
	 * Read in a Bible that is saved in the "ATV" format. The format is
	 * described below.
	 * 
	 * @param bibleFile
	 *            The file containing a Bible with .atv extension.
	 * @return A Bible object constructed from the file bibleFile, or null if
	 *         there was an error reading the file.
	 */
	private static VerseList readATV(File bibleFile) {
		// The ATV format
		//
		// The first line is a summary of what is in the file.
		// In the case of a Bible, the first line will be of the following form.
		// ABBREVIATION: FULL TITLE
		// where ABBREVIATION is generally the acronym used (e.g. KJV for the
		// King James Version), and FULL TITLE is the full title of the version.
		// If the first line does not contain a colon, the entire first line is
		// the version and the description is the empty string ("").
		// If the first line is blank, set the version to "unknown" and the
		// description to "".
		//
		// Each remaining line is of the form:
		// BOOK@CHAPTER:VERSE@TEXT
		// If the first line does not contain a colon, the entire first line is
		// the version and the description is the empty string ""/
		// If the first line is blank, set the version to "unknown" and
		// the description to "".
		//
		// For instance, here is Genesis 1:19 from the kjv file:
		// Ge@1:19@And the evening and the morning were the fourth day.
		//

		VerseList verseListToReturn = null;

		try {
			BufferedReader buffReader = new BufferedReader(new FileReader(
					bibleFile));

			String firstLine = buffReader.readLine();

			if (firstLine.length() > 0) {

				// Get the version and title.
				String[] splitOnColon = firstLine.split(": ");

				if (splitOnColon.length == 2) {
					String version = splitOnColon[0];
					String title = splitOnColon[1];

					// Create the verselist.
					verseListToReturn = new VerseList(version, title);
				} else {
					verseListToReturn = new VerseList(firstLine, "");
				}
			} else {
				verseListToReturn = new VerseList("unkown", "");
			}

			// Instead of creating new variables every time, we will reuse
			// these.
			BookOfBible bookOfBible;
			int book;
			int chapter;
			String text;

			while (buffReader.ready()) {
				String line = buffReader.readLine();

				String[] atSplit = line.split("@");

				// Add the verses.
				if (atSplit.length == 3) {
					bookOfBible = BookOfBible.getBookOfBible(atSplit[0]);
					// Make sure that the book of bible is not null.
					// If it is it means that the file is somehow corrupted
					// And we can return null
					if (bookOfBible == null) {
						buffReader.close();
						return null;
					}

					String[] bookAndChapter = atSplit[1].split(":");
					text = atSplit[2];

					try {
						if (bookAndChapter.length == 2) {
							chapter = Integer.parseInt(bookAndChapter[1]);
							book = Integer.parseInt(bookAndChapter[0]);

							verseListToReturn.add(new Verse(bookOfBible, book,
									chapter, text));
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
						buffReader.close();
						return null;
					}
				} else {
					buffReader.close();
					return null;
				}
			} // End of main loop.

			buffReader.close();
			return verseListToReturn;

		} catch (FileNotFoundException e) {
			return null;

		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Read in the Bible that is stored in the XMV format. This format uses xml
	 * style tags.
	 * 
	 * The first line is in the format <Version ASC: Title> Books are declared
	 * by the format <Book book, Title> Chapters are in the format <Chapter 1>
	 * Verses are in the format <Verse 23> Text
	 * 
	 * @param bibleFile
	 *            The file containing a Bible with .xmv extension.
	 * @return A Bible object constructed from the file bibleFile, or null if
	 *         there was an error reading the file.
	 */
	private static VerseList readXMV(File bibleFile) {
		// The XMV format
		//
		// The first line of the file is a summary of what is in the
		// file, in this case, it will be in the form,
		// "<Version VERSION: FULL TITLE>"
		// If the first line does not contain a colon,
		// We set the version to the entire first line and the description to an
		// empty string. If the first line is blank, we set the version to
		// "unknown"
		// and the description to "".
		//
		// The remainder of the file is a an xml like format where
		// Each new chapter is declared by the tag <CHAPTER NUMBER>"
		// Each new book is declared by the form <Book, BOOK, DESCRIPTION>
		//
		// For example:
		// <Book Genesis, The First Book of Moses, called Genesis>
		// <Chapter 1>
		// <Verse 1>In the beginning God created the heavens and the earth.

		String bookMatch = "<Book"; // All books of bible start with this.
		String verseMatch = "<Verse"; // All verses start with this.
		String chapterMatch = "<Chapter"; // All chapters start with this

		try {
			// These are the current chapter, book.
			int currentChapter = 0;
			BookOfBible currentBook = null;

			BufferedReader buffReader = new BufferedReader(new FileReader(
					bibleFile));

			// Get the first line of the file.
			String line = buffReader.readLine();

			if (line == null) {
				buffReader.close();
				return null;
			}

			// Declare the list that we will eventually return.
			VerseList list = null;

			// Get the information about the file from its first line.
			if (line.length() > 0) {
				if (line.startsWith("<Version") && line.length() > 9) {

					String firstLineNoTag = line.trim().substring(9,
							line.length());
					// I personally think it should be line.length() -1
					// to get rid of the closing bracket, but the tests did
					String[] td = firstLineNoTag.split(": ");
					if (td.length == 2) {
						list = new VerseList(td[0].trim(), td[1].trim());
					}
				} else {
					// The first line was not in the normal format.
					list = new VerseList("line", "");
				}
			} else {
				// The first line was blank
				list = new VerseList("unknown", "");
			}

			// If we're here and the list is still null, we cannot go on
			if (list == null) {
				buffReader.close();
				return null;
			}

			while (line != null) {

				// Trim trailing spaces off of the line
				line = line.trim();

				// The line starts with a book
				if (line.startsWith(bookMatch)) {
					String book = line.substring(bookMatch.length(),
							line.length() - 1);

					// Change the current book of bible.
					currentBook = BookOfBible.getBookOfBible(book.split(",")[0]
							.trim());

					// Make sure that we did not get a null book
					if (currentBook == null) {
						buffReader.close();
						return null;
					}
				}

				// The line starts with a chapter tag
				else if (line.startsWith(chapterMatch)) {
					try {
						currentChapter = Integer.parseInt(line.substring(
								chapterMatch.length(), line.length() - 1)
								.trim());
					} catch (NumberFormatException e) {
						e.printStackTrace();
						buffReader.close();
						return null;
					}
				}

				// The line starts with a verse
				else if (line.startsWith(verseMatch)) {
					try {
						String[] splitOnArrow = line.split(">");
						int verseNumber = Integer.parseInt(splitOnArrow[0]
								.substring(verseMatch.length()).trim());
						list.add(new Verse(new Reference(currentBook,
								currentChapter, verseNumber), splitOnArrow[1]));

					} catch (NumberFormatException e) {
						e.printStackTrace();
						buffReader.close();
						return null;
					}
				}

				line = buffReader.readLine();

			} // End of while loop

			buffReader.close();

			return list;

		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Write out the Bible in the ATV format.
	 * 
	 * @param file
	 *            The file that the Bible should be written to.
	 * @param bible
	 *            The Bible that will be written to the file.
	 */
	public static void writeBibleATV(File file, Bible bible) {
		// Get the verses that we wish to write. In this case, it is all of the
		// verses that are in this file.
		VerseList verses = bible.getAllVerses();

		// This is the first line which we wish to write to the file.
		String description = verses.getVersion() + ": "
				+ verses.getDescription();

		// Now we call a helper method in order to actually write the verses to
		// the file.
		writeVersesATV(file, description, verses);

	}

	/**
	 * Write out the given verses in the ATV format, using the description as
	 * the first line of the file.
	 * 
	 * @param file
	 *            The file that the Bible should be written to.
	 * @param description
	 *            The contents that will be placed on the first line of the
	 *            file, formatted appropriately.
	 * @param verses
	 *            The verses that will be written to the file.
	 */
	public static void writeVersesATV(File file, String description,
			VerseList verses) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			// Write the first line of the file.
			writer.write(description);
			writer.newLine();

			// Now write all of the verses to the file.
			for (Verse verse : verses) {

				// Get this verses reference
				Reference r = verse.getReference();

				// Write the verse to the file.
				writer.write(r.getBook() + "@" + r.getChapter() + ":"
						+ r.getVerse() + "@" + verse.getText());

				// Start the new line.
				writer.newLine();
			}

			// If we get here everything went right and it is time to close the
			// writer.
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the string out to the given file. It is presumed that the string is
	 * an HTML rendering of some verses, but really it can be anything.
	 * 
	 * @param file
	 * @param text
	 */
	public static void writeText(File file, String text) {

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(text);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
