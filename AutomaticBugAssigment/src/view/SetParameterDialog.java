package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class SetParameterDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2242760784249357116L;
	private BugassistGUI gui;

	private String workingDir;
	private String gitRepoPath;
	private String httpAddress;

	private int kFoldsNumber;
	private double cValue;


	// A dialógus azon vezérlõit melyekre szükség lesz az eseménykezelés során
	// osztályváltozóként definiáljuk

	private JTextField workingDirTextField;
	private JTextField gitRepoPathTextField;
	private JTextField httpAddressTextField;
	private JSpinner kFoldsNumberSpinner;
	private JSpinner cValueSpinner;

	private JButton workingDirFileChooserButton = new JButton(Labels.three_point);
	private JButton gitRepoPathFileChooserButton = new JButton(Labels.three_point);

	private JButton okButton = new JButton(Labels.ok);
	private JButton cancelButton = new JButton(Labels.cancel);

	public SetParameterDialog(BugassistGUI gui, boolean modal) {
		
		super(gui.getWindow(), modal);
		this.gui = gui;
		
		workingDir = gui.getController().getConfigFile().getWorkingDir();
		gitRepoPath = gui.getController().getConfigFile().getGitRepoPath();
		httpAddress = gui.getController().getConfigFile().gethttpAddress();
		kFoldsNumber = gui.getController().getConfigFile().getKFoldsNumber();
		cValue = gui.getController().getConfigFile().getCValue();
		
		// A dialógus címének beállítása
		this.setTitle(Labels.parameters_setting);

		workingDirTextField = new JTextField(workingDir, 20);
		workingDirTextField.setEditable(false);
		gitRepoPathTextField = new JTextField(gitRepoPath, 20);
		gitRepoPathTextField.setEditable(false);
		httpAddressTextField = new JTextField(httpAddress, 20);
		// from 0 to 9, in 1.0 steps start value 5:
		SpinnerNumberModel modelKFoldsNumberSpinner = new SpinnerNumberModel(kFoldsNumber, 0, 20, 1);
		kFoldsNumberSpinner = new JSpinner(modelKFoldsNumberSpinner);
		
		SpinnerNumberModel modelcValueSpinner = new SpinnerNumberModel(cValue, 0.0001, 1000.0, 1.0);
		cValueSpinner = new JSpinner(modelcValueSpinner);

		workingDirFileChooserButton.addActionListener(this);
		gitRepoPathFileChooserButton.addActionListener(this);

		// A beállításokat tartalmazó panel gyártása
		JPanel settingPanel = createSettingPanel();

		// A gombokat tartalmazó panel gyártása
		JPanel buttonPanel = createButtonPanel();

		// Az elõzõ két panelt egy panelre rakjuk
		JPanel dialogPanel = createDialogPanel(settingPanel, buttonPanel);

		// A dialogPanelt rárakjuk a dialógusra
		getContentPane().add(dialogPanel);

		// A dialógus megfelelõ méretének beállítása (a tartalmazott elemek alapján)
		pack();

		// A dialógust a BookShopGUI-hoz képest rajzolja ki
		setLocationRelativeTo(gui.getWindow());

		// Dialogus megjelenítése
		setVisible(true);

	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();

		// A panel elrendezése folytonos, középre igazítva
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		// Hozzáadjuk az ok gombot, és figyelünk rá
		buttonPanel.add(okButton);
		okButton.addActionListener(this);

		// Hozzáadjuk a cancel gombot, és figyelünk rá
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(this);

		return buttonPanel;
	}

	private JPanel createSettingPanel() {
		JPanel settingPanel = new JPanel();

		JPanel workingDirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		workingDirPanel.add(workingDirTextField);
		workingDirPanel.add(workingDirFileChooserButton);

		JPanel gitRepoPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		gitRepoPathPanel.add(gitRepoPathTextField);
		gitRepoPathPanel.add(gitRepoPathFileChooserButton);

		// A panel elrendezése mátrix, 4 sor és 2 oszlop, a cellák egyforma méretûek
		settingPanel.setLayout(new GridLayout(6, 2));

		// Az elsõ sor:
		settingPanel.add(new JLabel(Labels.set_working_directory));
		settingPanel.add(workingDirPanel);

		// második sor:
		settingPanel.add(new JLabel(Labels.set_git_repo_path));
		settingPanel.add(gitRepoPathPanel);
		// harmadik sor:
		settingPanel.add(new JLabel(Labels.set_http_address));
		settingPanel.add(httpAddressTextField);
		// negyedik sor:
		settingPanel.add(new JLabel(Labels.set_k_folds_number));
		settingPanel.add(kFoldsNumberSpinner);
		// ötödik sor:
		settingPanel.add(new JLabel(Labels.set_c_value));
		settingPanel.add(cValueSpinner);

		return settingPanel;
	}

	private JPanel createDialogPanel(JPanel settingPanel, JPanel buttonPanel) {
		JPanel dialogPanel = new JPanel();

		// A panel elrendezése BorderLayout
		dialogPanel.setLayout(new BorderLayout());

		// Középen lesz a settingPanel
		dialogPanel.add(settingPanel, BorderLayout.CENTER);

		// Alul pedig a gombok
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

		return dialogPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (okButton == e.getSource()) {
			// Ha az OK gombot nyomták meg, akkor elmentjük a beállításokat.

			if (workingDirTextField.getText().isEmpty() || gitRepoPathTextField.getText().isEmpty()
					|| httpAddressTextField.getText().isEmpty()) {
				// Ha nem adták meg az összes adatot, akkor egy hibaüzenetet írunk ki egy
				// error dialogra (JOptionPane.ERROR_MESSAGE)
				JOptionPane.showMessageDialog(gui.getWindow(), Labels.all_field_fill_must, Labels.error,
						JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				gui.getController().getConfigFile().setWorkingDir(workingDirTextField.getText());
				gui.getController().getConfigFile().setGitRepoPath(gitRepoPathTextField.getText());
				gui.getController().getConfigFile().setHttpAddress(httpAddressTextField.getText());
				gui.getController().getConfigFile().setKFoldsNumber((int) kFoldsNumberSpinner.getValue());
				gui.getController().getConfigFile().setCValue((double) cValueSpinner.getValue());
				gui.getController().getConfigFile().saveData();
				this.dispose();
			}
		} else if (cancelButton == e.getSource()) {
			this.dispose();
		} else if (workingDirFileChooserButton == e.getSource()) {
			String path = getDirPath();
			if (path != null)
			workingDirTextField.setText(path);
		} else if (gitRepoPathFileChooserButton == e.getSource()) {
			String path = getDirPath();
			if (path != null)
			gitRepoPathTextField.setText(path);
		}
	}

	

	private String getDirPath() {

		JFileChooser dirFileChooser = new JFileChooser();

		dirFileChooser.setCurrentDirectory(new java.io.File("."));
		dirFileChooser.setDialogTitle(Labels.set_directory);
		dirFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// disable the "All files" option.
		dirFileChooser.setAcceptAllFileFilterUsed(false);
		dirFileChooser.showOpenDialog(this);
		if (dirFileChooser.getSelectedFile() != null)
		return dirFileChooser.getSelectedFile().getAbsolutePath();
		else return null;
	}

}
