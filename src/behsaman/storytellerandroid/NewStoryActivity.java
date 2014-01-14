package behsaman.storytellerandroid;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import behsaman.storytellerandroid.networking.ServerIO;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class NewStoryActivity extends Activity {
	private static final String TAG = "NewStoryActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_story);
		
		//Load Categories
		loadCategories();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_story, menu);
		return true;
	}
	
	private void loadCategories()
	{
		final Spinner s = (Spinner)findViewById(R.id.spinner_new_story_category);
		final Activity newStoryActivity = this;
		RequestParams params = new RequestParams();
		params.add("limit", "100");
		ServerIO.getInstance().post(ServerIO.GET_CATEGORY_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONArray arr) {
				List<String> categories = new ArrayList<String>();
				for(int i=0;i<arr.length();i++) {
					try {
						JSONObject obj = (JSONObject) arr.get(i);
						String name = obj.getString("name");
						categories.add(name);
					} catch (JSONException e) {
						Log.e(TAG, "Error in getting JSONObject: "+e.getMessage());
					}
				}
				ArrayAdapter dataAdapter = new ArrayAdapter(newStoryActivity,android.R.layout.simple_spinner_item, categories);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				s.setAdapter(dataAdapter);
            }
		});
	}

}
