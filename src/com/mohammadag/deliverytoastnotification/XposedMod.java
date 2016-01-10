package com.mohammadag.deliverytoastnotification;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {

	private XSharedPreferences mPreferences;
	private String deliveryReportMethodName;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(Constants.MMS_PACKAGE_NAME))
			return;

		mPreferences = new XSharedPreferences(XposedMod.class.getPackage().getName());

		try {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				deliveryReportMethodName = "a";
			}
			else {
				deliveryReportMethodName = "updateReportNotification";
			}
			XposedHelpers.findAndHookMethod("com.android.mms.transaction.MessagingNotification",
					lpparam.classLoader, deliveryReportMethodName, Context.class, int.class,
					int.class, long.class, String.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Context context = (Context) param.args[0];
					int type = (Integer) param.args[1];
					int status = (Integer) param.args[2];
					String address = (String) param.args[4];

					mPreferences.reload();
					
					// Only handle message delivered notifications
					if (type == 0 && status == 0) {
						int toastLength = Toast.LENGTH_SHORT;

						if (mPreferences.getBoolean(Constants.SETTINGS_LONG_TOAST_KEY, false))
							toastLength = Toast.LENGTH_LONG;

						Toast.makeText(context, makeToastText(context, address),
								toastLength).show();
						param.setResult(null);
					}
				}
			});
		} catch (ClassNotFoundError e) {
			e.printStackTrace();
		} catch (NoSuchMethodError e) {
			e.printStackTrace();
		}
	}

	private String makeToastText(Context context, String address) {
		String str = context.getString(
				context.getResources().getIdentifier("delivery_toast_body",
						"string", Constants.MMS_PACKAGE_NAME));
		return String.format(str, getContactDisplayNameForAddress(context, address));
	}

	private String getContactDisplayNameForAddress(Context context, String address) {
		Cursor cursor = context.getContentResolver().query(
				Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address)),
				new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor.moveToFirst()) {
			address = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			if (mPreferences.getBoolean(Constants.SETTINGS_SHOW_NUMBER_KEY, true)
					|| address == null || TextUtils.isEmpty(address)) {
				address += " (" + address + ")";
			}
		}
		return address;
	}
}
