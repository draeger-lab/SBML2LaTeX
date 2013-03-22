/*
 * $Id: TextExport.java 83 2011-12-14 08:22:03Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn/SBML2LaTeX/tags/version0.9.9/src/org/sbml/tolatex/io/TextExport.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML2LaTeX, a program that creates 
 * human-readable reports for given SBML files.
 * 
 * Copyright (C) 2008-2011 by the University of Tuebingen, Germany.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.tolatex.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.sbml.jsbml.Event;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import de.zbit.io.SBFileFilter;

/**
 * This class writes the differential equations to a plain text file.
 * 
 * @since This was part of SBMLsqueezer 1.0
 * @version $Rev: 83 $
 * @author Andreas Dr&auml;ger
 * @author Nadine Hassis
 * @date Aug 1, 2007
 */
public class TextExport implements SBMLReportGenerator {

	/**
	 * <p>
	 * Default constructor.
	 * </p>
	 */
	public TextExport() {
		super();
	}

	/**
	 * <p>
	 * This constructor analyzes the given file. If it ends with "*.txt", a
	 * plain text file will be generated, which contains the ordinary equation
	 * system. If the file ends with "*.tex", the system will be written into a
	 * LaTeX file. Upper and lower cases for the file ending are ignored. In all
	 * other cases, nothing will happen.
	 * </p>
	 * 
	 * @param model
	 * @param file
	 * @throws IOException
	 * @throws SBMLException 
	 */
	public TextExport(Model model, File file)
			throws IOException, SBMLException {
		if ((new SBFileFilter(SBFileFilter.FileType.TEXT_FILES)).accept(file)) {
			writeTextFile(model, file);
		} else if ((new SBFileFilter(SBFileFilter.FileType.TeX_FILES))
				.accept(file)) {
			LaTeXReportGenerator export = new LaTeXReportGenerator();
			export.toLaTeX(model, file);
		} else
			throw new IllegalArgumentException("file type of " + file.getName()
					+ " not supported.");
	}

	/**
	 * This method appends one line to the given writer.
	 * 
	 * @param str
	 * @param writer
	 * @throws IOException
	 */
	private final void append(String str, BufferedWriter writer)
			throws IOException {
		writer.write(str);
		writer.newLine();
	}

	

	public void format(ListOf<Event> events, BufferedWriter buffer)
			throws IOException {
		// TODO Auto-generated method stub

	}

	public void format(Model model, BufferedWriter buffer) throws IOException {
		// TODO Auto-generated method stub
	}

	public void format(SBMLDocument doc, BufferedWriter buffer)
			throws IOException {
		// TODO Auto-generated method stub
	}
  
  /**
   * This method writes the ordinary differential equation system into a plain
   * text file. Note that the file extension does not matter.
   * 
   * @param file
   * @param klg
   * @throws IOException
   */
	@SuppressWarnings("deprecation")
	public final void writeTextFile(Model model, File file) throws IOException {
		int i;
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file.getPath())));
		append("SBMLsqueezer generated model report file", out);
		append("----------------------------------------", out);
		for (i = 0; i < model.getNumReactions(); i++) {
			Reaction r = model.getReaction(i);
			out.append("Reaction: ");
			out.append(r.getId());
			if (r.isSetName()) {
				out.append(", ");
				out.append(r.getName());
			}
			out.newLine();
			out.append("Kinetic: v_");
			out.append(r.getId());
			out.append(" = ");
			if (r.isSetKineticLaw())
				out.append(r.getKineticLaw().getMath().toString());
			else
				out.append("undefined");
			out.newLine();
		}
		out.newLine();
		for (i = 0; i < model.getNumSpecies(); i++) {
			Species s = model.getSpecies(i);
			StringBuffer ode = new StringBuffer();
			for (Reaction r : model.getListOfReactions()) {
				for (SpeciesReference reactant : r.getListOfReactants()) {
					if (reactant.getSpecies().equals(s.getId())) {
						ode.append('-');
						if (reactant.isSetStoichiometryMath()) {
							ode.append(reactant.getStoichiometryMath()
									.getMath().toString());
							ode.append(' ');
						} else if (reactant.getStoichiometry() != 1d) {
							String stoich = Double.toString(reactant
									.getStoichiometry());
							if (stoich.endsWith(".0"))
								ode.append(stoich.substring(0,
										stoich.length() - 2));
							else
								ode.append(stoich);
							ode.append(' ');
						}
						ode.append("v_");
						ode.append(r.getId());
					}
				}
				for (SpeciesReference product : r.getListOfProducts()) {
					if (product.getSpecies().equals(s.getId())) {
						if (ode.length() > 0)
							ode.append('+');
						if (product.isSetStoichiometryMath()) {
							ode.append(product.getStoichiometryMath().getMath()
									.toString());
							ode.append(' ');
						} else if (product.getStoichiometry() != 1d) {
							String stoich = Double.toString(product
									.getStoichiometry());
							if (stoich.endsWith(".0"))
								ode.append(stoich.substring(0,
										stoich.length() - 2));
							else
								ode.append(stoich);
							ode.append(' ');
						}
						ode.append("v_");
						ode.append(r.getId());
					}
				}
			}
			String toWrite = "Species: " + s.getId() + " ODE: d[" + s.getId()
					+ "]/dt = " + ode.toString();
			append(toWrite, out);
			append(" ", out);
		}
		out.close();
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.tolatex.io.SBMLReportGenerator#format(org.sbml.jsbml.ListOf, java.io.BufferedWriter, boolean)
	 */
	public void format(ListOf<? extends SBase> list, BufferedWriter buffer,
	    boolean section) throws IOException, SBMLException {
	    // TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.tolatex.io.SBMLReportGenerator#section(java.lang.String, boolean)
	 */
	public StringBuffer section(String title, boolean numbering) {
	    // TODO Auto-generated method stub
	    return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.tolatex.io.SBMLReportGenerator#subsection(java.lang.String, boolean)
	 */
	public StringBuffer subsection(String title, boolean numbering) {
	    // TODO Auto-generated method stub
	    return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.tolatex.io.SBMLReportGenerator#subsubsection(java.lang.String, boolean)
	 */
	public StringBuffer subsubsection(String title, boolean numbering) {
	    // TODO Auto-generated method stub
	    return null;
	}

}
