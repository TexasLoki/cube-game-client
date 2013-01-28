package game;
import org.lwjgl.opengl.GL11;

import terrain.World;
import types.Vector3;
import types.Vector3f;


public class Camera {

	// Moving directions
	public static final int FORWARD = 0;
	public static final int BACKWARD = 1;
	public static final int RIGHT = 2;
	public static final int LEFT = 3;
	
	// Coordinates and rotation
	public Vector3f coordinates;
	public Vector3f rotation;
	
	// Target block
	private Vector3 existingCube;
	private Vector3 emptyCube;
	
	private World world;
	
	public Camera(Vector3f coordinates, Vector3f rotation, World world) {
		this.coordinates = coordinates;
		this.rotation = rotation;
		this.world = world;
	}
	
	// Finds both target cubes
	public void calculateTargetBlock(float maxDistance, Camera camera, World world) {
		// Step size (less is more accurate, but slower)
		float step = 0.01f;
		
		// Create a vector keeping track of the position
		Vector3f testPos = new Vector3f(camera.coordinates.x, camera.coordinates.y, camera.coordinates.z);
		Vector3f testSpeed = new Vector3f((float) (Math.cos(Math.toRadians(camera.rotation.x)) * -Math.sin(Math.toRadians(camera.rotation.y))) * step,
										(float) (Math.sin(Math.toRadians(camera.rotation.x))) * step,
										(float) (Math.cos(Math.toRadians(camera.rotation.x)) * -Math.cos(Math.toRadians(camera.rotation.y))) * step);
		
		// Is one step behind of testPos
		Vector3f lastTestPos = new Vector3f(camera.coordinates.x, camera.coordinates.y, camera.coordinates.z);
		
		// Find blocks
		float distance = 0;
		
		while(distance < maxDistance) {
			// Check if a block was found
			if(world.solidAt(testPos)) {
				existingCube = world.arrayCoordinates(testPos);
				emptyCube = world.arrayCoordinates(lastTestPos);

				return;
			}
			
			// Save the last position
			lastTestPos.x = testPos.x;
			lastTestPos.y = testPos.y;
			lastTestPos.z = testPos.z;
			
			// Increase the position
			testPos.add(testSpeed);
			
			// Increase the distance tracking float
			distance += step;		
		}
		
		existingCube = null;
		emptyCube = null;
	}
	
	public Vector3 getTargetExistingCube() {
		return existingCube;
	}
	
	public Vector3 getTargetEmptyCube() {
		return emptyCube;
	}
	
	
	public void move(float delta, int direction, float gravityDelta, boolean collisionChecking, boolean flyMode) {
		Vector3f newCoordinates = new Vector3f(coordinates.x, coordinates.y, coordinates.z);
		
		if(direction == FORWARD) {
			if(flyMode) {
				// Includes moving in the Y-direction
				newCoordinates.x += Math.cos(Math.toRadians(rotation.x)) * -Math.sin(Math.toRadians(rotation.y)) * delta;
				newCoordinates.y += Math.sin(Math.toRadians(rotation.x)) * delta;
				newCoordinates.z += Math.cos(Math.toRadians(rotation.x)) * -Math.cos(Math.toRadians(rotation.y)) * delta;
			} else {
				// No moving in the Y-direction. (2D version, use if flying is forbidden)
				newCoordinates.x += -Math.sin(Math.toRadians(rotation.y)) * delta;
				newCoordinates.z += -Math.cos(Math.toRadians(rotation.y)) * delta;
			}
		} else if(direction == BACKWARD) {
			if(flyMode) {
				// Includes moving in the Y-direction
				newCoordinates.x -= Math.cos(Math.toRadians(rotation.x)) * -Math.sin(Math.toRadians(rotation.y)) * delta;
				newCoordinates.y -= Math.sin(Math.toRadians(rotation.x)) * delta;
				newCoordinates.z -= Math.cos(Math.toRadians(rotation.x)) * -Math.cos(Math.toRadians(rotation.y)) * delta;
			} else {
				// No moving in the Y-direction. (2D version, use if flying is forbidden)
				newCoordinates.x -= -Math.sin(Math.toRadians(rotation.y)) * delta;
				newCoordinates.z -= -Math.cos(Math.toRadians(rotation.y)) * delta;
			}
		} else if(direction == RIGHT) {
			// Only move in the XZ-directions
			newCoordinates.x += Math.sin(Math.toRadians(rotation.y + 90)) * delta;
			newCoordinates.z += Math.sin(Math.toRadians(-rotation.y)) * delta;
		} else if(direction == LEFT) {
			// Only move in the XZ-directions
			newCoordinates.x -= Math.sin(Math.toRadians(rotation.y + 90)) * delta;
			newCoordinates.z -= Math.sin(Math.toRadians(-rotation.y)) * delta;
		}
		
		// Add false gravity
		newCoordinates.y -= gravityDelta;
		
		// Collision detection
		if(collisionChecking) {
			if(!collision(newCoordinates.x, coordinates.y, coordinates.z)) {
				coordinates.x = newCoordinates.x;
			}
			
			if(!collision(coordinates.x, newCoordinates.y, coordinates.z)) {
				coordinates.y = newCoordinates.y;
			}
			
			if(!collision(coordinates.x, coordinates.y, newCoordinates.z)) {
				coordinates.z = newCoordinates.z;
			}
		} else {
			coordinates.x = newCoordinates.x;
			coordinates.y = newCoordinates.y;
			coordinates.z = newCoordinates.z;
		}
	}
	
	public boolean collision(float x, float y, float z) {
		// Simulate a cube cross around the point
		float cubeSize = 1.0f;
		
		Vector3f c1 = new Vector3f(x - cubeSize / 2, y, z);
		Vector3f c2 = new Vector3f(x + cubeSize / 2, y, z);
		Vector3f c3 = new Vector3f(x, y - 2f, z);				// This is 2f to somewhat simulate the proportions of a human (head/camera at top of body)
		Vector3f c4 = new Vector3f(x, y + cubeSize / 2, z);
		Vector3f c5 = new Vector3f(x, y, z - cubeSize / 2);
		Vector3f c6 = new Vector3f(x, y, z + cubeSize / 2);
		
		if(!world.solidAt(c1) && !world.solidAt(c2) && !world.solidAt(c3) && !world.solidAt(c4) && !world.solidAt(c5) && !world.solidAt(c6)) {
			return false;
		}
		
		return true;
	}
	
	/* Use this when adding rotation instead of Vector3f.add since this function makes the numbers stay under 360. */
	public void addRotation(Vector3f rot) {
		rotation.x += rot.x;
		rotation.y += rot.y;
		rotation.z += rot.z;
		
		if(rotation.x >= 360.0f || rotation.x <= -360.0f)
			rotation.x = rotation.x % 360.0f;
		
		if(rotation.y >= 360.0f || rotation.y <= -360.0f)
			rotation.y = rotation.y % 360.0f;
		
		if(rotation.z >= 360.0f || rotation.z <= -360.0f)
			rotation.z = rotation.z % 360.0f;
		
		// Gimbal lock
		if(rotation.x <= -90.0f)
			rotation.x = -90.0f;
		else if(rotation.x >= 90.0f)
			rotation.x = 90.0f;
	}
	
	public void applyMatrix() {
		// Rotate
		GL11.glRotatef(-rotation.x, 1.0f, 0.0f, 0.0f);
		GL11.glRotatef(-rotation.y, 0.0f, 1.0f, 0.0f);
		GL11.glRotatef(-rotation.z, 0.0f, 0.0f, 1.0f);
		
		// Translate
		GL11.glTranslatef(-coordinates.x, -coordinates.y, -coordinates.z);
	}
	
}
