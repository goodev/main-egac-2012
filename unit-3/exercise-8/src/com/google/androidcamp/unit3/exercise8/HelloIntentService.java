package com.google.androidcamp.unit3.exercise8;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;


public class HelloIntentService extends IntentService {

	  /** 
	   * Call the super IntentService(String) constructor with a name for the worker thread.
	   */
	  public HelloIntentService() {
	      super("HelloIntentService");
	  }

	  /**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */
	  @Override
	  protected void onHandleIntent(Intent paramIntent) {

	  }
}