package pl.adamp.testchecker.test.entities;

public class Student {
	private long id;
	private String fullName;
	private String description;
	private String email;
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	private String phone;
	
	public Student(String fullName) {
		this(-1, fullName);
	}
	
	public Student(long id, String fullName) {
		this.id = id;
		this.fullName = fullName;
	}
}
