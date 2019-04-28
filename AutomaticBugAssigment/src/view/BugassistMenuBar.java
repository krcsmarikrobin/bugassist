package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class BugassistMenuBar extends JMenuBar implements ActionListener {
	private BugassistGUI gui;
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -2979598914289076504L;

	public BugassistMenuBar(BugassistGUI gui) {
		super();
		 this.gui = gui;
		 // Három menüpontot gyártunk általánosan, a createMenuPoint metódussal
	        createMenuPoint(Labels.modell_build, Labels.parameters_setting, Labels.collect_data_and_build_model, Labels.exit);		 
	}

	
	
	

    

    private void createMenuPoint(String name, String... subnames) {
        // Létrehozunk egy menupontot az elsõ paraméter alapján
        JMenu menu = new JMenu(name);

        // A menupontot hozzáadjuk a bugassistMenuBar-hoz
        this.add(menu);

        // Az egyes menu itemeket a maradék paraméter értékeivel hozzuk létre
        for (String subname : subnames) {
            JMenuItem menuItem = new JMenuItem(subname);

            menu.add(menuItem);

            // Minden egyes menu itemet figyelünk
            // A menu itemek esetén a megfigyelést az ActionListener interfész
            // biztosítja, ezért a menubar implementálja ezt az interfészt és
            // felülírja az actionPerformed metódust
            menuItem.addActionListener(this);
        }
    }

    /*
     * Az interfészekhez tartozó metódusokat célszerû generálni, azután, hogy
     * megadtuk az implements kulcsó után az interfészt,
     * Jobb klikk + Source + Override/Implement 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if (actionCommand.equals(Labels.parameters_setting)) {
           
            new SetParameterDialog(gui, true);
        } else if (actionCommand.equals(Labels.collect_data_and_build_model)) {        	
        	new CollectDataAndBuildModelDialog(gui, true);
        	
        } else if (actionCommand.equals(Labels.exit)) {
        	System.exit(0);
        }
    }

	
	
	
	
	


}
