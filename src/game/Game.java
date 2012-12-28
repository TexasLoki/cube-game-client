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

import profiling.Profiling;
import profiling.ProfilingPart;
import terrain.CubeTerrain;


public class Game {
	
	private static final float MOUSE_SPEED_SCALE = 0.1f;
	private static final float MOVEMENT_SPEED = 7.0f;
	private static final float MOVEMENT_SPEED_FLYMODE = 12.0f;
	private static final float FALSE_GRAVITY_SPEED = 8.0f;
	private static final boolean FULLSCREEN = true;
	private static final boolean VSYNC = true;
	
	private boolean TEXTURES = true;
	
	// Game components
	private Camera camera;
	private CubeTerrain terrain;
	private TextureStore textureStore;
	private boolean running = true;
	
	// Toggles
	private boolean flyMode = true;
	private boolean doCollisionChecking = false;
	private boolean wireframe = false;
	
	// Profiling
	private Profiling profiling = new Profiling();
	private ProfilingPart displayUpdate = new ProfilingPart("Display.update()");
	private ProfilingPart renderFunction = new ProfilingPart("Render");
	private ProfilingPart updateFunction = new ProfilingPart("Update");
	
	public void start() {
		// Initialize OpenGL and LWJGL stuff
		init();
		
		// Create the texture store
		textureStore = new TextureStore();
		
		// Generate the terrain
		terrain = new CubeTerrain(new Vector3f(-25.0f, -40.0f, -25.0f), new Vector3(16, 1, 16), new Vector3(16, 50, 16), new Vector3f(1.0f, 1.0f, 1.0f), TEXTURES, textureStore);
		
		final int TERRAIN_MIN_HEIGHT = 0;
		final int TERRAIN_MAX_HEIGHT = 40;
		final float TERRAIN_GEN_RESOLUTION = 128.0f;
		final long TERRAIN_GEN_SEED = 2;
		
		terrain.generateTerrain(TERRAIN_MIN_HEIGHT, TERRAIN_MAX_HEIGHT, TERRAIN_GEN_RESOLUTION,
								TERRAIN_GEN_SEED);
		
		// Create the camera
		camera = new Camera(new Vector3f(0.0f, 50.0f, 0.0f), new Vector3f(-20.0f, -135.0f, 0.0f), terrain);
			
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
					" xRot: " + (int)camera.rotation.x + " yRot: " + (int)camera.rotation.y + " zRot: " + camera.rotation.z + " FPS: " + Math.round(profiling.fps()));

			lastFrame = t;
		}
		
		// Cleanup
		Display.destroy();
	}
	
	public void init() {
		// Create the display
		try {
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
		float ambientLightArray[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4);
		ambientLight.put(ambientLightArray);
		ambientLight.position(0);
		
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, ambientLight);
		GL11.glEnable(GL11.GL_LIGHT0);
		
		// Hide the mouse
		Mouse.setGrabbed(true);
	}
	
	public void render() {
		// Clear the screen
		GL11.glClearColor(0.25f, 0.8f, 1.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();
		
		if(wireframe)
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		else 
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

		// Apply the camera matrix
		camera.applyMatrix();
		
		// Render the terrain
		terrain.render();
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
	}
	
	public static void main(String[] args) {
		Game cubeGame = new Game();
		cubeGame.start();
	}
}

