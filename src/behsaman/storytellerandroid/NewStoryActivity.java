package behsaman.storytellerandroid;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import behsaman.storytellerandroid.datamodel.LOCK_TIME_MINS;
import behsaman.storytellerandroid.datamodel.MAX_MULTIMEDIA_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_NUM_PIECES_TYPE;
import behsaman.storytellerandroid.datamodel.MAX_TEXT_PIECE_LENGTH_TYPE;
import behsaman.storytellerandroid.datamodel.STORY_TYPE;
import behsaman.storytellerandroid.datamodel.StoryModel;
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
	
	public void addStoryHandler(View v)
	{
		EditText titleBox = (EditText)findViewById(R.id.et_new_story_title);
		Spinner categorySpinner = (Spinner)findViewById(R.id.spinner_new_story_category);
		RadioGroup numPiecesRadioGroup = (RadioGroup) findViewById(R.id.radiogroup_new_story_num_pieces);
		RadioGroup pieceLengthRadioGroup = (RadioGroup) findViewById(R.id.radiogroup_new_story_piece_length);
		RadioGroup lockTimeRadioGroup = (RadioGroup) findViewById(R.id.radiogroup_new_story_lock_time);
		RadioGroup storyTypeRadioGroup = (RadioGroup) findViewById(R.id.radiogroup_new_story_story_type);
		//Create a new Story Model
		StoryModel newStory = new StoryModel();
		newStory.setTitle(titleBox.getText().toString());
		newStory.setCategory(categorySpinner.getSelectedItem().toString());
		
		STORY_TYPE storyType = null;
		switch(storyTypeRadioGroup.getCheckedRadioButtonId())
		{
			case R.id.radio_new_story_type_text:
				storyType = STORY_TYPE.TEXT_ONLY;
				break;
			case R.id.radio_new_story_type_text_pic:
				storyType = STORY_TYPE.COMICS;
				break;
			case R.id.radio_new_story_type_audio:
				storyType = STORY_TYPE.AUDIO;
				break;
			case R.id.radio_new_story_type_video:
				storyType = STORY_TYPE.VIDEO;
				break;
		}
		newStory.setType(storyType);
		
		MAX_NUM_PIECES_TYPE numPiece = null;
		switch(numPiecesRadioGroup.getCheckedRadioButtonId())
		{
			case R.id.radio_new_story_num_piece_short:
				numPiece = MAX_NUM_PIECES_TYPE.SHORT;
				break;
			case R.id.radio_new_story_num_piece_medium:
				numPiece = MAX_NUM_PIECES_TYPE.MEDIUM;
				break;
			case R.id.radio_new_story_num_piece_long:
				numPiece = MAX_NUM_PIECES_TYPE.LONG;
				break;
		}

		if(storyType==STORY_TYPE.TEXT_ONLY||storyType==STORY_TYPE.COMICS)
		{
			MAX_TEXT_PIECE_LENGTH_TYPE text_piece_length = null;
			switch(pieceLengthRadioGroup.getCheckedRadioButtonId())
			{
				case R.id.radio_new_story_num_piece_short:
					text_piece_length = MAX_TEXT_PIECE_LENGTH_TYPE.SHORT;
					break;
				case R.id.radio_new_story_num_piece_medium:
					text_piece_length = MAX_TEXT_PIECE_LENGTH_TYPE.MEDIUM;
					break;
				case R.id.radio_new_story_num_piece_long:
					text_piece_length = MAX_TEXT_PIECE_LENGTH_TYPE.LONG;
					break;
			}
			newStory.setMax_text_piece_length(text_piece_length);
			newStory.setMax_multimedia_piece_length(MAX_MULTIMEDIA_PIECE_LENGTH_TYPE.ZERO);
		}
		else
		{
			MAX_MULTIMEDIA_PIECE_LENGTH_TYPE multimedia_piece_length = null;
			switch(pieceLengthRadioGroup.getCheckedRadioButtonId())
			{
				case R.id.radio_new_story_num_piece_short:
					multimedia_piece_length = MAX_MULTIMEDIA_PIECE_LENGTH_TYPE.SHORT;
					break;
				case R.id.radio_new_story_num_piece_medium:
					multimedia_piece_length = MAX_MULTIMEDIA_PIECE_LENGTH_TYPE.MEDIUM;
					break;
				case R.id.radio_new_story_num_piece_long:
					multimedia_piece_length = MAX_MULTIMEDIA_PIECE_LENGTH_TYPE.LONG;
					break;
			}
			newStory.setMax_multimedia_piece_length(multimedia_piece_length);
			newStory.setMax_text_piece_length(MAX_TEXT_PIECE_LENGTH_TYPE.ZERO);
		}
		
		LOCK_TIME_MINS lockTime = null;
		switch(lockTimeRadioGroup.getCheckedRadioButtonId())
		{
			case R.id.radio_new_story_lock_time_quick:
				lockTime = LOCK_TIME_MINS.QUICK;
				break;
			case R.id.radio_new_story_lock_time_fast:
				lockTime = LOCK_TIME_MINS.FAST;
				break;
			case R.id.radio_new_story_lock_time_moderate:
				lockTime = LOCK_TIME_MINS.MODERATE;
				break;
			case R.id.radio_new_story_lock_time_long:
				lockTime = LOCK_TIME_MINS.LONG;
				break;
			case R.id.radio_new_story_lock_time_very_long:
				lockTime = LOCK_TIME_MINS.VERY_LONG;
				break;
		}
		newStory.setLock_time_mins(lockTime);
		newStory.setMax_num_pieces(numPiece);
		
		validateAndSendStory(newStory);
	}
	
	private void validateAndSendStory(StoryModel newStory)
	{
		if(!ServerIO.getInstance().isLoggedIn())
		{
			Log.e(TAG,"FatalError: You can't add a new story while you're not logged in");
			return;
		}
		
		RequestParams params = new RequestParams();
		params.add("category", newStory.getCategory());
		params.add("title", newStory.getTitle());
		params.add("type", newStory.getType().toString());
		params.add("max_num_pieces", newStory.getMax_num_pieces().getNumVal().toString());
		params.add("max_multimedia_piece_length", newStory.getMax_multimedia_piece_length().getNumVal().toString());
		params.add("max_text_piece_length", newStory.getMax_text_piece_length().getNumVal().toString());
		params.add("lock_time_mins", newStory.getLock_time_mins().getNumVal().toString());
		
		//Create ProgressBar
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Adding your story...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
		
        progress.show();
		ServerIO.getInstance().post(ServerIO.INSERT_STORY_URL, params, new JsonHttpResponseHandler() {
			@Override
            public synchronized void onSuccess(JSONObject obj) {
				progress.dismiss();
				try {
					if(obj.getString("status").equals("Successful"))
					{
						changeViewToFeedActivity();
					}
					else
					{
						showFailureToast();
						Log.e(TAG, "Error in adding Story:"+obj.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
            }
		});
	}
	
	public void showSuccessToast()
	{
		Context context = this;
		CharSequence text = "Your story was added successfully!";
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.CENTER, 0,0);
		toast.show();
	}
	
	public void showFailureToast()
	{
		Context context = this;
		CharSequence text = "Sorry, an error occured while adding your story :(";
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.CENTER, 0,0);
		toast.show();
	}
	
	
	private void changeViewToFeedActivity() {
		Intent intent = new Intent(this, NewsfeedActivity.class);
		startActivity(intent);
		
	}
}
