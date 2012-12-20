package obstacle;

import game.Cube;
import game.CubeTerrain;
import game.TextureStore;
import game.Vector4f;

import org.newdawn.slick.opengl.Texture;

public class TreeObstacle extends Obstacle {

	public TreeObstacle(CubeTerrain terrain, Texture cubeTextures) {
		super(terrain, cubeTextures);
	}
	
	public void createTree(boolean textures) {
		// Specify size
		xLength = 5;
		zLength = 5;
		yLength = 10;
		
		// Create array
		obstacleArray = new Cube[xLength][yLength][zLength];
		
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
					obstacleArray[x][y][z] = new Cube(null, null, new Vector4f(0.0f, 0.25f, 0.06f, 1.0f), cubeTextures, TextureStore.getTexRect(0, 14));
				}
			}
		}	
		
		for(int x = 1; x < xLength - 1; x++) {
			for(int y = yLength - 1; y < yLength; y++) {
				for(int z = 1; z < zLength - 1; z++) {
					obstacleArray[x][y][z] = new Cube(null, null, new Vector4f(0.0f, 0.25f, 0.06f, 1.0f), cubeTextures, TextureStore.getTexRect(0, 14));
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
			obstacleArray[xLength/2][y][zLength/2] = new Cube(null, null, new Vector4f(0.25f, 0.125f, 0.0f, 1.0f), cubeTextures, TextureStore.getTexRect(1, 14));
		}
		
	}
	
}
