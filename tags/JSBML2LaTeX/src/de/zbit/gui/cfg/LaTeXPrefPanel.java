/**
 * 
 */
package de.zbit.gui.cfg;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbml.tolatex.LaTeXOptions;

import de.zbit.gui.LayoutHelper;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Andreas Dr&auml;ger
 * 
 */
public class LaTeXPrefPanel extends PreferencesPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3707457838933500693L;
	
	private static Set<Object> keySetFull; 
	
	/**
	 * @throws IOException
	 */
	public LaTeXPrefPanel() throws IOException {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(Object key) {
		if (keySetFull == null) {
			keySetFull = preferences.keySetFull();
		}
		return keySetFull.contains(key);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Auto LaTeX Panel";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.cfg.PreferencesPanel#autoBuildPanel()
	 */
	@Override
	public List<Option<?>> autoBuildPanel() {
		List<Option<?>> unprocessedOptions = new LinkedList<Option<?>>();
		LayoutHelper lh = new LayoutHelper(this);

		// search for OptionGroups first
		searchForOptionGroups();
		
  	// First we create GUI elements for all groups
		lh.add(createGroup(LaTeXOptions.CONFIGURATION_FILES, unprocessedOptions), 0, 0, 2, 1, 1d, 1d);
		lh.add(createGroup(LaTeXOptions.REPORT_OPTIONS, unprocessedOptions), 0, 1, 1, 1, 1d, 1d);
		lh.add(createGroup(LaTeXOptions.LAYOUT_OPTIONS, unprocessedOptions), 1, 1, 1, 1, 1d, 1d);
		lh.add(createGroup(LaTeXOptions.TYPOGRAPHICAL_OPTIONS, unprocessedOptions), 0, 2, 2, 1, 1d, 1d);
//		JPanel panel = new JPanel(new BorderLayout());
//		panel.add(createGroup(LaTeXOptions.TYPOGRAPHICAL_OPTIONS, unprocessedOptions), BorderLayout.CENTER);
//		LayoutHelper helper = new LayoutHelper(new JPanel());
//		helper.add(new JPanel(), 0, 0, 1, 1, 1d, 0d);
//		helper.add(new JLabel(UIManager.getIcon("ICON_LATEX_SMALL")), 1, 0, 1, 1, 0d, 0d);
//		helper.add(new JPanel(), 2, 0, 1, 1, 1d, 0d);
//		panel.add(helper.getContainer(), BorderLayout.EAST);
//		lh.add(panel, 0, 2, 2, 1, 1d, 1d);
		
		// Now we consider what is left
		unprocessedOptions.addAll(addOptions(lh, option2group.keySet(), null));
		
		return unprocessedOptions;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#init()
	 */
	@Override
	public void init() {
		autoBuildPanel();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.PreferencesPanel#loadPreferences()
	 */
	@Override
	protected SBPreferences loadPreferences() throws IOException {
		return SBPreferences.getPreferencesFor(LaTeXOptions.class);
	}
	
}
