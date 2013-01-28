package game;

import gui.GUI;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;

import profiling.Profiling;
import types.Vector3;
import types.Vector3f;
import world.CubeType;
import world.World;
import de.lessvoid.nifty.controls.ConsoleCommands.ConsoleCommand;

public class Game implements ConsoleCommand, Connection.OnReceiveListener {
	
	private static final float MOUSE_SPEED = 0.1f;
	private static final float MOVEMENT_SPEED = 7.0f;
	private static final float MOVEMENT_SPEED_FLYMODE = 12.0f;
	private static final float GRAVITY_SPEED = 8.0f;
	private static final float BLOCK_CHANGE_RATE = 0.3f;
	
	// LWJGL display mode
	private DisplayMode dispMode;
	
	// Controls the main loop
	private volatile boolean running = true;
	
	// Profiling
	private Profiling profiler;
	
	// Game components
	private TextureStore textureStore;
	private GUI gui;
	private Connection conn;
	private World world = null;
	private Camera camera = null;
	private Crosshair crosshair;
	
	// Queue of received network messages (ready to be processed)
	Queue<String> receivedMessages = new LinkedList<String>();
	
	// Toggles
	private boolean flymode = true;
	private boolean collisionDetection = true;
	private boolean wireframe = false;
	private boolean textures = true;
	
	// Block change timer
	private float blockChangeTimer = BLOCK_CHANGE_RATE;

	public static void main(String[] args) {
		Game cubeGame = new Game();
		cubeGame.start();
	}
	
	public void start() {
		// Initialize LWJGL and OpenGL
		try {
			glInit();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		// Create the profiler
		profiler = new Profiling();
		
		// Prepare the texture store
		textureStore = new TextureStore();
		
		// Create the GUI
		try {
			gui = new GUI("res/test.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Create the crosshair
		crosshair = new Crosshair(textureStore, dispMode);
		
		// Add console commands
		gui.getConsole().addCommand("help", this);
		gui.getConsole().addCommand("connect", this);
		gui.getConsole().addCommand("flymode", this);
		gui.getConsole().addCommand("collision_detection", this);
		gui.getConsole().addCommand("wireframe", this);
		gui.getConsole().addCommand("textures", this);
		
		gui.getConsole().output("To see available commands type 'help'\nPress F1 to toggle the console");
		
		// Create the connection instance (but don't connect yet)
		conn = new Connection();
		conn.setOnReceiveListener(this);
		
		// Show the console
		gui.getConsole().toggle();
		
		// Enter the main loop
		loop();
		
		// Cleanup
		conn.stop();
		Display.destroy();
	}
	
	@Override
	public void execute(String[] command) {
		if(command[0].equals("help")) {
			gui.getConsole().output("Available commands: connect, flymode, collision_detection, wireframe, textures");
		} else if(command[0].equals("connect")) {
			try {
				conn.connect(command[1], 6000);
				gui.getConsole().output("Connected to " + command[1] + ":6000");
			} catch (Exception e) {
				gui.getConsole().output(e.toString());
			}
		} else if(command[0].equals("flymode")) {
			if(command[1].equals("1")) {
				flymode = true;
			} else if(command[1].equals("0")) {
				flymode = false;
			} else {
				gui.getConsole().output("Use as: flymode 1|0");
			}
		} else if(command[0].equals("collision_detection")) {
			if(command[1].equals("1")) {
				collisionDetection = true;
			} else if(command[1].equals("0")) {
				collisionDetection = false;
			} else {
				gui.getConsole().output("Use as: collision_detection 1|0");
			}
		} else if(command[0].equals("wireframe")) {
			if(command[1].equals("1")) {
				wireframe = true;
			} else if(command[1].equals("0")) {
				wireframe = false;
			} else {
				gui.getConsole().output("Use as: wireframe 1|0");
			}
		} else if(command[0].equals("textures")) {
			if(command[1].equals("1")) {
				textures = true;
				world.setDrawTextures(textures);
			} else if(command[1].equals("0")) {
				textures = false;
				world.setDrawTextures(textures);
			} else {
				gui.getConsole().output("Use as: textures 1|0");
			}
		}
	}
	
	public void loop() {
		// Main loop
		long lastFrame = System.currentTimeMillis();
		
		while(!Display.isCloseRequested() && running) {
			// Calculate delta time
			long t = System.currentTimeMillis();
			float deltaTime = (t - lastFrame) * 0.001f;
			
			// Tell the profiler we are starting a new frame
			profiler.frameBegin();
			
			// Render
			render();
			
			// Updates the display, also polls the mouse and keyboard
			Display.update();
				
			// Update
			update(deltaTime);
			
			// Tell the profiler we are at the end of a frame
			profiler.frameEnd();
			
			// Set the debug info
			String debugString = "FPS: " + profiler.fps() +
								 "\ntextures: " + textures;
			
			if(camera != null) {
				debugString += "\nx: " + camera.coordinates.x + 
								"\ny: " + camera.coordinates.y + 
								"\nz: " + camera.coordinates.z +
								"\nxRot: " + camera.rotation.x + 
								"\nyRot: " + camera.rotation.y + 
								"\nzRot: " + camera.rotation.z;
			}
			
			debugString += "\n\nPress F1 to toggle console";
			
			gui.setDebugLabel(debugString);
			
			lastFrame = t;
		}
	}
	
	public void glInit() throws LWJGLException {
		// Hide the mouse pointer
		Mouse.setGrabbed(true);
		
		// Get the desktop display mode
		dispMode = Display.getDesktopDisplayMode();

		// Set the display mode for the application (fullscreen for now)
		Display.setDisplayModeAndFullscreen(dispMode);
		Display.setVSyncEnabled(true);
		Display.create(new PixelFormat().withDepthBits(24).withSamples(4).withSRGB(true));
		
		// Initialize OpenGL
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		
		GLU.gluPerspective(45.0f, (float)dispMode.getWidth() / (float)dispMode.getHeight(), 1.000000f, 300.0f);
		
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
		if(camera != null)
			camera.applyMatrix();
		
		// Render the world
		if(world != null)
			world.render();
		
		opengl2D();
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		
		// Render the GUI
		gui.render();
		
		// Draw the crosshair if the console is not visible
		if(!gui.getConsole().isVisible) {
			crosshair.render();
		}
	}
	
	public void opengl2D() {
		// Disable 3D things
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		// Set 2D
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		
		GLU.gluOrtho2D( 0, dispMode.getWidth(), dispMode.getHeight(), 0 );
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	
	public void opengl3D() {
		// Set 3D
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		
		GLU.gluPerspective(45.0f, (float)dispMode.getWidth() / (float)dispMode.getHeight(), 0.1f, 300.0f);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		// Enable 3D things
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	public void update(float deltaTime) {
		// Quit?
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			running = false;
		
		// Handle network messages
		handleMessages();
		
		// Trying to toggle console?
		if(!gui.getConsole().isVisible) {
			while(Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					if(Keyboard.getEventKey() == Keyboard.KEY_F1) {
						gui.getConsole().toggle();
					}			
				}
			}
		}
		
		float mouseDX = Mouse.getDX();
		float mouseDY = Mouse.getDY();
		
		// Only let the GUI take keyboard inputs if the console is visible
		if(gui.getConsole().isVisible) {
			gui.update();
		} else if(world != null){
			// Handle character camera
			camera.addRotation(new Vector3f(mouseDY * MOUSE_SPEED, -mouseDX * MOUSE_SPEED, 0.0f));
	
			// Handle character movement
			float movementSpeed = flymode ? MOVEMENT_SPEED_FLYMODE : MOVEMENT_SPEED;
			
			if(Keyboard.isKeyDown(Keyboard.KEY_W))
				camera.move(movementSpeed * deltaTime, Camera.FORWARD, 0, collisionDetection, flymode);
			if(Keyboard.isKeyDown(Keyboard.KEY_S))
				camera.move(movementSpeed * deltaTime, Camera.BACKWARD, 0, collisionDetection, flymode);
			if(Keyboard.isKeyDown(Keyboard.KEY_A))
				camera.move(movementSpeed * deltaTime, Camera.LEFT, 0, collisionDetection, flymode);
			if(Keyboard.isKeyDown(Keyboard.KEY_D))
				camera.move(movementSpeed * deltaTime, Camera.RIGHT, 0, collisionDetection, flymode);
			if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
				camera.move(0, Camera.RIGHT, -GRAVITY_SPEED * 2 * deltaTime, collisionDetection, flymode);
			if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				camera.move(0, Camera.RIGHT, GRAVITY_SPEED * 2 * deltaTime, collisionDetection, flymode);
			
			// Mouse variables
			boolean destroyBlock = false;
			boolean placeNewBlock = false;
			
			// Check mouse presses
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
			
			// Calculate target block
			camera.calculateTargetBlock(20.0f, camera, world);
			
			// Place or destroy block if mouse was pressed
			if(destroyBlock && camera.getTargetExistingCube() != null) {
				Vector3 arrayCoords = camera.getTargetExistingCube();
				conn.writeLine("CUBE " + arrayCoords.x + " " + arrayCoords.y + " " + arrayCoords.z + " " + ((int)CubeType.EMPTY));
				blockChangeTimer = BLOCK_CHANGE_RATE;
			} else if(placeNewBlock && camera.getTargetEmptyCube() != null) {
				Vector3 arrayCoords = camera.getTargetEmptyCube();
				conn.writeLine("CUBE " + arrayCoords.x + " " + arrayCoords.y + " " + arrayCoords.z + " " + ((int)CubeType.DIRT));
				blockChangeTimer = BLOCK_CHANGE_RATE;
			}
		}
		
		// Apply gravity
		if(!flymode)
			camera.move(0, Camera.FORWARD, deltaTime * GRAVITY_SPEED, collisionDetection, flymode);
		
		// Update the block change timer
		if(blockChangeTimer > 0)
			blockChangeTimer -= deltaTime;	
	}
	
	public void handleMessages() {
		// Handle network messages
		while(receivedMessages.size() > 0 ) {
			String line = receivedMessages.remove();
			StringTokenizer tokenizer = new StringTokenizer(line);
			
			// Find the command
			String command = tokenizer.nextToken();
			
			if(command.equals("CUBE")) {
				Vector3 pos = new Vector3(Integer.valueOf(tokenizer.nextToken()),
						Integer.valueOf(tokenizer.nextToken()),
						Integer.valueOf(tokenizer.nextToken()));
				
				int iType = Integer.valueOf(tokenizer.nextToken());
				char type = (char) iType;
				
				world.setCube(pos, type);
			} else if(command.equals("WORLD")) {
				Vector3 size = new Vector3(Integer.valueOf(tokenizer.nextToken()),
											Integer.valueOf(tokenizer.nextToken()),
											Integer.valueOf(tokenizer.nextToken()));
				
				char[][][] worldData = new char[size.x][size.y][size.z];
				
				for(int x = 0; x < size.x; x++) {
					for(int y = 0; y < size.y; y++) {
						for(int z = 0; z < size.z; z++) {
							int iType = Integer.valueOf(tokenizer.nextToken());
							worldData[x][y][z] = (char) iType;
						}
					}
				}
				
				// Create the world
				world = new World(textureStore, new Vector3f(1.0f, 1.0f, 1.0f), new Vector3(16, 16, 16));
				world.fromData(size, worldData);
				
				// Create the camera
				camera = new Camera(new Vector3f(0.0f, 50.0f, 0.0f), new Vector3f(-20.0f, -135.0f, 0.0f), world);
			} else if(command.equals("CAMERA")) {
				Vector3 pos = new Vector3(Integer.valueOf(tokenizer.nextToken()),
						Integer.valueOf(tokenizer.nextToken()),
						Integer.valueOf(tokenizer.nextToken()));
				
				Vector3 rot = new Vector3(Integer.valueOf(tokenizer.nextToken()),
						Integer.valueOf(tokenizer.nextToken()),
						Integer.valueOf(tokenizer.nextToken()));
				
				camera.coordinates.x = pos.x;
				camera.coordinates.y = pos.y;
				camera.coordinates.z = pos.z;
				
				camera.rotation.x = rot.x;
				camera.rotation.y = rot.y;
				camera.rotation.z = rot.z;
			}
		}
	}
	
	@Override
	public void onReceive(String line) {
		// When a network message is received, just add it to the queue and it will be handled in the main loop instead
		receivedMessages.add(line);
	}
	
	public FloatBuffer toBuffer(float[] array) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(array.length);
		buffer.put(array);
		buffer.position(0);
		return buffer;
	}
}

