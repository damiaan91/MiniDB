package security;

public class Rule {
	
	private String table;
	private boolean readAccess;
	private boolean writeAccess;
	
	public Rule(String table) {
		this(table, true, true);
	}
	
	public Rule (String table, boolean readAccess, boolean writeAccess) {
		this.table = table;
		this.readAccess= readAccess;
		this.writeAccess= writeAccess;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public boolean isHasReadAccess() {
		return readAccess;
	}

	public void setHasReadAccess(boolean hasReadAccess) {
		this.readAccess = hasReadAccess;
	}

	public boolean isHasWriteAccess() {
		return writeAccess;
	}

	public void setHasWriteAccess(boolean hasWriteAccess) {
		this.writeAccess = hasWriteAccess;
	}
	
	
}
