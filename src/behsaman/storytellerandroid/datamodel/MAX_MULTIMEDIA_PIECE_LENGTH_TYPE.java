package behsaman.storytellerandroid.datamodel;

public enum MAX_MULTIMEDIA_PIECE_LENGTH_TYPE {
	SHORT(100),MEDIUM(500),LONG(1000);
	
    private int val;
    MAX_MULTIMEDIA_PIECE_LENGTH_TYPE (int numVal) {
        this.val = numVal;
    }
   
    public Integer getNumVal() {
        return val;
    }

	public static MAX_MULTIMEDIA_PIECE_LENGTH_TYPE valueOf(int v) {
		switch (v)
    	{
    		case 100:
    			return SHORT;
    		case 500:
    			return MEDIUM;
    		case 1000:
    			return LONG;
			default:
				return null;
    				
    	}
	}
}
