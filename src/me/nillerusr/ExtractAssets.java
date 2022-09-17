package me.nillerusr;
import android.content.SharedPreferences;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import android.util.Log;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;


public class ExtractAssets
{
	public static String TAG = "ExtractAssets";
	static SharedPreferences mPref;

	public static final String VPK_NAME = "PACK_NAME";
	public static int PAK_VERSION = 1337;

	private static int chmod(String path, int mode)
	{
		int ret = -1;

		try
		{
			ret = Runtime.getRuntime().exec("chmod " + Integer.toOctalString(mode) + " " + path).waitFor();
			Log.d(TAG, "chmod " + Integer.toOctalString(mode) + " " + path + ": " + ret );
		}
		catch(Exception e)
		{
			ret = -1;
			Log.d(TAG, "chmod: Runtime not worked: " + e.toString() );
		}

		try
		{
			Class fileUtils = Class.forName("android.os.FileUtils");
			Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
			ret = (Integer) setPermissions.invoke(null, path, mode, -1, -1);
		}
		catch(Exception e)
		{
			ret = -1;
			Log.d(TAG, "chmod: FileUtils not worked: " + e.toString() );
		}

		return ret;
	}

	public static void extractAsset(Context context, String asset, Boolean force)
	{
		AssetManager am = context.getAssets();

		try
		{
			File asset_file = new File(context.getFilesDir().getPath() +"/"+ asset);

			Boolean asset_exists = asset_file.exists();
			if( !force && asset_exists )
				return;

			FileOutputStream os = null;
			InputStream is = am.open(asset);
			os = new FileOutputStream(context.getFilesDir().getPath() +"/tmp");
			byte[] buffer = new byte[8192];
			while (true)
			{
				int length = is.read(buffer);
				if (length <= 0)
					break;

				os.write(buffer, 0, length);
			}

			os.close();

			File tmp = new File( context.getFilesDir().getPath() + "/tmp" );

			if( asset_exists )
				asset_file.delete();

			tmp.renameTo(new File( context.getFilesDir().getPath() +"/"+ asset));
		}
		catch (Exception e)
		{
			Log.e("SRCAPK", "Failed to extract vpk:" + e.toString());
		}

		chmod(context.getFilesDir().getPath() +"/"+ asset, 0777);
	}

	public static void extractAssets(Context context)
	{
		ApplicationInfo appinf = context.getApplicationInfo();
		chmod(appinf.dataDir, 0777);
		chmod(context.getFilesDir().getPath(), 0777);

		extractVPK(context);
	}

	public static void extractVPK(Context context)
	{
		if( mPref == null )
			mPref = context.getSharedPreferences("mod", 0);

		Boolean force = mPref.getInt( "pakversion", 0 ) != PAK_VERSION;
		extractAsset( context, VPK_NAME, force );

		SharedPreferences.Editor editor = mPref.edit();
		editor.putInt( "pakversion", PAK_VERSION );
		editor.commit();
	}

	public static void extractVPK(Context context, Boolean force) 
	{
		if( VPK_NAME.isEmpty() || !VPK_NAME.contains(".vpk") )
			return;

		ApplicationInfo appinf = context.getApplicationInfo();

		FileOutputStream os = null;
		try {
			if( mPref == null )
				mPref = context.getSharedPreferences("mod", 0);

			File file = new File( context.getFilesDir().getPath() +"/"+ VPK_NAME );
			if(file.exists())
				force = true;

			if( mPref.getInt( "pakversion", 0 ) == PAK_VERSION && !force )
				return;

			InputStream is = context.getAssets().open(VPK_NAME);
			os = new FileOutputStream( context.getFilesDir().getPath() +"/"+ VPK_NAME);
			byte[] buffer = new byte[8192];
			while (true) {
				int length = is.read(buffer);
				if (length <= 0)
					break;

				os.write(buffer, 0, length);
			}

			SharedPreferences.Editor editor = mPref.edit();
			editor.putInt( "pakversion", PAK_VERSION );
			editor.commit();

			chmod(appinf.dataDir, 0777);
			chmod(context.getFilesDir().getPath(), 0777);
			chmod(context.getFilesDir().getPath() +"/"+ VPK_NAME, 0777);
		}
		catch (Exception e) {
			Log.e("SRCAPK", "Failed to extract vpk:" + e.toString());
		}
	}
}
