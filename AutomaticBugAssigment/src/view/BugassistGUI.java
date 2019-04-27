package view;

import java.awt.Container;

import javax.swing.JFrame;

import controller.BugassistController;

public class BugassistGUI {
	
	private JFrame window;
	private BugassistController controller;
	
	public BugassistGUI(BugassistController controller) {
		this.controller = controller;
	}
	
	public void startGUI() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
            public void run() {
                createAndShowGUI();
            }
        });
	}

	protected void createAndShowGUI() {
		
		
	        // Elõállítjuk az alkalmazás címsorát
	       
	        String title = String.format("Bugassist");

	        // A JFrame egy magas szintû konténer, egy ablak címmel és kerettel.
	        window = new JFrame(title);

	        // Ha bezárjuk az ablakot, akkor alapértelmezésben azt csak elrejtjuk.
	        // Ezt a viselkedést módosítjuk arra, hogy az ablak ténylegesen záródjon be.
	        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	        // Gyártunk egy bookMenuBar objektumot
	        BugassistMenuBar bugassistMenuBar = new BugassistMenuBar(this);

	        // Amit rárakunk a Book Shop ablakunkra
	        window.setJMenuBar(bugassistMenuBar);

	        // Az ablaknak beállítjuk a méretét
	        window.setSize(800,600);

	        // Készen vagyunk, megjeleníthetjük az ablakot
	        window.setVisible(true);

		
	}
	
	
	/**
     * A kapott containert beállítja a fõablak {@link JFrame} contentjeként.
     */
    public void setActualContent(Container container) {
        // tartalmat mindig a content pane-hez kell hozzáadni
        // vagy content pane-ként beállítani.
        window.setContentPane(container);
        window.setVisible(true);
    }

    /**
     * Visszaadja az alkalmazás fõablakát. A metódus az alkalmazás belsõ
     * vázának, infrastruktúrájának részét képezi, minden alkalmazásban szükség
     * van rá.
     */
    public JFrame getWindow() {
        return window;
    }

    /**
     * Visszaadja az alkalmazás controllerét. A metódus az alkalmazás belsõ
     * vázának, infrastruktúrájának részét képezi, minden alkalmazásban szükség
     * van rá.
     */
    public BugassistController getController() {
        return controller;
    }



}
