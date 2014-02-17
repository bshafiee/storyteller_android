package behsaman.storytellerandroid.networking;

import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONObject;

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
	private static final AsyncHttpClient client = new AsyncHttpClient();
	private static ServerIO m_instance = new ServerIO();
	//Logged in
	private static boolean logged_in = false;
	//JSON RESULTS
	public static final Integer SUCCESS = 200;
	public static final Integer FAILURE = 0;
	
	private ServerIO () {}
	
	public static ServerIO getInstance() {
		return m_instance;
	}
	
	public void initialize(Context c) {
		client.setSSLSocketFactory(newSslSocketFactory(c));
	}
	
	public void download(String url,MyBinaryHttpResponseHandler myBinaryHttpResponseHandler) {
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:Download("+url+")");
			return;
		}
		
		client.get(url, myBinaryHttpResponseHandler);
	}
	
	public void get(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:Get("+relativeURL+")");
			return;
		}
		client.get(BASE_URL+relativeURL, params, responseHandler);
	}
	
	public void post(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:Post("+relativeURL+")");
			return;
		}
		client.post(BASE_URL+relativeURL, params, responseHandler);
	}
	
	public void login(String user, String pass)
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
						logged_in = true;
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
	
	public boolean checkLoginStatus()	{
		if(client == null)
		{
			Log.e(TAG,"httpclient not initialized:CheckLoginStatus");
			return false;
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
		});
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
