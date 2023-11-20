package com.lzt.mynotepad.dialogs;

import javax.swing.*;

public class FileDialog extends JFileChooser {
    {
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        setMultiSelectionEnabled(false);
    }
}
