package game;

import org.lwjgl.opengl.GL11;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Console;
import de.lessvoid.nifty.controls.ConsoleCommands;
import de.lessvoid.nifty.controls.ConsoleCommands.ConsoleCommand;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;

public class GUI {

	public Nifty niftySystem;
	public LwjglInputSystem inputSystem = new LwjglInputSystem();
	
	private Label infoLabel;
	
	public GUI() {
		try {
			inputSystem.startup();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		niftySystem = new Nifty(new LwjglRenderDevice(), new NullSoundDevice(), inputSystem , new AccurateTimeProvider());
		
		//Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE); 
		//Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);
		
		try {
			niftySystem.validateXml("res/test.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}

		niftySystem.fromXml("res/test.xml", "start");
		infoLabel = niftySystem.getCurrentScreen().findNiftyControl("infoLabel", Label.class);
		
		
		
		// get the console control (this assumes that there is a console in the current screen with the id="console"
		Console console = niftySystem.getCurrentScreen().findNiftyControl("console", Console.class);

		// output hello to the console
		console.output("Hello :)");

		// create the console commands class and attach it to the console
		ConsoleCommands consoleCommands = new ConsoleCommands(niftySystem, console);

		// create a simple command (see below for implementation) this class will be called when the command is detected
		// and register the command as a command with the console
		ConsoleCommand simpleCommand = new SimpleCommand();
		consoleCommands.registerCommand("simple", simpleCommand);

		// create another command (this time we can even register arguments with nifty so that the command completion will work with arguments too)
		ConsoleCommand showCommand = new ShowCommand();
		consoleCommands.registerCommand("show a", showCommand);
		consoleCommands.registerCommand("show b", showCommand);
		consoleCommands.registerCommand("show c", showCommand);

		// finally enable command completion
		consoleCommands.enableCommandCompletion(true);
		
	}
	
	public void setInfoLabel(String text) {
	//	infoLabel.setText(text);
	}
	
	public boolean update() {
		return niftySystem.update();
	}
	
	public void render() {
		niftySystem.render(false);
	}
	
	private class SimpleCommand implements ConsoleCommand {
		  @Override
		  public void execute(final String[] args) {
		    System.out.println(args[0]); // this is always the command (in this case 'simple')
		    if (args.length > 1) {
		      for (String a : args) {
		        System.out.println(a);
		      }
		    }
		  }
		}

		private class ShowCommand implements ConsoleCommand {
		  @Override
		  public void execute(final String[] args) {
		    System.out.println(args[0] + " " + args[1]);
		  }
		}
}
