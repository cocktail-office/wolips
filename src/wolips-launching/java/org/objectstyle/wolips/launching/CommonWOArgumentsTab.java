/* ====================================================================
 *
 * The ObjectStyle Group Software License, Version 1.0
 *
 * Copyright (c) 2002 The ObjectStyle Group
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne"
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */

package org.objectstyle.wolips.launching;

import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.launcher.JavaLaunchConfigurationTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.objectstyle.wolips.core.plugin.IWOLipsPluginConstants;
import org.objectstyle.wolips.core.plugin.WOLipsPlugin;
import org.objectstyle.wolips.core.preferences.ILaunchInfo;
import org.objectstyle.wolips.core.preferences.Preferences;
import org.objectstyle.wolips.core.preferences.PreferencesMessages;
import org.objectstyle.wolips.logging.WOLipsLog;
/**
 * @author uli
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CommonWOArgumentsTab extends JavaLaunchConfigurationTab {

	private Table includeTable;
	private Button addButton;
	private Button removeButton;
	private String preferencesKey = IWOLipsPluginConstants.PREF_LAUNCH_GLOBAL;

	private Vector allParameter;
	private Vector allArguments;
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private boolean isTableFilled = false;
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parentComposite) {

		Composite parent = new Composite(parentComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		parent.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		parent.setLayoutData(data);

		// set F1 help
		//WorkbenchHelp.setHelp(parent, IHelpContextIds.IGNORE_PREFERENCE_PAGE);

		Label l1 = new Label(parent, SWT.NULL);
		l1.setText(PreferencesMessages.getString("LaunchPreferencesPage.label")); //$NON-NLS-1$
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		data.horizontalSpan = 3;
		l1.setLayoutData(data);

		//includeTable = new Table(parent, SWT.CHECK | SWT.BORDER);
		includeTable = new Table(parent, SWT.CHECK | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		//gd.widthHint = convertWidthInCharsToPixels(30);
		gd.widthHint = 150;
		gd.heightHint = 200;
		includeTable.setLayoutData(gd);
		includeTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleSelection();
			}
		});

		Composite buttons = new Composite(parent, SWT.NULL);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		addButton = new Button(buttons, SWT.PUSH);
		addButton.setText(PreferencesMessages.getString("LaunchPreferencesPage.add")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = 20;
		//convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = 100;
		//convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint =
			Math.max(
				widthHint,
				addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		addButton.setLayoutData(data);
		addButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addIgnore();
			}
		});

		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText(PreferencesMessages.getString("LaunchPreferencesPage.remove")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = 20;
		//Dialog.convertVerticalDLUsToPixels(new FontMetrics(), IDialogConstants.BUTTON_HEIGHT);
		widthHint = 100;
		//Dialog.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint =
			Math.max(
				widthHint,
				removeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		removeButton.setLayoutData(data);
		removeButton.setEnabled(false);
		removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				removeIgnore();
			}
		});
		
		Dialog.applyDialogFont(parent);
		this.setControl(parent);
	}

	/**
	 * @param ignore
	 */
	private void fillTable(ILaunchInfo[] launchInfoArray) {
		if(isTableFilled) return;
		isTableFilled = true;
		allArguments = new Vector();
		allParameter = new Vector();
		for (int i = 0; i < launchInfoArray.length; i++) {
			ILaunchInfo launchInfo = launchInfoArray[i];
			TableItem item = new TableItem(includeTable, SWT.NONE);
			item.setText(
				launchInfo.getParameter() + " " + launchInfo.getArgument());
			allParameter.add(launchInfo.getParameter());
			allArguments.add(launchInfo.getArgument());
			item.setChecked(launchInfo.isEnabled());
		}
	}

	private void addIgnore() {
		InputDialog parameterDialog = new InputDialog(getShell(), PreferencesMessages.getString("LaunchPreferencesPage.enterParameterShort"), Preferences.getString("IgnorePreferencePage.enterPatternLong"), null, null); //$NON-NLS-1$ //$NON-NLS-2$
		parameterDialog.open();
		if (parameterDialog.getReturnCode() != InputDialog.OK)
			return;
		InputDialog argumentDialog = new InputDialog(getShell(), PreferencesMessages.getString("LaunchPreferencesPage.enterArgumentShort"), Preferences.getString("IgnorePreferencePage.enterPatternLong"), null, null); //$NON-NLS-1$ //$NON-NLS-2$
		argumentDialog.open();
		if (argumentDialog.getReturnCode() != InputDialog.OK)
			return;
		String parameter = parameterDialog.getValue();
		String argument = argumentDialog.getValue();
		if (parameter.equals("") || argument.equals(""))
			return; //$NON-NLS-1$
		// Check if the item already exists
		TableItem[] items = includeTable.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getText(1).equals(parameter)) {
				MessageDialog.openWarning(getShell(), PreferencesMessages.getString("LaunchPreferencesPage.parameterExistsShort"), Preferences.getString("IgnorePreferencePage.patternExistsLong")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		}
		TableItem item = new TableItem(includeTable, SWT.NONE);
		item.setText(parameter + " " + argument);
		allParameter.add(parameter);
		allArguments.add(argument);
		item.setChecked(true);
	}

	private void removeIgnore() {
		int[] selection = includeTable.getSelectionIndices();
		includeTable.remove(selection);
		allParameter.remove(selection);
		allArguments.remove(selection);
	}
	private void handleSelection() {
		if (includeTable.getSelectionCount() > 0) {
			removeButton.setEnabled(true);
		} else {
			removeButton.setEnabled(false);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		return true;
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		String string = this.getDefaultArguments(config);
		config.setAttribute(
			WOJavaLocalApplicationLaunchConfigurationDelegate
				.ATTR_WOLIPS_LAUNCH_WOARGUMENTS,
			string);
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String string = configuration.getAttribute(WOJavaLocalApplicationLaunchConfigurationDelegate.ATTR_WOLIPS_LAUNCH_WOARGUMENTS, ""); //$NON-NLS-1$
			this.fillTable(Preferences.getLaunchInfoFrom(string));
		} catch (CoreException e) {
			setErrorMessage(LaunchingMessages.getString("WOArgumentsTab.Exception_occurred_reading_configuration___15") + e.getStatus().getMessage()); //$NON-NLS-1$
			JDIDebugUIPlugin.log(e);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		int count = includeTable.getItemCount();
		String[] parameter = new String[count];
		String[] arguments = new String[count];
		boolean[] enabled = new boolean[count];
		TableItem[] items = (TableItem[]) includeTable.getItems();
		for (int i = 0; i < count; i++) {
			parameter[i] = (String) allParameter.get(i);
			arguments[i] = (String) allArguments.get(i);
			enabled[i] = items[i].getChecked();
		}
		String string =
			Preferences.LaunchInfoToString(parameter, arguments, enabled);
		configuration.setAttribute(
			WOJavaLocalApplicationLaunchConfigurationDelegate
				.ATTR_WOLIPS_LAUNCH_WOARGUMENTS,
			string);
	}

	/**
	 * Retuns the string in the text widget, or <code>null</code> if empty.
	 * 
	 * @return text or <code>null</code>
	 */
	/*protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		if (content.length() > 0) {
			return content;
		}
		return null;
	}*/

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchingMessages.getString("CommonWOArgumentsTab.Name"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}
	/**
	 * @see ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		return super.getErrorMessage();
	}

	/**
	 * @see ILaunchConfigurationTab#getMessage()
	 */
	public String getMessage() {
		return super.getMessage();
	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_VIEW_ARGUMENTS_TAB);
	}

	/**
	 * Method getDefaultArguments.
	 * @return String
	 */
	private String getDefaultArguments(ILaunchConfigurationWorkingCopy config) {
		try {
			String path =
				config.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					(String) null);
			IPath aPath = null;
			if (path != null) {
				aPath = new Path(path);
			}
			IResource res =
				ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (res instanceof IContainer && res.exists()) {
				IResource aRes =
					((IContainer) res).findMember(aPath.toString() + ".woa");
				if (aRes != null) {
					if (aRes instanceof IContainer && aRes.exists()) {
						config.setAttribute(
							IJavaLaunchConfigurationConstants
								.ATTR_WORKING_DIRECTORY,
							((IContainer) aRes)
								.getFullPath()
								.toString()
								.substring(
								1));
					}
				}
			}

		} catch (Exception anException) {
			WOLipsLog.log(anException);
		}
		/*return this.getWOApplicationPlatformSpecificArguments()
			+ this.getWOApplicationClassNameArgument(config)
			+ this.getCommonWOApplicationArguments();*/
		return Preferences.getString(preferencesKey);
	}

	/**
	 * Method getWOApplicationPlatformSpecificArguments.
	 * @return String
	 */
	private String getWOApplicationPlatformSpecificArguments() {
		if (WOLipsPlugin
			.getDefault()
			.getWOEnvironment()
			.getWOVariables()
			.systemRoot()
			.startsWith("/System"))
			return "";
		return "-DWORoot="
			+ WOLipsPlugin
				.getDefault()
				.getWOEnvironment()
				.getWOVariables()
				.systemRoot()
			+ " "
			+ "-DWORootDirectory="
			+ WOLipsPlugin
				.getDefault()
				.getWOEnvironment()
				.getWOVariables()
				.systemRoot()
			+ " ";
	}

	/**
	 * Method getWOApplicationClassNameArgument.
	 * @return String
	 */
	private String getWOApplicationClassNameArgument(ILaunchConfigurationWorkingCopy config) {
		String main = null;
		try {
			main =
				config.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
					"");
		} catch (Exception anException) {
			WOLipsLog.log(anException);
			return "";
		}
		if ("".equals(main))
			return "";
		return "-WOApplicationClassName " + main + " ";
	}

	/**
	 * Method getCommonWOApplicationArguments.
	 * @return String
	 */
	private String getCommonWOApplicationArguments() {
		return LaunchingMessages.getString("WOArguments.common");
	}

	private GridData fillIntoGrid(Control control, int hspan, boolean grab) {
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = hspan;
		gd.grabExcessHorizontalSpace = grab;
		control.setLayoutData(gd);
		return gd;
	}

}
