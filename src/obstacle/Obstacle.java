package obstacle;


import org.newdawn.slick.opengl.Texture;

import terrain.TerrainCube;
import terrain.TerrainChunk;
import types.Vector3;
import types.Vector3f;

public abstract class Obstacle {

	protected TerrainChunk chunk;
	protected Texture cubeTextures;
	protected TerrainCube[][][] obstacleArray;
	protected int xLength, yLength, zLength;
	
	public Obstacle(TerrainChunk chunk, Texture cubeTextures) {
		this.chunk = chunk;
		this.cubeTextures = cubeTextures;
	}
	
	public boolean placeObstacle(Vector3 position, boolean spaceMustBeEmpty) {
		// Make sure it fits the terrain bounds
		if(position.x >= 0 && position.x + xLength < chunk.size.x &&
		   position.y >= 0 && position.y + yLength < chunk.size.y &&
		   position.z >= 0 && position.z + zLength < chunk.size.z) {

			if(spaceMustBeEmpty) {
				// Make sure the space where it should be put is empty
				for(int x = 0; x < xLength; x++) {
					for(int y = 0; y < yLength; y++) {
						for(int z = 0; z < zLength; z++) {
							if(obstacleArray[x][y][z] != null && chunk.cubes[position.x + x][position.y + y][position.z + z] != null) {
								return false;
							}
						}
					}
				}
			}
			
			// Place the obstacle
			for(int x = 0; x < xLength; x++) {
				for(int y = 0; y < yLength; y++) {
					for(int z = 0; z < zLength; z++) {
						if(obstacleArray[x][y][z] != null) {
							chunk.cubes[position.x + x][position.y + y][position.z + z] = new TerrainCube(null, null, obstacleArray[x][y][z].color, obstacleArray[x][y][z].texture, obstacleArray[x][y][z].texRect);
							chunk.cubes[position.x + x][position.y + y][position.z + z].pos1 = new Vector3f((position.x + x) * chunk.cubeSize.x, (position.y + y) * chunk.cubeSize.y, (position.z + z) * chunk.cubeSize.z);
							chunk.cubes[position.x + x][position.y + y][position.z + z].pos2 = Vector3f.add(chunk.cubes[position.x + x][position.y + y][position.z + z].pos1, chunk.cubeSize);
						}
					}
				}
			}
			
			return true;
		}

		return false;
	}
}
