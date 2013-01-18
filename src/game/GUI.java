package game;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.tools.TimeProvider;

public class GUI {

	private Nifty niftySystem;
	private Label infoLabel;
	
	public GUI() {
		niftySystem = new Nifty(new LwjglRenderDevice(), new NullSoundDevice(), new LwjglInputSystem(), new TimeProvider());
		
		try {
			niftySystem.validateXml("res/test.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}

		niftySystem.fromXml("res/test.xml", "start");
		infoLabel = niftySystem.getCurrentScreen().findNiftyControl("infoLabel", Label.class);
	}
	
	public void setInfoLabel(String text) {
		infoLabel.setText(text);
	}
	
	public boolean update() {
		return niftySystem.update();
	}
	
	public void render() {
		niftySystem.render(false);
	}
}
