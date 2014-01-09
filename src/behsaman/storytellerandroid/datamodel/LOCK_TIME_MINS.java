package behsaman.storytellerandroid.datamodel;

public enum LOCK_TIME_MINS {
	QUICK(1), FAST(5), MODERATE(10), LONG(60), VERY_LONG(60*24); 
    private int val;
    LOCK_TIME_MINS (int numVal) {
        this.val = numVal;
    }
   
    public int getNumVal() {
        return val;
    }

	public static LOCK_TIME_MINS valueOf(int v) {
		switch (v)
    	{
			case 1:
				return QUICK;
			case 5:
				return FAST;
			case 10:
				return MODERATE;
			case 60:
				return LONG;
			case 60*24:
				return VERY_LONG;
			default:
				return null;
    				
    	}
	}
}
