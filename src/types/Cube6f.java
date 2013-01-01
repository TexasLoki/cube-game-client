package types;

public class Cube6f {

	public float x1, y1, z1, x2, y2, z2;
	
	public Cube6f() { }
	
	public Cube6f(float x1, float y1, float z1, float x2, float y2, float z2) {
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
	}
	
	public void move(Vector3f delta) {
		x1 += delta.x;
		y1 += delta.y;
		z1 += delta.z;
		x2 += delta.x;
		y2 += delta.y;
		z2 += delta.z;
	}
	
	public static Cube6f move(Cube6f cube, Vector3f delta) {
		Cube6f c = new Cube6f();
		
		c.x1 = cube.x1 + delta.x;
		c.y1 = cube.y1 + delta.y;
		c.z1 = cube.z1 + delta.z;
		c.x2 = cube.x2 + delta.x;
		c.y2 = cube.y2 + delta.y;
		c.z2 = cube.z2 + delta.z;
		
		return c;
	}
}
