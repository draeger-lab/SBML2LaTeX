/**
 * 
 */
package org.sbml.tolatex.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.sbml.jsbml.SBase;
import org.sbml.tolatex.io.LaTeXOptionsIO;

import de.zbit.gui.GUIOptions;
import de.zbit.gui.LayoutHelper;
import de.zbit.gui.cfg.FileSelector;
import de.zbit.gui.cfg.LaTeXPrefPanel;
import de.zbit.io.SBFileFilter;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.SBPreferences;

/**
 * This {@link JPanel} provides a user interface to select the SBML input file
 * (if necessary, i.e., this element can also be initialized with a given
 * {@link SBase} indicating that no input file is necessary) and the destination
 * path for the TeX output file plus the full configuration of SBML2LaTeX as
 * provided by the {@link LaTeXPrefPanel}. In this way, this
 * {@link LaTeXExportPanel} fully qualifies all necessary settings to invoke
 * SBML2LaTeX.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-11-10
 */
public class LaTeXExportPanel extends JPanel {
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5356369953204413478L;
	
	/**
	 * Selectors for the SBML input and TeX output file.
	 */
	private FileSelector fsSBML, fsTeX;
	
	/**
	 * Preferences for GUI and input/output.
	 */
	private SBPreferences prefsIO, prefsGUI;
	/**
	 * The configuration of SBML2LaTeX.
	 */
	private LaTeXPrefPanel prefsPanelLaTeX;
	
	/**
	 * Creates a new {@link LaTeXExportPanel} with a {@link FileSelector} for the
	 * SBML input file.
	 * 
	 * @throws IOException
	 * 
	 */
	public LaTeXExportPanel() throws IOException {
		this(null);
	}
	
	/**
	 * Creates a new {@link LaTeXExportPanel} with a {@link FileSelector} for the
	 * SBML input file only if the given {@link SBase} is null.
	 * 
	 * @param sbase
	 *        might be null to indicate that a file has to be selected by the user
	 *        for reading SBML.
	 * @throws IOException
	 */
	public LaTeXExportPanel(SBase sbase) throws IOException {
		super();
		
		prefsIO = SBPreferences.getPreferencesFor(LaTeXOptionsIO.class);
		prefsGUI = SBPreferences.getPreferencesFor(GUIOptions.class);
		
		JPanel panel = new JPanel();
		
		fsSBML = new FileSelector(FileSelector.Type.OPEN, prefsGUI
				.get(GUIOptions.OPEN_DIR), SBFileFilter.SBML_FILE_FILTER);
		fsTeX = new FileSelector(FileSelector.Type.SAVE, prefsIO
				.get(LaTeXOptionsIO.LATEX_DIR), SBFileFilter.TeX_FILE_FILTER);
		
		LayoutHelper lh = new LayoutHelper(panel);
		if (sbase == null) {
			FileSelector.addSelectorsToLayout(lh, fsSBML);
			fsSBML.setLabelText(LaTeXOptionsIO.SBML_INPUT_FILE.formatOptionName());
			fsSBML.setToolTipText(StringUtil.toHTML(LaTeXOptionsIO.SBML_INPUT_FILE
					.getToolTip(), 60));
		}
		FileSelector.addSelectorsToLayout(lh, fsTeX);
		fsTeX.setLabelText(LaTeXOptionsIO.REPORT_OUTPUT_FILE.formatOptionName());
		fsTeX.setToolTipText(StringUtil.toHTML(LaTeXOptionsIO.REPORT_OUTPUT_FILE
				.getToolTip(), 60));
		
		String title = sbase == null ? "Choose the TeX output file"
				: LaTeXOptionsIO.INPUT_AND_OUTPUT_FILES.getName();
		
		panel.setBorder(BorderFactory.createTitledBorder(" " + title + " "));
		panel.setToolTipText(LaTeXOptionsIO.INPUT_AND_OUTPUT_FILES.getToolTip());
		
		lh = new LayoutHelper(this);
		lh.add(panel);
		prefsPanelLaTeX = new LaTeXPrefPanel();
		lh.add(prefsPanelLaTeX);
	}
	
	/**
	 * Provides access to the underlying {@link LaTeXPrefPanel}. This can be used,
	 * e.g., to call the {@link LaTeXPrefPanel#persist()} method.
	 */
	public LaTeXPrefPanel getLaTeXPrefPanel() {
		return prefsPanelLaTeX;
	}
	
	/**
	 * Yields the user-selected SBML {@link File} or throws an {@link IOException}
	 * if this is not possible. This method also tries to persistently store the
	 * {@link File } in the {@link LaTeXOptionsIO} preferences and the
	 * {@link GUIOptions#OPEN_DIR}.
	 * 
	 * @return
	 * @throws IOException
	 */
	public File getSelectedSBMLFile() throws IOException {
		File file = fsSBML.getSelectedFile();
		prefsIO.put(LaTeXOptionsIO.SBML_INPUT_FILE, file);
		prefsGUI.put(GUIOptions.OPEN_DIR, file.getParent());
		return file;
	}
	
	/**
	 * Yields the user-selected TeX {@link File} or throws an {@link IOException}
	 * if this is not possible. This method also tries to persistently store the
	 * {@link File} in the {@link LaTeXOptionsIO} preferences and the
	 * {@link LaTeXOptionsIO#LATEX_DIR}.
	 * 
	 * @return
	 * @throws IOException
	 */
	public File getSelectedTeXFile() throws IOException {
		File file = fsTeX.getSelectedFile();
		prefsIO.put(LaTeXOptionsIO.REPORT_OUTPUT_FILE, file);
		prefsIO.put(LaTeXOptionsIO.LATEX_DIR, file.getParent());
		return file;
	}
	
}
