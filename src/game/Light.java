package game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

public class Light {
	
	private int glLight;
	
	private float ambient[] = { 0.5f, 0.5f, 0.5f, 1.0f };
	private float diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	private float position[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	
	public Light() {
	        
	}
	
	public void setAmbient(float r, float g, float b, float a) {
		ambient = new float[] { r, g, b, a };
	}
	
	public void setDiffuse(float r, float g, float b, float a) {
		diffuse = new float[] { r, g, b, a };
	}
	
	public void setPosition(float x, float y, float z) {
		position = new float[] { x, y, z, 1.0f };
	}
	
	public void enable(int glLight) {
		this.glLight = glLight;
		
		// Create a byte buffer
		ByteBuffer temp = ByteBuffer.allocateDirect(16);
		temp.order(ByteOrder.nativeOrder());
		
        GL11.glLight(glLight, GL11.GL_AMBIENT, (FloatBuffer)temp.asFloatBuffer().put(ambient).flip());
        GL11.glLight(glLight, GL11.GL_DIFFUSE, (FloatBuffer)temp.asFloatBuffer().put(diffuse).flip());
        GL11.glLight(glLight, GL11.GL_POSITION,(FloatBuffer)temp.asFloatBuffer().put(position).flip());

		GL11.glEnable(glLight);
	}
	
	public void disable() {
		GL11.glDisable(glLight);
	}

}
