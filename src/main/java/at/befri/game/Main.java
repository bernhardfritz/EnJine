package at.befri.game;

import at.befri.engine.GameEngine;
import at.befri.engine.IGameLogic;

public class Main {
	public static void main(String[] args) {
		IGameLogic gameLogic = new DummyGame();
		GameEngine gameEngine = new GameEngine("GAME", 600, 480, true, gameLogic);
		gameEngine.start();
	}
}

/*
 * TODO:
 * Add functionality to detect file changes and update textures accordingly
 */