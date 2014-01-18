package behsaman.storytellerandroid.datamodel;

public enum MAX_TEXT_PIECE_LENGTH_TYPE {
	SHORT(100),MEDIUM(500),LONG(1000),ZERO(0);
	
    private int val = 0;
    MAX_TEXT_PIECE_LENGTH_TYPE (int numVal) {
        this.val = numVal;
    }
   
    public Integer getNumVal() {
        return val;
    }

	public static MAX_TEXT_PIECE_LENGTH_TYPE valueOf(int v) {
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
