package gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Console;
import de.lessvoid.nifty.controls.ConsoleCommands;
import de.lessvoid.nifty.controls.ConsoleCommands.ConsoleCommand;
import de.lessvoid.nifty.elements.Element;

public class GameConsole {

	private Console console;
	private Element consolePopup;
	private Nifty nifty;
	
	private ConsoleCommands commands;
	
	public boolean isVisible = false;
	
	public GameConsole(Console console, Element consolePopup, Nifty nifty) {
		this.console = console;
		this.consolePopup = consolePopup;
		this.nifty = nifty;
		
		commands = new ConsoleCommands(nifty, console);
		commands.enableCommandCompletion(true);
	}
	
	public void addCommand(String command, ConsoleCommand handler) {
		commands.registerCommand(command, handler);
	}
	
	public void output(String str) {
		console.output(str);
	}
	
	public void toggle() {
		isVisible = !isVisible;
		
		if(isVisible)
			nifty.showPopup(nifty.getCurrentScreen(), consolePopup.getId(), null);
		else
			nifty.closePopup(consolePopup.getId());
	}
}
