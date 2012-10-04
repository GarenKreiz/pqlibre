package org.gpssearch.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
public class AttributeDialog extends Dialog
{

	protected Object result;
	protected Shell shlAttributes;
	private Properties properties;
	private List<Attribute> attributes = new ArrayList<Attribute>();
	private List<Button> includeList = new ArrayList<Button>();
	private List<Button> excludeList = new ArrayList<Button>();
	private List<Button> ignoreList = new ArrayList<Button>();

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
		shlAttributes.setSize(481, 767);
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

		ScrolledComposite scrolledComposite = new ScrolledComposite(shlAttributes, SWT.BORDER | SWT.V_SCROLL);
		scrolledComposite.setAlwaysShowScrollBars(true);
		FormData fd_scrolledComposite = new FormData();
		fd_scrolledComposite.bottom = new FormAttachment(btnOk, -6);
		fd_scrolledComposite.top = new FormAttachment(0, 10);
		fd_scrolledComposite.right = new FormAttachment(0, 439);
		fd_scrolledComposite.left = new FormAttachment(0, 37);
		scrolledComposite.setLayoutData(fd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite_2 = new Composite(scrolledComposite, SWT.NONE);
		composite_2.setLayout(new GridLayout(2, false));

		Label lblNewLabel = new Label(composite_2, SWT.NONE);
		lblNewLabel.setText("Attribute                            ");

		Composite composite_3 = new Composite(composite_2, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_3.setLayout(new GridLayout(5, false));

		Label lblInclude = new Label(composite_3, SWT.NONE);
		lblInclude.setText("Include");
		new Label(composite_3, SWT.NONE);

		Label lblExclude = new Label(composite_3, SWT.NONE);
		lblExclude.setText("Exclude");
		new Label(composite_3, SWT.NONE);

		Label lblIgnore = new Label(composite_3, SWT.NONE);
		lblIgnore.setText("Ignore");

		// add all attributes
		for (Attribute a : Attribute.values())
		{
			attributes.add(a);
			Label lblFoo = new Label(composite_2, SWT.NONE);
			lblFoo.setText(a.toString());

			Composite composite_1 = new Composite(composite_2, SWT.NONE);
			composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			composite_1.setLayout(new GridLayout(5, false));

			Button includeButton = new Button(composite_1, SWT.RADIO);
			new Label(composite_1, SWT.NONE);
			includeList.add(includeButton);

			Button excludeButton = new Button(composite_1, SWT.RADIO);
			new Label(composite_1, SWT.NONE);
			excludeList.add(excludeButton);

			Button ignoreButton = new Button(composite_1, SWT.RADIO);
			ignoreButton.setSelection(true);
			ignoreList.add(ignoreButton);
		}
		scrolledComposite.setContent(composite_2);
		scrolledComposite.setMinSize(composite_2.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// set the properties
		applyProperties();

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
