package behsaman.storytellerandroid.datamodel;

import java.util.Date;
import java.util.UUID;

public class PullRequestResult {
	public UUID uuid = null;
	public int queueSize = 0;
	public Date expDate;
	
	public PullRequestResult(UUID uuid, int queueSize, Date exp) {
		super();
		this.uuid = uuid;
		this.queueSize = queueSize;
		this.expDate = exp;
	}
	
	@Override
	public String toString() {
		return "{UUID:"+uuid.toString()+"\tQueue Len:"+queueSize+"\tExpiry Date:"+this.expDate+"}";
	}
}