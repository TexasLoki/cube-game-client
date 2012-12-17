package game;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import obstacle.TreeObstacle;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class CubeTerrain {

	// Size of the terrain measured in cubes
	public Vector3 arraySize;
	
	// Size of each cube
	public Vector3f cubeSize;
	
	// Optional translation
	private Vector3f translation;
	
	// The 3d array containing the cubes
	public Cube[][][] terrain;
	
	// Textures
	private Texture textures;
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer normalBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer texCoordsBuffer;
	
	private TextureStore textureStore;
	
	public CubeTerrain(Vector3 arraySize, Vector3f cubeSize, Vector3f translation, TextureStore textureStore) {
		this.arraySize = arraySize;
		this.cubeSize = cubeSize;
		this.translation = translation;
		this.textureStore = textureStore;
		
		// Create the cube array
		terrain = new Cube[arraySize.x][arraySize.y][arraySize.z];
		
		for(int z = 0; z < arraySize.z; z++) {
			for(int y = 0; y < arraySize.y; y++) {
				for(int x = 0; x < arraySize.x; x++) {
					terrain[x][y][z] = null;
				}
			}
		}
		
		textures = textureStore.getTexture("res/cube_textures.png");
	}
	
	public void generateTerrain(int maxHeight, int minHeight, int smoothLevel, int seed, float noiseSize, float persistence, int octaves, boolean useTextures) {
		// Stores the height of each x, z coordinate
		int heightData[][] = new int[arraySize.x][arraySize.z];
		
		// Make sure maxHeight and minHeight are within bounds of the cube array
		if(maxHeight > arraySize.y)
			maxHeight = arraySize.y;
		
		if(maxHeight < 0)
			maxHeight = 0;
		
		if(minHeight > arraySize.y)
			minHeight = arraySize.y;
		
		if(minHeight < 0)
			minHeight = 0;
		
		// Randomize the heights using Perlin noise
		for(int z = 0; z < arraySize.z; z++) {
			for(int x = 0; x < arraySize.x; x++) {
					heightData[x][z] = (int) (PerlinNoise2D.perlin2D(x, z, arraySize.x, arraySize.z, seed, 100.0f, 0.0001f, octaves) * (maxHeight - minHeight) + minHeight);
			}
		}
		
		// Smoothen the terrain
		while(smoothLevel > 0) {
			for(int z = 0; z < arraySize.z; z += 1) {
				for(int x = 0; x < arraySize.x; x += 1) {
					float totalHeight = 0.0f;
					float count = 0;
					
					if(z > 0) {
						totalHeight += heightData[x][z - 1];
						count++;
					}
					
					if(z < arraySize.z - 1) {
						totalHeight += heightData[x][z + 1];
						count++;
					}
					
					if(x > 0) {
						totalHeight += heightData[x - 1][z];
						count++;
					}
					
					if(x < arraySize.x - 1) {
						totalHeight += heightData[x + 1][z];
						count++;
					}
					
					heightData[x][z] = Math.round(totalHeight / count);
				}
			}
			
			smoothLevel--;
		}
		
		// Create the cubes
		for(int z = 0; z < arraySize.z; z++) {
			for(int x = 0; x < arraySize.x; x++) {
					for(int y = heightData[x][z]; y >= 0; y--) {
						terrain[x][y][z] = createCube(new Vector3(x, y, z), useTextures);
					}
			}
		}
		
		Random rand = new Random();
		
		// Create tree obstacles
		TreeObstacle treeGen = new TreeObstacle(this, textures);
		int treeCount = 10;
		
		for(int treeIndex = 0; treeIndex < treeCount; treeIndex++) {
			// Select a random position on the terrain
			int x = rand.nextInt(arraySize.x);
			int z = rand.nextInt(arraySize.z);
			int y = heightData[x][z];
			
			// Create the tree
			treeGen.createTree(useTextures);
			treeGen.placeObstacle(new Vector3(x, y, z), false);
		}
		
		// Create spruce obstacles
		/*
		SpruceObstacle spruceGen = new SpruceObstacle(this, textureStore);
		int spruceCount = 5;
		
		for(int spruceIndex = 0; spruceIndex < spruceCount; spruceIndex++) {
			// Select a random position on the terrain
			int x = rand.nextInt(arraySize.x);
			int z = rand.nextInt(arraySize.z);
			int y = heightData[x][z];
			
			// Create the spruce
			spruceGen.createSpruce(textures);
			spruceGen.placeObstacle(new Vector3(x, y, z), false);
		}
		*/
		
		// Calculate which sides each cube needs to render
		for(int z = 0; z < arraySize.z; z++) {
			for(int x = 0; x < arraySize.x; x++) {
				for(int y = heightData[x][z]; y >= 0; y--) {
					boolean renderTop = (y == heightData[x][z]) || (y == 0);
					boolean renderBottom = (y == 0) || (y == 3);
					boolean renderFront = (z == arraySize.z - 1) || (terrain[x][y][z + 1] == null);
					boolean renderBack = (z == 0) || (terrain[x][y][z - 1] == null);
					boolean renderRight = (x == arraySize.x - 1) || (terrain[x + 1][y][z] == null);
					boolean renderLeft = (x == 0) || (terrain[x - 1][y][z] == null);
					
					terrain[x][y][z].setVisibleSides(renderTop, renderBottom, renderFront, renderBack, renderRight, renderLeft);
				}
			}
		}
		
		// First find out the required size of the buffer
		List<float[]> vertexArrays = new ArrayList<float[]>();
		int numberOfFloats = 0;
		
		for(int z = 0; z < arraySize.z; z++) {
			for(int x = 0; x < arraySize.x; x++) {
				for(int y = 0; y < arraySize.y; y++) {
					if(terrain[x][y][z] != null) {
						float[] a = terrain[x][y][z].getVertices();
						numberOfFloats += a.length;
						vertexArrays.add(a);
					}
				}
			}
		}
		
		// Create the buffer
		vertexBuffer = BufferUtils.createFloatBuffer(numberOfFloats);
		
		for(float[] a : vertexArrays) {
			vertexBuffer.put(a);
		}
		
		vertexBuffer.flip();
		
		// Do the same with the normal buffer
		List<float[]> normalArrays = new ArrayList<float[]>();
		int numberOfFloatsNormals = 0;
		
		for(int z = 0; z < arraySize.z; z++) {
			for(int x = 0; x < arraySize.x; x++) {
				for(int y = 0; y < arraySize.y; y++) {
					if(terrain[x][y][z] != null) {
						float[] a = terrain[x][y][z].getNormals();
						numberOfFloatsNormals += a.length;
						normalArrays.add(a);
					}
				}
			}
		}
	
		normalBuffer = BufferUtils.createFloatBuffer(numberOfFloatsNormals);
		
		for(float[] a : normalArrays) {
			normalBuffer.put(a);
		}
		
		normalBuffer.flip();
		
		// Do the same with the color buffer
		List<float[]> colorArrays = new ArrayList<float[]>();
		int numberOfFloatsColors = 0;
		
		for(int z = 0; z < arraySize.z; z++) {
			for(int x = 0; x < arraySize.x; x++) {
				for(int y = 0; y < arraySize.y; y++) {
					if(terrain[x][y][z] != null) {
						float[] a = terrain[x][y][z].getColors();
						numberOfFloatsColors += a.length;
						colorArrays.add(a);
					}
				}
			}
		}
	
		colorBuffer = BufferUtils.createFloatBuffer(numberOfFloatsColors);
		
		for(float[] a : colorArrays) {
			colorBuffer.put(a);
		}
		
		colorBuffer.flip();
		
		// Do the same with the tex coords buffer
		List<float[]> texArrays = new ArrayList<float[]>();
		int numberOfFloatsTex = 0;
		
		for(int z = 0; z < arraySize.z; z++) {
			for(int x = 0; x < arraySize.x; x++) {
				for(int y = 0; y < arraySize.y; y++) {
					if(terrain[x][y][z] != null) {
						float[] a = terrain[x][y][z].getTexCoords();
						numberOfFloatsTex += a.length;
						texArrays.add(a);
					}
				}
			}
		}
	
		texCoordsBuffer = BufferUtils.createFloatBuffer(numberOfFloatsTex);
		
		for(float[] a : texArrays) {
			texCoordsBuffer.put(a);
		}
		
		texCoordsBuffer.flip();
	}
	
	private Cube createCube(Vector3 arrayPosition, boolean useTextures) {
		// Calculate the coordinates
		Vector3f pos1 = new Vector3f(arrayPosition.x * cubeSize.x, arrayPosition.y * cubeSize.y, arrayPosition.z * cubeSize.z);
		Vector3f pos2 = Vector3f.add(pos1, cubeSize);
		
		// Set texture depending on y
		Vector4f color = null;
		Texture texture = textures;
		Rectf texCoords = null;
		
		if(arrayPosition.y == 0) {
			// Dirt
			color = new Vector4f(0.35f, 0.15f, 0.0f, 1.0f);
			texCoords = new Rectf(1/16f, 0/16f, 2/16f, 1/16f);
		} else if(arrayPosition.y < 3) {
			// Water
			color = new Vector4f(0.0f, 0.2f, 0.7f, 0.6f);
			texCoords = new Rectf(3/16f, 0/16f, 4/16f, 1/16f);
		} else if(arrayPosition.y < 6) {
			// Grass
			color = new Vector4f(0.2f, 0.4f, 0.1f, 1.0f);
			texCoords = new Rectf(0/16f, 0/16f, 1/16f, 1/16f);
		} else {
			// Stone
			color = new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
			texCoords = new Rectf(2/16f, 0/16f, 3/16f, 1/16f);
		}
		
		if(!useTextures)
			texture = null;
		
		return new Cube(pos1, pos2, color, texture, texCoords);
	}
	
	public void render() {
		// Save the current matrix
		GL11.glPushMatrix();
		
		// Add the translation matrix
		GL11.glTranslatef(translation.x, translation.y, translation.z);
		
		// Setup opengl
		//GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
		//GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		//GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.getTextureID());
		
		GL11.glVertexPointer(3, 0, vertexBuffer);
		GL11.glNormalPointer(0, normalBuffer);
		//GL11.glColorPointer(4, 0, colorBuffer);
		GL11.glTexCoordPointer(2, 0, texCoordsBuffer);
		
		GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexBuffer.limit() / 3);
		
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		//GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
		//GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		
		// Restore the matrix
		GL11.glPopMatrix();
		
	}
	
	/* Returns true if there is a solid cube at the given coordinates. */
	public boolean solidAt(Vector3f coordinates) {
		// Get the cube coordinates in the array
		Vector3 arrayCoordinates = new Vector3((int)((coordinates.x - translation.x) / cubeSize.x), (int)((coordinates.y - translation.y) / cubeSize.y), (int)((coordinates.z - translation.z) / cubeSize.z));
		
		// Is this within the array bounds?
		if(arrayCoordinates.x >= 0 && arrayCoordinates.x < arraySize.x &&
			arrayCoordinates.y >= 0 && arrayCoordinates.y < arraySize.y &&
			arrayCoordinates.z >= 0 && arrayCoordinates.z < arraySize.z) {
			// Is there a cube at this coordinate?
			if(terrain[arrayCoordinates.x][arrayCoordinates.y][arrayCoordinates.z] != null) {
				return true;
			}
		}
		
		return false;
	}
}


