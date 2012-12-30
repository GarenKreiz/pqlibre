package org.gpssearch.gui;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Various tools for GUI.
 *
 */
public class GuiTools
{
	private static final int DEFAULT_FONT_SIZE = 9;
	
	/**
	 * Force the font to be a certain number of pixels high.
	 * @param c the control to set the font size for.
	 * @param size the font size to set.
	 */
	public static void applyFontSize(Control c,int size)
	{
		Font f = c.getFont();
		FontData[] fds = f.getFontData();
		for (FontData fd : fds)
		{
			fd.setHeight(size);
		}
		Font nu = new Font(Display.getCurrent(), fds);
		c.setFont(nu);
	}
	
	public static void applyDefaultFontSize(Control c)
	{
		applyFontSize(c, DEFAULT_FONT_SIZE);
	}
}
