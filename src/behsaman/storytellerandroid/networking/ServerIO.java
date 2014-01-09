package behsaman.storytellerandroid.networking;

import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import android.R.integer;
import android.content.Context;
import android.util.Log;
import behsaman.storytellerandroid.R;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ServerIO {
	private static final String TAG = "ServerIO";
	//URLs
	public static final String BASE_URL = "https://www.noveldevelopments.com:8443/";
	public static final String LOGIN_URL = "login.jsp";
	public static final String INSERT_USER_URL = "InsertUser";
	public static final String INSERT_CATEGORY_URL = "InsertCategory";
	public static final String INSERT_STORY_URL = "InsertStory";
	public static final String INSERT_STORY_PIECE_URL = "InsertStoryPiece";
	public static final String GET_STORY_URL = "GetStory";
	public static final String GET_PIECE_URL = "GetPiece";
	public static final String GET_CATEGORY_URL = "GetCategory";
	//Connection
	private static AsyncHttpClient client = null;
	private static ServerIO m_instance = new ServerIO();
	//Logged in
	private static boolean logged_in = false;
	
	private ServerIO () {}
	
	public static ServerIO getInstance() {
		return m_instance;
	}
	
	public void initialize(Context c) {
		client = new AsyncHttpClient();
		client.setSSLSocketFactory(newSslSocketFactory(c));
	}
	
	public void get(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized");
			return;
		}
		client.get(BASE_URL+relativeURL, params, responseHandler);
	}
	
	public void post(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized");
			return;
		}
		client.post(BASE_URL+relativeURL, params, responseHandler);
	}
	
	public void login(String user, String pass)
	{
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized");
			return;
		}
		
		RequestParams params = new RequestParams();
		params.add("username", user);
		params.add("password", pass);
		post(ServerIO.LOGIN_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject obj) {
				try {
					if(obj.getString("status").equals("Successful"))
						logged_in = true;
					else
						logged_in = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
				
            }
		});
	}
	
	public boolean isLoggedIn()	{
		return logged_in;
	}
	
	private SSLSocketFactory newSslSocketFactory(Context context) {
	    try {
	      KeyStore trusted = KeyStore.getInstance("BKS");
	      InputStream in = context.getResources().openRawResource(R.raw.mystore);
	      try {
	        trusted.load(in, "ez24get".toCharArray());
	      } finally {
	        in.close();
	      }
	      return new SSLSocketFactory(trusted);
	    } catch (Exception e) {
	      throw new AssertionError(e);
	    }
	}
}
