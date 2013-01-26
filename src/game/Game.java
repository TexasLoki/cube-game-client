package game;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;

import profiling.Profiling;
import profiling.ProfilingPart;
import terrain.Terrain;
import terrain.TerrainCube;
import types.Vector3;
import types.Vector3f;
import types.Vector4f;

public class Game {
	
	private static final float MOUSE_SPEED_SCALE = 0.1f;
	private static final float MOVEMENT_SPEED = 7.0f;
	private static final float MOVEMENT_SPEED_FLYMODE = 12.0f;
	private static final float FALSE_GRAVITY_SPEED = 8.0f;
	private static final boolean FULLSCREEN = true;
	private static final boolean VSYNC = true;
	
	private boolean TEXTURES = true;
	
	private int width;
	private int height;
	
	// Game components
	private Camera camera;
	private Terrain terrain;
	private TextureStore textureStore;
	private boolean running = true;
	
	// Toggles
	private boolean flyMode = true;
	private boolean doCollisionChecking = true;
	private boolean wireframe = false;
	
	// Profiling
	private Profiling profiling = new Profiling();
	private ProfilingPart displayUpdate = new ProfilingPart("Display.update()");
	private ProfilingPart renderFunction = new ProfilingPart("Render");
	private ProfilingPart updateFunction = new ProfilingPart("Update");
	
	// Camera block distance
	private float distance;
	private Texture crossHairTexture;
	private float blockChangeTimer = 0.3f;
	
	// GUI
	private GUI gui;
	
	public void start() {
		// Initialize OpenGL and LWJGL stuff
		init();
		
		// Create the texture store
		textureStore = new TextureStore();
		crossHairTexture = textureStore.getTexture("res/crosshair.png");
		
		// Generate the terrain
		terrain = new Terrain(new Vector3f(-25.0f, -40.0f, -25.0f), new Vector3(16, 1, 16), new Vector3(16, 50, 16), new Vector3f(1.0f, 1.0f, 1.0f), TEXTURES, textureStore);
		
		final int TERRAIN_MIN_HEIGHT = 0;
		final int TERRAIN_MAX_HEIGHT = 40;
		final float TERRAIN_GEN_RESOLUTION = 128.0f;
		final long TERRAIN_GEN_SEED = 2;

		terrain.generateTerrain(TERRAIN_MIN_HEIGHT, TERRAIN_MAX_HEIGHT, TERRAIN_GEN_RESOLUTION,
								TERRAIN_GEN_SEED);
		
		// Create the camera
		camera = new Camera(new Vector3f(0.0f, 50.0f, 0.0f), new Vector3f(-20.0f, -135.0f, 0.0f), terrain);
		
		// Create the GUI
		gui = new GUI();
		
		// Main loop
		long lastFrame = System.currentTimeMillis();
		
		while(!Display.isCloseRequested() && running) {
			// Calculate delta time
			long t = System.currentTimeMillis();
			float deltaTime = (t - lastFrame) * 0.001f;
			
			// Start of frame
			profiling.frameBegin();
			
			// Render
			profiling.partBegin(renderFunction);
			render();
			profiling.partEnd(renderFunction);
			
			// Updates the display, also polls the mouse and keyboard
			profiling.partBegin(displayUpdate);
			Display.update();
			profiling.partEnd(displayUpdate);

			// Update
			profiling.partBegin(updateFunction);
			update(deltaTime);	
			profiling.partEnd(updateFunction);
			
			// End of frame
			profiling.frameEnd();
			
			// Set title to debug info
			Display.setTitle("x: " + (int)camera.coordinates.x + " y: " + (int)camera.coordinates.y + " z: " + (int)camera.coordinates.z +
					" xRot: " + (int)camera.rotation.x + " yRot: " + (int)camera.rotation.y + " zRot: " + camera.rotation.z + " FPS: " + Math.round(profiling.fps()) + " DISTANCE: " + distance);

			gui.setInfoLabel("FPS: " + profiling.fps() +
							"\nvsync: " + VSYNC + 
							"\nfulscreen: " + FULLSCREEN +
							"\ntextures: " + TEXTURES + 
							"\nTERRAIN_MIN_HEIGHT: " + TERRAIN_MIN_HEIGHT +
							"\nTERRAIN_MAX_HEIGHT: " + TERRAIN_MAX_HEIGHT + 
							"\nTERRAIN_GEN_RESOLUTION: " + TERRAIN_GEN_RESOLUTION +
							"\nTERRAIN_GEN_SEED: " + TERRAIN_GEN_SEED + 
							"\nchunk size (blocks) x=" + terrain.chunkSize.x + " y=" + terrain.chunkSize.y + " z=" + terrain.chunkSize.z +
							"\nworld size (chunks) x=" + terrain.chunks.x+ " y=" + terrain.chunks.y + " z=" + terrain.chunks.z);
		
			lastFrame = t;
		}
		
		// Cleanup
		Display.destroy();
	}
	
	public void init() {
		// Create the display
		try {
			width = Display.getDesktopDisplayMode().getWidth();
			height = Display.getDesktopDisplayMode().getHeight();
			
			Display.setDisplayMode(Display.getDesktopDisplayMode());
			Display.setFullscreen(FULLSCREEN);
			Display.setVSyncEnabled(VSYNC);
			Display.create(new PixelFormat().withDepthBits(24).withSamples(4).withSRGB(true));
		} catch(LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		int width = Display.getDesktopDisplayMode().getWidth();
		int height = Display.getDesktopDisplayMode().getHeight();
		
		// Initialize OpenGL
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		
		GLU.gluPerspective(45.0f, (float)width / (float)height, 1.000000f, 300.0f);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		// Set OpenGL options
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_SMOOTH); 
		GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
		
		// Create the ambient light
		float ambientLightArray[] = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
		FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4);
		ambientLight.put(ambientLightArray);
		ambientLight.position(0);
		
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, ambientLight);
		GL11.glEnable(GL11.GL_LIGHT0);
		
		// Hide the mouse
		Mouse.setGrabbed(true);
	}
	
	public void opengl2D() {
		// Disable 3D things
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		// Set 2D
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		
		GLU.gluOrtho2D( 0, width, height, 0 );
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	
	public void opengl3D() {
		// Set 3D
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		
		GLU.gluPerspective(45.0f, (float)width / (float)height, 0.1f, 300.0f);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		// Enable 3D things
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	public void render() {
		// Clear the screen
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();
		
		// 3D
		opengl3D();
		
		if(wireframe)
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		else 
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

		// Apply the camera matrix
		camera.applyMatrix();
		
		// Render the terrain
		terrain.render();
		
		opengl2D();
		
		// Render the GUI
		gui.render();
		
		// Draw the crosshair
		int crossX = (int) (width / 2 - crossHairTexture.getImageWidth() / 2);
		int crossY = (int) (height / 2 - crossHairTexture.getImageHeight() / 2);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, crossHairTexture.getTextureID());
		
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2f(0, 0); 
		GL11.glVertex2f(crossX, crossY);
		
		GL11.glTexCoord2f(0, 1); 
		GL11.glVertex2f(crossX, crossY + crossHairTexture.getImageHeight());
		
		GL11.glTexCoord2f(1, 1); 
		GL11.glVertex2f(crossX + crossHairTexture.getImageWidth(), crossY + crossHairTexture.getImageHeight());
		
		GL11.glTexCoord2f(1, 0); 
		GL11.glVertex2f(crossX + crossHairTexture.getImageWidth(), crossY);
		
		GL11.glEnd();
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
	public void update(float deltaTime) {
		// Handle mouse movement
		camera.addRotation(new Vector3f(Mouse.getDY() * MOUSE_SPEED_SCALE, -Mouse.getDX() * MOUSE_SPEED_SCALE, 0.0f));
		
		float movementSpeed = flyMode ? MOVEMENT_SPEED_FLYMODE : MOVEMENT_SPEED;
		
		// Handle keypresses
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			running = false;
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
			camera.move(movementSpeed * deltaTime, Camera.FORWARD, 0, doCollisionChecking, flyMode);
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
			camera.move(movementSpeed * deltaTime, Camera.BACKWARD, 0, doCollisionChecking, flyMode);
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
			camera.move(movementSpeed * deltaTime, Camera.LEFT, 0, doCollisionChecking, flyMode);
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
			camera.move(movementSpeed * deltaTime, Camera.RIGHT, 0, doCollisionChecking, flyMode);
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
			camera.move(0, Camera.RIGHT, -FALSE_GRAVITY_SPEED * 2 * deltaTime, doCollisionChecking, flyMode);
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			camera.move(0, Camera.RIGHT, FALSE_GRAVITY_SPEED * 2 * deltaTime, doCollisionChecking, flyMode);
		
		// Check for pressed keys
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_F) {
				    	flyMode = !flyMode;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_C) {
				    	doCollisionChecking = !doCollisionChecking;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_V) {
				    	wireframe = !wireframe;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_T) {
						TEXTURES = !TEXTURES;
						terrain.setUseTextures(TEXTURES);
				}
			}
		}
		
		// Apply gravity
		if(!flyMode)
			camera.move(0, Camera.FORWARD, deltaTime * FALSE_GRAVITY_SPEED, doCollisionChecking, flyMode);
		
		if(blockChangeTimer > 0)
			blockChangeTimer -= deltaTime;	
		
		// Find the block the user is looking at
		float step = 0.01f;
		
		float xLook = camera.coordinates.x / terrain.cubeSize.x;
		float yLook = camera.coordinates.y / terrain.cubeSize.y;
		float zLook = camera.coordinates.z / terrain.cubeSize.z;
		
		float xSpeed = (float) (Math.cos(Math.toRadians(camera.rotation.x)) * -Math.sin(Math.toRadians(camera.rotation.y)) * 1) * step;
		float ySpeed = (float) (Math.sin(Math.toRadians(camera.rotation.x))) * step;
		float zSpeed = (float) (Math.cos(Math.toRadians(camera.rotation.x)) * -Math.cos(Math.toRadians(camera.rotation.y)) * 1) * step;
		
		Vector3f lastLookPos = new Vector3f(xLook, yLook, zLook);
		
		// Check for mouse clicks
		boolean destroyBlock = false;
		boolean placeNewBlock = false;
		
		while(Mouse.next()) {
			if(blockChangeTimer <= 0) {
					if(Mouse.getEventButton() == 0) {
						destroyBlock = true;
					} else if(Mouse.getEventButton() == 1) {
						placeNewBlock = true;
					}
			}
		}
		
		if(blockChangeTimer <= 0) {
			if(Mouse.isButtonDown(0)) {
				destroyBlock = true;
			} else if(Mouse.isButtonDown(1)) {
				placeNewBlock = true;
			}
		}
		
		
		distance = 0.0f;
		while(distance < 20.0f) {
			if(terrain.solidAt(new Vector3f(xLook, yLook, zLook))) {
					if(destroyBlock) {
						terrain.setCube(new Vector3f(xLook, yLook, zLook), null);
						blockChangeTimer = 0.2f;
					} else if(placeNewBlock) {
						TerrainCube newCube = new TerrainCube(null, null, new Vector4f(0.3f, 0.3f, 0.3f, 1.0f), terrain.textures, TextureStore.getTexRect(0, 2));
						terrain.setCube(lastLookPos, newCube);
						blockChangeTimer = 0.3f;
					}

				break;
			}
			
			lastLookPos.x = xLook;
			lastLookPos.y = yLook;
			lastLookPos.z = zLook;
			
			xLook += xSpeed;
			yLook += ySpeed;
			zLook += zSpeed;
			
			distance += step;		
		}
		
		// Update gui
		gui.update();
	}
	
	public FloatBuffer toBuffer(float[] array) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(array.length);
		buffer.put(array);
		buffer.position(0);
		return buffer;
	}
	
	
	public static void main(String[] args) {
		Game cubeGame = new Game();
		cubeGame.start();
	}
}

