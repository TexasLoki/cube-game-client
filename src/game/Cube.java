package game;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class Cube {

	/* pos1 contains the lowest x, y, z. pos2 contains the heighest x, y, z */
	public Vector3f pos1, pos2;
	
	// Color to use if no texture is present
	protected Vector4f color;	
	
	// Texture class from Slick-Util library
	protected Texture texture;
	
	// Determines which sides to draw
	protected boolean renderTop, renderBottom, renderFront, renderBack, renderRight, renderLeft;
	
	public Cube(Vector3f pos1, Vector3f pos2, Vector4f color, Texture texture) {
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.color = color;
		this.texture = texture;
		
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
	
	/* Renders the cube. */
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
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
	}
}

