package behsaman.storytellerandroid;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import behsaman.storytellerandroid.datamodel.LOCK_TIME_MINS;
import behsaman.storytellerandroid.datamodel.MAX_MULTIMEDIA_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_NUM_PIECES_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_TEXT_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.STORY_TYPE;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class NewsfeedActivity extends Activity {
	private static final String TAG = "NewsfeedActivity";
	
	public static final String STORY_ID = "behsaman.storytellerandroid.NewsfeedActivity";
	
	ListView list;
    LazyAdapterStories adapter;
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newfeed);
		
		final Activity newsActivity = this;
		RequestParams params = new RequestParams();
		params.add("limit", "100");
		ServerIO.getInstance().post(ServerIO.GET_STORY_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject result) {
				try {
					if(result.getInt("Status")!=ServerIO.SUCCESS)
					{
						Log.e(TAG,result.getString("Error"));
						return;
					}
				} catch (JSONException e1) {
					Log.e(TAG,e1.getMessage());
				}
				
				JSONArray arr = null;
				try {
					arr = result.getJSONArray("data");
				} catch (JSONException e1) {
					Log.e(TAG,e1.getMessage());
				}
				ArrayList<Object> stories = new ArrayList<Object>();
				for(int i=0;i<arr.length();i++) {
					try {
						JSONObject obj = (JSONObject) arr.get(i);
						int id = obj.getInt("id");
						int owner_id = obj.getInt("owner_id");
						Integer category_id = obj.getInt("category_id");
						String title = obj.getString("title");
						STORY_TYPE type = STORY_TYPE.valueOf(obj.getString("type"));
						MAX_NUM_PIECES_TYPE max_num_pieces = MAX_NUM_PIECES_TYPE.valueOf(obj.getString("max_num_pieces"));
						MAX_MULTIMEDIA_PIECE_LENGTH_TYPE max_multimedia_piece_length = 
								MAX_MULTIMEDIA_PIECE_LENGTH_TYPE.valueOf(obj.getString("max_multimedia_piece_length"));
						MAX_TEXT_PIECE_LENGTH_TYPE max_text_piece_length = MAX_TEXT_PIECE_LENGTH_TYPE.valueOf(obj.getString("max_text_piece_length"));
						LOCK_TIME_MINS lock_time_mins = LOCK_TIME_MINS.valueOf(obj.getString("lock_time_mins"));
						int next_available_piece = obj.getInt("next_available_piece");
						Date created_on = Utils.parseDate(StoryModel.DATE_FORMAT, obj.getString("created_on"));
						
						StoryModel story = new StoryModel(id, owner_id, category_id.toString(), title, type,max_num_pieces, max_multimedia_piece_length, 
												max_text_piece_length, lock_time_mins, next_available_piece, created_on);
						stories.add(story);
						
						//Add to UI
						list=(ListView)findViewById(R.id.list);
						// Getting adapter by passing xml data ArrayList
				        adapter=new LazyAdapterStories(newsActivity, stories);        
				        list.setAdapter(adapter);
				        

				        // Click event for single list row
				        list.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
								LazyAdapterStories adaptor = (LazyAdapterStories)list.getAdapter();
								int story_id = adaptor.getStoryID(position);
								if(story_id > 0)
									ChangeViewToStoryPage(story_id);
								else
									showFailureToast();
								
							}
						});	
					} catch (JSONException e) {
						Log.e(TAG, "Error in getting JSONObject: "+e.getMessage());
					}
				}
            }
		});
	}	
	
	private void ChangeViewToStoryPage(int storyID) {
		Intent intent = new Intent(this, StoryPageActivity.class);
		intent.putExtra(STORY_ID, storyID);
		startActivity(intent);
		
	}
	
	public void showFailureToast()
	{
		Context context = this;
		CharSequence text = "Sorry, can't retrive story id :(";
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.CENTER, 0,0);
		toast.show();
	}
	
}