package me.nillerusr;

import com.valvesoftware.source.mod.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import android.content.pm.PackageManager;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.widget.LinearLayout.LayoutParams;
import android.graphics.drawable.*;
import me.nillerusr.ExtractAssets;
import android.content.ComponentName;

public class LauncherActivity extends Activity {
	public static String MOD_NAME = "hl2"; // Change mod name here!

	public static String PKG_NAME;
	public static boolean can_write = true;
	static EditText cmdArgs;
	public static SharedPreferences mPref;
	public static final int sdk = Integer.valueOf(Build.VERSION.SDK).intValue();

	static LinearLayout res_layout;

	public static void changeButtonsStyle( ViewGroup parent )
	{
		if( sdk >= 21 )
			return;

		for( int i = parent.getChildCount() - 1; i >= 0; i-- )
		{
			try
			{
				final View child = parent.getChildAt(i);

				if( child == null )
					continue;

				if( child instanceof ViewGroup )
				{
					changeButtonsStyle((ViewGroup) child);
					// DO SOMETHING WITH VIEWGROUP, AFTER CHILDREN HAS BEEN LOOPED
				}
				else if( child instanceof Button )
				{
					final Button b = (Button)child;
					final Drawable bg = b.getBackground();
					if(bg!= null)bg.setAlpha( 96 );
					b.setTextColor( 0xFFFFFFFF );
					b.setTextSize( 15f );
					//b.setText(b.getText().toString().toUpperCase());
					b.setTypeface( b.getTypeface(),Typeface.BOLD );
				}
				else if( child instanceof EditText )
				{
					final EditText b = ( EditText )child;
					b.setBackgroundColor( 0xFF272727 );
					b.setTextColor( 0xFFFFFFFF );
					b.setTextSize( 15f );
				}
			}
			catch( Exception e )
			{
			}
		}
	}


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PKG_NAME = getApplication().getPackageName();
		requestWindowFeature(1);

		if (sdk >= 21)
			super.setTheme(0x01030224);
		else
			super.setTheme(0x01030005);

		mPref = getSharedPreferences("mod", 0);

		setContentView(R.layout.activity_launcher);

		LinearLayout body = (LinearLayout)findViewById(R.id.body);

		cmdArgs = (EditText)findViewById(R.id.edit_cmdline);

		Button button = (Button)findViewById(R.id.button_launch);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				LauncherActivity.this.startSource(v);
			}
		});

		Button aboutButton = (Button) findViewById(R.id.button_about);
		aboutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Dialog dialog = new Dialog(LauncherActivity.this);
				dialog.setTitle(R.string.srceng_launcher_about);
				ScrollView scroll = new ScrollView(LauncherActivity.this);
				scroll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				scroll.setPadding(5,5,5,5);
				TextView text = new TextView(LauncherActivity.this);
				text.setText(R.string.srceng_launcher_about_text);
				text.setLinksClickable(true);
				Linkify.addLinks(text, Linkify.WEB_URLS|Linkify.EMAIL_ADDRESSES);
				scroll.addView(text);
				dialog.setContentView(scroll);
				dialog.show();
			}
		});

		cmdArgs.setText(mPref.getString("argv", "-console"));

		changeButtonsStyle((ViewGroup)this.getWindow().getDecorView());
	}

	private Intent prepareIntent(Intent i)
	{
		String argv = cmdArgs.getText().toString();
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		SharedPreferences.Editor editor = mPref.edit();

		editor.putString("argv", argv);
		editor.commit();

		if(argv.length() != 0)
			i.putExtra("argv", argv);

		i.putExtra("gamedir", MOD_NAME );
		i.putExtra("gamelibdir", getApplicationInfo().nativeLibraryDir);
		i.putExtra("vpk", getFilesDir().getPath() +"/"+ ExtractAssets.VPK_NAME);

		return i;
	}

	public void startSource(View view) {
		String argv = cmdArgs.getText().toString();
		SharedPreferences.Editor editor = mPref.edit();
		editor.putString("argv", argv);

		ExtractAssets.extractVPK(this, false);

		try
		{
			Intent intent = new Intent();
			intent.setComponent(new ComponentName("com.valvesoftware.source", "org.libsdl.app.SDLActivity"));
			intent = prepareIntent(intent);
			startActivity(intent);
			return;
		}
		catch(Exception e){}

		new AlertDialog.Builder(this).setTitle("Warning").setMessage("Please install Source Engine").setPositiveButton(R.string.srceng_launcher_ok, (DialogInterface.OnClickListener) null).show();
	}
}
