package bibleReader.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * Concordance is a class which implements a concordance for a Bible. In other
 * words, it allows the easy lookup of all references which contain a given
 * word.
 * 
 * @author Chuck Cusack, March 2013 (Provided the interface)
 * @author Logan Kragt 
 */
public class Concordance {
	private TreeMap<String, ReferenceList> concordance;

	/**
	 * Construct a concordance for the given Bible.
	 */
	public Concordance(Bible bible) {

		concordance = new TreeMap<String, ReferenceList>();
		for (Verse v : bible.getAllVerses()) {
			ArrayList<String> words = extractWords(v.getText());
			for (String text : words) {
				String word = text.toLowerCase();
				if (concordance.containsKey(word)) {
					if (!concordance.get(word).contains(v.getReference())) {
						concordance.get(word).add(v.getReference());
					}
				} else {
					concordance.put(word, new ReferenceList());
					concordance.get(word).add(v.getReference());
				}
			}
		}
	}

	/**
	 * Return the list of references to verses that contain the word 'word'
	 * (ignoring case) in the version of the Bible that this concordance was
	 * created with.
	 * 
	 * @param word
	 *            a single word (no spaces, etc.)
	 * @return the list of References of verses from this version that contain
	 *         the word, or an empty list if no verses contain the word.
	 */
	public ReferenceList getReferencesContaining(String word) {
		word = word.toLowerCase().trim();
		if (concordance.get(word) != null && word != "") {
			return concordance.get(word);
		}
		return new ReferenceList();
	}

	/**
	 * Given an array of Strings, where each element of the array is expected to
	 * be a single word (with no spaces, etc., but ignoring case), return a
	 * ReferenceList containing all of the verses that contain <i>all of the
	 * words</i>.
	 * 
	 * @param words
	 *            A list of words.
	 * @return An ReferenceList containing references to all of the verses that
	 *         contain all of the given words, or an empty list if
	 */
	public ReferenceList getReferencesContainingAll(ArrayList<String> words) {
		Set<ReferenceList> hash = new HashSet<ReferenceList>();
		ReferenceList refList = new ReferenceList();

		for (String word : words) {
			word = word.toLowerCase();
		}

		for (String word : words) {

			// this is the first word
			if (words.size() == 0 && words.indexOf(word) == 0) {
				refList.addAll(getReferencesContaining(words.get(0)));

			} else {
				hash.add(getReferencesContaining(word));
				refList.retainAll(hash);
			}

		}

		return refList;
	}

	public static ArrayList<String> extractWords(String text) {
		text = text.toLowerCase();
		text = text.replaceAll("(<sup>[,\\w]*?</sup>|'s|'s|&#\\w*;)", "");
		text = text.replaceAll(",", "");
		String[] words = text.split("\\W+");
		ArrayList<String> toReturn = new ArrayList<String>(Arrays.asList(words));
		toReturn.remove("");
		return toReturn;
	}
}