package org.gpssearch.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gpssearch.Exporter;

/**
 * This class is a dialog that lets a user select difficulty/terrain combinations.
 * 
 */
public class DiffTerrComboDialog extends Dialog
{

	protected Object result;
	protected Shell shlDifficultyterrain;
	private List<Button> allButtons = new ArrayList<Button>();
	private List<Button> diffMasterButtons = new ArrayList<Button>();
	private List<Button> terrMasterButtons = new ArrayList<Button>();
	private List<Button> selectionButtons = new ArrayList<Button>();
	private List<List<Button>> diffButtons = new ArrayList<List<Button>>();
	private List<List<Button>> terrButtons = new ArrayList<List<Button>>();
	private List<String> suffixes = new ArrayList<String>();
	private Properties props;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public DiffTerrComboDialog(Shell parent, int style, Properties props)
	{
		super(parent, style);
		this.props = props;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open()
	{
		createContents();
		shlDifficultyterrain.open();
		shlDifficultyterrain.layout();
		Display display = getParent().getDisplay();
		while (!shlDifficultyterrain.isDisposed())
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
		shlDifficultyterrain = new Shell(getParent(), getStyle());
		shlDifficultyterrain.setSize(802, 448);
		shlDifficultyterrain.setText("Difficulty/Terrain");
		shlDifficultyterrain.setLayout(new FormLayout());

		Button btnCancel = new Button(shlDifficultyterrain, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shlDifficultyterrain.close();
			}
		});
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.right = new FormAttachment(100, -23);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		GuiTools.applyDefaultFontSize(btnCancel);

		Label lblDifficulty = new Label(shlDifficultyterrain, SWT.CENTER);
		FormData fd_lblDifficulty = new FormData();
		lblDifficulty.setLayoutData(fd_lblDifficulty);
		lblDifficulty.setText("Difficulty");
		GuiTools.applyDefaultFontSize(lblDifficulty);

		Label lblTerrain = new Label(shlDifficultyterrain, SWT.NONE);
		FormData fd_lblTerrain = new FormData();
		fd_lblTerrain.right = new FormAttachment(100, -305);
		fd_lblTerrain.left = new FormAttachment(0, 367);
		lblTerrain.setLayoutData(fd_lblTerrain);
		lblTerrain.setText("Terrain");
		GuiTools.applyDefaultFontSize(lblTerrain);

		Button btnSelectAll = new Button(shlDifficultyterrain, SWT.NONE);
		btnSelectAll.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				selectAll();
			}
		});
		fd_lblDifficulty.bottom = new FormAttachment(btnSelectAll, -166);
		fd_lblDifficulty.left = new FormAttachment(btnSelectAll, 0, SWT.LEFT);
		fd_btnCancel.top = new FormAttachment(0, 380);
		FormData fd_btnSelectAll = new FormData();
		fd_btnSelectAll.left = new FormAttachment(0, 10);
		btnSelectAll.setLayoutData(fd_btnSelectAll);
		btnSelectAll.setText("Select all");
		GuiTools.applyDefaultFontSize(btnSelectAll);

		Button btnInvertSelection = new Button(shlDifficultyterrain, SWT.NONE);
		btnInvertSelection.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				invertSelection();
			}
		});
		fd_btnCancel.left = new FormAttachment(btnInvertSelection, 467);
		fd_btnSelectAll.right = new FormAttachment(btnInvertSelection, -6);
		FormData fd_btnInvertSelection = new FormData();
		fd_btnInvertSelection.top = new FormAttachment(btnSelectAll, 0, SWT.TOP);
		fd_btnInvertSelection.left = new FormAttachment(0, 97);
		fd_btnInvertSelection.right = new FormAttachment(0, 205);
		btnInvertSelection.setLayoutData(fd_btnInvertSelection);
		btnInvertSelection.setText("Invert selection");
		GuiTools.applyDefaultFontSize(btnInvertSelection);

		Composite composite = new Composite(shlDifficultyterrain, SWT.NONE);
		fd_lblDifficulty.right = new FormAttachment(composite, -6);
		fd_lblTerrain.bottom = new FormAttachment(composite, -6);
		fd_btnSelectAll.top = new FormAttachment(0, 380);
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0, 47);
		fd_composite.bottom = new FormAttachment(100, -46);
		fd_composite.right = new FormAttachment(100, -24);
		fd_composite.left = new FormAttachment(0, 73);
		composite.setLayoutData(fd_composite);
		GridLayout layout = new GridLayout(10, false);
		composite.setLayout(layout);
		String[] signs = new String[] { null, "1", "1.5", "2", "2.5", "3",
				"3.5", "4", "4.5", "5" };
		for (int x = 0; x < signs.length; x++)
		{
			if (signs[x] != null)
			{
				suffixes.add(signs[x]);
				diffButtons.add(new ArrayList<Button>());
				terrButtons.add(new ArrayList<Button>());
			}
		}
		for (int x = 0; x < signs.length; x++)
		{
			for (int y = 0; y < signs.length; y++)
			{
				if (signs[x] == null)
				{
					if (signs[y] == null)
					{
						Label label = new Label(composite, SWT.NONE);
						GuiTools.applyDefaultFontSize(label);
					}
					else
					{

						Button btnT = new Button(composite, SWT.CHECK);
						btnT.setText("T=" + signs[y]);
						GuiTools.applyDefaultFontSize(btnT);
						allButtons.add(btnT);
						terrMasterButtons.add(btnT);
						btnT.addSelectionListener(new SelectionAdapter()
						{
							@Override
							public void widgetSelected(SelectionEvent e)
							{
								updateColumn((Button) e.getSource());
							}
						});
					}
				}
				else
				{
					if (signs[y] == null)
					{

						Button btnD = new Button(composite, SWT.CHECK);
						btnD.setText("D=" + signs[x]);
						GuiTools.applyDefaultFontSize(btnD);
						allButtons.add(btnD);
						diffMasterButtons.add(btnD);
						btnD.addSelectionListener(new SelectionAdapter()
						{
							@Override
							public void widgetSelected(SelectionEvent e)
							{
								updateRow((Button) e.getSource());
							}
						});
					}
					else
					{
						Button btn = new Button(composite, SWT.CHECK);
						btn.setText(signs[x] + "/" + signs[y]);
						GuiTools.applyDefaultFontSize(btn);
						allButtons.add(btn);
						selectionButtons.add(btn);
						terrButtons.get(y - 1).add(btn);
						diffButtons.get(x - 1).add(btn);
						btn.addSelectionListener(new SelectionAdapter()
						{
							@Override
							public void widgetSelected(SelectionEvent e)
							{
								updateMasterSwitches();
							}
						});
					}
				}
			}

		}

		Button btnOk = new Button(shlDifficultyterrain, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				saveProperties();
				shlDifficultyterrain.close();
			}
		});
		FormData fd_btnOk = new FormData();
		fd_btnOk.top = new FormAttachment(btnCancel, 0, SWT.TOP);
		fd_btnOk.left = new FormAttachment(btnInvertSelection, 404);
		fd_btnOk.right = new FormAttachment(btnCancel, -6);
		btnOk.setLayoutData(fd_btnOk);
		btnOk.setText("OK");
		GuiTools.applyDefaultFontSize(btnOk);
		loadProperties();

	}


	/**
	 * @param source
	 */
	protected void updateRow(Button source)
	{
		String t = source.getText();
		t = t.split("=")[1];
		int index = suffixes.indexOf(t);
		List<Button> row = diffButtons.get(index);
		for (Button b : row)
		{
			b.setSelection(source.getSelection());
		}
		updateMasterSwitches();
	}

	/**
	 * @param source
	 */
	protected void updateColumn(Button source)
	{
		String t = source.getText();
		t = t.split("=")[1];
		int index = suffixes.indexOf(t);
		List<Button> row = terrButtons.get(index);
		for (Button b : row)
		{
			b.setSelection(source.getSelection());
		}
		updateMasterSwitches();
	}

	/**
	 * 
	 */
	protected void invertSelection()
	{
		for (Button b : selectionButtons)
		{
			b.setSelection(!b.getSelection());
		}
		updateMasterSwitches();
	}

	/**
	 * D or T row swithes are set to reflect the row/column they represent.
	 * 
	 */
	private void updateMasterSwitches()
	{
		// update D switches
		for (int x = 0; x < diffButtons.size(); x++)
		{
			diffMasterButtons.get(x).setSelection(
					allAreChecked(diffButtons.get(x)));
		}
		// update T switches
		for (int x = 0; x < terrButtons.size(); x++)
		{
			terrMasterButtons.get(x).setSelection(
					allAreChecked(terrButtons.get(x)));
		}
	}

	/**
	 * Return true if all the buttons are checked.
	 * 
	 * @param list
	 * @return
	 */
	private boolean allAreChecked(List<Button> list)
	{
		for (Button b : list)
		{
			if (!b.getSelection())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 */
	protected void selectAll()
	{
		for (Button b : allButtons)
		{
			b.setSelection(true);
		}
	}

	/**
	 * Read the selected D/T combos from controls and save them.
	 */
	protected void saveProperties()
	{
		for(Button b:selectionButtons)
		{
			String key = b.getText();
			String value = Boolean.toString(b.getSelection());
			props.setProperty(key, value);
		}
	}
	

	/**
	 * 
	 */
	private void loadProperties()
	{
		for(Object keyObj:props.keySet())
		{
			String key = (String)keyObj;
			if(Pattern.matches(Exporter.DT_COMBO_STRING, key))
			{
				for(Button b:this.selectionButtons)
				{
					if(b.getText().equals(key))
					{
						b.setSelection(Boolean.parseBoolean(props.getProperty(key)));
					}
				}
			}
		}
		updateMasterSwitches();
	}
}
