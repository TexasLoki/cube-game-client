package terrain;

import game.Cube;
import game.SimplexNoise;
import game.Vector3;
import game.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class TerrainChunk {

	public Cube[][][] cubes;
	public Vector3 position;
	public Vector3 size;
	public Vector3f cubeSize;
	
	private boolean drawTextures;
	
	// Buffers
	private FloatBuffer vertexBuffer;
	private FloatBuffer normalBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer texCoordsBuffer;
	
	// GL List
	private int glListIndex = 0;
	
	public TerrainChunk(Vector3 position, Vector3 size, Vector3f cubeSize, boolean drawTextures) {
		this.position = position;
		this.size = size;
		this.cubeSize = cubeSize;
		this.drawTextures = drawTextures;
		
		// Allocate an array of cubes
		cubes = new Cube[size.x][size.y][size.z];
	}
	
	public void generateChunk(int minHeight, int maxHeight, float resolution, CubeTerrain t) {
		// Generate a heightmap using perlin noise
		int heightData[][] = new int[size.x][size.z];
		
		for(int z = 0; z < size.z; z++) {
			for(int x = 0; x < size.x; x++) {
				heightData[x][z] = (int) (((SimplexNoise.noise((x + position.x) / resolution, (z + position.z) / resolution) + 1.0f) / 2.0f) * (maxHeight - minHeight) + minHeight);
			}
		}
		
		// Set minimum level to water level
		for(int z = 0; z < size.z; z++) {
			for(int x = 0; x < size.x; x++) {
				if(heightData[x][z] < 9)
					heightData[x][z] = 9;
			}
		}
		
		// Create the cubes
		for(int z = 0; z < size.z; z++) {
			for(int x = 0; x < size.x; x++) {
					for(int y = heightData[x][z]; y >= 0; y--) {
						cubes[x][y][z] = t.createCube(new Vector3(x, y, z));
					}
			}
		}
	}
	
	public void buildRenderData() {
		// Calculate which sides each cube needs to render
		for(int z = 0; z < size.z; z++) {
			for(int x = 0; x < size.x; x++) {
				for(int y = 0; y < size.y; y++) {
					if(cubes[x][y][z] != null) {
						boolean renderTop = (y == size.y - 1) || (cubes[x][y + 1][z] == null);
						boolean renderBottom = (y == 0) || (cubes[x][y - 1][z] == null);
						boolean renderFront = (z == size.z - 1) || (cubes[x][y][z + 1] == null);
						boolean renderBack = (z == 0) || (cubes[x][y][z - 1] == null);
						boolean renderRight = (x == size.x - 1) || (cubes[x + 1][y][z] == null);
						boolean renderLeft = (x == 0) || (cubes[x - 1][y][z] == null);
						
						cubes[x][y][z].setVisibleSides(renderTop, renderBottom,
														renderFront, renderBack,
														renderRight, renderLeft);
					}
				}
			}
		}
		
		// First find out the required size of the buffer
		List<float[]> vertexArrays = new ArrayList<float[]>();
		int numberOfFloats = 0;
		
		for(int z = 0; z < size.z; z++) {
			for(int x = 0; x < size.x; x++) {
				for(int y = 0; y < size.y; y++) {
					if(cubes[x][y][z] != null) {
						float[] a = cubes[x][y][z].getVertices();
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
		
		for(int z = 0; z < size.z; z++) {
			for(int x = 0; x < size.x; x++) {
				for(int y = 0; y < size.y; y++) {
					if(cubes[x][y][z] != null) {
						float[] a = cubes[x][y][z].getNormals();
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
		
		for(int z = 0; z < size.z; z++) {
			for(int x = 0; x < size.x; x++) {
				for(int y = 0; y < size.y; y++) {
					if(cubes[x][y][z] != null) {
						float[] a = cubes[x][y][z].getColors();
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
		
		for(int z = 0; z < size.z; z++) {
			for(int x = 0; x < size.x; x++) {
				for(int y = 0; y < size.y; y++) {
					if(cubes[x][y][z] != null) {
						float[] a = cubes[x][y][z].getTexCoords();
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
		
		// Delete old list
		if(glListIndex != 0) {
			GL11.glDeleteLists(glListIndex, 1);
			glListIndex = 0;
		}
	}
	
	public void render() {
		if(glListIndex == 0) {
			// Create a gl list
			glListIndex = GL11.glGenLists(1);
			GL11.glNewList(glListIndex, GL11.GL_COMPILE);
				// Save the current matrix
				GL11.glPushMatrix();
				
				// Add the translation matrix
				GL11.glTranslatef(position.x * cubeSize.x, position.y * cubeSize.y, position.z * cubeSize.z);
				
				GL11.glVertexPointer(3, 0, vertexBuffer);
				GL11.glNormalPointer(0, normalBuffer);
				
				if(drawTextures)
					GL11.glTexCoordPointer(2, 0, texCoordsBuffer);
				else
					GL11.glColorPointer(4, 0, colorBuffer);
				
				GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexBuffer.limit() / 3);
				
				// Restore the matrix
				GL11.glPopMatrix();
			GL11.glEndList();
		}
		
		GL11.glCallList(glListIndex);
	}
}
