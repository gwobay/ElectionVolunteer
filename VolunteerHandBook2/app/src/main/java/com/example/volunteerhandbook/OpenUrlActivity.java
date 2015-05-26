package com.example.volunteerhandbook;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class OpenUrlActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_url);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent=getIntent();
		String thisMsg=intent.getStringExtra(ListRecords.PASSDOWN_CHILD_KEY);
		String pageUrl="";
		int idx=thisMsg.indexOf(")");
		String title=" ";
		if (idx>0) {
			title=thisMsg.substring(idx+1);
			pageUrl=thisMsg.substring(0, idx);
		}
		
		if (pageUrl.length() < 5) return;
		this.setTitle(title);
		WebView page=(WebView)findViewById(R.id.web_view);
		
			page.getSettings().setJavaScriptEnabled(true);
			
			page.setWebViewClient(new WebViewClient(){
				public void onReceivedError(WebView v, int eCode, String what, String who)
				{
					Toast.makeText(getParent(), "Err("+eCode+" : "+what, Toast.LENGTH_SHORT).show();
				}
			});
		
			page.loadUrl(pageUrl);
			
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public void onStop()
	{
		super.onStop();
		finish();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.open_url, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
