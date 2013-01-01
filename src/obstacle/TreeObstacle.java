package obstacle;

import game.TextureStore;

import org.newdawn.slick.opengl.Texture;

import terrain.TerrainCube;
import terrain.TerrainChunk;
import types.Vector4f;

public class TreeObstacle extends Obstacle {

	public TreeObstacle(TerrainChunk chunk, Texture cubeTextures) {
		super(chunk, cubeTextures);
	}
	
	public void createTree(boolean textures) {
		// Specify size
		xLength = 5;
		zLength = 5;
		yLength = 10;
		
		// Create array
		obstacleArray = new TerrainCube[xLength][yLength][zLength];
		
		for(int x = 0; x < xLength; x++) {
			for(int y = 0; y < yLength; y++) {
				for(int z = 0; z < zLength; z++) {
					obstacleArray[x][y][z] = null;
				}
			}
		}
		// Create tree crown
		for(int x = 0; x < xLength; x++) {
			for(int y = yLength/2; y < yLength -1; y++) {
				for(int z = 0; z < zLength; z++) {
					obstacleArray[x][y][z] = new TerrainCube(null, null, new Vector4f(0.0f, 0.25f, 0.06f, 1.0f), cubeTextures, TextureStore.getTexRect(0, 14));
				}
			}
		}	
		
		for(int x = 1; x < xLength - 1; x++) {
			for(int y = yLength - 1; y < yLength; y++) {
				for(int z = 1; z < zLength - 1; z++) {
					obstacleArray[x][y][z] = new TerrainCube(null, null, new Vector4f(0.0f, 0.25f, 0.06f, 1.0f), cubeTextures, TextureStore.getTexRect(0, 14));
				}
			}
		}
		
		for(int x = 1; x < xLength - 1; x++) {
			for(int y = yLength/2; y < yLength/2 + 1; y++) {
				for(int z = 1; z < zLength - 1; z++) {
					obstacleArray[x][y][z] = null;
				}
			}
		}
		
		// Create stem
		for(int y = 0; y < yLength - 1; y++ ) {
			obstacleArray[xLength/2][y][zLength/2] = new TerrainCube(null, null, new Vector4f(0.25f, 0.125f, 0.0f, 1.0f), cubeTextures, TextureStore.getTexRect(1, 14));
		}
		
	}
	
}
