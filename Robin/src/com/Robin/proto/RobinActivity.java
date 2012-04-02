package com.Robin.proto;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class RobinActivity extends Activity {
	public WifiManager wifi = null;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
		tabHost.setup();

		TabSpec spec1 = tabHost.newTabSpec("Tab 1");
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("View Messages");

		TabSpec spec2 = tabHost.newTabSpec("Tab 2");
		spec2.setIndicator("Send Messages");
		spec2.setContent(R.id.tab2);

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		((TextView) findViewById(R.id.messageView))
				.setMovementMethod(new ScrollingMovementMethod());
		((Button) findViewById(R.id.quit))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onDestroy();

					}
				});

		wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		unlockMulticast();

		MessageManager tcp = new MessageManager(this);
		tcp.start();

		UDPAnnouncementManager udp = new UDPAnnouncementManager(tcp, this);
		udp.start();
	}

	public void unlockMulticast() {
		if (wifi != null) {
			MulticastLock mcLock = wifi.createMulticastLock("mylock");
			mcLock.acquire();
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.runFinalizersOnExit(true);
		System.exit(0);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	

}
