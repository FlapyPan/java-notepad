package com.lzt.mynotepad;


import com.lzt.mynotepad.dialogs.FileDialog;
import com.lzt.mynotepad.dialogs.FontSettingDialog;
import com.lzt.mynotepad.dialogs.SearchDialog;
import org.mozilla.universalchardet.UniversalDetector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    public static final int W = 800;
    public static final int H = 600;
    public static final String ABOUT_INFO;
    public static final String LOOK_AND_FEEL = UIManager.getSystemLookAndFeelClassName();

    static {
        Properties prop = System.getProperties();
        ABOUT_INFO = "Java记事本\n版本：v1.1\n作者：刘志腾\n" +
                "当前用户：" + prop.getProperty("user.name") + '\n' +
                "Java版本：" + prop.getProperty("java.vendor") + ' ' + prop.getProperty("java.version") + '\n' +
                "系统版本：" + prop.getProperty("os.name") + ' ' + prop.getProperty("os.arch") + '\n';
    }

    private final UndoManager undoManager = new UndoManager();
    private final JMenuBar mainMenuBar = new JMenuBar();
    private final JScrollPane mainScrollPane;
    private final JTextPane mainTextPane = new JTextPane();
    private final FileDialog fileChooser = new FileDialog();
    private final FontSettingDialog fontFrame = new FontSettingDialog(this, "字体设置", true);
    private final SearchDialog searchFrame = new SearchDialog(this, "查找和替换", false);
    private int lastSearchIndex = -1;
    private File editFile;
    private String fileCode = "GBK";
    private boolean isSaved = true;

    private MainFrame() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(W, H);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CloseWindowHandle();
            }
        });
        InitMenuBar();

        InitMainTextPane();
        InitFontFrame();
        InitSearchFrame();

        mainScrollPane = new JScrollPane(mainTextPane);
        InitMainScrollPane();
        setJMenuBar(mainMenuBar);
        add(mainScrollPane);

        ModMainFrameTitle();
        setVisible(true);
    }

    public static void createMainFrame() {
        try {
            UIManager.setLookAndFeel(LOOK_AND_FEEL);
            new MainFrame();
        } catch (Exception e) {
            ExceptionHandle(e, "未知错误");
        }
    }

    private void InitMenuBar() {
        JMenu fileMenu = new JMenu("文件(F)");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        LinkedHashMap<String, JMenuItem> fileMenuItems = new LinkedHashMap<>();
        for (String s : new String[]{"新建(Ctrl+N)", "打开(Ctrl+O)", "保存(Ctrl+S)", "另存为(Ctrl+Shift+S)", "退出"}) {
            JMenuItem item = new JMenuItem(s);
            fileMenuItems.put(s, item);
            fileMenu.add(item);
        }
        fileMenuItems.get("新建(Ctrl+N)").addActionListener(e -> New());
        fileMenuItems.get("打开(Ctrl+O)").addActionListener(e -> Open(null));
        fileMenuItems.get("保存(Ctrl+S)").addActionListener(e -> Save());
        fileMenuItems.get("另存为(Ctrl+Shift+S)").addActionListener(e -> SaveAs());
        fileMenuItems.get("退出").addActionListener(e -> CloseWindowHandle());
        mainMenuBar.add(fileMenu);

        JMenu editMenu = new JMenu("编辑(E)");
        editMenu.setMnemonic(KeyEvent.VK_E);
        LinkedHashMap<String, JMenuItem> editMenuItems = new LinkedHashMap<>();
        for (String s : new String[]{"撤销(Ctrl+Z)", "重做(Ctrl+Y)", "查找和替换(Ctrl+F/Ctrl+H)", "时间/日期(F5)"}) {
            JMenuItem item = new JMenuItem(s);
            editMenuItems.put(s, item);
            editMenu.add(item);
        }
        editMenuItems.get("撤销(Ctrl+Z)").addActionListener(e -> {
            if (undoManager.canUndo()) undoManager.undo();
        });
        editMenuItems.get("重做(Ctrl+Y)").addActionListener(e -> {
            if (undoManager.canRedo()) undoManager.redo();
        });
        editMenuItems.get("查找和替换(Ctrl+F/Ctrl+H)").addActionListener(e -> Search());
        editMenuItems.get("时间/日期(F5)").addActionListener(e ->
                mainTextPane.setText(mainTextPane.getText() + new SimpleDateFormat(" HH:mm yyyy/MM/dd ").format(new Date()))
        );
        mainMenuBar.add(editMenu);

        JMenu formatMenu = new JMenu("格式(O)");
        formatMenu.setMnemonic(KeyEvent.VK_O);
        LinkedHashMap<String, JMenuItem> formatMenuItems = new LinkedHashMap<>();
        for (String s : new String[]{"字体"}) {
            JMenuItem item = new JMenuItem(s);
            formatMenuItems.put(s, item);
            formatMenu.add(item);
        }
        formatMenuItems.get("字体").addActionListener(e -> SetFont());
        mainMenuBar.add(formatMenu);

        JMenu helpMenu = new JMenu("帮助(H)");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        LinkedHashMap<String, JMenuItem> helpMenuItems = new LinkedHashMap<>();
        for (String s : new String[]{"关于"}) {
            JMenuItem item = new JMenuItem(s);
            helpMenuItems.put(s, item);
            helpMenu.add(item);
        }
        helpMenuItems.get("关于").addActionListener(e -> JOptionPane.showMessageDialog(null, ABOUT_INFO, "关于", JOptionPane.PLAIN_MESSAGE));
        mainMenuBar.add(helpMenu);

    }

    private void InitMainScrollPane() {
        mainScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        mainScrollPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                Font font = mainTextPane.getFont();
                int size = font.getSize();
                if (e.getWheelRotation() < 0) {
                    if (size < 72) {
                        mainTextPane.setFont(new Font(font.getFontName(), font.getStyle(), size + 2));
                    }
                } else {
                    if (size > 8) {
                        mainTextPane.setFont(new Font(font.getFontName(), font.getStyle(), size - 2));
                    }
                }
            }
        });
    }

    private void InitMainTextPane() {
        mainTextPane.setBounds(0, 0, W, H);
        mainTextPane.setBorder(new EmptyBorder(3, 3, 3, 3));
        mainTextPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        InitUndoManager();
        mainTextPane.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_N) {
                    New();
                }
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_O) {
                    Open(null);
                }
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    Save();
                }
                if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    SaveAs();
                }
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    if (undoManager.canUndo()) undoManager.undo();
                }
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
                    if (undoManager.canRedo()) undoManager.redo();
                }
                if (e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_F || e.getKeyCode() == KeyEvent.VK_H)) {
                    Search();
                }
                if (e.getKeyCode() == KeyEvent.VK_F5) {
                    mainTextPane.setText(mainTextPane.getText() + new SimpleDateFormat(" HH:mm yyyy/MM/dd ").format(new Date()));
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        });

        new DropTarget(mainTextPane, new DropTargetListener() {

            private DataFlavor check(Transferable transfer) {
                DataFlavor flavor1 = DataFlavor.javaFileListFlavor;
                DataFlavor flavor2 = DataFlavor.stringFlavor;
                if (transfer.isDataFlavorSupported(flavor1)) return flavor1;
                if (transfer.isDataFlavorSupported(flavor2)) return flavor2;
                return null;
            }

            @Override
            public void dragEnter(DropTargetDragEvent event) {
                Transferable transferable = event.getTransferable();
                DataFlavor flavor = check(transferable);
                if (flavor == null) event.rejectDrag();
            }

            @Override
            public void dragOver(DropTargetDragEvent event) {

            }

            @Override
            public void dropActionChanged(DropTargetDragEvent event) {

            }

            @Override
            public void dragExit(DropTargetEvent event) {

            }

            @Override
            public void drop(DropTargetDropEvent event) {
                Transferable transferable = event.getTransferable();
                DataFlavor flavor = check(transferable);
                if (flavor != null) {
                    event.acceptDrop((DnDConstants.ACTION_COPY));
                    try {
                        if (flavor == DataFlavor.javaFileListFlavor) {
                            List<File> files = uncheckedCast(event.getTransferable().getTransferData(flavor));
                            if (files.size() > 0) {
                                Open(files.get(0));
                            }
                        } else if (flavor == DataFlavor.stringFlavor) {
                            String ret;
                            ret = (String) event.getTransferable().getTransferData(DataFlavor.stringFlavor);
                            mainTextPane.replaceSelection(ret);
                        }
                    } catch (Exception ex) {
                        ExceptionHandle(ex, "不支持的文件类型！");
                    }
                } else event.rejectDrop();
                event.dropComplete(true);
            }
        });
    }

    private void InitUndoManager() {
        undoManager.discardAllEdits();
        isSaved = true;
        mainTextPane.getDocument().addUndoableEditListener(undoManager);
        mainTextPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isSaved = false;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isSaved = false;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //isSaved = false;
            }
        });
    }

    private void InitFontFrame() {
        fontFrame.getOkBtn().addActionListener(e -> {
            mainTextPane.setForeground(fontFrame.getSelectColor());
            mainTextPane.setFont(fontFrame.getSelectFont());
        });
    }

    private void InitSearchFrame() {
        searchFrame.getSearchFieldDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                lastSearchIndex = -1;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                lastSearchIndex = -1;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lastSearchIndex = -1;
            }
        });
        searchFrame.getSearchBtn().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SearchOrReplace(searchFrame.getSearchText(), searchFrame.getReplaceText(), false);
            }
        });
        searchFrame.getReplaceBtn().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SearchOrReplace(searchFrame.getSearchText(), searchFrame.getReplaceText(), true);
            }
        });
        searchFrame.getReplaceAllBtn().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String searchText = searchFrame.getSearchText();
                String replaceText = searchFrame.getReplaceText();
                if (mainTextPane.getText() != null && searchFrame.getSearchText() != null) {
                    int index = mainTextPane.getText().replace("\r\n", "\n")
                            .indexOf(searchFrame.getSearchText(), lastSearchIndex + 1);
                    if (index != -1) {
                        mainTextPane.setText(
                                mainTextPane.getText().replace(searchText, replaceText)
                        );
                    }
                }
            }
        });

    }

    private void New() {
        if (!isSaved) {
            int result = JOptionPane
                    .showConfirmDialog(null, "当前文件未保存，是否保存？", "提示", JOptionPane.YES_NO_OPTION);
            if (result == 0) {
                Save();
            }
        }
        fileCode = "GBK";
        editFile = null;
        mainTextPane.setText(null);
        InitUndoManager();
        isSaved = true;
        ModMainFrameTitle();
    }

    private void Open(File file) {
        if (!isSaved) {
            int result = JOptionPane
                    .showConfirmDialog(null, "当前文件未保存，是否保存？", "提示", JOptionPane.YES_NO_OPTION);
            if (result == 0) {
                Save();
            }
        }
        if (file == null) {
            int val = fileChooser.showOpenDialog(null);
            if (val == JFileChooser.APPROVE_OPTION) {
                editFile = fileChooser.getSelectedFile();
                LoadContent();
            }
        } else {
            editFile = file;
            LoadContent();
        }

    }

    private boolean Save() {
        if (editFile != null) {
            if (!isSaved) {
                return SaveContent();
            }
        } else {
            int val = fileChooser.showSaveDialog(null);
            if (val == JFileChooser.APPROVE_OPTION) {
                editFile = fileChooser.getSelectedFile();
                ModMainFrameTitle();
                return Save();
            }
        }
        return false;
    }

    private void SaveAs() {
        int val = fileChooser.showSaveDialog(null);
        if (val == JFileChooser.APPROVE_OPTION) {
            editFile = fileChooser.getSelectedFile();
            SaveContent();
        }
    }

    private void SetFont() {
        fontFrame.setLocationRelativeTo(this);
        fontFrame.ShowDialog(mainTextPane.getForeground(), mainTextPane.getFont());
    }

    private void Search() {
        lastSearchIndex = -1;
        searchFrame.pack();
        searchFrame.setLocationRelativeTo(this);
        searchFrame.setVisible(true);
    }

    private void LoadContent() {
        try {
            fileCode = GetFileCode(editFile);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(editFile), fileCode);
            mainTextPane.setText(null);
            mainTextPane.read(reader, null);
            reader.close();
            ModMainFrameTitle();
            mainTextPane.select(0, 0);
            InitUndoManager();
        } catch (Exception e) {
            ExceptionHandle(e, "读取文件失败，请检查文件是否存在或者被占用！");
        }
    }

    private boolean SaveContent() {
        ModMainFrameTitle();
        boolean tag = false;
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(editFile), fileCode);
            mainTextPane.write(writer);
            writer.close();
            tag = true;
            isSaved = true;
        } catch (Exception e) {
            ExceptionHandle(e, "保存文件失败，请确认路径是否有效或文件是否被占用！");
        }
        return tag;
    }

    private void SearchOrReplace(String searchText, String replaceText, boolean isReplace) {
        int index = mainTextPane.getText().replace("\r\n", "\n")
                .indexOf(searchText, lastSearchIndex + 1);
        if (index != -1) {
            mainTextPane.requestFocus();
            if (isReplace) {
                mainTextPane.setText(mainTextPane.getText().replaceFirst(searchText, replaceText));
                mainTextPane.select(index, index + replaceText.length());
            } else {
                mainTextPane.select(index, index + searchText.length());
            }
        } else {
            JOptionPane.showMessageDialog(searchFrame, "已达到文件尾，将从头开始查找。", "记事本", JOptionPane.PLAIN_MESSAGE);
            index = mainTextPane.getText().replace("\r\n", "\n")
                    .indexOf(searchText);
            if (index != -1) {
                mainTextPane.requestFocus();
                if (isReplace) {
                    mainTextPane.setText(mainTextPane.getText().replaceFirst(searchText, replaceText));
                    mainTextPane.select(index, index + replaceText.length());
                } else {
                    mainTextPane.select(index, index + searchText.length());
                }
            } else {
                JOptionPane.showMessageDialog(searchFrame, "没有找到指定文本", "记事本", JOptionPane.PLAIN_MESSAGE);
            }
        }
        lastSearchIndex = index;
    }

    private void ModMainFrameTitle() {
        if (editFile == null) {
            setTitle("记事本 - " + fileCode);
        } else {
            setTitle(editFile.getName() + " - " + fileCode);
        }
    }

    private void CloseWindowHandle() {
        if (!isSaved) {
            int result = JOptionPane
                    .showConfirmDialog(this, "当前文件未保存，是否保存？", "提示", JOptionPane.YES_NO_OPTION);
            if (result == 0) {
                if (Save()) System.exit(0);
            }
            if (result == 1) System.exit(0);
        } else {
            System.exit(0);
        }
    }

    private String GetFileCode(File file) {
        byte[] buf = new byte[2048];
        try {
            FileInputStream stream = new FileInputStream(file);
            UniversalDetector detector = new UniversalDetector(null);
            stream.read(buf);
            detector.handleData(buf);
            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            stream.close();
            if (encoding == null) {
                encoding = "GBK";
            }
            return encoding;
        } catch (Exception e) {
            ExceptionHandle(e, "文件编码读取失败，使用默认GBK编码进行读取！");
            return "GBK";
        }
    }

    public static void ExceptionHandle(Exception e, String msg) {
        JOptionPane.showMessageDialog(null, msg + "\n" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Helps to avoid using {@code @SuppressWarnings({"unchecked"})} when casting to a generic type.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T uncheckedCast(Object obj) {
        return (T) obj;
    }

}
