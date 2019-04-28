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


	// A dial�gus azon vez�rl�it melyekre sz�ks�g lesz az esem�nykezel�s sor�n
	// oszt�lyv�ltoz�k�nt defini�ljuk

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
		
		// A dial�gus c�m�nek be�ll�t�sa
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

		// A be�ll�t�sokat tartalmaz� panel gy�rt�sa
		JPanel settingPanel = createSettingPanel();

		// A gombokat tartalmaz� panel gy�rt�sa
		JPanel buttonPanel = createButtonPanel();

		// Az el�z� k�t panelt egy panelre rakjuk
		JPanel dialogPanel = createDialogPanel(settingPanel, buttonPanel);

		// A dialogPanelt r�rakjuk a dial�gusra
		getContentPane().add(dialogPanel);

		// A dial�gus megfelel� m�ret�nek be�ll�t�sa (a tartalmazott elemek alapj�n)
		pack();

		// A dial�gust a BookShopGUI-hoz k�pest rajzolja ki
		setLocationRelativeTo(gui.getWindow());

		// Dialogus megjelen�t�se
		setVisible(true);

	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();

		// A panel elrendez�se folytonos, k�z�pre igaz�tva
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		// Hozz�adjuk az ok gombot, �s figyel�nk r�
		buttonPanel.add(okButton);
		okButton.addActionListener(this);

		// Hozz�adjuk a cancel gombot, �s figyel�nk r�
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

		// A panel elrendez�se m�trix, 4 sor �s 2 oszlop, a cell�k egyforma m�ret�ek
		settingPanel.setLayout(new GridLayout(6, 2));

		// Az els� sor:
		settingPanel.add(new JLabel(Labels.set_working_directory));
		settingPanel.add(workingDirPanel);

		// m�sodik sor:
		settingPanel.add(new JLabel(Labels.set_git_repo_path));
		settingPanel.add(gitRepoPathPanel);
		// harmadik sor:
		settingPanel.add(new JLabel(Labels.set_http_address));
		settingPanel.add(httpAddressTextField);
		// negyedik sor:
		settingPanel.add(new JLabel(Labels.set_k_folds_number));
		settingPanel.add(kFoldsNumberSpinner);
		// �t�dik sor:
		settingPanel.add(new JLabel(Labels.set_c_value));
		settingPanel.add(cValueSpinner);

		return settingPanel;
	}

	private JPanel createDialogPanel(JPanel settingPanel, JPanel buttonPanel) {
		JPanel dialogPanel = new JPanel();

		// A panel elrendez�se BorderLayout
		dialogPanel.setLayout(new BorderLayout());

		// K�z�pen lesz a settingPanel
		dialogPanel.add(settingPanel, BorderLayout.CENTER);

		// Alul pedig a gombok
		dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

		return dialogPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (okButton == e.getSource()) {
			// Ha az OK gombot nyomt�k meg, akkor elmentj�k a be�ll�t�sokat.

			if (workingDirTextField.getText().isEmpty() || gitRepoPathTextField.getText().isEmpty()
					|| httpAddressTextField.getText().isEmpty()) {
				// Ha nem adt�k meg az �sszes adatot, akkor egy hiba�zenetet �runk ki egy
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
