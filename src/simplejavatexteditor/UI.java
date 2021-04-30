package simplejavatexteditor;

import java.lang.reflect.Method;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.text.DefaultEditorKit;

public class UI extends JFrame implements ActionListener {

    private final String[] dragDropExtensionFilter = {".txt"};
    private final JTextArea textArea;
    private final JMenuBar menuBar;
    private final JMenu menuFile, menuEdit, menuFind;
    private final JMenuItem newFile, openFile, saveFile, closeFile, cutText, copyText, pasteText, clearTextFile, selectAllText, searchTextFile;
    private final Action selectAllAction;


    // setup icons - File Menu
    private final ImageIcon newFileIcon = new ImageIcon("icons/new.png");
    private final ImageIcon openFileIcon = new ImageIcon("icons/open.png");
    private final ImageIcon saveFileIcon = new ImageIcon("icons/save.png");
    private final ImageIcon closeFileIcon = new ImageIcon("icons/close.png");

    // setup icons - Edit Menu
    private final ImageIcon clearFileIcon = new ImageIcon("icons/clear.png");
    private final ImageIcon cutFileTextIcon = new ImageIcon("icons/cut.png");
    private final ImageIcon copyFileTextIcon = new ImageIcon("icons/copy.png");
    private final ImageIcon pasteFileTextIcon = new ImageIcon("icons/paste.png");
    private final ImageIcon selectAllFileTextIcon = new ImageIcon("icons/selectall.png");
  
    // setup icons - Search Menu
    private final ImageIcon searchTextInFileIcon = new ImageIcon("icons/search.png");


    private SupportedKeywords kw = new SupportedKeywords();
    private HighlightText languageHighlighter = new HighlightText(Color.GRAY);
    AutoComplete autocomplete;
    private boolean hasListener = false;
    private boolean edit = false;

    public UI() {
        try {
            ImageIcon appLogoimage = new ImageIcon("icons/app_logo.png");
            super.setIconImage(appLogoimage.getImage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Set the initial size of the window
        setSize(800, 500);

        // Set the title of the window
        setTitle("Untitled | " + SimpleJavaTextEditor.NAME);

        // Set the default close operation (exit when it gets closed)
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // center the frame on the monitor
        setLocationRelativeTo(null);

        // Set a default font for the TextArea
        textArea = new JTextArea("", 0, 0);
        textArea.setFont(new Font("Century Gothic", Font.PLAIN, 12));
        textArea.setTabSize(2);
        textArea.setFont(new Font("Century Gothic", Font.PLAIN, 12));
        textArea.setTabSize(2);

        /* SETTING BY DEFAULT WORD WRAP ENABLED OR TRUE */
        textArea.setLineWrap(true);
        DropTarget dropTarget = new DropTarget(textArea, dropTargetListener);

        // Set an higlighter to the JTextArea
        textArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                setTitle("Untitled | " + SimpleJavaTextEditor.NAME + "     [ Length: " + textArea.getText().length()
                        + "    Lines: " + (textArea.getText() + "|").split("\n").length
                        + "    Words: " + textArea.getText().trim().split("\\s+").length + " ]");
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                edit = true;
                languageHighlighter.highLight(textArea, kw.getCppKeywords());
                languageHighlighter.highLight(textArea, kw.getJavaKeywords());
            }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setWrapStyleWord(true);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        getContentPane().setLayout(new BorderLayout()); // the BorderLayout bit makes it fill it automatically
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane);
        getContentPane().add(panel);

        // Set the Menus
        menuFile = new JMenu("File");
        menuEdit = new JMenu("Edit");
        menuFind = new JMenu("Search");

        // Set the Items Menu
        newFile = new JMenuItem("New", newFileIcon);
        openFile = new JMenuItem("Open", openFileIcon);
        saveFile = new JMenuItem("Save", saveFileIcon);
        closeFile = new JMenuItem("Exit", closeFileIcon);
        clearTextFile = new JMenuItem("Clear", clearFileIcon);
        searchTextFile = new JMenuItem("Search", searchTextInFileIcon);

        menuBar = new JMenuBar();
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuFind);


        this.setJMenuBar(menuBar);

        // Set Actions:
        selectAllAction = new SelectAllAction("Select All", clearFileIcon, "Select all text", new Integer(KeyEvent.VK_A),
                textArea);

        this.setJMenuBar(menuBar);

        // New File
        newFile.addActionListener(this);  // Adding an action listener (so we know when it's been clicked).
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK)); // Set a keyboard shortcut
        menuFile.add(newFile); // Adding the file menu

        // Open File
        openFile.addActionListener(this);
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        menuFile.add(openFile);

        // Save File
        saveFile.addActionListener(this);
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        menuFile.add(saveFile);

        // Close File
        /*
         * Along with our "CTRL+F4" shortcut to close the window, we also have
         * the default closer, as stated at the beginning of this tutorial. this
         * means that we actually have TWO shortcuts to close:
         * 1) the default close operation (example, Alt+F4 on Windows)
         * 2) CTRL+F4, which we are
         * about to define now: (this one will appear in the label).
         */
        closeFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        closeFile.addActionListener(this);
        menuFile.add(closeFile);

        // Select All Text
        selectAllText = new JMenuItem(selectAllAction);
        selectAllText.setText("Select All");
        selectAllText.setIcon(selectAllFileTextIcon);
        selectAllText.setToolTipText("Select All");
        selectAllText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        menuEdit.add(selectAllText);

        // Clear File (Code)
        clearTextFile.addActionListener(this);
        clearTextFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
        menuEdit.add(clearTextFile);

        // Cut Text
        cutText = new JMenuItem(new DefaultEditorKit.CutAction());
        cutText.setText("Cut");
        cutText.setIcon(cutFileTextIcon);
        cutText.setToolTipText("Cut");
        cutText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        menuEdit.add(cutText);


        // Copy Text
        copyText = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyText.setText("Copy");
        copyText.setIcon(copyFileTextIcon);
        copyText.setToolTipText("Copy");
        copyText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        menuEdit.add(copyText);

        // Paste Text
        pasteText = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteText.setText("Paste");
        pasteText.setIcon(pasteFileTextIcon);
        pasteText.setToolTipText("Paste");
        pasteText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
        menuEdit.add(pasteText);

        // Find Word
        searchTextFile.addActionListener(this);
        searchTextFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
        menuFind.add(searchTextFile);

    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            if (edit) {
                Object[] options = {"Save and exit", "No Save and exit", "Return"};
                int n = JOptionPane.showOptionDialog(this, "Do you want to save the file ?", "Question",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                if (n == 0) {// save and exit
                    saveFile();
                    this.dispose();// dispose all resources and close the application
                } else if (n == 1) {// no save and exit
                    this.dispose();// dispose all resources and close the application
                }
            } else {
                System.exit(99);
            }
        }
    }

    // Make the TextArea available to the autocomplete handler
    protected JTextArea getEditor() {
        return textArea;
    }

    // Enable autocomplete option
    public void enableAutoComplete(File file) {
        if (hasListener) {
            textArea.getDocument().removeDocumentListener(autocomplete);
            hasListener = false;
        }

        ArrayList<String> arrayList;
        String[] list = kw.getSupportedLanguages();

        for (int i = 0; i < list.length; i++) {
            if (file.getName().endsWith(list[i])) {
                switch (i) {
                    case 0:
                        String[] jk = kw.getJavaKeywords();
                        arrayList = kw.setKeywords(jk);
                        autocomplete = new AutoComplete(this, arrayList);
                        textArea.getDocument().addDocumentListener(autocomplete);
                        hasListener = true;
                        break;
                    case 1:
                        String[] ck = kw.getCppKeywords();
                        arrayList = kw.setKeywords(ck);
                        autocomplete = new AutoComplete(this, arrayList);
                        textArea.getDocument().addDocumentListener(autocomplete);
                        hasListener = true;
                        break;
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        // If the source of the event was our "close" option
        if (e.getSource() == closeFile ){
            if (edit) {
                Object[] options = {"Save and exit", "No Save and exit", "Return"};
                int n = JOptionPane.showOptionDialog(this, "Do you want to save the file ?", "Question",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
                if (n == 0) {// save and exit
                    saveFile();
                    this.dispose();// dispose all resources and close the application
                } else if (n == 1) {// no save and exit
                    this.dispose();// dispose all resources and close the application
                }
            } else {
                this.dispose();// dispose all resources and close the application
            }
        } // If the source was the "new" file option
        else if (e.getSource() == newFile){
            if (edit) {
                Object[] options = {"Save", "No Save", "Return"};
                int n = JOptionPane.showOptionDialog(this, "Do you want to save the file at first ?", "Question",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
                if (n == 0) {// save
                    saveFile();
                    edit = false;
                } else if (n == 1) {
                    edit = false;
                    FEdit.clear(textArea);
                }
            } else {
                FEdit.clear(textArea);
            }

        } // If the source was the "open" option
        else if (e.getSource() == openFile ){
            JFileChooser open = new JFileChooser(); // open up a file chooser (a dialog for the user to  browse files to open)
            if( !(textArea.getText().equals("")) ) {
                saveFile();
            }
            // if true does normal operation
            int option = open.showOpenDialog(this); // get the option that the user selected (approve or cancel)

            /*
            * NOTE: because we are OPENing a file, we call showOpenDialog~ if
            * the user clicked OK, we have "APPROVE_OPTION" so we want to open
            * the file
            */
            if (option == JFileChooser.APPROVE_OPTION) {
                FEdit.clear(textArea); // clear the TextArea before applying the file contents
                try {
                    File openFile = open.getSelectedFile();
                    setTitle(openFile.getName() + " | " + SimpleJavaTextEditor.NAME);
                    Scanner scan = new Scanner(new FileReader(openFile.getPath()));
                    while (scan.hasNext()) {
                        textArea.append(scan.nextLine() + "\n");
                    }

                    enableAutoComplete(openFile);
                } catch (Exception ex) { // catch any exceptions, and...
                    // ...write to the debug console
                    System.err.println(ex.getMessage());
                }
            }

        } // If the source of the event was the "save" option
        else if (e.getSource() == saveFile  ){
            saveFile();
        }else
        // Clear File (Code)
        if (e.getSource() == clearTextFile ){

            Object[] options = {"Yes", "No"};
            int n = JOptionPane.showOptionDialog(this, "Are you sure to clear the text Area ?", "Question",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (n == 0) {// clear
                FEdit.clear(textArea);
            }
        }
        // Find
        if (e.getSource() == searchTextFile  ){
            new Find(textArea);
        }
    }

    class SelectAllAction extends AbstractAction {

        /**
         * Used for Select All function
         */

        public SelectAllAction(String text, ImageIcon icon, String desc, Integer mnemonic, final JTextArea textArea) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
            textArea.selectAll();
        }
    }

    private void saveFile() {
        // Open a file chooser
        JFileChooser fileChoose = new JFileChooser();
        // Open the file, only this time we call
        int option = fileChoose.showSaveDialog(this);

        /*
             * ShowSaveDialog instead of showOpenDialog if the user clicked OK
             * (and not cancel)
         */
        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                File openFile = fileChoose.getSelectedFile();
                setTitle(openFile.getName() + " | " + SimpleJavaTextEditor.NAME);

                BufferedWriter out = new BufferedWriter(new FileWriter(openFile.getPath()));
                out.write(textArea.getText());
                out.close();

                enableAutoComplete(openFile);
                edit = false;
            } catch (Exception ex) { // again, catch any exceptions and...
                // ...write to the debug console
                System.err.println(ex.getMessage());
            }
        }
    }
    DropTargetListener dropTargetListener = new DropTargetListener() {

        @Override
        public void dragEnter(DropTargetDragEvent e) {
        }

        @Override
        public void dragExit(DropTargetEvent e) {
        }

        @Override
        public void dragOver(DropTargetDragEvent e) {
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            if (edit) {
                Object[] options = {"Save", "No Save", "Return"};
                int n = JOptionPane.showOptionDialog(UI.this, "Do you want to save the file at first ?", "Question",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
                if (n == 0) {// save
                    UI.this.saveFile();
                    edit = false;
                } else if (n == 1) {
                    edit = false;
                    FEdit.clear(textArea);
                } else if (n == 2) {
                    e.rejectDrop();
                    return;
                }
            }
            try {
                Transferable tr = e.getTransferable();
                DataFlavor[] flavors = tr.getTransferDataFlavors();
                for (int i = 0; i < flavors.length; i++) {
                    if (flavors[i].isFlavorJavaFileListType()) {
                        e.acceptDrop(e.getDropAction());

                        try {
                            String fileName = tr.getTransferData(flavors[i]).toString().replace("[", "").replace("]", "");

                            // Allowed file filter extentions for drag and drop
                            boolean extensionAllowed = false;
                            for (int j = 0; j < dragDropExtensionFilter.length; j++) {
                                if (fileName.endsWith(dragDropExtensionFilter[j])) {
                                    extensionAllowed = true;
                                    break;
                                }
                            }
                            if (!extensionAllowed) {
                                JOptionPane.showMessageDialog(UI.this, "This file is not allowed for drag & drop", "Error", JOptionPane.ERROR_MESSAGE);

                            } else {
                                FileInputStream fis = new FileInputStream(new File(fileName));
                                byte[] ba = new byte[fis.available()];
                                fis.read(ba);
                                textArea.setText(new String(ba));
                                fis.close();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        e.dropComplete(true);
                        return;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            e.rejectDrop();
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent e) {
        }
    };

}
