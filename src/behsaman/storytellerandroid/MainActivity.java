package behsaman.storytellerandroid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends ActionBarActivity{
	LinedEditText lined;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//setContentView(R.layout.activity_newfeed);
        imageView = (ImageView) findViewById(R.id.result);
        lined = new LinedEditText(this, null);
        lined.setLayoutParams(new LayoutParams(250,250));
        LinearLayout layout = (LinearLayout)findViewById(R.id.mainlinearlayout);
        layout.addView(lined);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	      // Handle item selection
	      switch (item.getItemId()) {
	      case R.id.action_new_story:
	          changeViewToCreateNewStory();
	          return true;
	      }
	      return false;
	}
	
	public void changeViewToCreateNewStory() {
		Intent intent = new Intent(this, NewStoryActivity.class);
		startActivity(intent);
		
	}
	
	public void changeView(View v) {
		Intent intent = new Intent(this, NewsfeedActivity.class);
		startActivity(intent);
	}
	
	public void changeViewToStoryPage(View v) {
		Intent intent = new Intent(this, StoryPageActivity.class);
		startActivity(intent);
	}
	
	public void changeViewTotextPiece(View view)
	{
		Intent intent = new Intent(this, TextPieceActivity.class);
		startActivity(intent);
	}
	
	private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;
    private ImageView imageView;
	public void pickImage(View view) {
		Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);
		
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
            try {
                // We need to recyle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }
                InputStream stream = getContentResolver().openInputStream(
                        data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	
}
