package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

public class CollectDataAndBuildModelDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -1648146044213314023L;

	private BugassistGUI gui;

	private JCheckBox chckbxGitCollect;
	private JCheckBox chckbxHttpCollect;
	private JCheckBox chckbxCollectResults;
	private JButton okButton;
	private JButton cancelButton;	
	private JTextArea textArea;
	private String consoleText = "";

	JCheckBox chckbxRankingSvmCompute;
	JCheckBox chckbxClassification;

	public CollectDataAndBuildModelDialog(BugassistGUI gui, Boolean modal) {

		//super(gui.getWindow(), modal);
		this.gui = gui;

		setTitle(Labels.collect_data);

		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 600);

		JPanel checkBoxCollectDataPanel = new JPanel();
		checkBoxCollectDataPanel.setBounds(10, 0, 774, 81);
		checkBoxCollectDataPanel.setLayout(null);
		getContentPane().setLayout(null);
		getContentPane().add(checkBoxCollectDataPanel);

		chckbxGitCollect = new JCheckBox(Labels.collect_git_data_from_repo);
		chckbxGitCollect.setBounds(6, 28, 401, 23);
		checkBoxCollectDataPanel.add(chckbxGitCollect);

		chckbxHttpCollect = new JCheckBox(Labels.collect_http_bug_data);
		chckbxHttpCollect.setBounds(6, 54, 434, 23);
		checkBoxCollectDataPanel.add(chckbxHttpCollect);

		JLabel lblDataCollect = new JLabel(Labels.collect_data);
		lblDataCollect.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblDataCollect.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDataCollect.setBounds(6, 7, 758, 14);
		checkBoxCollectDataPanel.add(lblDataCollect);

		JPanel checkBoxModelCreatePanel = new JPanel();
		checkBoxModelCreatePanel.setBounds(10, 92, 774, 110);
		checkBoxModelCreatePanel.setLayout(null);
		getContentPane().add(checkBoxModelCreatePanel);

		
		chckbxRankingSvmCompute = new JCheckBox(Labels.ranking_svm_model_compute);
		chckbxRankingSvmCompute.setBounds(6, 28, 424, 23);
		checkBoxModelCreatePanel.add(chckbxRankingSvmCompute);

		chckbxClassification = new JCheckBox(Labels.classification_run);
		chckbxClassification.setBounds(6, 54, 424, 23);
		checkBoxModelCreatePanel.add(chckbxClassification);

		JLabel lblModelCompute = new JLabel(Labels.model_compute);
		lblModelCompute.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblModelCompute.setHorizontalAlignment(SwingConstants.RIGHT);
		lblModelCompute.setBounds(6, 7, 758, 14);
		checkBoxModelCreatePanel.add(lblModelCompute);
		
		chckbxCollectResults = new JCheckBox(Labels.collect_results);
		chckbxCollectResults.setSelected(true);
		chckbxCollectResults.setBounds(6, 80, 424, 23);
		checkBoxModelCreatePanel.add(chckbxCollectResults);

		JPanel buttonPane = new JPanel();
		buttonPane.setBounds(10, 213, 774, 33);
		getContentPane().add(buttonPane);

		okButton = new JButton(Labels.execute);
		okButton.setBounds(10, 0, 81, 23);
		buttonPane.add(okButton);
		okButton.setActionCommand(Labels.execute);
		okButton.addActionListener(this);
		getRootPane().setDefaultButton(okButton);
		
				cancelButton = new JButton(Labels.cancel);
				cancelButton.setBounds(129, 0, 80, 23);
				buttonPane.add(cancelButton);
				cancelButton.setActionCommand(Labels.cancel);
				cancelButton.addActionListener(this);

		JPanel textPanel = new JPanel();
		textPanel.setBounds(10, 257, 774, 303);
		textPanel.setLayout(null);
		getContentPane().add(textPanel);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 774, 303);
		textPanel.add(scrollPane);

		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setEnabled(false);
		textArea.setEditable(false);
		textArea.setText(consoleText);
		
		this.setVisible(true);
		
		
	}

	public void writeLineToConsole(String line) {
		textArea.append(line + "\n");
	}

	public void cleanConsole() {
		textArea.setText("");
	}
	
	public void setEnabledStatus(Boolean bool) {
		
		chckbxGitCollect.setEnabled(bool);
		chckbxHttpCollect.setEnabled(bool);
		chckbxClassification.setEnabled(bool);
		chckbxRankingSvmCompute.setEnabled(bool);
		okButton.setEnabled(bool);
		
	}

	public void actionPerformed(ActionEvent e) {

		if (okButton == e.getSource()) {
			Long runtime = System.currentTimeMillis();
			cleanConsole();
			setEnabledStatus(false);		

			if (chckbxGitCollect.isSelected()) {
				long a = System.currentTimeMillis();
				gui.getController().runCollectGitRepoData();
				a = (System.currentTimeMillis() - a) / 1000;
				writeLineToConsole(Labels.message_collect_bug_git_data_finished);
				a = (System.currentTimeMillis() - a) / 1000;
				writeLineToConsole(Labels.message_running_time + (int) a / 60 + "min " + (int) a % 60 + "sec"); 
				chckbxGitCollect.setSelected(false);
			}
			if (chckbxHttpCollect.isSelected()) {
				cancelButton.setEnabled(true);
				long a = System.currentTimeMillis();
				gui.getController().runCollecHttpBugData();
				a = (System.currentTimeMillis() - a) / 1000;
				writeLineToConsole(Labels.message_collect_bug_http_data_finished);
				writeLineToConsole(Labels.message_running_time + (int) a / 60 + "min " + (int) a % 60 + "sec");
				chckbxHttpCollect.setSelected(false);
			}
			if (chckbxRankingSvmCompute.isSelected()) {
				long a = System.currentTimeMillis();
				gui.getController().runRankingSVMModelCompute();
				a = (System.currentTimeMillis() - a) / 1000;
				writeLineToConsole(Labels.message_ranking_SVM_model_compute_finished);
				writeLineToConsole(Labels.message_running_time + (int) a / 60 + "min " + (int) a % 60 + "sec"); //~92 min
				chckbxRankingSvmCompute.setSelected(false);
				
			}
			if (chckbxClassification.isSelected()) {
				long a = System.currentTimeMillis();
				gui.getController().runClassification();
				a = (System.currentTimeMillis() - a) / 1000;
				writeLineToConsole(Labels.classification_finished);
				writeLineToConsole(Labels.message_running_time + (int) a / 60 + "min " + (int) a % 60 + "sec"); //~ 1 min
				chckbxClassification.setSelected(false);
				cancelButton.setEnabled(true);
			}
			if (chckbxCollectResults.isSelected()) {
				long a = System.currentTimeMillis();
				writeLineToConsole(gui.getController().runCollectResults(20));
				writeLineToConsole(Labels.collect_result_finished);
				a = (System.currentTimeMillis() - a) / 1000;
				writeLineToConsole(Labels.message_running_time + (int) a / 60 + "min " + (int) a % 60 + "sec"); // ~ 3 min
				chckbxCollectResults.setSelected(false);
			}

			writeLineToConsole(Labels.message_ready);
			runtime = (System.currentTimeMillis() - runtime) / 1000;
			writeLineToConsole(Labels.complete_running_time + (int)(runtime / 60) + "min " + (int)(runtime % 60) + "sec"); //~ 93 min
			setEnabledStatus(true);

		} else if (cancelButton == e.getSource()) {
			this.dispose();
		}

	}
}
