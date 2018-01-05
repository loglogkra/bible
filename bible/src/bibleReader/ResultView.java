package bibleReader;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import bibleReader.model.BibleReaderModel;
import bibleReader.model.NavigableResults;
import bibleReader.model.Reference;
import bibleReader.model.ReferenceList;
import bibleReader.model.ResultType;

/**
 * The display panel for the Bible Reader.
 * 
 * 
 * @author cusack, Logan
 * @modified March 23 2015
 */
public class ResultView extends JPanel {
	private BibleReaderModel model;

	private JScrollPane scrollPane;
	private JEditorPane editorPane;

	private JButton nextButton;
	private JButton previousButton;
	private JButton clear;
	private JLabel pageLabel;

	private JPanel statsPanel;
	private JLabel statsText;

	private NavigableResults nav;

	/**
	 * Construct a new ResultView and set its model to myModel. It needs to
	 * model to look things up.
	 * 
	 * @param myModel
	 *            The model this view will access to get information.
	 */
	public ResultView(BibleReaderModel myModel) {
		model = myModel;
		setUpGUI();
	}

	/**
	 * Set up the GUI for this result view object.
	 */
	private void setUpGUI() {
		// Create the stats panel.
		statsText = new JLabel();
		statsPanel = new JPanel(new BorderLayout());
		statsPanel.add(statsText);

		// Create the scroll pane and editorPane.
		editorPane = new JEditorPane();
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		editorPane.setName("OutputEditorPane");
		scrollPane = new JScrollPane();

		// Set the editorPane as the scrollPane's viewport.
		scrollPane.setViewportView(editorPane);

		// Create and add the navigation buttons and label
		pageLabel = new JLabel();
		nextButton = new JButton("Next");
		nextButton.setName("NextButton");
		nextButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (nav != null) {
					if (nav.getType() == ResultType.SEARCH) {
						search(nav.nextResults());
					} else if (nav.getType() == ResultType.PASSAGE) {
						passage(nav.nextResults());
					}
					toggleButtons();
					updateStats();
				}
			}
		});
		previousButton = new JButton("Previous");
		previousButton.setName("PreviousButton");
		previousButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (nav != null) {
					if (nav.getType() == ResultType.SEARCH) {
						search(nav.previousResults());
					} else if (nav.getType() == ResultType.PASSAGE) {
						passage(nav.previousResults());
					}
					toggleButtons();
					updateStats();
				}
			}
		});
		
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(pageLabel);
		buttonPanel.add(previousButton);
		buttonPanel.add(nextButton);
		statsPanel.add(buttonPanel, BorderLayout.EAST);
		toggleButtons();

		// Add the stats panel and scrollPane to the main panel.
		this.setLayout(new BorderLayout());
		this.add(statsPanel, BorderLayout.SOUTH);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Toggle the next and previous buttons.
	 */
	private void toggleButtons() {
		if (nav != null) {
			// Disable buttons if not useable
			if (nav.hasNextResults())
				nextButton.setEnabled(true);
			else
				nextButton.setEnabled(false);
			if (nav.hasPreviousResults())
				previousButton.setEnabled(true);
			else
				previousButton.setEnabled(false);
		} else {
			nextButton.setEnabled(false);
			previousButton.setEnabled(false);
		}
	}

	/**
	 * Updates the display by getting results and displaying them.
	 * 
	 * @param text
	 *            The text to search for.
	 * 
	 */
	private void search(ReferenceList list) {
		if (list == null) {
			return;
		}
		// Create a string buffer.
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>");

		int numVersions = model.getNumberOfVersions();
		String[] versions = new String[model.getNumberOfVersions()];
		TreeSet<String> set = new TreeSet<String>();
		for (String s : model.getVersions()) {
			set.add(s);
		}
		versions = (String[]) set.toArray(new String[numVersions]);

		// Create a legend for the data.
		if (list.size() > 0) {
			buffer.append("<tr> <td> Reference </td>");
			for (String version : versions) {
				buffer.append("<td>" + version + "</td>");
			}
			buffer.append("</tr>");
		}

		// Now parse the data and enter it into the editorPane.
		for (Reference reference : list) {
			// Add the reference to the table column
			buffer.append("<tr> <td>" + reference.toString() + "</td>");

			// Now loop through the actual versions.

			for (String version : versions) {
				String verseText = model.getBible(version).getVerseText(
						reference);
				if (verseText == null) {
					verseText = "";

				} else {
					buffer.append("<td>"
							+ verseText.replaceAll(
									"(?i)" + nav.getQueryPhrase(), "<b>$0</b>")
							+ "</td>");
				}
			}
			// Close the table row tag
			buffer.append("</tr>");
		}
		// Close the html tag.
		buffer.append("</html>");

		// Set the editorPane to this text.
		if (list.size() == 0) {
			editorPane.setText("No results found");
		} else {
			editorPane.setText(buffer.toString());
		}
		editorPane.setCaretPosition(0);
	}

	/**
	 * Update the stats panel.
	 * 
	 */
	private void updateStats() {
		if (nav != null) {
			int numPages = nav.getNumberPages();
			if (numPages == 1)
				statsText.setText("Your search for '" + nav.getQueryPhrase()
						+ "' returned " + numPages + " page of results");
			else
				statsText.setText("Your search for '" + nav.getQueryPhrase()
						+ "' returned " + numPages + " pages of results");
			if (numPages > 0)
				pageLabel.setText("Page " + nav.getPageNumber() + " of "
						+ nav.getNumberPages());
			else
				pageLabel.setText("");
		}
	}

	/**
	 * Search by passage and update the display and stats panel
	 * 
	 * @param passage
	 *            The passage to search for.
	 * 
	 */
	private void passage(ReferenceList list) {
		if (list.size() < 1 || list == null) {
			editorPane.setText("No results found");
			return;
		}

		// Get the first and last references from the list of results.
		int size = list.size();
		Reference ref1 = list.get(0);
		Reference ref2 = list.get(size - 1);
		// Check if the two refernces are from the same book or not.
		String refTitle = "";
		if (ref1.getBookOfBible() == ref2.getBookOfBible()) {
			if (ref1.getChapter() == ref2.getChapter())
				refTitle = ref1.toString() + "-" + ref2.getVerse();
			else
				refTitle = ref1.toString() + "-" + ref2.getChapter() + ":"
						+ ref2.getVerse();
		} else
			refTitle = ref1.toString() + "-" + ref2.toString();

		StringBuffer buf = new StringBuffer();

		int numVersions = model.getNumberOfVersions();
		String[] versions = new String[model.getNumberOfVersions()];
		TreeSet<String> set = new TreeSet<String>();
		for (String s : model.getVersions()) {
			set.add(s);
		}
		versions = (String[]) set.toArray(new String[numVersions]);

		buf.append("<table>");

		// Display the passage text that the user searched for
		buf.append("<tr> <th colspan=" + numVersions + ">" + refTitle
				+ "</th></tr>");
		// Display the Version Name at the top of the table column
		int i = 0;
		buf.append("<tr>");
		while (i != numVersions) {
			buf.append("<td>");
			buf.append(versions[i]);
			buf.append("</td>");
			i++;
		}
		buf.append("</tr>");
		// Display the Verse Text for each reference for each version
		buf.append("<tr valign=top>");
		for (int j = 0; j < numVersions; j++) {
			buf.append("<td>");
			for (Reference r : list) {
				if (model.getBible(versions[j]).getVerseText(r) != null) {

					if (r.getVerse() == 1) {
						if (!list.get(0).equals(r)) {
							buf.append("<br><br>");
						}
						buf.append("<sup><strong>");
						buf.append(r.getChapter());
						buf.append("</strong></sup>");
					} else {
						buf.append("<sup>");
						buf.append(r.getVerse());
						buf.append("</sup>");
					}
					buf.append(model.getBible(versions[j]).getVerseText(r));
				}
			}
			buf.append("</td>");
		}
		buf.append("</tr></table>");
		editorPane.setText("No results found");

		editorPane.setText(buf.toString());

		editorPane.setCaretPosition(0);
	}

	/**
	 * Create the Navigable results
	 * 
	 * @param text
	 *            The search phrase
	 * @param type
	 *            The type of search
	 */
	public void setNavigableResults(String text, ResultType type) {
		if (type == ResultType.SEARCH)
			nav = new NavigableResults(model.getReferencesContaining(text),
					text, type);
		else if (type == ResultType.PASSAGE)
			nav = new NavigableResults(model.getReferencesForPassage(text),
					text, type);
	}

	/**
	 * Display results for passages or searches. setNavigableResults() should be
	 * called first.
	 */
	public void displayResults() {
		if (nav != null) {
			if (nav.getType() == ResultType.SEARCH) {
				search(nav.currentResults());
			} else if (nav.getType() == ResultType.PASSAGE) {
				passage(nav.currentResults());
			}
			updateStats();
			toggleButtons();
		}
	}
}
