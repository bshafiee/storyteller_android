package behsaman.storytellerandroid.datamodel;

import java.util.Date;


public class UserModel {
	//Data Fields
	private int id;
	private String username;
	private String firstname;
	private String lastname;
	private String email;
	private Date	created_on;
	private Date	last_login;
	
	public UserModel() {this.reset();}
	
	public UserModel(String username, String firstname, String lastname, String email) {
		super();
		this.reset();
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}
	
	private void reset()
	{
		this.username = null;
		this.firstname = null;
		this.lastname = null;
		this.email = null;
		this.id = -1;
		this.created_on = null;
		this.last_login = null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getCreated_on() {
		return created_on;
	}

	public void setCreated_on(Date created_on) {
		this.created_on = created_on;
	}

	public Date getLast_login() {
		return last_login;
	}

	public void setLast_login(Date last_login) {
		this.last_login = last_login;
	}
}
