package org.gpssearch.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.geoscrape.Attribute;

/**
 * Dialog that lets users select attributes that a cache must satisfy to be
 * included in a search.
 * 
 */
public class AttributeDialog extends Dialog implements SelectionListener
{

	protected Object result;
	protected Shell shlAttributes;
	private Properties properties;
	private List<Attribute> attributes = new ArrayList<Attribute>();
	private List<Button> includeList = new ArrayList<Button>();
	private List<Button> excludeList = new ArrayList<Button>();
	private List<Button> ignoreList = new ArrayList<Button>();
	private List<List<Control>> controlList = new ArrayList<List<Control>>();

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public AttributeDialog(Shell parent, int style, Properties props)
	{
		super(parent, style);
		setText("Attribute selection");
		this.properties = props;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open()
	{
		createContents();
		shlAttributes.open();
		shlAttributes.layout();
		Display display = getParent().getDisplay();
		while (!shlAttributes.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents()
	{
		shlAttributes = new Shell(getParent(), getStyle());
		shlAttributes.setSize(495, 767);
		shlAttributes.setText("Attributes");
		shlAttributes.setLayout(new FormLayout());

		Button btnOk = new Button(shlAttributes, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				saveProperties();
				shlAttributes.close();
			}
		});
		FormData fd_btnOk = new FormData();
		fd_btnOk.bottom = new FormAttachment(100, -10);
		fd_btnOk.right = new FormAttachment(100, -94);
		fd_btnOk.left = new FormAttachment(0, 315);
		btnOk.setLayoutData(fd_btnOk);
		btnOk.setText("OK");
		GuiTools.applyDefaultFontSize(btnOk);

		Button btnCancel = new Button(shlAttributes, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shlAttributes.close();
			}
		});
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		fd_btnCancel.right = new FormAttachment(100, -10);
		fd_btnCancel.left = new FormAttachment(btnOk, 3);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		GuiTools.applyDefaultFontSize(btnCancel);

		ScrolledComposite scrolledComposite = new ScrolledComposite(shlAttributes, SWT.BORDER | SWT.V_SCROLL);
		scrolledComposite.setAlwaysShowScrollBars(true);
		FormData fd_scrolledComposite = new FormData();
		fd_scrolledComposite.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_scrolledComposite.bottom = new FormAttachment(btnOk, -6);
		fd_scrolledComposite.top = new FormAttachment(0, 10);
		fd_scrolledComposite.left = new FormAttachment(0, 10);
		scrolledComposite.setLayoutData(fd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite_2 = new Composite(scrolledComposite, SWT.NONE);

		composite_2.setLayout(new GridLayout(3, false));
		
		new Label(composite_2, SWT.NONE);
		Label lblNewLabel = new Label(composite_2, SWT.NONE);
		lblNewLabel.setText("Attribute                            ");
		GuiTools.applyDefaultFontSize(lblNewLabel);

		Composite composite_3 = new Composite(composite_2, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_3.setLayout(new GridLayout(5, false));

		Label lblInclude = new Label(composite_3, SWT.NONE);
		lblInclude.setText("Include");
		GuiTools.applyDefaultFontSize(lblInclude);
		Label tmp = new Label(composite_3, SWT.NONE);
		GuiTools.applyDefaultFontSize(tmp);
			
		Label lblExclude = new Label(composite_3, SWT.NONE);
		lblExclude.setText("Exclude");
		GuiTools.applyDefaultFontSize(lblExclude);
		tmp = new Label(composite_3, SWT.NONE);
		GuiTools.applyDefaultFontSize(tmp);

		Label lblIgnore = new Label(composite_3, SWT.NONE);
		lblIgnore.setText("Ignore");
		GuiTools.applyDefaultFontSize(lblIgnore);

		//load the red bar
		String redbarName = "attribute__strikethru.png";

		InputStream redbarInput  = getClass().getResourceAsStream("/res/"+redbarName);
		if (redbarInput == null)
		{
			try
			{
				redbarInput = new FileInputStream("res"+File.separator+redbarName);
			}
			catch (FileNotFoundException e1)
			{
				//TODO: Notify the user
				e1.printStackTrace();
			}
		}
		Image redBar = new Image(getParent().getDisplay(),redbarInput);
		

		// add all attributes
		for (Attribute a : Attribute.values())
		{
			attributes.add(a);
			List<Control>controls  = new ArrayList<Control>();
			controlList.add(controls);
			
			Label imgLbl = new Label(composite_2,SWT.NONE);
			String imageName = AttributeImageMap.getAttributFileName(a)+".png";
			InputStream input  = getClass().getResourceAsStream("/res/"+imageName);

			if (input == null)
			{
				try
				{
					input = new FileInputStream("res"+File.separator+imageName);
				}
				catch (FileNotFoundException e1)
				{
					//TODO: Notify the user
					e1.printStackTrace();
				}
			}
			Image image = new Image(getParent().getDisplay(), input);
			if(a.getInc()==0)
			{
				//draw a red bar across the image
				GC gc = new GC(image);
				gc.drawImage(redBar, 0, 0);
			}
			imgLbl.setImage(image);
			
			Label lblFoo = new Label(composite_2, SWT.NONE);
			lblFoo.setText(a.toString());
			GuiTools.applyDefaultFontSize(lblFoo);

			Composite composite_1 = new Composite(composite_2, SWT.NONE);
			composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			composite_1.setLayout(new GridLayout(5, false));

			Button includeButton = new Button(composite_1, SWT.RADIO);
			new Label(composite_1, SWT.NONE);
			includeList.add(includeButton);
			GuiTools.applyDefaultFontSize(includeButton);
			includeButton.addSelectionListener(this);

			Button excludeButton = new Button(composite_1, SWT.RADIO);
			new Label(composite_1, SWT.NONE);
			excludeList.add(excludeButton);
			GuiTools.applyDefaultFontSize(excludeButton);
			excludeButton.addSelectionListener(this);

			Button ignoreButton = new Button(composite_1, SWT.RADIO);
			ignoreButton.setSelection(true);
			ignoreList.add(ignoreButton);
			GuiTools.applyDefaultFontSize(ignoreButton);
			ignoreButton.addSelectionListener(this);

			controls.add(imgLbl);
			controls.add(lblFoo);
			controls.add(composite_1);
			controls.add(includeButton);
			controls.add(excludeButton);
			controls.add(ignoreButton);
			
		}
		scrolledComposite.setContent(composite_2);
		scrolledComposite.setMinSize(composite_2.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// set the properties
		applyProperties();
		
		updateCategories();

	}

	@Override
	public void widgetSelected(SelectionEvent e)
	{
		//the button has been selected, trigger an update
		if(((Button)e.getSource()).getSelection())
		{
			updateCategories();						
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{					
	}

	private void updateCategories()
	{
		for (int x = 0; x < attributes.size() - 1; x++)
		{

			Attribute a1 = attributes.get(x);
			for (int y = x + 1; y < attributes.size(); y++)
			{
				Attribute a2 = attributes.get(y);
				if (a1.getId() == a2.getId() && a1.getInc() != a2.getInc())
				{
					if (includeList.get(x).getSelection() && includeList.get(y).getSelection())
					{
						highlightLine(x, true);
						highlightLine(y, true);
					}
					else
					{
						highlightLine(x, false);
						highlightLine(y, false);
					}
				}
			}
		}
	}
	
	/**
	 * Highlight the given line.
	 * 
	 * @param line
	 */
	private void highlightLine(int line, boolean highlight)
	{
		List<Control> controls = controlList.get(line);
		for (Control w : controls)
		{
			if (highlight)
			{
				w.setBackground(new Color(null, 255, 80, 80));
			}
			else
			{
				w.setBackground(null);
			}
		}
	}

	/**
	 * Copy options from properties object to dialog controls.
	 * 
	 */
	private void applyProperties()
	{
		Set<Object> keys = properties.keySet();
		for (Object k : keys)
		{
			String key = k.toString();
			if (key.startsWith("attribute/"))
			{
				try
				{
					String name = key.split("/")[1];
					Attribute attr = Attribute.parse(name);
					int index = attributes.indexOf(attr);
					if (index >= 0)
					{
						String value = properties.getProperty(key);
						if (value.equals("include"))
						{
							includeList.get(index).setSelection(true);
							ignoreList.get(index).setSelection(false);
						}
						else if (value.equals("exclude"))
						{
							excludeList.get(index).setSelection(true);
							ignoreList.get(index).setSelection(false);
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Copy options from controls to properties object.
	 * 
	 */
	protected void saveProperties()
	{
		for (int x = 0; x < attributes.size(); x++)
		{
			String attributeKey = "attribute/" + attributes.get(x).name();
			if (includeList.get(x).getSelection())
			{
				properties.setProperty(attributeKey, "include");
			}
			else if (excludeList.get(x).getSelection())
			{
				properties.setProperty(attributeKey, "exclude");
			}
			else
			{
				// don't store the ignored attributes
				properties.remove(attributeKey);
			}
		}
	}
}
