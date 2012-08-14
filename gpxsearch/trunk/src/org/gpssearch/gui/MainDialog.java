package org.gpssearch.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.geoscrape.Cache;
import org.geoscrape.ListSearcher;
import org.geoscrape.Location;
import org.geoscrape.Login;
import org.gpssearch.GpxWriter;
import org.gpssearch.SearcherProgress;
import org.gpssearch.UserIdManager;

/**
 * The main application dialog. Offers the user an interface to select cache
 * search parameters. Also allows launching sub-dialog for further specification
 * of search parameters, and opens a sub-dialog to inform the user about search
 * progress.
 * 
 */
public class MainDialog extends Dialog
{
	private static final String PROPERTIES_FILENAME = ".gpxexporter.properties";

	private Text locationInput;
	private Text afterDateField;
	private Text beforeDateField;
	private Group sizeGroup;
	private Button btnFilterSize;
	private Button btnSmallSize;
	private Button btnRegularSize;
	private Button btnOtherSize;
	private Button btnUknownSize;
	private Button btnVirtualSize;
	private Button btnLargeSize;
	private Button btnMicroSize;
	private Button btnPlacedBefore;
	private Button btnPlacedAfter;
	private Button btnFilterType;
	private Group typeGroup;
	private String dateFormat;
	private Properties properties;
	private Button btnNotChosenSize;
	private Button btnTraditional;
	private Button btnVirtualType;
	private Button btnFoundInLast;
	private Button btnEarthcache;
	private Button btnWherigo;
	private Button btnGpsAdventure;
	private Button btnCito;
	private Button btnLetterbox;
	private Button btnEvent;
	private Button btnMegaEvent;
	private Button btnMystery;
	private Button btnMulti;
	private Button btnWebcam;
	private Button btnFilterAttributes;
	private Button btnIgnoreOwn;
	private Button btnIgnoreFound;
	private Button btnIgnoreDisabled;
	private Button btnHasTrackable;

	private Spinner foundInlastDaysField;
	private Spinner searchradius;
	private Spinner maximumresults;
	private Label lblMaximumResults;
	private Button btnHasNotBeen;
	private Button btnRequireFav;
	private Spinner favouritePoints;
	private Label lblFavouritePoints;
	private Combo searchRadiusUnits;
	private Button btnFilterDifficultyterrain;
	private Login login;
	private UserIdManager idManager;

	private Button btnIncludeLogs;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public MainDialog(Shell parentShell, Login login)
	{
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		this.login = login;
		this.dateFormat = login.getDateFormat();
		properties = new Properties();
		try
		{
			idManager = new UserIdManager(login);
			idManager.loadDb();
			// read and apply properties
			String fileName = System.getProperty("user.home") + File.separator + PROPERTIES_FILENAME;
			Properties savedProps = new Properties();
			try
			{
				savedProps.load(new FileInputStream(fileName));
				setSearchProperties(savedProps);
			}
			catch (FileNotFoundException e)
			{
				// ignore, this just means there are no properties yet
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText("Pocket Query Libre");
		shell.setImages(getParentShell().getImages());
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite container = (Composite) super.createDialogArea(parent);
		container.setToolTipText("");
		container.setLayout(null);

		Label lblLocation = new Label(container, SWT.NONE);
		lblLocation.setBounds(10, 10, 105, 15);
		lblLocation.setText("Location:");

		locationInput = new Text(container, SWT.BORDER);
		locationInput.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				parseLocation();
			}
		});
		locationInput.setToolTipText("Enter the coordinates of your search start point");
		locationInput.setBounds(10, 31, 268, 21);

		Label lblSearchRadius = new Label(container, SWT.NONE);
		lblSearchRadius.setBounds(10, 68, 105, 15);
		lblSearchRadius.setText("Search radius:");

		searchRadiusUnits = new Combo(container, SWT.READ_ONLY);
		searchRadiusUnits.setItems(new String[] { "km", "miles" });
		searchRadiusUnits.setBounds(100, 89, 64, 21);
		searchRadiusUnits.select(0);

		btnFilterDifficultyterrain = new Button(container, SWT.CHECK);
		btnFilterDifficultyterrain.setToolTipText("Only caches with the selected D/T combos are found.");
		btnFilterDifficultyterrain.setBounds(10, 116, 173, 26);
		btnFilterDifficultyterrain.setText("Filter Difficulty/Terrain");

		Button btnEdit = new Button(container, SWT.NONE);
		btnEdit.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				openDTDialog();
			}
		});
		btnEdit.setBounds(197, 118, 81, 24);
		btnEdit.setText("Edit");

		btnPlacedAfter = new Button(container, SWT.CHECK);
		btnPlacedAfter.setToolTipText("Only show caches placed on or after the specified date");
		btnPlacedAfter.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
			}
		});
		btnPlacedAfter.setBounds(10, 346, 138, 26);
		btnPlacedAfter.setText("Placed on or after");

		afterDateField = new Text(container, SWT.BORDER);
		afterDateField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (btnPlacedAfter.getSelection())
				{
					verifyDate(afterDateField);
				}
			}
		});
		afterDateField.setToolTipText("Enter date in " + dateFormat + " format.");
		afterDateField.setBounds(156, 346, 148, 21);

		btnPlacedBefore = new Button(container, SWT.CHECK);
		btnPlacedBefore.setToolTipText("Only show caches placed on or before the specified date");
		btnPlacedBefore.setText("Placed on or before");
		btnPlacedBefore.setBounds(10, 378, 138, 26);

		beforeDateField = new Text(container, SWT.BORDER);
		beforeDateField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (btnPlacedBefore.getSelection())
				{
					verifyDate(beforeDateField);
				}
			}
		});
		beforeDateField.setToolTipText("Enter date in " + dateFormat + " format.");
		beforeDateField.setBounds(156, 378, 148, 21);

		sizeGroup = new Group(container, SWT.NONE);
		sizeGroup.setBounds(10, 188, 239, 140);

		btnMicroSize = new Button(sizeGroup, SWT.CHECK);
		btnMicroSize.setEnabled(false);
		btnMicroSize.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
			}
		});
		btnMicroSize.setText("Micro");
		btnMicroSize.setBounds(10, 10, 63, 26);

		btnSmallSize = new Button(sizeGroup, SWT.CHECK);
		btnSmallSize.setEnabled(false);
		btnSmallSize.setText("Small");
		btnSmallSize.setBounds(10, 42, 63, 26);

		btnRegularSize = new Button(sizeGroup, SWT.CHECK);
		btnRegularSize.setEnabled(false);
		btnRegularSize.setText("Regular");
		btnRegularSize.setBounds(10, 74, 88, 26);

		btnOtherSize = new Button(sizeGroup, SWT.CHECK);
		btnOtherSize.setEnabled(false);
		btnOtherSize.setText("Other");
		btnOtherSize.setBounds(128, 10, 63, 26);

		btnUknownSize = new Button(sizeGroup, SWT.CHECK);
		btnUknownSize.setEnabled(false);
		btnUknownSize.setText("Unknown");
		btnUknownSize.setBounds(128, 42, 95, 26);

		btnVirtualSize = new Button(sizeGroup, SWT.CHECK);
		btnVirtualSize.setEnabled(false);
		btnVirtualSize.setText("Virtual");
		btnVirtualSize.setBounds(128, 74, 78, 26);

		btnLargeSize = new Button(sizeGroup, SWT.CHECK);
		btnLargeSize.setEnabled(false);
		btnLargeSize.setText("Large");
		btnLargeSize.setBounds(10, 106, 63, 26);

		btnNotChosenSize = new Button(sizeGroup, SWT.CHECK);
		btnNotChosenSize.setEnabled(false);
		btnNotChosenSize.setText("Not chosen");
		btnNotChosenSize.setBounds(128, 106, 95, 26);

		typeGroup = new Group(container, SWT.NONE);
		typeGroup.setBounds(255, 188, 366, 140);

		btnTraditional = new Button(typeGroup, SWT.CHECK);
		btnTraditional.setEnabled(false);
		btnTraditional.setText("Traditional");
		btnTraditional.setBounds(10, 10, 107, 26);

		btnVirtualType = new Button(typeGroup, SWT.CHECK);
		btnVirtualType.setEnabled(false);
		btnVirtualType.setText("Virtual");
		btnVirtualType.setBounds(123, 74, 95, 26);

		btnWebcam = new Button(typeGroup, SWT.CHECK);
		btnWebcam.setEnabled(false);
		btnWebcam.setText("Webcam");
		btnWebcam.setBounds(10, 106, 107, 26);

		btnMulti = new Button(typeGroup, SWT.CHECK);
		btnMulti.setEnabled(false);
		btnMulti.setText("Multi");
		btnMulti.setBounds(10, 74, 88, 26);

		btnMystery = new Button(typeGroup, SWT.CHECK);
		btnMystery.setEnabled(false);
		btnMystery.setText("Mystery");
		btnMystery.setBounds(10, 42, 107, 26);

		btnMegaEvent = new Button(typeGroup, SWT.CHECK);
		btnMegaEvent.setEnabled(false);
		btnMegaEvent.setText("Mega Event");
		btnMegaEvent.setBounds(123, 42, 101, 26);

		btnEvent = new Button(typeGroup, SWT.CHECK);
		btnEvent.setEnabled(false);
		btnEvent.setText("Event");
		btnEvent.setBounds(123, 10, 95, 26);

		btnLetterbox = new Button(typeGroup, SWT.CHECK);
		btnLetterbox.setEnabled(false);
		btnLetterbox.setText("Letterbox");
		btnLetterbox.setBounds(123, 106, 95, 26);

		btnCito = new Button(typeGroup, SWT.CHECK);
		btnCito.setEnabled(false);
		btnCito.setText("CITO");
		btnCito.setBounds(240, 10, 95, 26);

		btnGpsAdventure = new Button(typeGroup, SWT.CHECK);
		btnGpsAdventure.setEnabled(false);
		btnGpsAdventure.setText("GPS adventure");
		btnGpsAdventure.setBounds(240, 106, 109, 26);

		btnWherigo = new Button(typeGroup, SWT.CHECK);
		btnWherigo.setEnabled(false);
		btnWherigo.setText("Wherigo");
		btnWherigo.setBounds(240, 74, 95, 26);

		btnEarthcache = new Button(typeGroup, SWT.CHECK);
		btnEarthcache.setEnabled(false);
		btnEarthcache.setText("Earthcache");
		btnEarthcache.setBounds(240, 42, 124, 26);

		btnFoundInLast = new Button(container, SWT.CHECK);
		btnFoundInLast.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (btnFoundInLast.getSelection())
				{
					btnHasNotBeen.setSelection(false);
				}
			}
		});
		btnFoundInLast.setBounds(10, 410, 107, 26);
		btnFoundInLast.setText("Found in last ");

		Label lblDays = new Label(container, SWT.NONE);
		lblDays.setBounds(192, 415, 57, 15);
		lblDays.setText("days");

		btnFilterAttributes = new Button(container, SWT.CHECK);
		btnFilterAttributes.setToolTipText("Only caches with the selected attributes are found");
		btnFilterAttributes.setText("Filter attributes");
		btnFilterAttributes.setBounds(353, 116, 131, 26);

		Button button_2 = new Button(container, SWT.NONE);
		button_2.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				openAttributeDialog();
			}
		});
		button_2.setText("Edit");
		button_2.setBounds(490, 118, 81, 24);

		btnIgnoreOwn = new Button(container, SWT.CHECK);
		btnIgnoreOwn.setToolTipText("Ignore caches you own");
		btnIgnoreOwn.setSelection(true);
		btnIgnoreOwn.setBounds(8, 442, 107, 26);
		btnIgnoreOwn.setText("Ignore own");

		btnIgnoreFound = new Button(container, SWT.CHECK);
		btnIgnoreFound.setToolTipText("Ignore caches you have already found");
		btnIgnoreFound.setSelection(true);
		btnIgnoreFound.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
			}
		});
		btnIgnoreFound.setText("Ignore found");
		btnIgnoreFound.setBounds(129, 442, 107, 26);

		btnIgnoreDisabled = new Button(container, SWT.CHECK);
		btnIgnoreDisabled.setToolTipText("Ignore caches that have been disabled");
		btnIgnoreDisabled.setText("Ignore disabled");
		btnIgnoreDisabled.setBounds(255, 442, 131, 26);

		btnHasTrackable = new Button(container, SWT.CHECK);
		btnHasTrackable.setToolTipText("Only show caches that contains trackables.");
		btnHasTrackable.setText("Contains trackable");
		btnHasTrackable.setBounds(10, 474, 154, 26);

		btnFilterSize = new Button(container, SWT.CHECK);
		btnFilterSize.setBounds(10, 163, 107, 26);
		btnFilterSize.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				ableAll(sizeGroup, btnFilterSize.getSelection());
			}
		});
		btnFilterSize
				.setToolTipText("If checked, only selected cache sizes will be included. If unchecked, all cache sizes.");
		btnFilterSize.setText("Filter size");

		btnFilterType = new Button(container, SWT.CHECK);
		btnFilterType.setBounds(255, 163, 107, 26);
		btnFilterType
				.setToolTipText("If checked, only selected cache types will be included. If unchecked, all cache types.");
		btnFilterType.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				ableAll(typeGroup, btnFilterType.getSelection());
			}
		});
		btnFilterType.setText("Filter type");

		searchradius = new Spinner(container, SWT.BORDER);
		searchradius.setDigits(1);
		searchradius.setMaximum(10000);
		searchradius.setMinimum(1);
		searchradius.setSelection(100);
		searchradius.setBounds(10, 89, 81, 21);

		foundInlastDaysField = new Spinner(container, SWT.BORDER);
		foundInlastDaysField.setMaximum(10000);
		foundInlastDaysField.setMinimum(1);
		foundInlastDaysField.setSelection(1);
		foundInlastDaysField.setBounds(116, 415, 67, 21);

		maximumresults = new Spinner(container, SWT.BORDER);
		maximumresults.setToolTipText("Select the maximum number of results returned. 0 means unlimited.");
		maximumresults.setMaximum(100000);
		maximumresults.setBounds(213, 89, 81, 21);

		lblMaximumResults = new Label(container, SWT.NONE);
		lblMaximumResults.setText("Maximum results:");
		lblMaximumResults.setBounds(213, 68, 105, 15);

		btnHasNotBeen = new Button(container, SWT.CHECK);
		btnHasNotBeen.setToolTipText("Only get caches that have not been found by anyone.");
		btnHasNotBeen.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (btnHasNotBeen.getSelection())
				{
					// unselect incompatible button
					btnFoundInLast.setSelection(false);
				}
			}
		});
		btnHasNotBeen.setBounds(255, 410, 173, 26);
		btnHasNotBeen.setText("Has not been found");

		btnRequireFav = new Button(container, SWT.CHECK);
		btnRequireFav.setText("Has at least ");
		btnRequireFav.setBounds(10, 506, 107, 26);

		favouritePoints = new Spinner(container, SWT.BORDER);
		favouritePoints.setPageIncrement(1);
		favouritePoints.setMaximum(10000);
		favouritePoints.setMinimum(1);
		favouritePoints.setSelection(1);
		favouritePoints.setBounds(116, 511, 67, 21);

		lblFavouritePoints = new Label(container, SWT.NONE);
		lblFavouritePoints.setText("favourite points");
		lblFavouritePoints.setBounds(192, 511, 112, 15);

		Button btnNewButton = new Button(container, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (login.getHomeLocation() != null)
				{
					String homeLocText = login.getHomeLocation().toString();
					locationInput.setText(homeLocText);
				}
			}
		});
		btnNewButton.setBounds(284, 28, 124, 24);
		btnNewButton.setText("Use home location");

		btnIncludeLogs = new Button(container, SWT.CHECK);
		btnIncludeLogs.setBounds(10, 575, 196, 26);
		btnIncludeLogs.setText("Include full logs in output");

		applyProperties();
		return container;
	}

	/**
	 * Check that the date entered in the date field corresponds to what the
	 * application expects, pop up a notification otherwise
	 * 
	 * @param dateField
	 */
	protected void verifyDate(final Text dateField)
	{
		String text = dateField.getText();
		SimpleDateFormat df = new SimpleDateFormat(this.dateFormat);
		try
		{
			df.parse(text);
		}
		catch (ParseException e)
		{
			// something wrong, inform the user

			Shell shell = new Shell(getShell());

			ToolTip tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
			tip.setText("I could not understand the date.");
			int x = getShell().getBounds().x;
			x += dateField.getBounds().x + dateField.getBounds().width;
			int y = getShell().getBounds().y;
			y += dateField.getBounds().y + dateField.getBounds().height * 1.5;

			tip.setMessage("The date must be in the "
					+ dateFormat
					+ " format.\nThis is the same format you use on geocaching.com.\nTo change it, go to geocaching.com.");
			tip.setLocation(x, y);

			tip.setVisible(true);

			// force the focus back to the input field
			shell.getDisplay().asyncExec(new Runnable()
			{
				public void run()
				{
					dateField.setFocus();
				}
			});
		}
	}

	/**
	 * 
	 */
	protected void openAttributeDialog()
	{
		AttributeDialog aDialog = new AttributeDialog(getShell(), SWT.DIALOG_TRIM, this.properties);
		aDialog.open();
	}

	/**
	 * 
	 */
	protected void openDTDialog()
	{
		DiffTerrComboDialog dtDialog = new DiffTerrComboDialog(getShell(), SWT.DIALOG_TRIM, this.properties);
		dtDialog.open();
	}

	public void setSearchProperties(Properties prop)
	{
		// merge prop into the properties of this object
		Set<Entry<Object, Object>> es = prop.entrySet();
		for (Entry<Object, Object> e : es)
		{
			properties.setProperty(e.getKey().toString(), e.getValue().toString());
		}
		applyProperties();
	}

	/**
	 * Copy values from this.properties to controls.
	 */
	private void applyProperties()
	{
		Set<Entry<Object, Object>> set = properties.entrySet();
		for (Entry<Object, Object> e : set)
		{
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			// get the corresponding field, if it exists
			try
			{
				Field f = this.getClass().getDeclaredField(key);
				if (f.getType().equals(Button.class))
				{
					Button b = (Button) f.get(this);
					if (b != null)
						b.setSelection(Boolean.parseBoolean(value));
				}
				else if (f.getType().equals(Text.class))
				{
					Text t = (Text) f.get(this);
					if (t != null)
						t.setText(value);
				}
				else if (f.getType().equals(Spinner.class))
				{
					Spinner s = (Spinner) f.get(this);
					if (s != null)
						s.setSelection(Integer.parseInt(value));
				}
				else if (f.getType().equals(Combo.class))
				{
					Combo c = (Combo) f.get(this);
					if (c != null)
					{
						c.select(Integer.parseInt(value));
					}
				}
			}
			catch (SecurityException ex)
			{
				ex.printStackTrace();
			}
			catch (NoSuchFieldException ex)
			{
				// ignore, this just means we have a garbage property
			}
			catch (IllegalArgumentException ex)
			{
				ex.printStackTrace();
			}
			catch (IllegalAccessException ex)
			{
				ex.printStackTrace();
			}
		}
		if (btnFilterSize != null)
		{
			ableAll(sizeGroup, btnFilterSize.getSelection());
		}
		if (btnFilterType != null)
		{
			ableAll(typeGroup, btnFilterType.getSelection());
		}
		// if the location area is not set, fill it with something
		if (locationInput != null && (locationInput.getText() == null || locationInput.getText().length() == 0))
		{
			if (login.getHomeLocation() != null)
			{
				String homeLocText = login.getHomeLocation().toString();
				locationInput.setText(homeLocText);
			}
		}
	}

	/**
	 * Copy values from controls to this.properties.
	 */
	private void copyProperties()
	{
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			try
			{
				if (field.getType().equals(Button.class))
				{
					Button value = (Button) field.get(this);
					properties.setProperty(field.getName(), Boolean.toString(value.getSelection()));
				}
				else if (field.getType().equals(Text.class))
				{
					Text value = (Text) field.get(this);
					properties.setProperty(field.getName(), value.getText());
				}
				else if (field.getType().equals(Spinner.class))
				{
					Spinner value = (Spinner) field.get(this);
					properties.setProperty(field.getName(), Integer.toString(value.getSelection()));
				}
				else if (field.getType().equals(Combo.class))
				{
					Combo value = (Combo) field.get(this);
					properties.setProperty(field.getName(), Integer.toString(value.getSelectionIndex()));
				}
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

	}

	public Properties getSearchProperties()
	{
		return properties;
	}

	/**
	 * Enable or disable all controls in a group.
	 * 
	 * @param typeGroup2
	 * @param selection
	 */
	protected void ableAll(Group group, boolean selection)
	{
		Composite comp = (Composite) group;
		for (Control c : comp.getChildren())
		{
			c.setEnabled(selection);
		}
	}

	/**
	 * 
	 */
	protected void parseLocation()
	{
		String location = this.locationInput.getText();
		try
		{
			new Location(location);
		}
		catch (Exception e)
		{
			Shell shell = new Shell(getShell());

			ToolTip tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
			tip.setText("I could not understand the location");
			int x = getShell().getBounds().x;
			x += locationInput.getBounds().x + locationInput.getBounds().width;
			int y = getShell().getBounds().y;
			y += locationInput.getBounds().y + locationInput.getBounds().height * 1.5;

			tip.setMessage("Location must be same format as on GC.com website,\nfor example \"N 46 28.222 W 063 30.956\". The degree symbol is optional.");
			tip.setLocation(x, y);

			tip.setVisible(true);

			// force the focus back to the input field
			shell.getDisplay().asyncExec(new Runnable()
			{
				public void run()
				{
					locationInput.setFocus();
				}
			});

		}

	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		Button button_1 = createButton(parent, IDialogConstants.BACK_ID, "Search", true);
		button_1.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					copyProperties();

					Properties props = getSearchProperties();
					// save the properties
					String fileName = System.getProperty("user.home") + File.separator + PROPERTIES_FILENAME;
					props.store(new FileOutputStream(fileName, false), null);
					// start the search
					ListSearcher searcher = new ListSearcher(login);
					SearcherProgress progr = new SearcherProgress(searcher, login, idManager, props);
					ProgressMonitorDialog progdialog = new ProgressMonitorDialog(getShell());
					progdialog.run(true, true, progr);
					List<Cache> caches = progr.getCaches();

					// Generate GPX, ask user to save it
					GpxWriter writer = new GpxWriter(caches, idManager);

					FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
					fileDialog.setText("Save .gpx file");
					fileDialog.setOverwrite(true);
					String[] filterExt = { "*.gpx" };
					fileDialog.setFilterExtensions(filterExt);
					String outputName = fileDialog.open();
					if (outputName != null)
					{
						if (!outputName.toLowerCase().endsWith(".gpx"))
						{
							outputName += ".gpx";
						}
						writer.write(outputName);
					}
					idManager.saveDb();
				}
				catch (InvocationTargetException ex)
				{
					ex.printStackTrace();
				}
				catch (InterruptedException ex)
				{
					ex.printStackTrace();
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID, "Exit", false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize()
	{
		return new Point(642, 690);
	}
}
