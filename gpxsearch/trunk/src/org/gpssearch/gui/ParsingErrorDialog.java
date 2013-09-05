package org.gpssearch.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog that informs the user that something went wrong and asks what to do now.
 * 
 *
 */
public class ParsingErrorDialog extends Dialog
{

	protected Object result;
	protected Shell shlParsingError;
	private String cacheCode = "<CACHE CODE>";
	protected boolean doContinue = true;
	protected boolean doCancel = false;
	protected boolean doIgnore = false;
	private Button btnIgnoreErrors;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ParsingErrorDialog(Shell parent, String cacheCode)
	{
		super(parent);
		setText("-");
		this.cacheCode = cacheCode;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open()
	{
		createContents();
		shlParsingError.open();
		shlParsingError.layout();
		Display display = getParent().getDisplay();
		while (!shlParsingError.isDisposed())
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
		shlParsingError = new Shell(getParent(), SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL );
		shlParsingError.setSize(450, 226);
		shlParsingError.setText("Parsing error");
		
		btnIgnoreErrors = new Button(shlParsingError, SWT.CHECK);
		btnIgnoreErrors.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doIgnore = btnIgnoreErrors.getSelection();
			}
		});
		btnIgnoreErrors.setBounds(10, 147, 167, 26);
		btnIgnoreErrors.setText("Ignore further errors");
		GuiTools.applyFontSize(btnIgnoreErrors,9);
		
		Button btnContinue = new Button(shlParsingError, SWT.NONE);
		btnContinue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doContinue = true;
				shlParsingError.close();
			}
		});
		btnContinue.setBounds(10, 117, 100, 24);
		btnContinue.setText("Continue");
		GuiTools.applyFontSize(btnContinue,9);
		
		Button btnCancel = new Button(shlParsingError, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doCancel = true;
				shlParsingError.close();
			}
		});
		btnCancel.setBounds(335, 117, 100, 24);
		btnCancel.setText("Cancel");
		
		Label lblThereWasA = new Label(shlParsingError, SWT.CENTER);
		lblThereWasA.setBounds(10, 10, 425, 15);
		lblThereWasA.setText("There was a problem parsing cache");
		GuiTools.applyFontSize(lblThereWasA,9);
		
		Label lblNewLabel = new Label(shlParsingError, SWT.CENTER);
		GuiTools.applyFontSize(lblNewLabel,12);
		lblNewLabel.setBounds(10, 31, 425, 38);
		lblNewLabel.setText(this.cacheCode);
		
		Label lblWhatWouldYou = new Label(shlParsingError, SWT.CENTER);
		lblWhatWouldYou.setText("What would you like to do?");
		lblWhatWouldYou.setBounds(10, 91, 425, 15);
		GuiTools.applyFontSize(lblWhatWouldYou,9);

	}

	/**
	 * @return the doContinue
	 */
	public boolean isDoContinue()
	{
		return doContinue;
	}

	/**
	 * @return the doCancel
	 */
	public boolean isDoCancel()
	{
		return doCancel;
	}

	/**
	 * @return the doIgnore
	 */
	public boolean isDoIgnore()
	{
		return doIgnore;
	}
	
	
}
