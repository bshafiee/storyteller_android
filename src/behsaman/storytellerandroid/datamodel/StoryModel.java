package behsaman.storytellerandroid.datamodel;

import java.io.Serializable;
import java.util.Date;

public class StoryModel implements Serializable{
	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = -8315185619566159803L;
	
	public static final String DATE_FORMAT = "MMM dd, yyyy"; 
	//Fields
	private Integer id;
	private Integer owner;
	private String category;
	private String title;
	private STORY_TYPE type;
	private MAX_NUM_PIECES_TYPE max_num_pieces;
	private MAX_MULTIMEDIA_PIECE_LENGTH_TYPE max_multimedia_piece_length;
	private MAX_TEXT_PIECE_LENGTH_TYPE max_text_piece_length;
	private LOCK_TIME_MINS lock_time_mins;
	private int next_available_piece; //Pieces start at 1
	private Date created_on;
	
	public StoryModel() {this.reset();}
	
	public StoryModel(Integer id, Integer owner_id, String category, String title,
			STORY_TYPE s_type,
			MAX_NUM_PIECES_TYPE max_num_pieces,
			MAX_MULTIMEDIA_PIECE_LENGTH_TYPE max_multimedia_piece_length,
			MAX_TEXT_PIECE_LENGTH_TYPE max_text_piece_length,
			LOCK_TIME_MINS lock_time,
			int next_available_piece, Date created_on) {
		super();
		this.id = id;
		this.owner = owner_id;
		this.category = category;
		this.title = title;
		this.type = s_type;
		this.max_num_pieces = max_num_pieces;
		this.max_multimedia_piece_length = max_multimedia_piece_length;
		this.max_text_piece_length = max_text_piece_length;
		this.lock_time_mins = lock_time;
		this.next_available_piece = next_available_piece;
		this.created_on = created_on;
	}

	public StoryModel(Integer owner_id, String category, String title,
			STORY_TYPE s_type,
			MAX_NUM_PIECES_TYPE max_num_pieces,
			MAX_MULTIMEDIA_PIECE_LENGTH_TYPE max_multimedia_piece_length,
			MAX_TEXT_PIECE_LENGTH_TYPE max_text_piece_length,
			LOCK_TIME_MINS lock_time) {
		super();
		this.reset();
		this.owner = owner_id;
		this.category = category;
		this.title = title;
		this.type = s_type;
		this.max_num_pieces = max_num_pieces;
		this.max_multimedia_piece_length = max_multimedia_piece_length;
		this.max_text_piece_length = max_text_piece_length;
		this.lock_time_mins = lock_time;
	}

	private void reset()
	{
		this.id = -1;
		this.owner = null;
		this.category = null;
		this.title = null;
		this.type = null;
		this.max_num_pieces = MAX_NUM_PIECES_TYPE.SHORT;
		this.max_multimedia_piece_length = MAX_MULTIMEDIA_PIECE_LENGTH_TYPE.SHORT;
		this.max_text_piece_length = MAX_TEXT_PIECE_LENGTH_TYPE.SHORT;
		this.lock_time_mins = LOCK_TIME_MINS.MODERATE;
		this.next_available_piece = 0;
		this.created_on = null;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getOwner_id() {
		return owner;
	}

	public void setOwner_id(Integer owner_id) {
		this.owner = owner_id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public MAX_NUM_PIECES_TYPE getMax_num_pieces() {
		return max_num_pieces;
	}

	public void setMax_num_pieces(MAX_NUM_PIECES_TYPE max_num_pieces) {
		this.max_num_pieces = max_num_pieces;
	}

	public MAX_MULTIMEDIA_PIECE_LENGTH_TYPE getMax_multimedia_piece_length() {
		return max_multimedia_piece_length;
	}

	public void setMax_multimedia_piece_length(
			MAX_MULTIMEDIA_PIECE_LENGTH_TYPE max_multimedia_piece_length) {
		this.max_multimedia_piece_length = max_multimedia_piece_length;
	}

	public MAX_TEXT_PIECE_LENGTH_TYPE getMax_text_piece_length() {
		return max_text_piece_length;
	}

	public void setMax_text_piece_length(
			MAX_TEXT_PIECE_LENGTH_TYPE max_text_piece_length) {
		this.max_text_piece_length = max_text_piece_length;
	}

	public LOCK_TIME_MINS getLock_time_mins() {
		return lock_time_mins;
	}

	public void setLock_time_mins(LOCK_TIME_MINS lock_time_mins) {
		this.lock_time_mins = lock_time_mins;
	}

	public Integer getNext_available_piece() {
		return next_available_piece;
	}

	public void setNext_available_piece(int next_available_piece) {
		this.next_available_piece = next_available_piece;
	}

	public Date getCreated_on() {
		return created_on;
	}

	public void setCreated_on(Date created_on) {
		this.created_on = created_on;
	}

	public STORY_TYPE getType() {
		return type;
	}

	public void setType(STORY_TYPE type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ID:"+this.id+"\tTitle:"+this.title;
	}

	
}

