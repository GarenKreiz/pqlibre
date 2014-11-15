package org.gpssearch.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.geoscrape.ListSearcher;
import org.geoscrape.Location;
import org.geoscrape.Login;
import org.gpssearch.MyCachesProgress;
import org.gpssearch.MyHidesProgress;
import org.gpssearch.Progress;
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

	private Properties properties;
	private Login login;
	private UserIdManager idManager;
	private String dateFormat;

	private Text locationInput;
	private Text afterDateField;
	private Text beforeDateField;
	private Text keywordText;
	private Text textOtherUsername;
	private Text textFoundSince;

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
	private Button btnLast;
	private Button btnUseHomeLocation;
	private Button btnEditAttributes;
	private Button btnEditDT;
	private Button btnAll;
	private Button btnFoundAfter;
	private Button btnMine;
	private Button btnUser;
	private Button btnHasNotBeen;
	private Button btnRequireFav;
	private Button btnIncludeLogs;
	private Button btnMatchKeyword;
	private Button btnFilterDifficultyterrain;
	private Label lblWhoseCacheHides;
	private Button btnMineHides;
	private Button btnUserHides;
	private Text textOtherUsernameHides;
	private Label lblHowManyCachesHides;
	private Button btnAllHides;
	private Spinner cacheCountHides;
	private Button btnLastHides;
	private Button btnFoundAfterHides;
	private Text textFoundSinceHides;
	
	private Group sizeGroup;
	private Group typeGroup;

	private Spinner foundInlastDaysField;
	private Spinner searchradius;
	private Spinner maximumresults;
	private Spinner cacheCount;
	private Spinner favouritePoints;

	private Combo searchRadiusUnits;

	private TabFolder tabFolder;

	private TabItem tabSearch;
	private TabItem tabMyfinds;
	private TabItem tabMyhides;

	private Label lblMaximumResults;
	private Label lblFavouritePoints;
	private Label lblDays;
	private Label lblLocation;
	private Label lblSearchRadius;
	private Label lblWhoseCacheFinds;
	private Label lblHowManyCaches;

	protected HashMap<Text, Button> belongButtonsUnselect;
	protected HashMap<Text, Button> belongButtonsSelect;

	private Button btnFullLogHides;

	private Image[] images;



	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public MainDialog(Image [] images, Login login)
	{
		super((Shell)null);
		this.images = images;
		setShellStyle(SWT.CLOSE | SWT.MIN | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.login = login;
		this.dateFormat = login.getDateFormat();
		properties = new Properties();
		belongButtonsUnselect = new HashMap<Text, Button>();
		belongButtonsSelect = new HashMap<Text, Button>();
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
		shell.setText("PQLibre 0.8.5 (beta)");
		shell.setImages(this.images);
		int desiredWidth = 665;
		int desiredHeight = 710;
		int heightDiff = shell.getBounds().height-shell.getClientArea().height;
		int widthDiff = shell.getBounds().width-shell.getClientArea().width;
		shell.setSize(desiredWidth+widthDiff, desiredHeight+heightDiff);
		shell.setLocation(200,200);
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

		// create tabs
		tabFolder = new TabFolder(container, SWT.NONE);
		tabFolder.setBounds(10, 20, 645, 632);

		setUpSearch();

		setUpMyfinds();
		
		setUpMyhides();

		setAllFontSizes();

		Link link = new Link(container, SWT.NONE);
		GuiTools.applyFontSize(link, 7);
		link.setTouchEnabled(true);
		link.setToolTipText("Click link to visit site");
		link.setBounds(536, 0, 169, 21);
		link.setText("Maps by <a href=\"http://gpsvisualizer.com\">GPSVisualizer.com</a>");

		link.addListener(SWT.Selection, new Listener()
		{
			// handle clicks on the link
			public void handleEvent(Event event)
			{
				try
				{
					if (Desktop.isDesktopSupported())
					{
						Desktop d = Desktop.getDesktop();
						d.browse(new URI(event.text));
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

		});

		applyProperties();
		return container;
	}

	/**
	 * Iterate over all declared fields. If a field is an instance of Control,
	 * set the font size to default.
	 */
	private void setAllFontSizes()
	{
		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields)
		{
			Object o;
			try
			{
				o = f.get(this);
				if (o instanceof Control)
				{
					GuiTools.applyDefaultFontSize((Control) o);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	private void setUpSearch()
	{
		tabSearch = new TabItem(tabFolder, SWT.NONE);
		tabSearch.setText("Cache search");

		Composite searchComposite = new Composite(tabFolder, SWT.NONE);
		searchComposite.setBounds(10, 10, 622, 632);
		tabSearch.setControl(searchComposite);

		lblLocation = new Label(searchComposite, SWT.NONE);
		lblLocation.setBounds(10, 10, 105, 15);
		lblLocation.setText("Location:");

		locationInput = new Text(searchComposite, SWT.BORDER);
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

		btnUseHomeLocation = new Button(searchComposite, SWT.NONE);
		btnUseHomeLocation.addSelectionListener(new SelectionAdapter()
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
		btnUseHomeLocation.setBounds(284, 28, 124, 24);
		btnUseHomeLocation.setText("Use home location");

		lblSearchRadius = new Label(searchComposite, SWT.NONE);
		lblSearchRadius.setBounds(10, 68, 105, 15);
		lblSearchRadius.setText("Search radius:");

		searchradius = new Spinner(searchComposite, SWT.BORDER);
		searchradius.setDigits(1);
		searchradius.setMaximum(10000);
		searchradius.setMinimum(1);
		searchradius.setSelection(100);
		searchradius.setBounds(10, 89, 81, 21);

		searchRadiusUnits = new Combo(searchComposite, SWT.READ_ONLY);
		searchRadiusUnits.setToolTipText("Select search radius units");
		searchRadiusUnits.setItems(new String[] { "km", "miles" });
		searchRadiusUnits.setBounds(100, 89, 64, 23);
		searchRadiusUnits.select(0);

		lblMaximumResults = new Label(searchComposite, SWT.NONE);
		lblMaximumResults.setText("Maximum results:");
		lblMaximumResults.setBounds(213, 68, 105, 15);

		maximumresults = new Spinner(searchComposite, SWT.BORDER);
		maximumresults.setToolTipText("Select the maximum number of results returned. 0 means unlimited.");
		maximumresults.setMaximum(100000);
		maximumresults.setBounds(213, 89, 81, 21);

		btnFilterDifficultyterrain = new Button(searchComposite, SWT.CHECK);
		btnFilterDifficultyterrain.setToolTipText("Only caches with the selected D/T combos are found.");
		btnFilterDifficultyterrain.setBounds(10, 116, 173, 26);
		btnFilterDifficultyterrain.setText("Filter Difficulty/Terrain");

		btnEditDT = new Button(searchComposite, SWT.NONE);
		btnEditDT.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				openDTDialog();
			}
		});
		btnEditDT.setBounds(197, 118, 81, 24);
		btnEditDT.setText("Edit");

		btnFilterAttributes = new Button(searchComposite, SWT.CHECK);
		btnFilterAttributes.setToolTipText("Only caches with the selected attributes are found");
		btnFilterAttributes.setText("Filter attributes");
		btnFilterAttributes.setBounds(353, 116, 131, 26);

		btnEditAttributes = new Button(searchComposite, SWT.NONE);
		btnEditAttributes.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				openAttributeDialog();
			}
		});
		btnEditAttributes.setText("Edit");
		btnEditAttributes.setBounds(490, 118, 81, 24);

		btnFilterSize = new Button(searchComposite, SWT.CHECK);
		btnFilterSize.setBounds(10, 148, 107, 26);
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

		sizeGroup = new Group(searchComposite, SWT.NONE);
		sizeGroup.setBounds(10, 175, 239, 140);

		btnMicroSize = new Button(sizeGroup, SWT.CHECK);
		btnMicroSize.setEnabled(false);
		btnMicroSize.setText("Micro");
		btnMicroSize.setBounds(10, 10, 63, 26);

		btnOtherSize = new Button(sizeGroup, SWT.CHECK);
		btnOtherSize.setEnabled(false);
		btnOtherSize.setText("Other");
		btnOtherSize.setBounds(128, 10, 63, 26);

		btnSmallSize = new Button(sizeGroup, SWT.CHECK);
		btnSmallSize.setEnabled(false);
		btnSmallSize.setText("Small");
		btnSmallSize.setBounds(10, 42, 63, 26);

		btnUknownSize = new Button(sizeGroup, SWT.CHECK);
		btnUknownSize.setEnabled(false);
		btnUknownSize.setText("Unknown");
		btnUknownSize.setBounds(128, 42, 95, 26);

		btnRegularSize = new Button(sizeGroup, SWT.CHECK);
		btnRegularSize.setEnabled(false);
		btnRegularSize.setText("Regular");
		btnRegularSize.setBounds(10, 74, 88, 26);

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

		btnFilterType = new Button(searchComposite, SWT.CHECK);
		btnFilterType.setBounds(255, 148, 107, 26);
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

		typeGroup = new Group(searchComposite, SWT.NONE);
		typeGroup.setBounds(255, 175, 372, 140);

		btnTraditional = new Button(typeGroup, SWT.CHECK);
		btnTraditional.setEnabled(false);
		btnTraditional.setText("Traditional");
		btnTraditional.setBounds(10, 10, 107, 26);

		btnEvent = new Button(typeGroup, SWT.CHECK);
		btnEvent.setEnabled(false);
		btnEvent.setText("Event");
		btnEvent.setBounds(123, 10, 95, 26);

		btnCito = new Button(typeGroup, SWT.CHECK);
		btnCito.setEnabled(false);
		btnCito.setText("CITO");
		btnCito.setBounds(240, 10, 95, 26);

		btnMystery = new Button(typeGroup, SWT.CHECK);
		btnMystery.setEnabled(false);
		btnMystery.setText("Mystery");
		btnMystery.setBounds(10, 42, 107, 26);

		btnMegaEvent = new Button(typeGroup, SWT.CHECK);
		btnMegaEvent.setEnabled(false);
		btnMegaEvent.setText("Mega Event");
		btnMegaEvent.setBounds(123, 42, 101, 26);

		btnEarthcache = new Button(typeGroup, SWT.CHECK);
		btnEarthcache.setEnabled(false);
		btnEarthcache.setText("Earthcache");
		btnEarthcache.setBounds(240, 42, 124, 26);

		btnMulti = new Button(typeGroup, SWT.CHECK);
		btnMulti.setEnabled(false);
		btnMulti.setText("Multi");
		btnMulti.setBounds(10, 74, 88, 26);

		btnVirtualType = new Button(typeGroup, SWT.CHECK);
		btnVirtualType.setEnabled(false);
		btnVirtualType.setText("Virtual");
		btnVirtualType.setBounds(123, 74, 95, 26);

		btnWherigo = new Button(typeGroup, SWT.CHECK);
		btnWherigo.setEnabled(false);
		btnWherigo.setText("Wherigo");
		btnWherigo.setBounds(240, 74, 95, 26);

		btnWebcam = new Button(typeGroup, SWT.CHECK);
		btnWebcam.setEnabled(false);
		btnWebcam.setText("Webcam");
		btnWebcam.setBounds(10, 106, 107, 26);

		btnLetterbox = new Button(typeGroup, SWT.CHECK);
		btnLetterbox.setEnabled(false);
		btnLetterbox.setText("Letterbox");
		btnLetterbox.setBounds(123, 106, 95, 26);

		btnGpsAdventure = new Button(typeGroup, SWT.CHECK);
		btnGpsAdventure.setEnabled(false);
		btnGpsAdventure.setText("GPS adventure");
		btnGpsAdventure.setBounds(240, 106, 109, 26);

		btnPlacedAfter = new Button(searchComposite, SWT.CHECK);
		btnPlacedAfter.setToolTipText("Only show caches placed on or after the specified date");
		btnPlacedAfter.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				afterDateField.setEnabled(btnPlacedAfter.getSelection());
			}

		});
		btnPlacedAfter.setBounds(10, 321, 138, 26);
		btnPlacedAfter.setText("Placed on or after");

		afterDateField = new Text(searchComposite, SWT.BORDER);
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
		afterDateField.setBounds(156, 321, 148, 21);
		belongButtonsUnselect.put(afterDateField, btnPlacedAfter);

		btnPlacedBefore = new Button(searchComposite, SWT.CHECK);
		btnPlacedBefore.setToolTipText("Only show caches placed on or before the specified date");
		btnPlacedBefore.setText("Placed on or before");
		btnPlacedBefore.setBounds(10, 353, 138, 26);
		btnPlacedBefore.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				beforeDateField.setEnabled(btnPlacedBefore.getSelection());
			}

		});

		beforeDateField = new Text(searchComposite, SWT.BORDER);
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
		beforeDateField.setBounds(156, 353, 148, 21);
		belongButtonsUnselect.put(beforeDateField, btnPlacedBefore);

		btnFoundInLast = new Button(searchComposite, SWT.CHECK);
		btnFoundInLast.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (btnFoundInLast.getSelection())
				{
					btnHasNotBeen.setSelection(false);
				}
				foundInlastDaysField.setEnabled(btnFoundInLast.getSelection());
			}
		});
		btnFoundInLast.setBounds(10, 385, 107, 26);
		btnFoundInLast.setText("Found in last ");

		foundInlastDaysField = new Spinner(searchComposite, SWT.BORDER);
		foundInlastDaysField.setMaximum(10000);
		foundInlastDaysField.setMinimum(1);
		foundInlastDaysField.setSelection(1);
		foundInlastDaysField.setBounds(118, 390, 67, 21);

		lblDays = new Label(searchComposite, SWT.NONE);
		lblDays.setBounds(192, 390, 57, 15);
		lblDays.setText("days");

		btnHasNotBeen = new Button(searchComposite, SWT.CHECK);
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
					foundInlastDaysField.setEnabled(false);
				}
			}
		});
		btnHasNotBeen.setBounds(255, 385, 173, 26);
		btnHasNotBeen.setText("Has not been found");

		btnIgnoreOwn = new Button(searchComposite, SWT.CHECK);
		btnIgnoreOwn.setToolTipText("Ignore caches you own");
		btnIgnoreOwn.setSelection(true);
		btnIgnoreOwn.setBounds(10, 417, 107, 26);
		btnIgnoreOwn.setText("Ignore own");

		btnIgnoreFound = new Button(searchComposite, SWT.CHECK);
		btnIgnoreFound.setBounds(123, 417, 126, 26);
		btnIgnoreFound.setToolTipText("Ignore caches you have already found");
		btnIgnoreFound.setSelection(true);
		btnIgnoreFound.setText("Ignore my finds");

		btnIgnoreDisabled = new Button(searchComposite, SWT.CHECK);
		btnIgnoreDisabled.setToolTipText("Ignore caches that have been disabled");
		btnIgnoreDisabled.setText("Ignore disabled");
		btnIgnoreDisabled.setBounds(255, 417, 131, 26);

		btnHasTrackable = new Button(searchComposite, SWT.CHECK);
		btnHasTrackable.setToolTipText("Only show caches that contains trackables.");
		btnHasTrackable.setText("Contains trackable");
		btnHasTrackable.setBounds(10, 449, 154, 26);

		btnRequireFav = new Button(searchComposite, SWT.CHECK);
		btnRequireFav.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				favouritePoints.setEnabled(btnRequireFav.getSelection());
			}
		});
		btnRequireFav.setText("Has at least ");
		btnRequireFav.setBounds(10, 481, 107, 26);

		favouritePoints = new Spinner(searchComposite, SWT.BORDER);
		favouritePoints.setPageIncrement(1);
		favouritePoints.setMaximum(10000);
		favouritePoints.setMinimum(1);
		favouritePoints.setSelection(1);
		favouritePoints.setBounds(118, 486, 67, 21);

		lblFavouritePoints = new Label(searchComposite, SWT.NONE);
		lblFavouritePoints.setText("favourite points");
		lblFavouritePoints.setBounds(192, 486, 112, 15);

		btnMatchKeyword = new Button(searchComposite, SWT.CHECK);
		btnMatchKeyword.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				keywordText.setEnabled(btnMatchKeyword.getSelection());
			}
		});
		btnMatchKeyword.setBounds(10, 513, 173, 26);
		btnMatchKeyword.setText("Title contains keyword:");

		keywordText = new Text(searchComposite, SWT.BORDER);
		keywordText.setEnabled(false);
		keywordText.setBounds(183, 513, 203, 21);

		btnIncludeLogs = new Button(searchComposite, SWT.CHECK);
		btnIncludeLogs.setBounds(10, 545, 220, 26);
		btnIncludeLogs.setText("Include full logs in output");
	}
	
	private void setUpMyhides()
	{
		tabMyhides = new TabItem(tabFolder, SWT.NONE);
		tabMyhides.setText("My hides");
		Composite myhidesComposite = new Composite(tabFolder, SWT.NONE);
		myhidesComposite.setBounds(10, 10, 622, 642);
		tabMyhides.setControl(myhidesComposite);

		lblWhoseCacheHides = new Label(myhidesComposite, SWT.NONE);
		lblWhoseCacheHides.setBounds(10, 10, 326, 15);
		lblWhoseCacheHides.setText("Whose cache hides to download: ");

		Composite compositeWhose = new Composite(myhidesComposite, SWT.BORDER);
		compositeWhose.setEnabled(true);
		compositeWhose.setBounds(10, 31, 337, 84);

		btnMineHides = new Button(compositeWhose, SWT.RADIO);
		btnMineHides.setEnabled(true);
		btnMineHides.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyhidesButtons();
			}
		});
		btnMineHides.setBounds(10, 10, 103, 26);
		btnMineHides.setSelection(true);
		btnMineHides.setText("mine");

		btnUserHides = new Button(compositeWhose, SWT.RADIO);
		btnUserHides.setEnabled(true);
		btnUserHides.setBounds(10, 42, 59, 26);
		btnUserHides.setText("user: ");
		btnUserHides.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyhidesButtons();
			}
		});

		textOtherUsernameHides = new Text(compositeWhose, SWT.BORDER);
		textOtherUsernameHides.setBounds(74, 47, 183, 21);
		textOtherUsernameHides.setEnabled(true);

		lblHowManyCachesHides = new Label(myhidesComposite, SWT.NONE);
		lblHowManyCachesHides.setBounds(10, 120, 257, 15);
		lblHowManyCachesHides.setText("How many caches to download:");

		Composite compositeHowmany = new Composite(myhidesComposite, SWT.BORDER);
		compositeHowmany.setEnabled(true);
		compositeHowmany.setBounds(10, 141, 337, 121);

		btnAllHides = new Button(compositeHowmany, SWT.RADIO);
		btnAllHides.setEnabled(true);
		btnAllHides.setBounds(10, 10, 103, 26);
		btnAllHides.setSelection(true);
		btnAllHides.setText("all");
		btnAllHides.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyhidesButtons();
			}
		});

		btnLastHides = new Button(compositeHowmany, SWT.RADIO);
		btnLastHides.setEnabled(true);
		btnLastHides.setBounds(10, 42, 49, 26);
		btnLastHides.setText("last");
		btnLastHides.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyhidesButtons();
			}
		});

		cacheCountHides = new Spinner(compositeHowmany, SWT.BORDER);
		cacheCountHides.setBounds(74, 42, 84, 21);
		cacheCountHides.setMaximum(10000);
		cacheCountHides.setMinimum(1);
		cacheCountHides.setEnabled(false);

		btnFoundAfterHides = new Button(compositeHowmany, SWT.RADIO);
		btnFoundAfterHides.setEnabled(true);
		btnFoundAfterHides.setBounds(10, 74, 118, 26);
		btnFoundAfterHides.setText("all found since");
		btnFoundAfterHides.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyhidesButtons();
			}
		});

		textFoundSinceHides = new Text(compositeHowmany, SWT.BORDER);
		textFoundSinceHides.setBounds(131, 79, 110, 21);
		textFoundSinceHides.setEnabled(false);
		textFoundSinceHides.setToolTipText("Use the same date format as you use on geocaching.com: " + this.dateFormat);
		textFoundSinceHides.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (btnFoundAfterHides.getSelection())
				{
					verifyDate(textFoundSinceHides);
				}
			}
		});
		

		btnFullLogHides = new Button(myhidesComposite, SWT.CHECK);
		btnFullLogHides.setBounds(10, 545, 220, 26);
		btnFullLogHides.setText("Include full logs in output");
		
		
		belongButtonsUnselect.put(textFoundSinceHides, btnFoundAfterHides);
		belongButtonsSelect.put(textFoundSinceHides, btnAllHides);
		
	}

	/**
	 * 
	 */
	private void setUpMyfinds()
	{
		tabMyfinds = new TabItem(tabFolder, SWT.NONE);
		tabMyfinds.setText("My finds");
		Composite myfindsComposite = new Composite(tabFolder, SWT.NONE);
		myfindsComposite.setBounds(10, 10, 622, 642);
		tabMyfinds.setControl(myfindsComposite);

		lblWhoseCacheFinds = new Label(myfindsComposite, SWT.NONE);
		lblWhoseCacheFinds.setBounds(10, 10, 326, 15);
		lblWhoseCacheFinds.setText("Whose cache finds to download: ");

		Composite compositeWhose = new Composite(myfindsComposite, SWT.BORDER);
		compositeWhose.setEnabled(true);
		compositeWhose.setBounds(10, 31, 337, 84);

		btnMine = new Button(compositeWhose, SWT.RADIO);
		btnMine.setEnabled(true);
		btnMine.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyfindsButtons();
			}
		});
		btnMine.setBounds(10, 10, 103, 26);
		btnMine.setSelection(true);
		btnMine.setText("mine");

		btnUser = new Button(compositeWhose, SWT.RADIO);
		btnUser.setEnabled(true);
		btnUser.setBounds(10, 42, 59, 26);
		btnUser.setText("user: ");
		btnUser.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyfindsButtons();
			}
		});

		textOtherUsername = new Text(compositeWhose, SWT.BORDER);
		textOtherUsername.setBounds(74, 47, 183, 21);
		textOtherUsername.setEnabled(true);

		lblHowManyCaches = new Label(myfindsComposite, SWT.NONE);
		lblHowManyCaches.setBounds(10, 120, 257, 15);
		lblHowManyCaches.setText("How many caches to download:");

		Composite compositeHowmany = new Composite(myfindsComposite, SWT.BORDER);
		compositeHowmany.setEnabled(true);
		compositeHowmany.setBounds(10, 141, 337, 121);

		btnAll = new Button(compositeHowmany, SWT.RADIO);
		btnAll.setEnabled(true);
		btnAll.setBounds(10, 10, 103, 26);
		btnAll.setSelection(true);
		btnAll.setText("all");
		btnAll.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyfindsButtons();
			}
		});

		btnLast = new Button(compositeHowmany, SWT.RADIO);
		btnLast.setEnabled(true);
		btnLast.setBounds(10, 42, 49, 26);
		btnLast.setText("last");
		btnLast.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyfindsButtons();
			}
		});

		cacheCount = new Spinner(compositeHowmany, SWT.BORDER);
		cacheCount.setBounds(74, 42, 84, 21);
		cacheCount.setMaximum(10000);
		cacheCount.setMinimum(1);
		cacheCount.setEnabled(false);

		btnFoundAfter = new Button(compositeHowmany, SWT.RADIO);
		btnFoundAfter.setEnabled(true);
		btnFoundAfter.setBounds(10, 74, 118, 26);
		btnFoundAfter.setText("all found since");
		btnFoundAfter.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				checkMyfindsButtons();
			}
		});

		textFoundSince = new Text(compositeHowmany, SWT.BORDER);
		textFoundSince.setBounds(131, 79, 110, 21);
		textFoundSince.setEnabled(false);
		textFoundSince.setToolTipText("Use the same date format as you use on geocaching.com: " + this.dateFormat);
		textFoundSince.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (btnFoundAfter.getSelection())
				{
					verifyDate(textFoundSince);
				}
			}
		});
		belongButtonsUnselect.put(textFoundSince, btnFoundAfter);
		belongButtonsSelect.put(textFoundSince, btnAll);
	}

	/**
	 * Check that the correct fields are enabled/disabled on the "my finds" page.
	 */
	protected void checkMyfindsButtons()
	{
		if (textOtherUsername.getEnabled() != btnUser.getSelection())
		{
			textOtherUsername.setEnabled(btnUser.getSelection());
		}
		if (btnLast.getSelection() != cacheCount.getEnabled())
		{
			cacheCount.setEnabled(btnLast.getSelection());
		}
		if (btnFoundAfter.getSelection() != textFoundSince.getEnabled())
		{
			textFoundSince.setEnabled(btnFoundAfter.getSelection());
		}
	}
	/**
	 * Check that the correct fields are enabled/disabled on the "my finds" page.
	 */
	protected void checkMyhidesButtons()
	{
		if (textOtherUsernameHides.getEnabled() != btnUserHides.getSelection())
		{
			textOtherUsernameHides.setEnabled(btnUserHides.getSelection());
		}
		if (btnLastHides.getSelection() != cacheCountHides.getEnabled())
		{
			cacheCountHides.setEnabled(btnLastHides.getSelection());
		}
		if (btnFoundAfterHides.getSelection() != textFoundSinceHides.getEnabled())
		{
			textFoundSinceHides.setEnabled(btnFoundAfterHides.getSelection());
		}
	}

	/**
	 * Check that the date entered in the date field corresponds to what the
	 * application expects, pop up a notification otherwise
	 * 
	 * @param dateField
	 */
	protected boolean verifyDate(final Text dateField)
	{
		boolean res = true;
		if (dateField.getEnabled())
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
						Button b = belongButtonsUnselect.get(dateField);
						if (b != null)
						{
							b.setSelection(false);
						}
						b = belongButtonsSelect.get(dateField);
						if (b != null)
						{
							b.setSelection(true);
						}
						dateField.setEnabled(false);
					}
				});
				res = false;
			}
		}
		return res;
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
		// set the keywordText abledness to selection of corresponding button
		if (keywordText != null && btnMatchKeyword != null)
		{
			keywordText.setEnabled(btnMatchKeyword.getSelection());
		}
		if (btnPlacedBefore != null && beforeDateField != null)
		{
			beforeDateField.setEnabled(btnPlacedBefore.getSelection());
		}
		if (btnPlacedAfter != null && afterDateField != null)
		{
			afterDateField.setEnabled(btnPlacedAfter.getSelection());
		}
		if (favouritePoints != null && btnRequireFav != null)
		{
			favouritePoints.setEnabled(btnRequireFav.getSelection());
		}
		if (foundInlastDaysField != null && btnFoundInLast != null)
		{
			foundInlastDaysField.setEnabled(btnFoundInLast.getSelection());
		}
		checkMyfindsButtons();
		checkMyhidesButtons();
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

			tip.setMessage("Location must be in one of the following formats:\n" +
					"MinDec (e.g. \"N 46° 28.222' W 063° 30.956'\")\n" +
					"DegDec (e.g. \"46.47037 -63.51593\")\n" +
					"DMS (e.g. \"N 46° 28' 13.332\" W 63° 30' 57.348\"\")\n" +
					"The degree, minute, and second symbols are optional.");
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
		Button btnSearch = createButton(parent, IDialogConstants.BACK_ID, "Search", true);
		btnSearch.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					// get the properties
					copyProperties();

					Properties props = getSearchProperties();
					TabItem[] sel = tabFolder.getSelection();

					// check all date fields
					if (sel[0].equals(tabSearch))
					{
						if (!verifyDate(afterDateField))
						{
							props.setProperty("btnPlacedAfter", "false");
						}
						if (!verifyDate(beforeDateField))
						{
							props.setProperty("btnPlacedBefore", "false");
						}
					}
					else if (sel[0].equals(tabMyfinds))
					{
						if (!verifyDate(textFoundSince))
						{
							//invalid date, reset options
							props.setProperty("btnAll", "true");
							props.setProperty("btnFoundAfter", "false");
						}
					}
					else if (sel[0].equals(tabMyhides))
					{
						if (!verifyDate(textFoundSinceHides))
						{
							//invalid date, reset options
							props.setProperty("btnAllHides", "true");
							props.setProperty("btnFoundAfterHides", "false");
						}
					}
					// save the properties
					String fileName = System.getProperty("user.home") + File.separator + PROPERTIES_FILENAME;
					props.store(new FileOutputStream(fileName, false), null);
					// start the search
					ListSearcher searcher = new ListSearcher(login);
					Progress progr = null;
					//create a temporary file
					File tmpFile = File.createTempFile("gpxsearchersearch", ".bin");
					tmpFile.deleteOnExit();
					if (sel.length > 0)
					{
						if (sel[0].equals(tabSearch))
						{
							progr = new SearcherProgress(searcher,tmpFile, login, idManager, props,getShell());
						}
						else if (sel[0].equals(tabMyfinds))
						{
							progr = new MyCachesProgress(searcher,tmpFile, login, idManager, props,getShell());
						}
						else if(sel[0].equals(tabMyhides))
						{
							progr = new MyHidesProgress(searcher,tmpFile, login, idManager, props,getShell());
						}
						ProgressMonitorDialog progdialog = new ProgressMonitorDialog(getShell());
						progdialog.run(true, true, progr);
						progr.closeFile();
						int count = progr.getCount();
						ResultDialog resDiag = new ResultDialog(getShell(), SWT.APPLICATION_MODAL);
						resDiag.open(count,tmpFile, idManager, login.getUserName());
						idManager.saveDb();
					}
					tmpFile.delete();
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
		GuiTools.applyDefaultFontSize(btnSearch);
		Button btnExit = createButton(parent, IDialogConstants.CANCEL_ID, "Exit", false);
		GuiTools.applyDefaultFontSize(btnExit);
	}
}
