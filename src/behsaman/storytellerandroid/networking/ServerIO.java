package behsaman.storytellerandroid.networking;

import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.Header;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import behsaman.storytellerandroid.LoginActivity;
import behsaman.storytellerandroid.R;
import behsaman.storytellerandroid.StoryPageActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

public class ServerIO {
	private static final String TAG = "ServerIO";
	//URLs
	public static final String BASIC_HTTP_AUTH_PATTERN = "https://%s:%s@www.noveldevelopments.com:8443/";
	public static final String BASE_URL = "https://www.noveldevelopments.com:8443/";
	public static final String LOGIN_URL = "login.jsp";
	public static final String LOGOUT_URL = "logout";
	public static final String CHECK_LOGIN_URL = "SuccessfulLogin";
	public static final String INSERT_USER_URL = "InsertUser";
	public static final String INSERT_CATEGORY_URL = "InsertCategory";
	public static final String INSERT_STORY_URL = "InsertStory";
	public static final String INSERT_STORY_PIECE_URL = "InsertStoryPiece";
	public static final String GET_STORY_URL = "GetStory";
	public static final String GET_STORY_BY_ID_URL = "GetStoryByID";
	public static final String GET_PIECE_URL = "GetPiece";
	public static final String GET_CATEGORY_URL = "GetCategory";
	public static final String CONTRIBUTE_REQUEST_URL = "Contribute";
	public static final String HAS_REQ_CONTRIBUTION_URL = "HasReqContribution";
	//Connection
	private static AsyncHttpClient client;
	private static ServerIO m_instance = new ServerIO();
	//Logged in
	private static boolean logged_in = false;
	//JSON RESULTS
	public static final Integer SUCCESS = 200;
	public static final Integer FAILURE = 0;
	
	//Credential Info for BASIC HTTP AUTHENTICATION
	private String username = null;
	private String password = null; 
	
	private ServerIO () {}
	
	public static ServerIO getInstance() {
		return m_instance;
	}
	
	public void initialize(Context c) {
		client = new AsyncHttpClient();
		client.setSSLSocketFactory(newSslSocketFactory(c));
		m_instance = new ServerIO();
		logged_in = false;
	}
	
	public RequestHandle download(String url,MyBinaryHttpResponseHandler myBinaryHttpResponseHandler) {
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:Download("+url+")");
			return null;
		}
		
		return client.get(url, myBinaryHttpResponseHandler);
	}
	
	public RequestHandle get(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:Get("+relativeURL+")");
			return null;
		}
		return client.get(BASE_URL+relativeURL, params, responseHandler);
	}
	
	public RequestHandle post(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:Post("+relativeURL+")");
			return null;
		}
		return client.post(BASE_URL+relativeURL, params, responseHandler);
	}
	
	public void login(final String user,final String pass)
	{
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:Login");
			return;
		}
		
		RequestParams params = new RequestParams();
		params.add("username", user);
		params.add("password", pass);
		params.add("rememberMe", "true");
		post(ServerIO.LOGIN_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject obj) {
				try {
					int status = obj.getInt("Status");
					if(status == SUCCESS)
					{
						logged_in = true;
						username = user;
						password = pass;
					}
					else
						logged_in = false;
				} catch (Exception e) {
					Log.e(TAG,e.getMessage());
				}
            }
		});
	}
	
	public boolean isLoggedIn()	{
		return logged_in;
	}
	
	public void checkLoginStatus()	{
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:CheckLoginStatus");
			logged_in = false;
		}
		
		RequestParams params = new RequestParams();
		logged_in = false;
		post(ServerIO.CHECK_LOGIN_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject obj) {
				try {
					int status = obj.getInt("Status");
					if(status == SUCCESS)
						logged_in = true;
					else
						logged_in = false;
				} catch (Exception e) {
					Log.e(TAG,e.getMessage());
				}
            }

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Log.e(TAG,"CONNECTION ERROR:"+arg3.getMessage());
				logged_in = false;
			}

			@Override
			public void onFailure(String responseBody, Throwable error) {
				Log.e(TAG,"CONNECTION ERROR:"+error.getMessage()+" RespBody:"+responseBody);
				logged_in = false;
			}
		});
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

	public void connectionError(Context c) {
		logged_in = false;
		checkLoginStatus();
		showConnectionErrorToast(c);
		//Change Intent to the Login Page
		Intent intent = new Intent(c, LoginActivity.class);
		c.startActivity(intent);
	}
	
	private void showConnectionErrorToast(Context c) {
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(c, "    Sorry :( \nConnectivity Error.", duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public void logout(final Context c) {
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:Logout");
			return;
		}
		
		RequestParams params = new RequestParams();
		post(ServerIO.LOGOUT_URL, params, new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				logged_in = false;
				//Change Intent to the Login Page
				Intent intent = new Intent(c, LoginActivity.class);
				c.startActivity(intent);
			}
		});
	}

	public String getBasicAuthURL(String video_file_addr) {
		if(username == null || password == null) {
			Log.e(TAG, "You are not authenticated!! BasicHTTPAUTH is not possible.");
			return null;
		}
		
		String removedURL = video_file_addr.replace(BASE_URL, "");
		String newBaseURL = String.format(BASIC_HTTP_AUTH_PATTERN, username,password);
		
		return newBaseURL+removedURL;
	}
}
