package behsaman.storytellerandroid;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import behsaman.storytellerandroid.networking.MyHttpClient;
import behsaman.storytellerandroid.networking.ServerIO;

public class MainActivity extends ActionBarActivity{
	MyHttpClient client;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//setContentView(R.layout.activity_newfeed);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void changeView(View view) {
		Intent intent = new Intent(this, CustomizedListView.class);
		startActivity(intent);
		
	}
	
	/** Called when the user clicks the Send button */
	public void signIn(View view) {
		/*client = new MyHttpClient(getApplicationContext());
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("username", "behrooz");
		params.put("password", "Behrooz2");
	    //Object instream = NetworkIO.doPost("https://192.168.0.101:8443/login.jsp", params);
		//new NetworkIO().execute(new PostRequest(params, "https://192.168.0.101:8443/login.jsp"));
		new NetworkIO().execute(new PostRequest(params, "https://www.noveldevelopments.com:8443/login.jsp"));
		params = new HashMap<String, String>();
		params.put("limit", "10");
		new NetworkIO().execute(new PostRequest(params, "https://www.noveldevelopments.com:8443/GetStory"));*/
		
		final EditText et = (EditText) findViewById(R.id.edit_message);

		
		RequestParams params = new RequestParams();
		params.add("limit", "100");
		ServerIO.getInstance().post(ServerIO.GET_STORY_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONArray arr) {
				et.setText(arr.toString());
            }
		});
	}
	
	public class PostRequest {
		HashMap<String, String> params;
		String url;
		private PostRequest(HashMap<String, String> params, String url) {
			super();
			this.params = params;
			this.url = url;
		}
	}
	
	public class NetworkIO extends AsyncTask<PostRequest,Integer,Object>{
		
		@Override
		protected Object doInBackground(PostRequest... param) {
			URL url;
			try {
			/*	url = new URL(param[0].url);
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setReadTimeout(10000 );
		        conn.setConnectTimeout(15000 );
		        conn.setRequestMethod("POST");
		        conn.setDoInput(true);
		        //Set Params
		        for(String key:param[0].params.keySet())
		        	conn.addRequestProperty(key, param[0].params.get(key));
		        // Starts the query
		        conn.connect();
		        int response = conn.getResponseCode();
		        if(response != 200)
		        	return "Unsuccessfull! HTTP REQUEST:"+response;
		        InputStream is = conn.getInputStream();
		        return is;*/
				HttpPost httpPost = new HttpPost(param[0].url);
				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				//Set Params
		        for(String key:param[0].params.keySet())
		        	nvps.add(new BasicNameValuePair(key, param[0].params.get(key)));
				httpPost.setEntity(new UrlEncodedFormEntity(nvps));
				HttpResponse response1 = client.execute(httpPost);
				HttpEntity entity = response1.getEntity();
			    if (entity != null) {
		        	InputStream instream = entity.getContent();
		        	return instream;
		        }
		        else
		        	return "Null entity";
			} catch (Exception e) {
				return "Error in Connecting:"+e.getMessage();
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
	        
	    }

	    protected void onPostExecute(Object instream) {
	    	EditText et = (EditText) findViewById(R.id.edit_message);
	    	if(instream instanceof String)
		    {
		    	et.setText((String)instream);
		    	return;
		    }
		    
		    String text = "";
		    Scanner s = new Scanner((InputStream)instream);
	        try {
	           while(s.hasNext())
	           {
	        	   text+=s.nextLine();
	           }
	        }
	        catch(Exception e) {
	        	e.printStackTrace();
	        }
	        finally {
	        	s.close();
	        }
		    et.setText(text);
	    }

	}
}
