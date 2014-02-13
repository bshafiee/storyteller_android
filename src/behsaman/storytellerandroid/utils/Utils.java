package behsaman.storytellerandroid.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

public class Utils {
	private static final String TAG = "Utils";
	
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    
    public static Date parseDate(String pattern, String input) {
    	try {
			return new SimpleDateFormat(pattern).parse(input);
		} catch (ParseException e) {
			Log.e(TAG, "Error in parsing input date. Input:"+input+"\tPattern:"+pattern+"\t"+e.getMessage());
			return null;
		}
    }
    
    public static File getCacheDir(Context context) {
    	File cacheDir = null;
    	if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"LazyList");
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
        return cacheDir;
    }
}