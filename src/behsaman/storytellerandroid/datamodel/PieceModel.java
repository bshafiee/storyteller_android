package behsaman.storytellerandroid.datamodel;

import java.util.Date;

public class PieceModel {
	//Fields
	private int id;
	private int story_id;
	private int creator_id;
	private int index;
	private String text_val;
	private String audio_file_addr;
	private String video_file_addr;
	private String picture_file_addr;
	private Date created_on;
	
	public PieceModel() {this.reset();}
	
	
	
	public PieceModel(int id, int story_id, int creator_id, int index,
			String text_val, String audio_file_addr, String video_file_addr,
			String picture_file_addr, Date created_on) {
		super();
		this.id = id;
		this.story_id = story_id;
		this.creator_id = creator_id;
		this.index = index;
		this.text_val = text_val;
		this.audio_file_addr = audio_file_addr;
		this.video_file_addr = video_file_addr;
		this.picture_file_addr = picture_file_addr;
		this.created_on = created_on;
	}



	public PieceModel(int story_id, int creator_id, int index,
			String text, String audio_file_addr, String video_file_addr,
			String picture_file_addr, Date timeout_timestamp) {
		super();
		this.reset();
		this.story_id = story_id;
		this.creator_id = creator_id;
		this.index = index;
		this.text_val = text;
		this.audio_file_addr = audio_file_addr;
		this.video_file_addr = video_file_addr;
		this.picture_file_addr = picture_file_addr;
		this.created_on = timeout_timestamp;
	}

	private void reset()
	{
		this.id = -1;
		this.story_id = -1;
		this.creator_id = -1;
		this.index = -1;
		this.text_val = null;
		this.audio_file_addr = null;
		this.video_file_addr = null;
		this.picture_file_addr = null;
		this.created_on = null;
	}
	

	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public int getStory_id() {
		return story_id;
	}



	public void setStory_id(int story_id) {
		this.story_id = story_id;
	}



	public int getCreator_id() {
		return creator_id;
	}



	public void setCreator_id(int creator_id) {
		this.creator_id = creator_id;
	}



	public int getIndex() {
		return index;
	}



	public void setIndex(int index) {
		this.index = index;
	}



	public String getText_val() {
		return text_val;
	}



	public void setText_val(String text_val) {
		this.text_val = text_val;
	}



	public String getAudio_file_addr() {
		return audio_file_addr;
	}



	public void setAudio_file_addr(String audio_file_addr) {
		this.audio_file_addr = audio_file_addr;
	}



	public String getVideo_file_addr() {
		return video_file_addr;
	}



	public void setVideo_file_addr(String video_file_addr) {
		this.video_file_addr = video_file_addr;
	}



	public String getPicture_file_addr() {
		return picture_file_addr;
	}



	public void setPicture_file_addr(String picture_file_addr) {
		this.picture_file_addr = picture_file_addr;
	}



	public Date getCreated_on() {
		return created_on;
	}



	public void setCreated_on(Date created_on) {
		this.created_on = created_on;
	}
}
