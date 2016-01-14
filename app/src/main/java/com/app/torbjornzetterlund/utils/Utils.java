package com.app.torbjornzetterlund.utils;

import android.annotation.TargetApi;
import com.app.torbjornzetterlund.R;
import com.app.torbjornzetterlund.app.Const;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.support.v7.app.AlertDialog;

import java.text.NumberFormat;
import java.util.Locale;

public class Utils {
	private String TAG = Utils.class.getSimpleName();
	private Context _context;
	private PrefManager pref;

	// constructor
	public Utils(Context context) {
		this._context = context;
		pref = new PrefManager(_context);
	}

	/*
	 * getting screen width
	 */
	@SuppressWarnings("deprecation")
	public int getScreenWidth() {
		int columnWidth;
		WindowManager wm = (WindowManager) _context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (NoSuchMethodError ignore) {
			// Older device
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		columnWidth = point.x;
		return columnWidth;
	}

    public static void showAlertDialog(Activity context, String title, String message) {

        AlertDialog alertDialog = new AlertDialog.Builder(
                context,
                R.style.AlertDialogCustom_Destructive)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setTitle(title).setMessage(message).create();
        alertDialog.show();
    }

	public static String formatNumber (Integer t){
		String s = NumberFormat.getInstance().format(t).toString();
		return s;
	}
    public static boolean isLollipop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarcolor(Window window, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(color);
        }
    }

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void forceRTLIfSupported(Activity activity)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Const.forceRTL==Boolean.TRUE){
			activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
			Configuration cfg = new Configuration();
			cfg.locale = new Locale(Const.forceRTLLang);
			activity.getResources().updateConfiguration(cfg, null);
        }
	}
}