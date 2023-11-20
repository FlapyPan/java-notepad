package com.lzt.mynotepad.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.stream.IntStream;

public class FontSettingDialog extends JDialog {
    private final JColorChooser colorChooser = new JColorChooser();
    private final String[] styles = {"常规", "粗体", "斜体"};
    private final JComboBox<String> styleBox = new JComboBox<>(styles);
    private final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    private final JComboBox<String> fontBox = new JComboBox<>(fonts);
    private final Integer[] sizes = IntStream.rangeClosed(8, 72).boxed().toArray(Integer[]::new);
    private final JComboBox<Integer> sizeBox = new JComboBox<>(sizes);
    private final JButton okBtn = new JButton("确认");
    private final JButton cancelBtn = new JButton("取消");

    {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(colorChooser, BorderLayout.CENTER);

        Container fontContainer = new Container();
        fontContainer.setLayout(new FlowLayout());

        fontBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                Font font = colorChooser.getPreviewPanel().getFont();
                colorChooser.getPreviewPanel().setFont(new Font((String) fontBox.getSelectedItem(),
                        font.getStyle(), font.getSize()));
            }
        });
        fontContainer.add(new JLabel("字体："));
        fontContainer.add(fontBox, BorderLayout.NORTH);

        styleBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                Font font = colorChooser.getPreviewPanel().getFont();
                int style = 0;
                if (styleBox.getSelectedIndex() == 1) style = Font.BOLD;
                if (styleBox.getSelectedIndex() == 2) style = Font.ITALIC;
                colorChooser.getPreviewPanel().setFont(new Font(font.getFontName(),
                        style, font.getSize()));
            }
        });
        fontContainer.add(new JLabel("字体样式："));
        fontContainer.add(styleBox, BorderLayout.NORTH);

        sizeBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                Font font = colorChooser.getPreviewPanel().getFont();
                colorChooser.getPreviewPanel().setFont(new Font(font.getFontName(),
                        font.getStyle(), sizes[sizeBox.getSelectedIndex()]));
            }
        });
        fontContainer.add(new JLabel("大小："));
        fontContainer.add(sizeBox, BorderLayout.NORTH);

        mainPanel.add(fontContainer, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        Container btnContainer = new Container();
        btnContainer.setLayout(new FlowLayout(FlowLayout.RIGHT));

        okBtn.addActionListener(e -> dispose());
        cancelBtn.addActionListener(e -> dispose());
        btnContainer.add(okBtn);
        btnContainer.add(cancelBtn);
        add(btnContainer, BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(false);
    }

    public FontSettingDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public JButton getOkBtn() {
        return okBtn;
    }

    public JButton getCancelBtn() {
        return cancelBtn;
    }

    public Color getSelectColor() {
        return colorChooser.getPreviewPanel().getForeground();
    }

    public Font getSelectFont() {
        return colorChooser.getPreviewPanel().getFont();
    }

    public FontSettingDialog ShowDialog(Color color, Font font) {
        colorChooser.setColor(color);
        colorChooser.getPreviewPanel().setFont(font);
        fontBox.setSelectedItem(font.getName());
        styleBox.setSelectedIndex(font.getStyle());
        sizeBox.setSelectedItem(font.getSize());
        pack();
        setVisible(true);
        return this;
    }
}
