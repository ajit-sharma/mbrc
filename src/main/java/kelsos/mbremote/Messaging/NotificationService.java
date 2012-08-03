package kelsos.mbremote.Messaging;

import android.content.Context;
import android.widget.Toast;
import com.google.inject.Inject;
import kelsos.mbremote.controller.RunningActivityAccessor;

public class NotificationService
{
	private Context context;
	private RunningActivityAccessor accessor;

	@Inject
	public NotificationService(Context context, RunningActivityAccessor accessor)
	{
		this.context = context;
		this.accessor = accessor;
	}

	/**
	 * Given a message
	 *
	 * @param message
	 */
	private void showToast(final String message)
	{
		if (accessor.getRunningActivity() == null) return;
		accessor.getRunningActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Using an id of the string stored in the strings XML this function
	 * displays a toast window.
	 */
	public void showToastMessage(final int id)
	{
		String data = context.getString(id);
		showToast(data);
	}

	/**
	 * Given a message, it displays the message on a toast window.
	 * If the AppNotification manager is not properly initialized
	 * nothing happens.
	 *
	 * @param message
	 */
	public void showToastMessage(final String message)
	{
		showToast(message);
	}

}