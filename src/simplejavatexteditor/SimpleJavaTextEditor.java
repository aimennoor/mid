package simplejavatexteditor;

import javax.swing.JTextPane;

public class SimpleJavaTextEditor extends JTextPane {

    public final static String NAME = "MyNotePad";

    public static void main(String[] args) {
        new UI().setVisible(true);
    }

}
