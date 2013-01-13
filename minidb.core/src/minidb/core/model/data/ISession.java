package minidb.core.model.data;

import minidb.core.model.action.*;

public interface ISession {
	String select(Select select);
	String insert(Insert insert);
	String alter(Alter alter);
	String create(Create create);
	String execute(IAction action);
	String getDatabaseName();
	boolean isActive();
	void disconnect();
}
