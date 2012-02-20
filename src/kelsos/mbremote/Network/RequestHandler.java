package kelsos.mbremote.Network;

import java.util.Timer;
import java.util.TimerTask;

import kelsos.mbremote.Intents;
import kelsos.mbremote.R;
import kelsos.mbremote.Network.ProtocolHandler.PlayerAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class RequestHandler {
	private final ConnectivityHandler connectivityHandler;
	private static boolean _isPollingTimerRunning;
	private static boolean _requestCoverAndInfo;
	private Timer _pollingTimer;
	private PollingTimerTask _ptt;

	public RequestHandler(ConnectivityHandler connectivityHandler) {
		this.connectivityHandler = connectivityHandler;
		installFilter();
	}

	public static boolean isPollingTimerRunning() {
		return _isPollingTimerRunning;
	}

	public static void coverAndInfoOutdated() {
		_requestCoverAndInfo = true;
	}

	public void requestAction(ProtocolHandler.PlayerAction action,
			String actionContent) {
		connectivityHandler.sendData(ProtocolHandler.getActionString(action,
				actionContent));
	}

	public void requestAction(ProtocolHandler.PlayerAction action) {
		connectivityHandler.sendData(ProtocolHandler
				.getActionString(action, ""));
	}

	void requestUpdate() {
		if (_requestCoverAndInfo) {
			requestAction(PlayerAction.SongCover);
			requestAction(PlayerAction.SongInformation);
			_requestCoverAndInfo = false;
		}
		requestAction(PlayerAction.SongChangedStatus);
		requestAction(PlayerAction.PlayerStatus);
	}

	void startPollingTimer() {
		if (_pollingTimer == null)
			_pollingTimer = new Timer(true);
		if (_ptt == null)
			_ptt = new PollingTimerTask();
		_pollingTimer.schedule(_ptt, 1000, 1000);
		_isPollingTimerRunning = true;
		Log.d("ConnectivityHandler", "startPollingTimer();");
	}

	void stopPollingTimer() {
		_ptt.cancel();
		_ptt = null;
		_pollingTimer.cancel();
		_pollingTimer = null;
		_isPollingTimerRunning = false;
		Log.d("ConnectivityHandler", "stopPollingTimer();");
	}

	private class PollingTimerTask extends TimerTask {
		@Override
		public void run() {
			requestUpdate();
		}
	}

	private final BroadcastReceiver nmBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(connectivityHandler
							.getApplicationContext());
			if (intent.getAction().equals(
					TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
				Bundle bundle = intent.getExtras();
				if (null == bundle)
					return;
				String state = bundle.getString(TelephonyManager.EXTRA_STATE);
				if (state
						.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
					if (sharedPreferences
							.getBoolean(
									connectivityHandler
											.getApplicationContext()
											.getString(
													R.string.settings_reduce_volume_on_ring),
									false)) {
						requestAction(
								PlayerAction.Volume,
								Integer.toString((int) (ReplyHandler
										.getInstance().getCurrentVolume() * 0.2)));
					}
				}
			}
		}
	};

	/**
	 * Initialized and installs the IntentFilter listening for the SONG_CHANGED
	 * intent fired by the ReplyHandler or the PHONE_STATE intent fired by the
	 * Android operating system.
	 */
	private void installFilter() {
		IntentFilter _nmFilter = new IntentFilter();
		_nmFilter.addAction(Intents.SONG_CHANGED);
		_nmFilter.addAction("android.intent.action.PHONE_STATE");
		connectivityHandler.getApplicationContext().registerReceiver(
				nmBroadcastReceiver, _nmFilter);
	}

	protected void finalize() {
		connectivityHandler.getApplicationContext().unregisterReceiver(
				nmBroadcastReceiver);
	}
}