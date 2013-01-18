package terrain;
import game.Rectf;
import game.SimplexNoise;
import game.TextureStore;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

import types.Cube6f;
import types.Vector3;
import types.Vector3f;
import types.Vector4f;

public class Terrain {

	// Number of chunks and chunk size
	private Vector3f translation;
	public Vector3 chunks;
	public Vector3 chunkSize;
	
	public TerrainChunk[][][] chunkArray;
	
	// Size of each cube
	public Vector3f cubeSize;
	
	// Textures
	public Texture textures;
	private boolean drawTextures;
	
	public Terrain(Vector3f translation, Vector3 chunks, Vector3 chunkSize, Vector3f cubeSize, boolean drawTextures, TextureStore textureStore) {
		this.chunks = chunks;
		this.chunkSize = chunkSize;
		this.translation = translation;
		this.cubeSize = cubeSize;
		this.translation = translation;
		this.drawTextures = drawTextures;
		
		// Create the chunk array and the chunks
		chunkArray = new TerrainChunk[chunks.x][chunks.y][chunks.z];
		
		for(int x = 0; x < chunks.x; x++) {
			for(int y = 0; y < chunks.y; y++) {
				for(int z = 0; z < chunks.z; z++) {
					chunkArray[x][y][z] = new TerrainChunk(new Vector3(x * chunkSize.x, y * chunkSize.y, z * chunkSize.z),
															chunkSize, cubeSize, drawTextures);
				}
			}
		}
		
		// Load texture
		textures = textureStore.getTexture("res/cube_textures.png");
	}
	
	public void generateTerrain(int minHeight, int maxHeight, float resolution, long seed) {
		// Randomize the heights using Simplex 2D noise
		SimplexNoise.genGrad(seed);
		
		// Generate chunks
		for(int x = 0; x < chunks.x; x++) {
			for(int y = 0; y < chunks.y; y++) {
				for(int z = 0; z < chunks.z; z++) {
					chunkArray[x][y][z].generateChunk(minHeight, maxHeight, resolution, this);
				}
			}
		}
		
		/*
		Random rand = new Random(seed);
		int genX, genY, genZ;
		
		// Create tree obstacles
		TreeObstacle treeGen = new TreeObstacle(this, textures);
		treeGen.createTree(useTextures);
		int treeCount = 25;
		
		for(int treeIndex = 0; treeIndex < treeCount; treeIndex++) {
			do {
				// Select a random position on the terrain
				genX = rand.nextInt(arraySize.x);
				genZ = rand.nextInt(arraySize.z);
				genY = heightData[genX][genZ];
			// Create the tree
			} while(!treeGen.placeObstacle(new Vector3(genX, genY, genZ), true));
		}
		
		// Create spruce obstacles
		SpruceObstacle spruceGen = new SpruceObstacle(this, textures);
		spruceGen.createSpruce(useTextures);
		
		int spruceCount = 15;
		
		for(int spruceIndex = 0; spruceIndex < spruceCount; spruceIndex++) {
			do {
				// Select a random position on the terrain
				genX = rand.nextInt(arraySize.x);
				genZ = rand.nextInt(arraySize.z);
				genY = heightData[genX][genZ];
			// Create the tree
			} while(!spruceGen.placeObstacle(new Vector3(genX, genY, genZ), true));
		}
		*/
		
		// Build render data
		for(int x = 0; x < chunks.x; x++) {
			for(int y = 0; y < chunks.y; y++) {
				for(int z = 0; z < chunks.z; z++) {
					chunkArray[x][y][z].buildRenderData();
				}
			}
		}
	}
	
	public TerrainCube createCube(Vector3 arrayPosition) {
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
			texCoords = TextureStore.getTexRect(0, 1);
		} else if(arrayPosition.y < 5) {
			// Stone
			color = new Vector4f(0.3f, 0.3f, 0.3f, 1.0f);
			texCoords = TextureStore.getTexRect(0, 2);
		} else if(arrayPosition.y < 10) {
			// Water
			color = new Vector4f(0.0f, 0.2f, 0.7f, 0.6f);
			texCoords = TextureStore.getTexRect(0, 3);
			
		} else {
			// Grass
			color = new Vector4f(0.0f, 0.3f, 0.00f, 1.0f);
			texCoords = TextureStore.getTexRect(0, 0);
		}
		
		return new TerrainCube(pos1, pos2, color, texture, texCoords);
	}
	
	public void render() {
		// Save the current matrix
		GL11.glPushMatrix();
		
		// Add the translation matrix
		GL11.glTranslatef(translation.x, translation.y, translation.z);
		
		if(drawTextures) {
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.getTextureID());
		} else {
			GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		}
		
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);

		// CALL ALL CHUNK RENDER FUNCTIONS
		for(int x = 0; x < chunks.x; x++) {
			for(int y = 0; y < chunks.y; y++) {
				for(int z = 0; z < chunks.z; z++) {
					chunkArray[x][y][z].render();
				}
			}
		}
		
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		
		if(drawTextures) {
			GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		} else {
			GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		}
		
		// Restore the matrix
		GL11.glPopMatrix();
	}
	
	/* Returns true if there is a solid cube at the given coordinates. */
	public boolean solidAt(Vector3f coordinates) {
		// Get the cube coordinates in the array
		Vector3 cubeCoordinates = new Vector3((int)((coordinates.x - translation.x) / cubeSize.x), (int)((coordinates.y - translation.y) / cubeSize.y), (int)((coordinates.z - translation.z) / cubeSize.z));
		
		// Is this within the chunk bounds?
		if(cubeCoordinates.x >= 0 && cubeCoordinates.x < chunks.x * chunkSize.x &&
			cubeCoordinates.y >= 0 && cubeCoordinates.y < chunks.y * chunkSize.y &&
			cubeCoordinates.z >= 0 && cubeCoordinates.z < chunks.z * chunkSize.z) {
			// Calculate which chunk this belongs to
			Vector3 chunkCoordinates = new Vector3(cubeCoordinates.x / chunkSize.x, cubeCoordinates.y / chunkSize.y, cubeCoordinates.z / chunkSize.z);
			TerrainChunk chunk = chunkArray[chunkCoordinates.x][chunkCoordinates.y][chunkCoordinates.z];
			
			// Is there a cube at this coordinate?
			if(chunk.cubes[cubeCoordinates.x - chunkCoordinates.x * chunkSize.x][cubeCoordinates.y - chunkCoordinates.y * chunkSize.y][cubeCoordinates.z - chunkCoordinates.z * chunkSize.z] != null) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean collision(Cube6f cube) {
		// Calculate array coordinates
		Vector3 coordStart = new Vector3((int)((cube.x1 - translation.x) / cubeSize.x), (int)((cube.y1 - translation.y) / cubeSize.y), (int)((cube.z1 - translation.z) / cubeSize.z));
		Vector3 coordEnd = new Vector3((int)Math.ceil((cube.x1 - translation.x) / cubeSize.x), (int)Math.ceil((cube.y1 - translation.y) / cubeSize.y), (int)Math.ceil((cube.z1 - translation.z) / cubeSize.z));
		
		// Check each terrain cube withing the Cube6f
		for(int x = coordStart.x; x < coordEnd.x; x++) {
			for(int y = coordStart.y; y < coordEnd.y; y++) {
				for(int z = coordStart.z; z < coordEnd.z; z++) {
					if(x >= 0 && x < chunks.x * chunkSize.x &&
							y >= 0 && y < chunks.y * chunkSize.y &&
							z >= 0 && z < chunks.z * chunkSize.z) {
						// Calculate which chunk this belongs to
						Vector3 chunkCoordinates = new Vector3(x / chunkSize.x, y / chunkSize.y, z / chunkSize.z);
						TerrainChunk chunk = chunkArray[chunkCoordinates.x][chunkCoordinates.y][chunkCoordinates.z];
						
						// Is there a cube at this coordinate?
						if(chunk.cubes[x - chunkCoordinates.x * chunkSize.x][y - chunkCoordinates.y * chunkSize.y][z - chunkCoordinates.z * chunkSize.z] != null) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public void setCube(Vector3f coordinates, TerrainCube cube) {
		// Get the cube coordinates in the array
		Vector3 cubeCoordinates = new Vector3((int)((coordinates.x - translation.x) / cubeSize.x), (int)((coordinates.y - translation.y) / cubeSize.y), (int)((coordinates.z - translation.z) / cubeSize.z));
		
		// Is this within the chunk bounds?
		if(cubeCoordinates.x >= 0 && cubeCoordinates.x < chunks.x * chunkSize.x &&
			cubeCoordinates.y >= 0 && cubeCoordinates.y < chunks.y * chunkSize.y &&
			cubeCoordinates.z >= 0 && cubeCoordinates.z < chunks.z * chunkSize.z) {
			// Calculate which chunk this belongs to
			Vector3 chunkCoordinates = new Vector3(cubeCoordinates.x / chunkSize.x, cubeCoordinates.y / chunkSize.y, cubeCoordinates.z / chunkSize.z);
			TerrainChunk chunk = chunkArray[chunkCoordinates.x][chunkCoordinates.y][chunkCoordinates.z];
			
			// Set correct cube pos
			if(cube != null) {
				Vector3f pos1 = new Vector3f((cubeCoordinates.x - chunkCoordinates.x * chunkSize.x) * cubeSize.x, (cubeCoordinates.y - chunkCoordinates.y * chunkSize.y) * cubeSize.y, (cubeCoordinates.z - chunkCoordinates.z * chunkSize.z) * cubeSize.z);
				Vector3f pos2 = Vector3f.add(pos1, cubeSize);
				
				cube.pos1 = pos1;
				cube.pos2 = pos2;
			}
	
			// Put block
			chunk.cubes[cubeCoordinates.x - chunkCoordinates.x * chunkSize.x][cubeCoordinates.y - chunkCoordinates.y * chunkSize.y][cubeCoordinates.z - chunkCoordinates.z * chunkSize.z] = cube;
			
			// Rebuild render data for the chunk
			chunk.buildRenderData();
		}
	}
	
	public Vector3 cubeCoordinates(Vector3f coordinates) {
		Vector3 cubeCoordinates = new Vector3((int)((coordinates.x - translation.x) / cubeSize.x), (int)((coordinates.y - translation.y) / cubeSize.y), (int)((coordinates.z - translation.z) / cubeSize.z));
		if(cubeCoordinates.x >= 0 && cubeCoordinates.x < chunks.x * chunkSize.x &&
				cubeCoordinates.y >= 0 && cubeCoordinates.y < chunks.y * chunkSize.y &&
				cubeCoordinates.z >= 0 && cubeCoordinates.z < chunks.z * chunkSize.z) {
			
			return cubeCoordinates;
		}
		
		return null;
	}
	
	public void setUseTextures(boolean useTextures) {
		this.drawTextures = useTextures;
	}
}


