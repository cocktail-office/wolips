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

package org.objectstyle.wolips.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.objectstyle.wolips.plugin.IWOLipsPluginConstants;
import org.objectstyle.wolips.plugin.WOLipsPlugin;

/**
 * @author uli
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Preferences {

	private static final String trueString = "true";
	private static final String falseString = "false";
	/**
	 * Method setDefaults.
	 */
	public static void setDefaults() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(
			IWOLipsPluginConstants.PREF_ANT_BUILD_FILE,
			org.apache.tools.ant.Main.DEFAULT_BUILD_FILENAME);
		store.setDefault(
			IWOLipsPluginConstants.PREF_RUN_WOBUILDER_ON_BUILD,
			Preferences.trueString);
		store.setDefault(
			IWOLipsPluginConstants.PREF_RUN_ANT_AS_EXTERNAL_TOOL,
			Preferences.falseString);
		store.setDefault(
			IWOLipsPluginConstants.PREF_MODEL_NAVIGATOR_FILTER,
			PreferencesMessages.getString(
				"Preferences.ModelNavigatorFilter.Default"));
		store.setDefault(
			IWOLipsPluginConstants.PREF_WO_NAVIGATOR_FILTER,
			PreferencesMessages.getString(
				"Preferences.WONavigatorFilter.Default"));
		store.setDefault(
			IWOLipsPluginConstants.PREF_PRODUCT_NAVIGATOR_FILTER,
			PreferencesMessages.getString(
				"Preferences.ProductNavigatorFilter.Default"));
		store.setDefault(
			IWOLipsPluginConstants
				.PREF_OPEN_WOCOMPONENT_ACTION_INCLUDES_OPEN_HTML,
			Preferences.falseString);
		store.setDefault(
			IWOLipsPluginConstants.PREF_SHOW_BUILD_OUTPUT,
			Preferences.falseString);
		store.setDefault(
			IWOLipsPluginConstants.PREF_NS_PROJECT_SEARCH_PATH,
			"");
		store.setDefault(
			IWOLipsPluginConstants
				.PREF_REBUILD_WOBUILD_PROPERTIES_ON_NEXT_LAUNCH,
			PreferencesMessages.getString(Preferences.falseString));
		store.setDefault(
			IWOLipsPluginConstants.PREF_LOG_LEVEL,
			PreferencesMessages.getString("Preferences.LogLevel.Default"));
	}

	/**
	 * Method getString.
	 * @param key
	 * @return String
	 */
	public static String getString(String key) {
		IPreferenceStore store = getPreferenceStore();
		return store.getString(key);
	}
	/**
	 * Method setString.
	 * @param key
	 * @param value
	 */
	public static void setString(String key, String value) {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(key, value);
	}

	/**
	* Method getBoolean.
	* @param key
	* @return boolean
	*/
	public static boolean getBoolean(String key) {
		IPreferenceStore store = getPreferenceStore();
		return (Preferences.trueString.equals(store.getString(key)));
	}
	/**
	 * Method setBoolean.
	 * @param key
	 * @param value
	 */
	public static void setBoolean(String key, boolean value) {
		IPreferenceStore store = getPreferenceStore();
		if (value)
			store.setValue(key, Preferences.trueString);
		else
			store.setValue(key, Preferences.falseString);
	}
	/**
	 * Method getPreferenceStore.
	 * @return IPreferenceStore
	 */
	public static IPreferenceStore getPreferenceStore() {
		return WOLipsPlugin.getDefault().getPreferenceStore();
	}
}
