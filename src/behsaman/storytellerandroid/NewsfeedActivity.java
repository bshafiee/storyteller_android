package behsaman.storytellerandroid;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import behsaman.storytellerandroid.datamodel.LOCK_TIME_MINS;
import behsaman.storytellerandroid.datamodel.MAX_MULTIMEDIA_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_NUM_PIECES_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_TEXT_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class NewsfeedActivity extends Activity {
	private static final String TAG = "NewsfeedActivity";
	
	ListView list;
    LazyAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newfeed);
		
		//new XMLFetcher().execute(this);
		
		final Activity newsActivity = this;
		RequestParams params = new RequestParams();
		params.add("limit", "100");
		ServerIO.getInstance().post(ServerIO.GET_STORY_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONArray arr) {
				ArrayList<StoryModel> stories = new ArrayList<StoryModel>();
				for(int i=0;i<arr.length();i++) {
					try {
						JSONObject obj = (JSONObject) arr.get(i);
						int id = obj.getInt("id");
						int owner_id = obj.getInt("owner_id");
						Integer category_id = obj.getInt("category_id");
						String title = obj.getString("title");
						MAX_NUM_PIECES_TYPE max_num_pieces = MAX_NUM_PIECES_TYPE.valueOf(obj.getString("max_num_pieces"));
						MAX_MULTIMEDIA_PIECE_LENGTH_TYPE max_multimedia_piece_length = 
								MAX_MULTIMEDIA_PIECE_LENGTH_TYPE.valueOf(obj.getString("max_multimedia_piece_length"));
						MAX_TEXT_PIECE_LENGTH_TYPE max_text_piece_length = MAX_TEXT_PIECE_LENGTH_TYPE.valueOf(obj.getString("max_text_piece_length"));
						LOCK_TIME_MINS lock_time_mins = LOCK_TIME_MINS.valueOf(obj.getString("lock_time_mins"));
						int next_available_piece = obj.getInt("next_available_piece");
						Date created_on = Utils.parseDate(StoryModel.DATE_FORMAT, obj.getString("created_on"));
						
						StoryModel story = new StoryModel(id, owner_id, category_id.toString(), title, max_num_pieces, max_multimedia_piece_length, 
												max_text_piece_length, lock_time_mins, next_available_piece, created_on);
						stories.add(story);
						
						//Add to UI
						list=(ListView)findViewById(R.id.list);
						// Getting adapter by passing xml data ArrayList
				        adapter=new LazyAdapter(newsActivity, stories);        
				        list.setAdapter(adapter);
				        

				        // Click event for single list row
				        list.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {

							}
						});	
					} catch (JSONException e) {
						Log.e(TAG, "Error in getting JSONObject: "+e.getMessage());
					}
				}
            }
		});
	}	
	/*
public class XMLFetcher extends AsyncTask<Activity,Integer,Object>{
		Activity parentActivity = null;
		@Override
		protected Object doInBackground(Activity... params) {
			parentActivity = params[0];
			ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
			XMLParser parser = new XMLParser();
			String xml = parser.getXmlFromUrl(URL); // getting XML from URL
			Document doc = parser.getDomElement(xml); // getting DOM element
			
			NodeList nl = doc.getElementsByTagName(KEY_SONG);
			// looping through all song nodes <song>
			for (int i = 0; i < nl.getLength(); i++) {
				// creating new HashMap
				HashMap<String, String> map = new HashMap<String, String>();
				Element e = (Element) nl.item(i);
				// adding each child node to HashMap key => value
				map.put(KEY_ID, parser.getValue(e, KEY_ID));
				map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
				map.put(KEY_ARTIST, parser.getValue(e, KEY_ARTIST));
				map.put(KEY_DURATION, parser.getValue(e, KEY_DURATION));
				map.put(KEY_THUMB_URL, parser.getValue(e, KEY_THUMB_URL));

				// adding HashList to ArrayList
				songsList.add(map);
			}
			
			return songsList;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
	        
	    }

	    protected void onPostExecute(Object songslist) {
	    	list=(ListView)findViewById(R.id.list);
			
			// Getting adapter by passing xml data ArrayList
	        adapter=new LazyAdapter(this.parentActivity, (ArrayList<HashMap<String, String>>)songslist);        
	        list.setAdapter(adapter);
	        

	        // Click event for single list row
	        list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
								

				}
			});		
	    }

	}*/
}