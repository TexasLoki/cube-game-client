package game;

import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class Crosshair {

	private Texture texture;
	private DisplayMode dispMode;
	
	public Crosshair(TextureStore textureStore, DisplayMode dispMode) {
		this.dispMode = dispMode;
		texture = textureStore.getTexture("res/crosshair.png");
	}
	
	public void render() {
		int crossX = (int) (dispMode.getWidth() / 2 - texture.getImageWidth() / 2);
		int crossY = (int) (dispMode.getHeight() / 2 - texture.getImageHeight() / 2);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
		
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2f(0, 0); 
		GL11.glVertex2f(crossX, crossY);
		
		GL11.glTexCoord2f(0, 1); 
		GL11.glVertex2f(crossX, crossY + texture.getImageHeight());
		
		GL11.glTexCoord2f(1, 1); 
		GL11.glVertex2f(crossX + texture.getImageWidth(), crossY + texture.getImageHeight());
		
		GL11.glTexCoord2f(1, 0); 
		GL11.glVertex2f(crossX + texture.getImageWidth(), crossY);
		
		GL11.glEnd();
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}
