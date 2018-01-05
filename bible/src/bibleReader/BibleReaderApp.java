package bibleReader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import bibleReader.model.ArrayListBible;
import bibleReader.model.Bible;
import bibleReader.model.BibleReaderModel;
import bibleReader.model.ResultType;
import bibleReader.model.VerseList;

/**
 * The main class for the Bible Reader Application.
 * 
 * @author cusack, Logan
 */
public class BibleReaderApp extends JFrame {
	// Change these to suit your needs.
	public static final int width = 1200;
	public static final int height = 600;

	public static void main(String[] args) {
		new BibleReaderApp();
	}

	// Fields
	private BibleReaderModel model;
	private ResultView resultView;

	private JTextField searchTextField;

	/**
	 * Default constructor. We may want to replace this with a different one.
	 */
	public BibleReaderApp() {
		
		setupLookAndFeel();

		model = new BibleReaderModel(); // For now call the default constructor.
										// This might change.
		File kjvFile = new File("kjv.atv");
		VerseList verses = BibleIO.readBible(kjvFile);
		Bible kjv = new ArrayListBible(verses);
		model.addBible(kjv);

		File asvFile = new File("asv.xmv");
		VerseList verses1 = BibleIO.readBible(asvFile);
		Bible asv = new ArrayListBible(verses1);
		model.addBible(asv);

		File esvFile = new File("esv.atv");
		VerseList verses3 = BibleIO.readBible(esvFile);
		Bible esv = new ArrayListBible(verses3);
		model.addBible(esv);

		setupGUI();
		pack();
		setSize(width, height);
		setMinimumSize(new Dimension(550, 200));

		// So the application exits when you click the "x".
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	// Method that can be used to enhance the look of your application.
	private void setupLookAndFeel() {
		UIManager.put("control", new Color(200,200,200));
		UIManager.put("nimbusLightBackground", new Color(220,220,220));
		UIManager.put("NimbusFocus", new Color(150,150,150));
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// will use default styling.
		}
	}

	/**
	 * setupGUI
	 */
	private void setupGUI() {

		// Create the resultview.
		resultView = new ResultView(model);

		this.setTitle("Bible Reader");
		this.setLayout(new BorderLayout());

		// Create the menu bar.
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu help = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		JMenuItem exit = new JMenuItem("Exit");
		JMenuItem open = new JMenuItem("Open");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);

			}
		});

		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"A bible reading program which has search functions");

			}
		});
		open.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.showOpenDialog(BibleReaderApp.this);
				int result = fileChooser.showSaveDialog(BibleReaderApp.this);
				if (result == JFileChooser.CANCEL_OPTION) {
					return;
				}
				File selectedFile = fileChooser.getSelectedFile();
				VerseList newVerses = BibleIO.readBible(selectedFile);
				if (newVerses != null) {

					// Make sure that the bile is not already stored.
					if (model.getBible(newVerses.getVersion()) != null) {
						JOptionPane.showMessageDialog(BibleReaderApp.this,
								"This file has already been added!");
					} else {
						model.addBible(new ArrayListBible(newVerses));
						// Now update the display.
						BibleReaderApp.this.resultView.displayResults();
					}
				} else {
					JOptionPane.showMessageDialog(BibleReaderApp.this,
							"This file is not compatable");
				}
			}
		});

		bar.add(file);
		bar.add(help);
		file.add(open);
		file.add(exit);
		help.add(about);

		this.setJMenuBar(bar);

		// Create text field.
		searchTextField = new JTextField();
		searchTextField.setName("InputTextField");
		searchTextField.setColumns(30);
		searchTextField.setText("Enter search phrase here");
		searchTextField.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                searchTextField.setText("");
            }
        });
		searchTextField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				resultView.setNavigableResults(searchTextField.getText(),
						ResultType.SEARCH);
				resultView.displayResults();
			}
		});

		// create the buttons.
		JButton searchButton = new JButton("Search");
		searchButton.setName("SearchButton");
		JButton passageButton = new JButton("Passage");
		passageButton.setName("PassageButton");

		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resultView.setNavigableResults(searchTextField.getText(),
						ResultType.SEARCH);
				resultView.displayResults();
			}
		});

		passageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resultView.setNavigableResults(searchTextField.getText(),
						ResultType.PASSAGE);
				resultView.displayResults();

			}
		});

		// Create the input panel.
		JPanel inputpanel = new JPanel(new FlowLayout());

		inputpanel.add(searchTextField);
		inputpanel.add(searchButton);
		inputpanel.add(passageButton);

		// here we will add the results view

		// Add the two panels to the main frame.
		this.add(inputpanel, BorderLayout.NORTH);
		this.add(resultView, BorderLayout.CENTER);

		// TODO Add passage lookup: Stage ?
		// TODO Add 2nd version on display: Stage ?
		// TODO Limit the displayed search results to 20 at a time: Stage ?
		// TODO Add 3rd versions on display: Stage ?
		// TODO Format results better: Stage ?
		// TODO Display cross references for third version: Stage ?
		// TODO Save/load search results: Stage ?
	}
}
