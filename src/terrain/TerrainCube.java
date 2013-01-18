package terrain;
import game.Rectf;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

import types.Vector3f;
import types.Vector4f;

public class TerrainCube {

	/* pos1 contains the lowest x, y, z. pos2 contains the heighest x, y, z */
	public Vector3f pos1, pos2;
	
	// Color to use if no texture is present
	public Vector4f color;
	
	// Texture class from Slick-Util library
	public Texture texture;
	public Rectf texRect;
	
	public static final boolean PER_FACE_NORMALS = false;
	
	// Determines which sides to draw
	protected boolean renderTop, renderBottom, renderFront, renderBack, renderRight, renderLeft;
	
	public TerrainCube(Vector3f pos1, Vector3f pos2, Vector4f color, Texture texture, Rectf texRect) {
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.color = color;
		this.texture = texture;
		this.texRect = texRect;
		
		// Default is to draw all sides
		this.renderTop = true;
		this.renderBottom = true;
		this.renderFront = true;
		this.renderBack = true;
		this.renderRight = true;
		this.renderLeft = true;
	}
	
	/* Sets information about which sides to draw. */
	public void setVisibleSides(boolean drawTop, boolean drawBottom, boolean drawFront, boolean drawBack, boolean drawRight, boolean drawLeft) {
		this.renderTop = drawTop;
		this.renderBottom = drawBottom;
		this.renderFront = drawFront;
		this.renderBack = drawBack;
		this.renderRight = drawRight;
		this.renderLeft = drawLeft;
	}
	
	/* Renders the cube (sloooooow function). */
	public void render() {
		if(texture != null) {
			// Set the texture
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
		} else {
			// Set the color
			GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glColor4f(color.x, color.y, color.z, color.a);
		}
		
		GL11.glBegin(GL11.GL_QUADS);
		
		// Top
		if(renderTop) {
			GL11.glNormal3f(1.0f, 1.0f, -1.0f);
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos1.z);
			GL11.glNormal3f(-1.0f, 1.0f, -1.0f);
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos1.z);
			GL11.glNormal3f(-1.0f, 1.0f, 1.0f);
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos2.z);
			GL11.glNormal3f(1.0f, 1.0f, 1.0f);
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos2.z);
		}
		
		// Bottom
		if(renderBottom) {
			GL11.glNormal3f(1.0f, -1.0f, 1.0f);
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos2.z);
			GL11.glNormal3f(-1.0f, -1.0f, 1.0f);
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos2.z);
			GL11.glNormal3f(-1.0f, -1.0f, -1.0f);
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos1.z);
			GL11.glNormal3f(1.0f, -1.0f, -1.0f);
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos1.z);
		}
		
		// Front
		if(renderFront) {
			GL11.glNormal3f(1.0f, 1.0f, 1.0f);
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos2.z);
			GL11.glNormal3f(-1.0f, 1.0f, 1.0f);
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos2.z);
			GL11.glNormal3f(-1.0f, -1.0f, 1.0f);
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos2.z);
			GL11.glNormal3f(1.0f, -1.0f, 1.0f);
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos2.z);
		}
		
		// Back
		if(renderBack) {
			GL11.glNormal3f(-1.0f, 1.0f, -1.0f);
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos1.z);
			GL11.glNormal3f(1.0f, 1.0f, -1.0f);
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos1.z);
			GL11.glNormal3f(1.0f, -1.0f, -1.0f);
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos1.z);
			GL11.glNormal3f(-1.0f, -1.0f, -1.0f);
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos1.z);
		}
		
		// Right
		if(renderRight) {
			GL11.glNormal3f(1.0f, 1.0f, -1.0f);
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos1.z);
			GL11.glNormal3f(1.0f, 1.0f, 1.0f);
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(pos2.x, pos2.y, pos2.z);
			GL11.glNormal3f(1.0f, -1.0f, 1.0f);
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos2.z);
			GL11.glNormal3f(1.0f, -1.0f, -1.0f);
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(pos2.x, pos1.y, pos1.z);
		}
		
		// Left
		if(renderLeft) {
			GL11.glNormal3f(-1.0f, 1.0f, 1.0f);
			GL11.glTexCoord2f(1.0f, 0.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos2.z);
			GL11.glNormal3f(-1.0f, 1.0f, -1.0f);
			GL11.glTexCoord2f(0.0f, 0.0f);
			GL11.glVertex3f(pos1.x, pos2.y, pos1.z);
			GL11.glNormal3f(-1.0f, -1.0f, -1.0f);
			GL11.glTexCoord2f(0.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos1.z);
			GL11.glNormal3f(-1.0f, -1.0f, 1.0f);
			GL11.glTexCoord2f(1.0f, 1.0f);
			GL11.glVertex3f(pos1.x, pos1.y, pos2.z);
		}
		
		GL11.glEnd();
		
		// Reset color if color was used
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
	}
	
	/* Returns the vertices representing this cube as a float array. */
	public float[] getVertices() {
		float[] vertices;
		int sides = 0;
		
		// Calculate the array size
		if(renderTop)
			sides++;
			
		if(renderBottom)
			sides++;
		
		if(renderFront)
			sides++;
		
		if(renderBack)
			sides++;
		
		if(renderRight)
			sides++;
		
		if(renderLeft)
			sides++;
		
		// Allocate the array
		vertices = new float[sides * 4 * 3];
		
		// Write array data
		int pos = 0;
		
		// Top
		if(renderTop) {
			putCoordinatesInArray(vertices, pos, pos2.x, pos2.y, pos1.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos2.y, pos1.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos2.y, pos2.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos2.x, pos2.y, pos2.z);
			pos += 3;
		}
		
		// Bottom
		if(renderBottom) {
			putCoordinatesInArray(vertices, pos, pos2.x, pos1.y, pos2.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos1.y, pos2.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos1.y, pos1.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos2.x, pos1.y, pos1.z);
			pos += 3;
		}
		
		// Front
		if(renderFront) {
			putCoordinatesInArray(vertices, pos, pos2.x, pos2.y, pos2.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos2.y, pos2.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos1.y, pos2.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos2.x, pos1.y, pos2.z);
			pos += 3;
		}
		
		// Back
		if(renderBack) {
			putCoordinatesInArray(vertices, pos, pos1.x, pos2.y, pos1.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos2.x, pos2.y, pos1.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos2.x, pos1.y, pos1.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos1.y, pos1.z);
			pos += 3;
		}
		
		// Right
		if(renderRight) {
			putCoordinatesInArray(vertices, pos, pos2.x, pos2.y, pos1.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos2.x, pos2.y, pos2.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos2.x, pos1.y, pos2.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos2.x, pos1.y, pos1.z);
			pos += 3;
		}
		
		// Left
		if(renderLeft) {
			putCoordinatesInArray(vertices, pos, pos1.x, pos2.y, pos2.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos2.y, pos1.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos1.y, pos1.z);
			pos += 3;
			
			putCoordinatesInArray(vertices, pos, pos1.x, pos1.y, pos2.z);
			pos += 3;
		}
		
		return vertices;
	}
	
	/* Returns the normals representing of this cube as a float array. */
	public float[] getNormals() {
		float[] normals;
		int sides = 0;
		
		// Calculate the array size
		if(renderTop)
			sides++;
			
		if(renderBottom)
			sides++;
		
		if(renderFront)
			sides++;
		
		if(renderBack)
			sides++;
		
		if(renderRight)
			sides++;
		
		if(renderLeft)
			sides++;
		
		// Allocate the array
		normals = new float[sides * 4 * 3];
		
		// Write array data
		int pos = 0;
		
		if(PER_FACE_NORMALS) {
			// Top
			if(renderTop) {
				putCoordinatesInArray(normals, pos, 0.0f, 1.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, 1.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, 1.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, 1.0f, 0.0f);
				pos += 3;
			}
			
			// Bottom
			if(renderBottom) {
				putCoordinatesInArray(normals, pos, 0.0f, -1.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, -1.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, -1.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, -1.0f, 0.0f);
				pos += 3;
			}
			
			// Front
			if(renderFront) {
				putCoordinatesInArray(normals, pos, 0.0f, 0.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, 0.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, 0.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, 0.0f, 1.0f);
				pos += 3;
			}
			
			// Back
			if(renderBack) {
				putCoordinatesInArray(normals, pos, 0.0f, 0.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, 0.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, 0.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 0.0f, 0.0f, -1.0f);
				pos += 3;
			}
			
			// Right
			if(renderRight) {
				putCoordinatesInArray(normals, pos, 1.0f, 0.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, 0.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, 0.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, 0.0f, 0.0f);
				pos += 3;
			}
			
			// Left
			if(renderLeft) {
				putCoordinatesInArray(normals, pos, -1.0f, 0.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, 0.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, 0.0f, 0.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, 0.0f, 0.0f);
				pos += 3;
			}
			
		} else {
			// Top
			if(renderTop) {
				putCoordinatesInArray(normals, pos, 1.0f, 1.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, 1.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, 1.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, 1.0f, 1.0f);
				pos += 3;
			}
			
			// Bottom
			if(renderBottom) {
				putCoordinatesInArray(normals, pos, 1.0f, -1.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, -1.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, -1.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, -1.0f, -1.0f);
				pos += 3;
			}
			
			// Front
			if(renderFront) {
				putCoordinatesInArray(normals, pos, 1.0f, 1.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, 1.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, -1.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, -1.0f, 1.0f);
				pos += 3;
			}
			
			// Back
			if(renderBack) {
				putCoordinatesInArray(normals, pos, -1.0f, 1.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, 1.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, -1.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, -1.0f, -1.0f);
				pos += 3;
			}
			
			// Right
			if(renderRight) {
				putCoordinatesInArray(normals, pos, 1.0f, 1.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, 1.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, -1.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, 1.0f, -1.0f, -1.0f);
				pos += 3;
			}
			
			// Left
			if(renderLeft) {
				putCoordinatesInArray(normals, pos, -1.0f, 1.0f, 1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, 1.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, -1.0f, -1.0f);
				pos += 3;
				
				putCoordinatesInArray(normals, pos, -1.0f, -1.0f, 1.0f);
				pos += 3;
			}
		}

		return normals;
	}
	
	/* Returns the colors representing of the vertices of this cube as a float array. */
	public float[] getColors() {
		float[] colors;
		int sides = 0;
		
		// Calculate the array size
		if(renderTop)
			sides++;
			
		if(renderBottom)
			sides++;
		
		if(renderFront)
			sides++;
		
		if(renderBack)
			sides++;
		
		if(renderRight)
			sides++;
		
		if(renderLeft)
			sides++;
		
		// Allocate the array
		colors = new float[sides * 4 * 4];
		
		// Write array data
		int pos = 0;
		
		// Top
		if(renderTop) {
			for(int v = 0; v < 4; v++) {
				colors[pos] = color.x;
				colors[pos + 1] = color.y;
				colors[pos + 2] = color.z;
				colors[pos + 3] = color.a;
				pos+=4;
			}
		}
		
		// Bottom
		if(renderBottom) {
			for(int v = 0; v < 4; v++) {
				colors[pos] = color.x;
				colors[pos + 1] = color.y;
				colors[pos + 2] = color.z;
				colors[pos + 3] = color.a;
				pos+=4;
			}
		}
		
		// Front
		if(renderFront) {
			for(int v = 0; v < 4; v++) {
				colors[pos] = color.x;
				colors[pos + 1] = color.y;
				colors[pos + 2] = color.z;
				colors[pos + 3] = color.a;
				pos+=4;
			}
		}
		
		// Back
		if(renderBack) {
			for(int v = 0; v < 4; v++) {
				colors[pos] = color.x;
				colors[pos + 1] = color.y;
				colors[pos + 2] = color.z;
				colors[pos + 3] = color.a;
				pos+=4;
			}
		}
		
		// Right
		if(renderRight) {
			for(int v = 0; v < 4; v++) {
				colors[pos] = color.x;
				colors[pos + 1] = color.y;
				colors[pos + 2] = color.z;
				colors[pos + 3] = color.a;
				pos+=4;
			}
		}
		
		// Left
		if(renderLeft) {
			for(int v = 0; v < 4; v++) {
				colors[pos] = color.x;
				colors[pos + 1] = color.y;
				colors[pos + 2] = color.z;
				colors[pos + 3] = color.a;
				pos+=4;
			}
		}
		
		return colors;
	}
	
	/* Returns the texture coordinates of this cube as a float array. */
	public float[] getTexCoords() {
		float[] coords;
		int sides = 0;
		
		// Calculate the array size
		if(renderTop)
			sides++;
			
		if(renderBottom)
			sides++;
		
		if(renderFront)
			sides++;
		
		if(renderBack)
			sides++;
		
		if(renderRight)
			sides++;
		
		if(renderLeft)
			sides++;
		
		// Allocate the array
		coords = new float[sides * 4 * 2];
		
		// Write array data
		int pos = 0;
		
		// Top
		if(renderTop) {
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.bottom);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.bottom);
			pos += 2;
		}
		
		// Bottom
		if(renderBottom) {
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.bottom);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.bottom);
			pos += 2;
		}
		
		// Front
		if(renderFront) {
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.bottom);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.bottom);
			pos += 2;
		}
		
		// Back
		if(renderBack) {
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.bottom);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.bottom);
			pos += 2;
		}
		
		// Right
		if(renderRight) {
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.bottom);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.bottom);
			pos += 2;
		}
		
		// Left
		if(renderLeft) {
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.top);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.left, texRect.bottom);
			pos += 2;
			
			putTexCoordinatesInArray(coords, pos, texRect.right, texRect.bottom);
			pos += 2;
		}
		
		return coords;
	}
	
	/* Helper function for putting coordinates in an array. */
	private void putCoordinatesInArray(float[] array, int pos, float x, float y, float z) {
		array[pos] 		= x;
		array[pos + 1] 	= y; 
		array[pos + 2] 	= z;
	}
	
	/* Helper function for putting texture coordinates in an array. */
	private void putTexCoordinatesInArray(float[] array, int pos, float x, float y) {
		array[pos] 		= x;
		array[pos + 1] 	= y; 
	}
}

