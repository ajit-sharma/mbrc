package kelsos.mbremote.Command;

import com.google.inject.Inject;
import kelsos.mbremote.Interfaces.ICommand;
import kelsos.mbremote.Interfaces.IEvent;
import kelsos.mbremote.Services.ProtocolHandler;

import java.util.EventObject;

public class RequestPlayStopCommand implements ICommand
{
	@Inject private ProtocolHandler protocolHandler;
	@Override
	public void execute(IEvent e)
	{
		protocolHandler.requestAction(ProtocolHandler.PlayerAction.Stop);
	}
}
