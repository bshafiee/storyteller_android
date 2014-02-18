package behsaman.storytellerandroid;

import behsaman.storytellerandroid.networking.ServerIO;
import android.app.Activity;
import android.app.ProgressDialog;
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
								// Switching to Register screen
				                Intent i = new Intent(getApplicationContext(), NewsfeedActivity.class);
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

}
