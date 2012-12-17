package obstacle;

import game.Cube;
import game.CubeTerrain;
import game.Vector4f;

import org.newdawn.slick.opengl.Texture;

public class SpruceObstacle extends Obstacle {

	public SpruceObstacle(CubeTerrain terrain, Texture cubeTextures) {
		super(terrain, cubeTextures);
	}
	
	public void createSpruce(boolean textures) {
		
		// Defines how the spruce tree will look
		int stemHeight = 17;
		int stemThickness = 1;
		int crownStartY = 5;	// Counting from 0
		int crownHeightPerLevel = 3;
		
		// Calculate the obstacle array size
		xLength = (int) Math.ceil((float)(stemHeight - crownStartY) / (float)crownHeightPerLevel) * 2 + stemThickness;
		zLength = (int) Math.ceil((float)(stemHeight - crownStartY) / (float)crownHeightPerLevel) * 2 + stemThickness;
		yLength = stemHeight + crownHeightPerLevel;
		
		// Create the array
		obstacleArray = new Cube[xLength][yLength][zLength];
		
		// Create the green crown/leaves
		for(int y = stemHeight; y < stemHeight + crownHeightPerLevel; y++) {
			for(int x = 0; x < stemThickness; x++) {
				for(int z = 0; z < stemThickness; z++) {
					obstacleArray[(xLength - stemThickness) / 2 + x][y][(zLength - stemThickness) / 2 + z] = new Cube(null, null, new Vector4f(0.0f, 0.20f, 0.04f, 1.0f), null, null);
				}
			}
		}
		
		for(int y = stemHeight - 1; y >= crownStartY; y--) {
			int rectSize = (int) Math.ceil((float)(stemHeight - y) / (float)crownHeightPerLevel) * 2 + stemThickness;
			
			for(int x = 0; x < rectSize; x++) {
				for(int z = 0; z < rectSize; z++) {
					obstacleArray[(xLength - rectSize) / 2 + x][y][(zLength - rectSize) / 2 + z] = new Cube(null, null, new Vector4f(0.0f, 0.20f, 0.04f, 1.0f), null, null);
				}
			}
			
		}
		
		// Create the stem
		for(int y = 0; y < stemHeight; y++) {
			for(int x = 0; x < stemThickness; x++) {
				for(int z = 0; z < stemThickness; z++) {
					obstacleArray[(xLength - stemThickness) / 2 + x][y][(zLength - stemThickness) / 2 + z] = new Cube(null, null, new Vector4f(0.25f, 0.125f, 0.0f, 1.0f), null, null);
				}
			}
			
		}
	}
	
}
