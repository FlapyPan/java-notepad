package com.lzt.mynotepad.dialogs;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class SearchDialog extends JDialog {
    private final JTextField searchField = new JTextField(24);
    private final JButton searchBtn = new JButton("查找");
    private final JTextField replaceField = new JTextField(24);
    private final JButton replaceBtn = new JButton("替换");
    private final JButton replaceAllBtn = new JButton("替换全部");

    {
        setLayout(new BorderLayout());
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("查找内容"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        JPanel replacePanel = new JPanel();
        replacePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        replacePanel.add(new JLabel("替换内容"));
        replacePanel.add(replaceField);
        replacePanel.add(replaceBtn);
        replacePanel.add(replaceAllBtn);
        add(searchPanel, BorderLayout.NORTH);
        add(replacePanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(false);
    }

    public Document getSearchFieldDocument() {
        return searchField.getDocument();
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public String getReplaceText() {
        return replaceField.getText();
    }

    public JButton getSearchBtn() {
        return searchBtn;
    }

    public JButton getReplaceBtn() {
        return replaceBtn;
    }

    public JButton getReplaceAllBtn() {
        return replaceAllBtn;
    }

    public SearchDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }
}
