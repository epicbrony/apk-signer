/*
 *    Copyright (C) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.desktop.app.apksigner;

import group.pals.desktop.app.apksigner.i18n.Messages;
import group.pals.desktop.app.apksigner.i18n.R;
import group.pals.desktop.app.apksigner.utils.Files;
import group.pals.desktop.app.apksigner.utils.Files.JFileChooserEx;
import group.pals.desktop.app.apksigner.utils.KeyTools;
import group.pals.desktop.app.apksigner.utils.Preferences;
import group.pals.desktop.app.apksigner.utils.Signer;
import group.pals.desktop.app.apksigner.utils.Texts;
import group.pals.desktop.app.apksigner.utils.ui.Dlg;
import group.pals.desktop.app.apksigner.utils.ui.FileDrop;
import group.pals.desktop.app.apksigner.utils.ui.JEditorPopupMenu;
import group.pals.desktop.app.apksigner.utils.ui.UI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

/**
 * Panel to sign APK files.
 * 
 * @author Hai Bison
 * @since v1.6 beta
 */
public class PanelSigner extends JPanel {

    /**
     * Auto-generated by Eclipse.
     */
    private static final long serialVersionUID = -874904794558103202L;

    /**
     * The class name.
     */
    private static final String CLASSNAME = PanelSigner.class.getName();

    /**
     * This key holds the last working directory.
     */
    private static final String PKEY_LAST_WORKING_DIR = CLASSNAME
            + ".last_working_dir";

    /**
     * This key holds the last target file filter's ID.
     */
    private static final String PKEY_LAST_TARGET_FILE_FILTER_ID = CLASSNAME
            + ".last_target_file_filter_ID";

    /**
     * Target file filters.
     */
    private static final List<FileFilter> TARGET_FILE_FILTERS = Arrays.asList(
            Files.newFileFilter(JFileChooser.FILES_ONLY, Texts.REGEX_APK_FILES,
                    Messages.getString(R.string.desc_apk_files)), Files
                    .newFileFilter(JFileChooser.FILES_ONLY,
                            Texts.REGEX_JAR_FILES,
                            Messages.getString(R.string.desc_jar_files)), Files
                    .newFileFilter(JFileChooser.FILES_ONLY,
                            Texts.REGEX_ZIP_FILES,
                            Messages.getString(R.string.desc_zip_files)));

    /**
     * Delay time to load keystore's aliases after the user stopped typying the
     * keystore's password, in milliseconds.
     */
    private static final int DELAY_TIME_TO_LOAD_KEY_ALIASES = 499;

    /*
     * FIELDS
     */

    private File mKeyfile;
    private File mTargetFile;
    private Timer mKeyFileAliasesLoader;

    /*
     * CONTROLS
     */

    private JPasswordField mTextPassword;
    @SuppressWarnings("rawtypes")
    private JComboBox mCbxAlias;
    private JPasswordField mTextAliasPassword;
    private JPanel mPanelKeyFile;
    private JButton mBtnChooseKeyfile;
    private JPanel mPanelTargetFile;
    private JButton mBtnChooseTargetFile;
    private JButton mBtnSign;
    private JPanel panel;

    /**
     * Create the panel.
     */
    @SuppressWarnings({ "rawtypes" })
    public PanelSigner() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                Double.MIN_VALUE };
        setLayout(gridBagLayout);

        mPanelKeyFile = new JPanel();
        GridBagConstraints gbc_mPanelKeyFile = new GridBagConstraints();
        gbc_mPanelKeyFile.fill = GridBagConstraints.BOTH;
        gbc_mPanelKeyFile.insets = new Insets(10, 3, 5, 3);
        gbc_mPanelKeyFile.gridx = 0;
        gbc_mPanelKeyFile.gridy = 0;
        add(mPanelKeyFile, gbc_mPanelKeyFile);
        GridBagLayout gbl_mPanelKeyFile = new GridBagLayout();
        gbl_mPanelKeyFile.columnWidths = new int[] { 0, 0 };
        gbl_mPanelKeyFile.rowHeights = new int[] { 0, 0 };
        gbl_mPanelKeyFile.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_mPanelKeyFile.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        mPanelKeyFile.setLayout(gbl_mPanelKeyFile);
        new FileDrop(mPanelKeyFile, UI.BORDER_FILE_DROP,
                mCompKeyFileFileDropListener);

        mBtnChooseKeyfile = new JButton(
                Messages.getString(R.string.desc_load_key_file));
        GridBagConstraints gbc_mBtnChooseKeyfile = new GridBagConstraints();
        gbc_mBtnChooseKeyfile.gridx = 0;
        gbc_mBtnChooseKeyfile.gridy = 0;
        mPanelKeyFile.add(mBtnChooseKeyfile, gbc_mBtnChooseKeyfile);
        mBtnChooseKeyfile.addActionListener(mBtnChooseKeyfileActionListener);
        new FileDrop(mBtnChooseKeyfile, BorderFactory.createCompoundBorder(
                UI.BORDER_FILE_DROP, mBtnChooseKeyfile.getBorder()),
                mCompKeyFileFileDropListener);

        mTextPassword = new JPasswordField();
        mTextPassword
                .addPropertyChangeListener(mTextPasswordPropertyChangeListener);
        mTextPassword.addKeyListener(mTextPasswordKeyAdapter);
        mTextPassword.setHorizontalAlignment(SwingConstants.CENTER);
        mTextPassword.setBorder(new TitledBorder(null, Messages
                .getString(R.string.password), TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        GridBagConstraints gbc_mTextPassword = new GridBagConstraints();
        gbc_mTextPassword.insets = new Insets(3, 3, 5, 3);
        gbc_mTextPassword.fill = GridBagConstraints.HORIZONTAL;
        gbc_mTextPassword.gridx = 0;
        gbc_mTextPassword.gridy = 1;
        add(mTextPassword, gbc_mTextPassword);

        panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.insets = new Insets(3, 3, 5, 3);
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 2;
        add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] { 0, 0 };
        gbl_panel.rowHeights = new int[] { 0, 0 };
        gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        panel.setLayout(gbl_panel);
        panel.setBorder(new TitledBorder(null, Messages
                .getString(R.string.alias), TitledBorder.LEADING,
                TitledBorder.TOP, null, null));

        mCbxAlias = new JComboBox();
        mCbxAlias.setMinimumSize(new Dimension(199, 24));
        mCbxAlias.addItemListener(mCbxAliasItemListener);
        GridBagConstraints gbc_mCbxAlias = new GridBagConstraints();
        gbc_mCbxAlias.gridx = 0;
        gbc_mCbxAlias.gridy = 0;
        panel.add(mCbxAlias, gbc_mCbxAlias);
        mCbxAlias.setEditable(true);

        mTextAliasPassword = new JPasswordField();
        mTextAliasPassword.setHorizontalAlignment(SwingConstants.CENTER);
        mTextAliasPassword.setBorder(new TitledBorder(null, Messages
                .getString(R.string.alias_password), TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        GridBagConstraints gbc_mTextAliasPassword = new GridBagConstraints();
        gbc_mTextAliasPassword.insets = new Insets(3, 3, 5, 3);
        gbc_mTextAliasPassword.fill = GridBagConstraints.HORIZONTAL;
        gbc_mTextAliasPassword.gridx = 0;
        gbc_mTextAliasPassword.gridy = 3;
        add(mTextAliasPassword, gbc_mTextAliasPassword);

        mPanelTargetFile = new JPanel();
        GridBagConstraints gbc_mPanelTargetFile = new GridBagConstraints();
        gbc_mPanelTargetFile.fill = GridBagConstraints.BOTH;
        gbc_mPanelTargetFile.insets = new Insets(3, 3, 5, 3);
        gbc_mPanelTargetFile.gridx = 0;
        gbc_mPanelTargetFile.gridy = 4;
        add(mPanelTargetFile, gbc_mPanelTargetFile);
        GridBagLayout gbl_mPanelTargetFile = new GridBagLayout();
        gbl_mPanelTargetFile.columnWidths = new int[] { 0, 0 };
        gbl_mPanelTargetFile.rowHeights = new int[] { 0, 0 };
        gbl_mPanelTargetFile.columnWeights = new double[] { 1.0,
                Double.MIN_VALUE };
        gbl_mPanelTargetFile.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        mPanelTargetFile.setLayout(gbl_mPanelTargetFile);
        new FileDrop(mPanelTargetFile, UI.BORDER_FILE_DROP,
                mCompTargetFileFileDropListener);

        mBtnChooseTargetFile = new JButton(
                Messages.getString(R.string.desc_load_target_file));
        GridBagConstraints gbc_mBtnChooseTargetFile = new GridBagConstraints();
        gbc_mBtnChooseTargetFile.gridx = 0;
        gbc_mBtnChooseTargetFile.gridy = 0;
        mPanelTargetFile.add(mBtnChooseTargetFile, gbc_mBtnChooseTargetFile);
        mBtnChooseTargetFile
                .addActionListener(mBtnChooseTargetFileActionListener);
        new FileDrop(mBtnChooseTargetFile, BorderFactory.createCompoundBorder(
                UI.BORDER_FILE_DROP, mBtnChooseTargetFile.getBorder()),
                mCompTargetFileFileDropListener);

        mBtnSign = new JButton(Messages.getString(R.string.sign));
        mBtnSign.addActionListener(mBtnSignActionListener);
        GridBagConstraints gbc_mBtnSign = new GridBagConstraints();
        gbc_mBtnSign.insets = new Insets(10, 10, 10, 10);
        gbc_mBtnSign.gridx = 0;
        gbc_mBtnSign.gridy = 5;
        add(mBtnSign, gbc_mBtnSign);

        JEditorPopupMenu.apply(this);
    }// PanelSigner()

    /**
     * Creates new {@link Timer} which automatically schedules a
     * {@link TimerTask} to load current keystore's aliases.
     * 
     * @return the timer.
     */
    private Timer createAndScheduleKeyAliasesLoader() {
        final boolean[] cancelled = { false };

        TimerTask timerTask = new TimerTask() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                if (mKeyfile != null && mKeyfile.isFile() && mKeyfile.canRead()) {
                    char[] pwd = mTextPassword.getPassword();
                    if (pwd != null && pwd.length > 0) {
                        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<Object>(
                                new Object[] { "" });

                        for (String keystoreType : new String[] {
                                KeyTools.KEYSTORE_TYPE_JKS,
                                KeyTools.KEYSTORE_TYPE_JCEKS,
                                KeyTools.KEYSTORE_TYPE_PKCS12 }) {
                            final List<String> aliases = KeyTools.getAliases(
                                    mKeyfile, keystoreType, pwd);

                            for (final String alias : aliases) {
                                model.addElement(new Object() {

                                    @Override
                                    public String toString() {
                                        return alias;
                                    }// toString()
                                });
                            }// for

                            if (!aliases.isEmpty())
                                break;
                        }// for

                        if (!cancelled[0]) {
                            mCbxAlias.setModel(model);
                            if (model.getSize() > 1)
                                mCbxAlias.setSelectedIndex(1);
                        }
                    }// if
                }// if
            }// run()
        };

        Timer timer = new Timer() {

            @Override
            public void cancel() {
                cancelled[0] = true;
                super.cancel();
            }// cancel()
        };
        timer.schedule(timerTask, DELAY_TIME_TO_LOAD_KEY_ALIASES);
        return timer;
    }// createAndScheduleKeyAliasesLoader()

    /**
     * Sets key file.
     * 
     * @param file
     *            the key file, can be {@code null}.
     */
    private void setKeyFile(File file) {
        if (file != null && file.isFile()) {
            mKeyfile = file;

            mBtnChooseKeyfile.setText(mKeyfile.getName());
            mBtnChooseKeyfile.setForeground(UI.COLOUR_SELECTED_FILE);
            Preferences.getInstance().set(PKEY_LAST_WORKING_DIR,
                    mKeyfile.getParentFile().getAbsolutePath());

            if (mKeyFileAliasesLoader != null)
                mKeyFileAliasesLoader.cancel();
            mKeyFileAliasesLoader = createAndScheduleKeyAliasesLoader();

            mTextPassword.requestFocus();
        } else {
            mKeyfile = null;

            mBtnChooseKeyfile.setText(Messages
                    .getString(R.string.desc_load_key_file));
            mBtnChooseKeyfile.setForeground(UI.COLOUR_WAITING_CMD);
        }
    }// setKeyFile()

    /**
     * Sets target file.
     * 
     * @param file
     *            the target file.
     */
    private void setTargetFile(File file) {
        if (file != null && file.isFile()) {
            mTargetFile = file;

            mBtnChooseTargetFile.setText(mTargetFile.getName());
            mBtnChooseTargetFile.setForeground(UI.COLOUR_SELECTED_FILE);
            Preferences.getInstance().set(PKEY_LAST_WORKING_DIR,
                    mTargetFile.getParentFile().getAbsolutePath());

            mBtnSign.requestFocus();
        } else {
            mTargetFile = null;

            mBtnChooseTargetFile.setText(Messages
                    .getString(R.string.desc_load_target_file));
            mBtnChooseTargetFile.setForeground(UI.COLOUR_WAITING_CMD);
        }
    }// setTargetFile()

    /**
     * Validates all fields.
     * 
     * @return {@code true} or {@code false}.
     */
    private boolean validateFields() {
        if (mKeyfile == null || !mKeyfile.isFile() || !mKeyfile.canRead()) {
            Dlg.showErrMsg(Messages
                    .getString(R.string.msg_keyfile_doesnt_exist));
            mBtnChooseKeyfile.requestFocus();
            return false;
        }

        if (mTextPassword.getPassword() == null
                || mTextPassword.getPassword().length == 0) {
            Dlg.showErrMsg(Messages.getString(R.string.msg_password_is_empty));
            mTextPassword.requestFocus();
            return false;
        }

        if (Texts.isEmpty(String.valueOf(mCbxAlias.getSelectedItem()))) {
            Dlg.showErrMsg(Messages.getString(R.string.msg_alias_is_empty));
            mCbxAlias.requestFocus();
            return false;
        }

        if (mTextAliasPassword.getPassword() == null
                || mTextAliasPassword.getPassword().length == 0) {
            Dlg.showErrMsg(Messages
                    .getString(R.string.msg_alias_password_is_empty));
            mTextAliasPassword.requestFocus();
            return false;
        }

        if (mTargetFile == null || !mTargetFile.isFile()
                || !mTargetFile.canWrite()) {
            Dlg.showInfoMsg(Messages
                    .getString(R.string.msg_load_a_file_to_sign));
            mBtnChooseTargetFile.requestFocus();
            return false;
        }

        return true;
    }// validateFields()

    /**
     * Signs the target file.
     * <p>
     * <b>Notes:</b> You should call {@link #validateFields()} first.
     * </p>
     */
    private void signTargetFile() {
        try {
            String info = Signer.sign(Preferences.getInstance().getJdkPath(),
                    mTargetFile, mKeyfile, mTextPassword.getPassword(),
                    String.valueOf(mCbxAlias.getSelectedItem()),
                    mTextAliasPassword.getPassword());
            /*
             * TODO JDK 7 shows a warning for missing option `-tsa` (Timestamp
             * Authorization).
             */
            if (Texts.isEmpty(info) || info.matches("(?sim)^jar signed.+"))
                Dlg.showInfoMsg(Messages.getString(R.string.msg_file_is_signed));
            else
                Dlg.showErrMsg(Messages.getString(
                        R.string.pmsg_error_signing_file, info));
        } catch (Exception e) {
            Dlg.showErrMsg(Messages.getString(R.string.pmsg_error_signing_file,
                    e));
        }
    }// signTargetFile()

    /*
     * LISTENERS
     */

    private final FileDrop.Listener mCompKeyFileFileDropListener = new FileDrop.Listener() {

        @Override
        public void onFilesDropped(File[] files) {
            setKeyFile(files[0]);
        }// onFilesDropped()
    };// mCompKeyFileFileDropListener

    private final ActionListener mBtnChooseKeyfileActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            setKeyFile(Files.chooseFile(
                    new File(Preferences.getInstance().get(
                            PKEY_LAST_WORKING_DIR, "/")),
                    Texts.REGEX_KEYSTORE_FILES,
                    Messages.getString(R.string.desc_keystore_files)));
        }// actionPerformed()
    };// mBtnChooseKeyfileActionListener

    private final PropertyChangeListener mTextPasswordPropertyChangeListener = new PropertyChangeListener() {

        final String[] mActionNames = {
                JEditorPopupMenu.ACTION_NAME_CLEAR_AND_PASTE,
                JEditorPopupMenu.ACTION_NAME_CUT,
                JEditorPopupMenu.ACTION_NAME_DELETE,
                JEditorPopupMenu.ACTION_NAME_PASTE };
        Timer mTimer;

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            for (String action : mActionNames) {
                if (action.equals(e.getPropertyName())) {
                    if (mTimer != null)
                        mTimer.cancel();
                    mTimer = createAndScheduleKeyAliasesLoader();
                    break;
                }
            }
        }// propertyChange()
    };// mTextPasswordPropertyChangeListener

    private final KeyAdapter mTextPasswordKeyAdapter = new KeyAdapter() {

        Timer mTimer;

        @Override
        public void keyTyped(KeyEvent e) {
            if (mTimer != null)
                mTimer.cancel();
            mTimer = createAndScheduleKeyAliasesLoader();
        }// keyTyped()
    };// mTextPasswordKeyAdapter

    private final ItemListener mCbxAliasItemListener = new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (mCbxAlias.getSelectedItem() != null
                    && !Texts.isEmpty(mCbxAlias.getSelectedItem().toString()))
                mTextAliasPassword.requestFocus();
        }// itemStateChanged()
    };// mCbxAliasItemListener

    private final FileDrop.Listener mCompTargetFileFileDropListener = new FileDrop.Listener() {

        @Override
        public void onFilesDropped(File[] files) {
            setTargetFile(files[0]);
        }// onFilesDropped()
    };// mCompTargetFileFileDropListener

    private final ActionListener mBtnChooseTargetFileActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            final String lastFileFilterId = Preferences.getInstance().get(
                    PKEY_LAST_TARGET_FILE_FILTER_ID);
            final JFileChooserEx fileChooser = new JFileChooserEx(new File(
                    Preferences.getInstance().get(PKEY_LAST_WORKING_DIR, "/")));
            fileChooser
                    .setDialogTitle(Messages.getString(R.string.choose_file));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            for (int i = 0; i < TARGET_FILE_FILTERS.size(); i++) {
                FileFilter filter = TARGET_FILE_FILTERS.get(i);
                fileChooser.addChoosableFileFilter(filter);
                if ((Texts.isEmpty(lastFileFilterId) && i == 0)
                        || Integer.toString(i).equals(lastFileFilterId))
                    fileChooser.setFileFilter(filter);
            }

            switch (fileChooser.showOpenDialog(null)) {
            case JFileChooser.APPROVE_OPTION: {
                setTargetFile(fileChooser.getSelectedFile());
                Preferences.getInstance().set(
                        PKEY_LAST_TARGET_FILE_FILTER_ID,
                        Integer.toString(TARGET_FILE_FILTERS
                                .indexOf(fileChooser.getFileFilter())));
                break;
            }// APPROVE_OPTION

            default: {
                setTargetFile(null);
                break;
            }// default
            }
        }// actionPerformed()
    };// mBtnChooseTargetFileActionListener

    private final ActionListener mBtnSignActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (validateFields())
                signTargetFile();
        }// actionPerformed()
    };// mBtnSignActionListener

}
