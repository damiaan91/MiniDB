package minidb.core.model.action;

import minidb.core.model.data.ISession;

public interface IAction {
	public String toString();
	public String ExecuteUsing(ISession session);
}
