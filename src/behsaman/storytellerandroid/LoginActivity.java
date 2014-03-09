package behsaman.storytellerandroid;

import behsaman.storytellerandroid.networking.ServerIO;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

	private static final int LOGIN_TIMEOUT = 5000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Initialization all the application initialization codes
		ServerIO.getInstance().initialize(this);
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		TextView registerScreen = (TextView) findViewById(R.id.link_to_register);
        // Listening to register new account link
        registerScreen.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });
        
        //Create ProgressBar
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Connecting...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        
        
        Button loginButton = (Button) findViewById(R.id.btnLogin);
        //Sign in
        loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText userBox = (EditText) findViewById(R.id.editTextUsername);
				EditText passBox = (EditText) findViewById(R.id.editTextPassword);
				ServerIO.getInstance().login(userBox.getText().toString(),passBox.getText().toString());
				progress.show();
				Thread loginThread = new Thread(new Runnable() {
					public void run() {
						long now = System.currentTimeMillis();
						while(System.currentTimeMillis()-now < LOGIN_TIMEOUT)
						{
							if(ServerIO.getInstance().isLoggedIn())
							{
								// Switching to Main Screen
				                Intent i = new Intent(getApplicationContext(), MainActivity.class);
				                startActivity(i);
								break;
							}
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						progress.cancel();
					}
				});
				loginThread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				loginThread.start();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setMessage("Are you sure you want to exit?")
				.setCancelable(false)
				.setPositiveButton("Yes :(",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								LoginActivity.this.finish();
								Intent intent = new Intent(Intent.ACTION_MAIN);
								intent.addCategory(Intent.CATEGORY_HOME);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}
						}).setNegativeButton("No :)", null).show();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		//Initialization all the application initialization codes
		super.onResume();
		ServerIO.getInstance().initialize(this);
	}
	
	

}
