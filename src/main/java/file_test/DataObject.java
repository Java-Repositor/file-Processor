package file_test;

public class DataObject {
	
	private String name;
	private Integer age;
	private boolean available;
	private double weight;
	private byte bb;
	private float height;

	public String getName() {
		return this.name;
	}

	public DataObject(String name, Integer age, boolean available, double weight, byte bb, float height) {
		this.name = name;
		this.age = age;
		this.available = available;
		this.weight = weight;
		this.bb = bb;
		this.height = height;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return this.age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public boolean isAvailable() {
		return this.available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public double getWeight() {
		return this.weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public byte getBb() {
		return this.bb;
	}

	public void setBb(byte bb) {
		this.bb = bb;
	}

	public float getHeight() {
		return this.height;
	}

	public void setHeight(float height) {
		this.height = height;
	}
}