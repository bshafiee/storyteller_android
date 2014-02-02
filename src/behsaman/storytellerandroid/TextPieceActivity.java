package behsaman.storytellerandroid;

import behsaman.storytellerandroid.datamodel.StoryModel;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextPieceActivity extends Activity {

	private static final String TAG = "TextPieceActivity";
	
	StoryModel model = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_piece);
		//UI Elements
		addUIComponents();
	}

	private void addUIComponents() {
		LinearLayout layout_text_piece = (LinearLayout) findViewById(R.id.layout_text_piece);
		/*
		 */
		//Add Text Editor
		final LinedEditText textEditor = new LinedEditText(this, null);
		LinearLayout.LayoutParams linLayout = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		int margins = 15;
		int height = (int)(getScreenHeight()*6.0/9.0);
		linLayout.height = height;
		linLayout.setMargins(margins, margins, margins, margins);
		textEditor.setLayoutParams(linLayout);
		textEditor.setMinLines(12);
		textEditor.setHeight(height);
		
		//textEditor.setMaxHeight(layout_text_piece.getHeight()-layout_text_piece.getHeight()/4);
		layout_text_piece.addView(textEditor);
		//Add Text View
		final TextView tView = new TextView(this);
		tView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		tView.setText("0 of 500 Words");
		layout_text_piece.addView(tView);
		
		//Button
		Button button = new Button(this);		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity=Gravity.CENTER;
        button.setLayoutParams(params);
		button.setText("Add Piece");
		layout_text_piece.addView(button);
		//Word Change event
		textEditor.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				int count = countWords(textEditor.getText().toString());
				tView.setText(count+" of 500");
			}
		});
		
		//Get Intent Info
		model = (StoryModel) getIntent().getSerializableExtra(StoryPageActivity.STORY_MODEL);
		Log.e(TAG, model.toString());
		
		//Set Title
		TextView titleView = (TextView)findViewById(R.id.tv_text_piece_title);
		titleView.setText(model.getTitle());
	}
	
	public int countWords(String in)
	{
		if(in == null)
			return 0;
		String trim = in.trim();
		if (trim.isEmpty()) return 0;
		return trim.split("\\s+").length; //separate string around spaces
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.text_piece, menu);
		return true;
	}

	@SuppressLint("NewApi")
	public int getScreenHeight()
	{
		int Measuredwidth = 0;
		int Measuredheight = 0;
		Point size = new Point();
		WindowManager w = getWindowManager();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
		{
			w.getDefaultDisplay().getSize(size);
		
			Measuredwidth = size.x;
			Measuredheight = size.y; 
		}
		else
		{
			Display d = w.getDefaultDisplay(); 
			Measuredwidth = d.getWidth(); 
			Measuredheight = d.getHeight();
		}
		return Measuredheight;
	}
}
