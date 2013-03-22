package org.sbml.tolatex.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.sbml.jsbml.SBase;

import de.zbit.gui.GUITools;
import de.zbit.gui.ImageTools;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-10-20
 */
public class LaTeXExportDialog extends JDialog {
	
	
	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -408657221271532557L;
	
	static {
		initGUI();
	}
	
	/**
	 * 
	 */
	public static void initGUI() {
		GUITools.initLaF("SBML2LaTeX");
		ImageTools.initImages(LaTeXExportDialog.class.getResource("img"));
	}
	
	public static void main(String args[]) {
		// "/home/draeger/workspace/JSBML2LaTeX/files/jamboree_model_final_annotated_speciesType.xml"
		new LaTeXExportDialog();
		System.exit(0);
	}
	
	/**
	 * 
	 */
	public LaTeXExportDialog() {
		this((JFrame) null, null);
	}
	
	/**
	 * 
	 * @param owner
	 */
	public LaTeXExportDialog(Dialog owner) {
		this(owner, null);
	}
	
	/**
	 * @param owner
	 * @param properties
	 * @param sbase
	 */
	public LaTeXExportDialog(Dialog owner, SBase sbase) {
		super(owner, "SBML2LaTeX", true);
	}
	
	/**
	 * 
	 * @param owner
	 */
	public LaTeXExportDialog(Frame owner) {
		this(owner, null);
	}
	
	/**
	 * This constructor allows us to store the given model or the given reaction
	 * in a text file. This can be a LaTeX or another format.
	 * 
	 * @param owner
	 * @param sbase
	 *        allowed are a reaction or a model instance.
	 */
	public LaTeXExportDialog(Frame owner, SBase sbase) {
		super(owner, "SBML2LaTeX", true);
	}
	
	/**
	 * 
	 * @return true if user clicked on OK button, false otherwise.
	 * @throws IOException
	 */
	public boolean showExportDialog() throws IOException {
		return showExportDialog(null);
	}
	
	/**
	 * 
	 */
	private LaTeXExportPanel exportPanel;
	
	/**
	 * @param sbase
	 * @return true if user clicked on OK button, false otherwise.
	 * @throws IOException
	 */
	public boolean showExportDialog(SBase sbase) throws IOException {
		exportPanel = new LaTeXExportPanel(sbase);
		
		return JOptionPane.showConfirmDialog(this, exportPanel, "LaTeX export",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, UIManager
					.getIcon("ICON_LATEX_SMALL")) == JOptionPane.OK_OPTION;
	}
	
	/**
	 * 
	 * @param owner
	 * @return
	 */
	public static boolean showDialog(Window owner) {
		LaTeXExportDialog dialog;
		if (owner instanceof Frame) {
			dialog = new LaTeXExportDialog((Frame) owner);
		} else if (owner instanceof Dialog) {
			dialog = new LaTeXExportDialog((Dialog) owner);
		} else {
			dialog = new LaTeXExportDialog((Frame) null);
		}
		boolean accept = false;
		try {
			accept = dialog.showExportDialog();
			if (accept) {
				try {
					dialog.exportPanel.getSelectedSBMLFile();
					dialog.exportPanel.getSelectedTeXFile();
					dialog.exportPanel.getLaTeXPrefPanel().persist();
				} catch (BackingStoreException exc) {
					GUITools.showErrorMessage(dialog, exc);
					dialog.dispose();
					return showDialog(owner);
				}
			}
		} catch (IOException exc) {
			GUITools.showErrorMessage(dialog, exc);
		}
		return accept;
	}

	/**
	 * 
	 * @return
	 */
	public static boolean showDialog() {
		return showDialog(null);
	}
}
