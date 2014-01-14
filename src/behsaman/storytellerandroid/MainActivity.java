package behsaman.storytellerandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import behsaman.storytellerandroid.networking.MyHttpClient;

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
	
	public void changeView(View view) {
		Intent intent = new Intent(this, NewsfeedActivity.class);
		startActivity(intent);
		
	}
	
}
