/*
 * Created on 29.07.2005
 *
 */
package org.sbml.displaysbml;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/** Stellt Methoden zur Verf&uuml;gung, die die Arbeit mit
  * einem LayoutManager vereinfachen.
  * 
  * @author Andreas Dr&auml;ger
  */
public class LayoutHelper
{

  /** Methode, die uns beim Anornden von Elementen im GridBagLayout hilft.
   * @param cont
   * @param gbl
   * @param c
   * @param x
   * @param y
   * @param width
   * @param height
   * @param weightx
   * @param weighty
   */
 public static void addComponent(
         Container cont,
         GridBagLayout gbl,
         Component c,
         int x, int y,
         int width, int height,
         double weightx, double weighty )
 {
   GridBagConstraints gbc = new GridBagConstraints();
   gbc.fill               = GridBagConstraints.BOTH;
   gbc.gridx              = x; 
   gbc.gridy              = y;
   gbc.gridwidth          = width; 
   gbc.gridheight         = height;
   gbc.weightx            = weightx; 
   gbc.weighty            = weighty;
   gbl.setConstraints(c, gbc);
   cont.add(c);
 }
  
}
