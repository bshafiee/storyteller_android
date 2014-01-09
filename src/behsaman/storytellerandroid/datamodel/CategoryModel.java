package behsaman.storytellerandroid.datamodel;


public class CategoryModel {
	//Fields
	private int id;
	private String name;
	private String description;
	
	public CategoryModel() {this.reset();}
	
	public CategoryModel(int id, String name, String description) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public CategoryModel(String name, String description) {
		super();
		this.reset();
		this.name = name;
		this.description = description;
	}

	private void reset()
	{
		this.id = -1;
		this.name = null;
		this.description = null;
	}

	public int getID() 
	{
		return id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
