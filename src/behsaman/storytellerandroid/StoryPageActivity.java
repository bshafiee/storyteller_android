package behsaman.storytellerandroid;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import behsaman.storytellerandroid.datamodel.LOCK_TIME_MINS;
import behsaman.storytellerandroid.datamodel.MAX_MULTIMEDIA_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_NUM_PIECES_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_TEXT_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.PieceModel;
import behsaman.storytellerandroid.datamodel.PullRequestResult;
import behsaman.storytellerandroid.datamodel.STORY_TYPE;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class StoryPageActivity extends Activity {

	private static final String TAG = "StoryPageActivity";
	
	public static final String STORY_MODEL_KEY = "behsaman.storytellerandroid.StoryPageActivity.STORY_MODEL";
	public static final String UUID_KEY = "behsaman.storytellerandroid.StoryPageActivity.UUID";
	
	private Integer story_id = null;
	private final StoryModel model = new StoryModel();
	private UUID generatedUUID = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_page);
		
		//Get Story ID
		Intent intent = getIntent();
		this.story_id = intent.getIntExtra(NewsfeedActivity.STORY_ID, -1);
		
		this.updateView(); 
		this.updateContirbuteionStatus();
		this.updatePieces();
	}

	private void updatePieces() {
		//Invalid id
		if(story_id == null || story_id < 1)
			return;
		
		final Activity storyPageActivity = this;
		RequestParams params = new RequestParams();
		params.add("story_id", story_id.toString());
		params.add("from_index_inclusive", "0");
		params.add("limit", "-1");//all
		ServerIO.getInstance().post(ServerIO.GET_PIECE_URL, params, new JsonHttpResponseHandler() {
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
				ArrayList<Object> pieces = new ArrayList<Object>();
				for(int i=0;i<arr.length();i++) {
					try {
						JSONObject obj = (JSONObject) arr.get(i);
						
						int id = obj.getInt("id");
						int index = obj.getInt("index");
						String text_val = obj.getString("text_val");
						int creator_id = obj.getInt("creator_id");
						Date date = Utils.parseDate(StoryModel.DATE_FORMAT, obj.getString("created_on"));
						PieceModel p = new PieceModel(id, story_id, creator_id, index, text_val, null, null, null, date);
						pieces.add(p);
					} catch (JSONException e) {
						Log.e(TAG, "Error in getting JSONObject: "+e.getMessage());
					}
					
				}
		
				//Add to UI
				ListView list=(ListView)findViewById(R.id.list_story_page_pieces);
				// Getting adapter by passing xml data ArrayList
		        LazyAdapterTextPieces adapter=new LazyAdapterTextPieces(storyPageActivity, (ArrayList<Object>)pieces);        
		        list.setAdapter(adapter);
				        
		        // Click event for single list row
		        list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) 
						{}
					});
			}
		});
	}

	private void updateView() {
		//Invalid id
		if(story_id == null || story_id < 1)
			return;
		
		RequestParams params = new RequestParams();
		params.add("id", story_id.toString());
		ServerIO.getInstance().post(ServerIO.GET_STORY_BY_ID_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject result) {
				try {
					if(result.getInt("Status")==ServerIO.FAILURE) {
						Log.e(TAG,result.getString("Error"));
						return;
					}
				} catch (JSONException e1) {
					Log.e(TAG,e1.getMessage());
				}
				try {
					JSONObject obj = result.getJSONObject("data");
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
					
					model.setId(id);
					model.setOwner_id(owner_id);
					model.setCategory(category_id.toString());
					model.setTitle(title);
					model.setType(type);
					model.setMax_num_pieces(max_num_pieces);
					model.setMax_multimedia_piece_length(max_multimedia_piece_length);
					model.setMax_text_piece_length(max_text_piece_length);
					model.setLock_time_mins(lock_time_mins);
					model.setNext_available_piece(next_available_piece);
					model.setCreated_on(created_on);
					
					// Setting all values in listview
			        TextView titleBox = (TextView)findViewById(R.id.tv_story_page_title);  
					titleBox.setText(title);
			        String description = "Category: "+category_id;
			        description += "\nCreated By: "+owner_id;
			        description += "\nPieces Left: "+(max_num_pieces.getNumVal() - next_available_piece);
			        description += "\nStory Type: "+(type.toString());
			        TextView infoBox = (TextView)findViewById(R.id.tv_story_page_info);
			        infoBox.setText(description);
				} catch (JSONException e) {
					Log.e(TAG,e.getMessage());
				}
            }
		});
	}

	private void updateContirbuteionStatus() {
		//Invalid id
		if(story_id == null || story_id < 1)
			return;
		
		//Contrib Button
		Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
		contribButton.setText("Request to Contribute");
		contribButton.setEnabled(false);

		//Send Contribution Request
		RequestParams params = new RequestParams();
		params.add("story_id", story_id.toString());
		ServerIO.getInstance().post(ServerIO.HAS_REQ_CONTRIBUTION_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject result) {
				Log.e(TAG,"Update:"+result);
				try {
					if(result.getInt("Status")==ServerIO.FAILURE) 
					{
						//No request for this story && Enable Contrib button
						if(model.getMax_num_pieces().getNumVal() > model.getNext_available_piece()) {
							Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
							contribButton.setText("Request to Contribute");
							contribButton.setEnabled(true);
						}
						return;
					}
				} catch (JSONException e1) {
					Log.e(TAG,e1.getMessage());
				}
				// we already sent a request
				try {
					JSONObject obj = result.getJSONObject("data");
					long request_date = obj.getLong("request_date");
					int queue_index = obj.getInt("queue_index");
					Long leftMins = new Date().getTime()-request_date;
					leftMins /= 1000;//second
					leftMins /= 60;//Mins
					Log.e(TAG,"Curr:"+new Date().getTime()+"\tReqDate:"+request_date);
					generatedUUID = UUID.fromString(obj.getString("generatedUUID"));
					
					
					TextView infoBox = (TextView)findViewById(R.id.tv_story_page_contrib_info);
			        infoBox.setText("You have "+leftMins+" minutes left to send your piece!");
			        //Contribute Button
			        if(queue_index == 1)//our turn
			        {
			        	Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
			        	contribButton.setEnabled(true);
			        	contribButton.setText("Contribute");
			        }
			        else
			        {
			        	Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
			        	contribButton.setEnabled(false);
			        	contribButton.setText("Waiting");
			        }
			        
				} catch (Exception e) {
					Log.e(TAG,e.getMessage());
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.story_page, menu);
		return true;
	}
	
	public void ContributeHandler(View v) {
		//Invalid id
		if(story_id == null || story_id < 1)
			return;

		Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
    	if(contribButton.getText().equals("Request to Contribute"))
    		sendContributeRequest();
    	else 
    		goToContribetePage();
		
	}

	private void goToContribetePage() {
		if(model.getType()==STORY_TYPE.TEXT_ONLY) {
			changeViewToNewTextPiece(model,generatedUUID);
		}
	}

	private void sendContributeRequest() {
		//Send Contribution Request
		RequestParams params = new RequestParams();
		params.add("story_id", story_id.toString());
		ServerIO.getInstance().post(ServerIO.CONTRIBUTE_REQUEST_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject result) {
				try {
					if(result.getInt("Status")==ServerIO.FAILURE) {
						Log.e(TAG,result.getString("Error"));
						return;
					}
				} catch (JSONException e1) {
					Log.e(TAG,e1.getMessage());
				}
				try {
					JSONObject obj = result.getJSONObject("data");
					generatedUUID = UUID.fromString(obj.getString("uuid"));
					int queueSize = obj.getInt("queueIndex");
					Date expDate = new Date(obj.getLong("expDate"));
					//Update View
					updateContirbuteionStatus();
					//Check Queue Size
					if(queueSize == 1) {//our turn
						if(model.getType()==STORY_TYPE.TEXT_ONLY) {
							changeViewToNewTextPiece(model,generatedUUID);
						}
					}
					else
					{
						updateContirbuteionStatus();
					}
				} catch (JSONException e) {
					Log.e(TAG,e.getMessage());
				}
			}
		});
		
	}

	private void changeViewToNewTextPiece(StoryModel model, UUID uuid) {
		Intent intent = new Intent(this, TextPieceActivity.class);
		intent.putExtra(STORY_MODEL_KEY, model);
		intent.putExtra(UUID_KEY, uuid.toString());
		startActivity(intent);
	}
}
