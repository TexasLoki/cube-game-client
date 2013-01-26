package gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Console;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyInputMapping;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;

public class GUI {
	
	private static final String START_SCREEN_ID = "startScreen";
	private static final String DEBUG_LABEL_ID = "debugLabel";
	private static final String CONSOLE_POPUP_ID = "consolePopup";
	private static final String CONSOLE_ID = "console";
	
	// Nifty components
	private Nifty nifty;
	private LwjglInputSystem niftyInput = new LwjglInputSystem();
	
	// Debug label
	private Label debugLabel;
	
	// Console
	private GameConsole gameConsole;
	private Element consolePopup;
	private Console console;
	
	public GUI(String xmlFile) throws Exception {
		// Initialize nifty
		initializeNifty();
		
		// Validate xml file
		nifty.validateXml(xmlFile);
		
		// Load xml file
		nifty.fromXml(xmlFile, START_SCREEN_ID);
		
		// Set the key press handler
		nifty.getCurrentScreen().addPreKeyboardInputHandler(new NiftyInputMapping() {

			@Override
			public NiftyInputEvent convert(KeyboardInputEvent inputEvent) {
				 if (inputEvent.isKeyDown()) {
				      if (inputEvent.getKey() == KeyboardInputEvent.KEY_F1) {
				        return NiftyInputEvent.ConsoleToggle;
				      } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_RETURN) {
				        return NiftyInputEvent.Activate;
				      } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_SPACE) {
				        return NiftyInputEvent.Activate;
				      } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_TAB) {
				        if (inputEvent.isShiftDown()) {
				          return NiftyInputEvent.PrevInputElement;
				        } else {
				          return NiftyInputEvent.NextInputElement;
				        }
				      }
				    }
				    return null;
			} }, new KeyInputHandler() {

			@Override
			public boolean keyEvent(NiftyInputEvent inputEvent) {
				if(inputEvent == NiftyInputEvent.ConsoleToggle) {
					gameConsole.toggle();
				}

				return false;
			} });
		
		// Find the debug label
		debugLabel = nifty.getCurrentScreen().findNiftyControl(DEBUG_LABEL_ID, Label.class);
		
		// Create the console popup
		consolePopup = nifty.createPopup(CONSOLE_POPUP_ID);
		
		// Find the console
		console = consolePopup.findNiftyControl(CONSOLE_ID, Console.class);
		
		// Create the game console
		gameConsole = new GameConsole(console, consolePopup, nifty);
	}
	
	public void initializeNifty() throws Exception {
		// Create and start the input system
		niftyInput = new LwjglInputSystem();
		niftyInput.startup();
		
		// Create nifty
		nifty = new Nifty(new LwjglRenderDevice(),
						  new NullSoundDevice(),
						  niftyInput,
						  new AccurateTimeProvider());
		
		// Disable logging
		Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE); 
		Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);
	}
	
	public GameConsole getConsole() {
		return gameConsole;
	}
	
	public void setDebugLabel(String text) {
		debugLabel.setText(text);
	}
	
	public boolean update() {
		return nifty.update();
	}
	
	public void render() {
		nifty.render(false);
	}
}
