package behsaman.storytellerandroid;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import behsaman.storytellerandroid.datamodel.LOCK_TIME_MINS;
import behsaman.storytellerandroid.datamodel.MAX_MULTIMEDIA_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_NUM_PIECES_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_TEXT_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.PullRequestResult;
import behsaman.storytellerandroid.datamodel.STORY_TYPE;
import behsaman.storytellerandroid.datamodel.StoryModel;
import behsaman.storytellerandroid.networking.ServerIO;
import behsaman.storytellerandroid.utils.Utils;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class StoryPageActivity extends Activity {

	private static final String TAG = "StoryPageActivity";
	
	public static final String STORY_MODEL = "behsaman.storytellerandroid.StoryPageActivity";
	
	private Integer story_id = null;
	private final StoryModel model = new StoryModel();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_page);
		//Get Story ID
		Intent intent = getIntent();
		this.story_id = intent.getIntExtra(NewsfeedActivity.STORY_ID, -1);
		this.updateView(); 
		this.updateContirbuteionStatus();
	}

	private void updateView() {
		//Invalid id
		if(story_id == null || story_id < 1)
			return;
		RequestParams params = new RequestParams();
		params.add("id", story_id.toString());
		ServerIO.getInstance().post(ServerIO.GET_STORY_BY_ID_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject obj) {
				try {
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
			        //Contribute Button
			        Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
			        if(max_num_pieces.getNumVal() > next_available_piece)
			        	contribButton.setEnabled(true);
			        	
					
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

		//Send Contribution Request
		RequestParams params = new RequestParams();
		params.add("story_id", story_id.toString());
		ServerIO.getInstance().post(ServerIO.HAS_REQ_CONTRIBUTION_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject obj) {
				try {
					long request_date = obj.getLong("request_date");
					int user_id = obj.getInt("user_id");
					int story_id = obj.getInt("story_id");
					int story_lock_time = obj.getInt("story_lock_time");
					Long leftMins = new Date().getTime()-request_date;
					leftMins /= 1000;//second
					leftMins /= 60;//Mins
					Log.e(TAG,"Curr:"+new Date().getTime()+"\tReqDate:"+request_date);
					UUID generatedUUID = UUID.fromString(obj.getString("generatedUUID"));
					
					
					TextView infoBox = (TextView)findViewById(R.id.tv_story_page_contrib_info);
			        infoBox.setText("You have "+leftMins+" minutes left to send your piece!");
			        //Contribute Button
			        Button contribButton = (Button) findViewById(R.id.bt_story_page_contribute);
			        contribButton.setEnabled(false);
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

		//Send Contribution Request
		RequestParams params = new RequestParams();
		params.add("story_id", story_id.toString());
		ServerIO.getInstance().post(ServerIO.CONTRIBUTE_REQUEST_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject obj) {
				try {
					UUID uuid = UUID.fromString(obj.getString("uuid"));
					int queueSize = obj.getInt("queueSize");
					Date expDate = new Date(obj.getLong("expDate"));
					PullRequestResult reqResult = new PullRequestResult(uuid, queueSize, expDate);
					Log.e(TAG,reqResult.toString());
					//Update View
					updateContirbuteionStatus();
					//Check Queue Size
					if(queueSize == 1) {//our turn
						if(model.getType()==STORY_TYPE.TEXT_ONLY) {
							changeViewToNewTextPiece(model,reqResult);
						}
					}
				} catch (JSONException e) {
					Log.e(TAG,e.getMessage());
				}
			}
		});
	}

	private void changeViewToNewTextPiece(StoryModel model, PullRequestResult reqResult) {
		Intent intent = new Intent(this, TextPieceActivity.class);
		intent.putExtra(STORY_MODEL, model);
		startActivity(intent);
	}
}
