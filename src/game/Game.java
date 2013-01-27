package game;

import gui.GUI;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;
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
import terrain.CubeType;
import terrain.World;
import types.Vector3;
import types.Vector3f;
import de.lessvoid.nifty.controls.ConsoleCommands.ConsoleCommand;

public class Game implements ConsoleCommand, Connection.OnReceiveListener {
	
	private static final float MOUSE_SPEED_SCALE = 0.1f;
	private static final float MOVEMENT_SPEED = 7.0f;
	private static final float MOVEMENT_SPEED_FLYMODE = 12.0f;
	private static final float FALSE_GRAVITY_SPEED = 8.0f;
	private static final boolean FULLSCREEN = true;
	private static final boolean VSYNC = true;
	
	private boolean TEXTURES = true;
	
	// Graphics
	private int width;
	private int height;
	
	// Game components
	private Camera camera;
	private World world;
	private TextureStore textureStore;
	private boolean running = true;
	
	// Toggles
	private boolean flymode = true;
	private boolean collisionDetection = true;
	private boolean wireframe = false;
	
	// Profiling
	private Profiling profiling = new Profiling();
	
	// Camera block distance
	private float distance;
	private Texture crossHairTexture;
	private float blockChangeTimer = 0.3f;
	
	// GUI
	private GUI gui;
	
	// Connection
	private Connection conn;
	private Queue<String> receivedCommands;
	
	private volatile boolean readyToRoll = false;
	
	public void start() {
		receivedCommands = new LinkedList<String>();
		
		// Initialize OpenGL and LWJGL stuff
		init();
		
		// Create the texture store
		textureStore = new TextureStore();
		crossHairTexture = textureStore.getTexture("res/crosshair.png");
		textureStore.getTexture("res/cube_textures.png");
		conn = new Connection();
		
		// Create the GUI
		try {
			gui = new GUI("res/test.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		gui.getConsole().addCommand("help", this);
		gui.getConsole().addCommand("flymode", this);
		gui.getConsole().addCommand("collision_detection", this);
		gui.getConsole().addCommand("wireframe", this);
		gui.getConsole().addCommand("textures", this);
		
		gui.getConsole().output("To see available commands type 'help'");
	
		// Connect
		try {
			conn.connect("localhost", 6000);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		conn.setOnReceiveListener(this);
		
		// Main loop
		long lastFrame = System.currentTimeMillis();
		
		while(!Display.isCloseRequested() && running) {
			// Calculate delta time
			long t = System.currentTimeMillis();
			float deltaTime = (t - lastFrame) * 0.001f;
			
			// Tell the profiler we are starting a new frame
			profiling.frameBegin();
			
			// Render
			if(readyToRoll) {
				render();
			
				// Updates the display, also polls the mouse and keyboard
				Display.update();
				
				// Update
				update(deltaTime);
				
				// Set the debug info
				gui.setDebugLabel("x: " + camera.coordinates.x + 
							"\ny: " + camera.coordinates.y + 
							"\nz: " + camera.coordinates.z +
							"\nxRot: " + camera.rotation.x + 
							"\nyRot: " + camera.rotation.y + 
							"\nzRot: " + camera.rotation.z + 
							"\ntarget block distance: " + distance +
							"\nFPS: " + profiling.fps() +
							"\nvsync: " + VSYNC + 
							"\nfullscreen: " + FULLSCREEN +
							"\ntextures: " + TEXTURES);
			}
			
			// Handle network messages
			while(receivedCommands.size() > 0 ) {
				String line = receivedCommands.remove();
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
					
					readyToRoll = true;
					
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
				}
			}
			
			// Tell the profiler we are at the end of a frame
			profiling.frameEnd();

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
		world.render();
		
		opengl2D();
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		
		// Render the GUI
		gui.render();
		
		// Draw the crosshair if the console is not visible
		if(!gui.getConsole().isVisible) {
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
	}
	
	public void update(float deltaTime) {
		// Quit?
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			running = false;
		
		// Mouse variables
		boolean destroyBlock = false;
		boolean placeNewBlock = false;
		
		// Only let the GUI take keyboard inputs if the console is visible
		if(gui.getConsole().isVisible) {
			gui.update();
		} else {
			// Handle character camera
			camera.addRotation(new Vector3f(Mouse.getDY() * MOUSE_SPEED_SCALE, -Mouse.getDX() * MOUSE_SPEED_SCALE, 0.0f));
	
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
				camera.move(0, Camera.RIGHT, -FALSE_GRAVITY_SPEED * 2 * deltaTime, collisionDetection, flymode);
			if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				camera.move(0, Camera.RIGHT, FALSE_GRAVITY_SPEED * 2 * deltaTime, collisionDetection, flymode);
			
			// Check for pressed keys
			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					if(Keyboard.getEventKey() == Keyboard.KEY_F1) {
						gui.getConsole().toggle();
					}			
				}
			}
			
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
		}
		
		// Apply gravity
		if(!flymode)
			camera.move(0, Camera.FORWARD, deltaTime * FALSE_GRAVITY_SPEED, collisionDetection, flymode);
		
		if(blockChangeTimer > 0)
			blockChangeTimer -= deltaTime;	
		
		// Find the block the user is looking at
		float step = 0.01f;
		
		float xLook = camera.coordinates.x / 1.0f;
		float yLook = camera.coordinates.y / 1.0f;
		float zLook = camera.coordinates.z / 1.0f;
		
		float xSpeed = (float) (Math.cos(Math.toRadians(camera.rotation.x)) * -Math.sin(Math.toRadians(camera.rotation.y)) * 1) * step;
		float ySpeed = (float) (Math.sin(Math.toRadians(camera.rotation.x))) * step;
		float zSpeed = (float) (Math.cos(Math.toRadians(camera.rotation.x)) * -Math.cos(Math.toRadians(camera.rotation.y)) * 1) * step;
		
		Vector3f lastLookPos = new Vector3f(xLook, yLook, zLook);
		
		// Place or destroy block
		if(blockChangeTimer <= 0) {
			if(Mouse.isButtonDown(0)) {
				destroyBlock = true;
			} else if(Mouse.isButtonDown(1)) {
				placeNewBlock = true;
			}
		}
		
		distance = 0.0f;
		while(distance < 20.0f) {
			if(world.solidAt(new Vector3f(xLook, yLook, zLook))) {
					if(destroyBlock) {
						Vector3 arrayCoords = world.arrayCoordinates(new Vector3f(xLook, yLook, zLook));
						conn.writeLine("CUBE " + arrayCoords.x + " " + arrayCoords.y + " " + arrayCoords.z + " " + ((int)CubeType.EMPTY));
						blockChangeTimer = 0.2f;
					} else if(placeNewBlock) {
						Vector3 arrayCoords = world.arrayCoordinates(lastLookPos);
						conn.writeLine("CUBE " + arrayCoords.x + " " + arrayCoords.y + " " + arrayCoords.z + " " + ((int)CubeType.DIRT));
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

	@Override
	public void execute(String[] command) {
		if(command[0].equals("help")) {
			gui.getConsole().output("Available commands: flymode, collision_detection, wireframe, textures");
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
				TEXTURES = true;
				world.setDrawTextures(TEXTURES);
			} else if(command[1].equals("0")) {
				TEXTURES = false;
				world.setDrawTextures(TEXTURES);
			} else {
				gui.getConsole().output("Use as: textures 1|0");
			}
		}
	}

	@Override
	public void onReceive(String line) {
		receivedCommands.add(line);
	}
}

