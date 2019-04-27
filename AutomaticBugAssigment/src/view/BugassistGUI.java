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
		
		
	        // El��ll�tjuk az alkalmaz�s c�msor�t
	       
	        String title = String.format("Bugassist");

	        // A JFrame egy magas szint� kont�ner, egy ablak c�mmel �s kerettel.
	        window = new JFrame(title);

	        // Ha bez�rjuk az ablakot, akkor alap�rtelmez�sben azt csak elrejtjuk.
	        // Ezt a viselked�st m�dos�tjuk arra, hogy az ablak t�nylegesen z�r�djon be.
	        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	        // Gy�rtunk egy bookMenuBar objektumot
	        BugassistMenuBar bugassistMenuBar = new BugassistMenuBar(this);

	        // Amit r�rakunk a Book Shop ablakunkra
	        window.setJMenuBar(bugassistMenuBar);

	        // Az ablaknak be�ll�tjuk a m�ret�t
	        window.setSize(800,600);

	        // K�szen vagyunk, megjelen�thetj�k az ablakot
	        window.setVisible(true);

		
	}
	
	
	/**
     * A kapott containert be�ll�tja a f�ablak {@link JFrame} contentjek�nt.
     */
    public void setActualContent(Container container) {
        // tartalmat mindig a content pane-hez kell hozz�adni
        // vagy content pane-k�nt be�ll�tani.
        window.setContentPane(container);
        window.setVisible(true);
    }

    /**
     * Visszaadja az alkalmaz�s f�ablak�t. A met�dus az alkalmaz�s bels�
     * v�z�nak, infrastrukt�r�j�nak r�sz�t k�pezi, minden alkalmaz�sban sz�ks�g
     * van r�.
     */
    public JFrame getWindow() {
        return window;
    }

    /**
     * Visszaadja az alkalmaz�s controller�t. A met�dus az alkalmaz�s bels�
     * v�z�nak, infrastrukt�r�j�nak r�sz�t k�pezi, minden alkalmaz�sban sz�ks�g
     * van r�.
     */
    public BugassistController getController() {
        return controller;
    }



}
