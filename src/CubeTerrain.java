import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.MipMap;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;


public class CubeTerrain {

	// Size of the terrain measured in cubes
	private Vector3 arraySize;
	
	// Size of each cube
	private Vector3f cubeSize;
	
	// Optional translation
	private Vector3f translation;
	
	// The 3d array containing the cubes
	private Cube[][][] terrain;
	
	// Textures
	private Texture stoneTexture;
	private Texture grassTexture;
	private Texture waterTexture;
	private Texture dirtTexture;
	
	public CubeTerrain(Vector3 arraySize, Vector3f cubeSize, Vector3f translation) {
		this.arraySize = arraySize;
		this.cubeSize = cubeSize;
		this.translation = translation;
		
		// Create the cube array
		terrain = new Cube[arraySize.x][arraySize.y][arraySize.z];
		
		for(int z = 0; z < arraySize.z; z++) {
			for(int y = 0; y < arraySize.y; y++) {
				for(int x = 0; x < arraySize.x; x++) {
					terrain[x][y][z] = null;
				}
			}
		}
		
		try {
			// Load textures
			stoneTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/stone.png"));
			grassTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/grass.png"));
			waterTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/water.png"));
			dirtTexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/dirt.png"));
			
			// Create mipmaps
			createMipmaps(stoneTexture);
			createMipmaps(grassTexture);
			createMipmaps(waterTexture);
			createMipmaps(dirtTexture);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void generateTerrain(int maxHeight, int minHeight, int smoothLevel, int seed, float noiseSize, float persistence, int octaves) {
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
			for(int z = 1; z < arraySize.z; z += 1) {
				for(int x = 1; x < arraySize.x; x += 1) {
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
						terrain[x][y][z] = createCube(new Vector3(x, y, z));
					}
			}
		}
		
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
	}
	
	private Cube createCube(Vector3 arrayPosition) {
		// Calculate the coordinates
		Vector3f pos1 = new Vector3f(arrayPosition.x * cubeSize.x, arrayPosition.y * cubeSize.y, arrayPosition.z * cubeSize.z);
		Vector3f pos2 = Vector3f.add(pos1, cubeSize);
		
		// Set texture depending on y
		Vector4f color = null;
		Texture texture = null;
		
		if(arrayPosition.y == 0) {
			// Dirt
			color = new Vector4f(0.5f, 0.25f, 0.0f, 1.0f);
			texture = dirtTexture;
		} else if(arrayPosition.y < 3) {
			// Water
			color = new Vector4f(0.2f, 0.2f, 0.7f, 0.7f);
			texture = waterTexture;
		} else if(arrayPosition.y < 6) {
			// Grass
			color = new Vector4f(0.2f, 0.7f, 0.2f, 1.0f);
			texture = grassTexture;
		} else {
			// Stone
			color = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);
			texture = stoneTexture;
		}
		
		return new Cube(pos1, pos2, color, texture);
	}
	
	public void render() {
		// Save the current matrix
		GL11.glPushMatrix();
		
		// Add the translation matrix
		GL11.glTranslatef(translation.x, translation.y, translation.z);
		
		// Draw each cube
		for(int z = 0; z < arraySize.z; z++) {
			for(int y = 0; y < arraySize.y; y++) {
				for(int x = 0; x < arraySize.x; x++) {
					if(terrain[x][y][z] != null)
						terrain[x][y][z].render();
				}
			}
		}
		
		// Restore the matrix
		GL11.glPopMatrix();
	}
	
	/* Function which generates mipmaps. Found on the internet. */
	public static void createMipmaps(Texture tex) {
		tex.bind();

		int width = (int)tex.getImageWidth();
		int height = (int)tex.getImageHeight();

		byte[] texbytes = tex.getTextureData();
		int components = texbytes.length / (width*height);

		ByteBuffer texdata = ByteBuffer.allocateDirect(texbytes.length);
		texdata.put(texbytes);
		texdata.rewind();

		MipMap.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, components, width, height, components==3 ? GL11.GL_RGB : GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,texdata);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8);
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


