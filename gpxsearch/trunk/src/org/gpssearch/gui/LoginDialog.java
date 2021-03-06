package org.gpssearch.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple dialog that lets users enter login information.
 * 
 */
public class LoginDialog extends Dialog
{

	protected Object result;
	protected Shell shell;

	private boolean doLogin = false;
	private Text usernameField;
	private Text passwordField;
	private Button checkboxRemember;
	private String username;
	private String password;
	private boolean remember = true;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public LoginDialog(Shell parent)
	{
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		setText("Geocaching.com login");
	}

	/**
	 * Open the dialog.
	 * 
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
		shell = new Shell(getParent(), SWT.BORDER | SWT.TITLE
				| SWT.APPLICATION_MODAL);
		shell.setSize(368, 224);
		shell.setText(getText());
		shell.setLayout(null);

		Label lblUsername = new Label(shell, SWT.NONE);
		lblUsername.setBounds(10, 29, 68, 15);
		lblUsername.setText("Username:");
		GuiTools.applyDefaultFontSize(lblUsername);

		usernameField = new Text(shell, SWT.BORDER);
		usernameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR)
				{
					//enter was pressed, go to password field
					shell.getDisplay().asyncExec(new Runnable()
					{
						public void run()
						{
							passwordField.setFocus();
						}
					});
				}
			}
		});
		usernameField.setBounds(10, 50, 212, 21);
		if (username != null)
		{
			usernameField.setText(username);
		}
		GuiTools.applyDefaultFontSize(usernameField);

		Label lblPassword = new Label(shell, SWT.NONE);
		lblPassword.setBounds(10, 86, 68, 15);
		lblPassword.setText("Password:");
		GuiTools.applyDefaultFontSize(lblPassword);

		passwordField = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		passwordField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR)
				{
					//enter was pressed, try logging in
					shell.getDisplay().asyncExec(new Runnable()
					{
						public void run()
						{
							remember = checkboxRemember.getSelection();
							password = passwordField.getText();
							username = usernameField.getText();
							doLogin = true;
							shell.close();
						}
					});
				}
			}
		});
		passwordField.setBounds(10, 107, 212, 21);
		if (password != null)
		{
			passwordField.setText(password);
		}
		GuiTools.applyDefaultFontSize(passwordField);

		checkboxRemember = new Button(shell, SWT.CHECK);
		checkboxRemember.setSelection(remember);
		checkboxRemember.setBounds(10, 156, 212, 26);
		checkboxRemember.setText("Remember login");
		GuiTools.applyDefaultFontSize(checkboxRemember);

		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				remember = checkboxRemember.getSelection();
				password = passwordField.getText();
				username = usernameField.getText();
				doLogin = false;
				shell.close();
			}
		});
		btnCancel.setBounds(264, 158, 81, 24);
		btnCancel.setText("Cancel");
		GuiTools.applyDefaultFontSize(btnCancel);

		Button btnLogin = new Button(shell, SWT.NONE);
		btnLogin.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				remember = checkboxRemember.getSelection();
				password = passwordField.getText();
				username = usernameField.getText();
				doLogin = true;
				shell.close();
			}
		});
		btnLogin.setBounds(264, 128, 81, 24);
		btnLogin.setText("Login");
		GuiTools.applyDefaultFontSize(btnLogin);
		shell.setTabList(new Control[] { usernameField, passwordField,
				checkboxRemember, btnLogin, btnCancel });

	}

	public boolean isLogin()
	{
		return doLogin;
	}

	/**
	 * @return
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @return
	 */
	public String getUsername()
	{
		return username;
	}

	public boolean isSaveCredentials()
	{
		return remember;
	}

	/**
	 * @param loadUsername
	 */
	public void setUsername(String username)
	{
		this.username = username;
		if (username != null && usernameField != null)
		{
			usernameField.setText(username);
		}

	}

	/**
	 * @param loadPassword
	 */
	public void setPassword(String password)
	{
		this.password = password;
		if (password != null && passwordField != null)
		{
			passwordField.setText(password);
		}
	}

	/**
	 * @param loadSaveCredentials
	 */
	public void setRemember(boolean loadSaveCredentials)
	{
		this.remember = loadSaveCredentials;
		if (checkboxRemember != null)
		{
			checkboxRemember.setSelection(loadSaveCredentials);
		}
	}
}
