package game;


public class Entity {
	/*

	private Cube6f box;
	public Vector3f velocity;
	public Vector3f acceleration;
	
	// Interaction properties
	private Terrain collisionTerrain;
	
	public Entity(Cube6f box, Terrain collisionTerrain) {
		this.box = box;
		this.collisionTerrain = collisionTerrain;
	}
	
	public void update(float deltaTime) {
		// Apply the acceleration to the velocity
		velocity.add(Vector3f.multiply(acceleration, deltaTime));
		
		// Apply the velocity to the position
		if(collisionTerrain != null) {
			// Try moving in all directions
			Vector3f deltaPosXYZ = Vector3f.multiply(velocity, deltaTime);
			Cube6f newBox = Cube6f.move(box, deltaPosXYZ);
			
			if(!collisionTerrain.collision(newBox)) {
				box = newBox;
				return;
			}
			
			// Try moving in two directions
			Vector3f deltaPosXY = Vector3f.multiply(velocity, deltaTime);
			deltaPosXY.z = 0.0f;
			
			newBox = Cube6f.move(box, deltaPosXY);
			
			if(!collisionTerrain.collision(newBox)) {
				box = newBox;
				return;
			}
			
			Vector3f deltaPosYZ = Vector3f.multiply(velocity, deltaTime);
			deltaPosXY.x = 0.0f;
			
			newBox = Cube6f.move(box, deltaPosYZ);
			
			if(!collisionTerrain.collision(newBox)) {
				box = newBox;
				return;
			}
			
			Vector3f deltaPosXZ = Vector3f.multiply(velocity, deltaTime);
			deltaPosXY.y = 0.0f;
			
			newBox = Cube6f.move(box, deltaPosXZ);
			
			if(!collisionTerrain.collision(newBox)) {
				box = newBox;
				return;
			}
			
			// Try moving in one direction only
			Vector3f deltaPosX = Vector3f.multiply(velocity, deltaTime);
			deltaPosXY.y = 0.0f;
			deltaPosXY.z = 0.0f;
			
			newBox = Cube6f.move(box, deltaPosX);
			
			if(!collisionTerrain.collision(newBox)) {
				box = newBox;
				return;
			}
			
			Vector3f deltaPosY = Vector3f.multiply(velocity, deltaTime);
			deltaPosXY.x = 0.0f;
			deltaPosXY.z = 0.0f;
			
			newBox = Cube6f.move(box, deltaPosY);
			
			if(!collisionTerrain.collision(newBox)) {
				box = newBox;
				return;
			}
			
			Vector3f deltaPosZ = Vector3f.multiply(velocity, deltaTime);
			deltaPosXY.y = 0.0f;
			deltaPosXY.x = 0.0f;
			
			newBox = Cube6f.move(box, deltaPosZ);
			
			if(!collisionTerrain.collision(newBox)) {
				box = newBox;
				return;
			}
		} else {
			box.move(Vector3f.multiply(velocity, deltaTime));
		}
	}
	
	public void render() {
		Vector3f pos1 = new Vector3f(box.x1, box.y1, box.z1);
		Vector3f pos2 = new Vector3f(box.x2, box.y2, box.z2);
		
		// Draw the box (this method generally should be overridden)
		GL11.glBegin(GL11.GL_QUADS);
		
			GL11.glNormal3f(1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos1.z);
			GL11.glNormal3f(-1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos1.z);
			GL11.glNormal3f(-1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos2.z);
			GL11.glNormal3f(1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos2.z);
			
			GL11.glNormal3f(1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos2.z);
			GL11.glNormal3f(-1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos2.z);
			GL11.glNormal3f(-1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos1.z);
			GL11.glNormal3f(1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos1.z);

			GL11.glNormal3f(1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos2.z);
			GL11.glNormal3f(-1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos2.z);
			GL11.glNormal3f(-1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos2.z);
			GL11.glNormal3f(1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos2.z);

			GL11.glNormal3f(-1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos1.z);
			GL11.glNormal3f(1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos1.z);
			GL11.glNormal3f(1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos1.z);
			GL11.glNormal3f(-1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos1.z);

			GL11.glNormal3f(1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos1.z);
			GL11.glNormal3f(1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos2.z);
			GL11.glNormal3f(1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos2.z);
			GL11.glNormal3f(1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos1.z);

			GL11.glNormal3f(-1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos2.z);
			GL11.glNormal3f(-1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos1.z);
			GL11.glNormal3f(-1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos1.z);
			GL11.glNormal3f(-1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos2.z);
		
		GL11.glEnd();
	}
	
	public Vector3f getCenterPoint() {
		return new Vector3f((box.x1 + box.x2) / 2, (box.y1 + box.y2) / 2, (box.z1 + box.z2) / 2);
	}
	*/

}
