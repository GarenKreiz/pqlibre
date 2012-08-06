package org.gpssearch.gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Notification dialog that shows users that their login failed.
 * 
 */
public class LoginFailedDialog extends Dialog
{

	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public LoginFailedDialog(Shell parent, int style)
	{
		super(parent, style);
		setText("Login failed");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open()
	{
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
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
		shell = new Shell(getParent(), SWT.BORDER);
		shell.setSize(244, 139);
		shell.setText(getText());
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		btnOk.setBounds(79, 66, 81, 24);
		btnOk.setText("OK");
		
		Label lblLoginFailed = new Label(shell, SWT.NONE);
		lblLoginFailed.setAlignment(SWT.CENTER);
		lblLoginFailed.setBounds(31, 27, 176, 15);
		lblLoginFailed.setText("Login failed.");

	}

}
