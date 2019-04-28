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
		 // H�rom men�pontot gy�rtunk �ltal�nosan, a createMenuPoint met�dussal
	        createMenuPoint(Labels.modell_build, Labels.parameters_setting, Labels.collect_data_and_build_model, Labels.exit);		 
	}

	
	
	

    

    private void createMenuPoint(String name, String... subnames) {
        // L�trehozunk egy menupontot az els� param�ter alapj�n
        JMenu menu = new JMenu(name);

        // A menupontot hozz�adjuk a bugassistMenuBar-hoz
        this.add(menu);

        // Az egyes menu itemeket a marad�k param�ter �rt�keivel hozzuk l�tre
        for (String subname : subnames) {
            JMenuItem menuItem = new JMenuItem(subname);

            menu.add(menuItem);

            // Minden egyes menu itemet figyel�nk
            // A menu itemek eset�n a megfigyel�st az ActionListener interf�sz
            // biztos�tja, ez�rt a menubar implement�lja ezt az interf�szt �s
            // fel�l�rja az actionPerformed met�dust
            menuItem.addActionListener(this);
        }
    }

    /*
     * Az interf�szekhez tartoz� met�dusokat c�lszer� gener�lni, azut�n, hogy
     * megadtuk az implements kulcs� ut�n az interf�szt,
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
