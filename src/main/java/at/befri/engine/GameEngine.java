package at.befri.engine;

public class GameEngine implements Runnable {
	private static final int TARGET_FPS = 75;
	private static final int TARGET_UPS = 30;
	
	private final Thread gameLoopThread;
	private final Window window;
	private final MouseInput mouseInput;
	private final IGameLogic gameLogic;
	private final Timer timer;
	
	public GameEngine(String title, int width, int height, boolean vSync, IGameLogic gameLogic) {
		gameLoopThread = new Thread(this, "GAME_LOOP_THREAD");
		window = new Window(title, width, height, vSync);
		mouseInput = new MouseInput();
		this.gameLogic = gameLogic;
		timer = new Timer();
	}
	
	public void start() {
		String osName = System.getProperty("os.name");
		if (osName.contains("Mac")) {
			gameLoopThread.run();
		} else {
			gameLoopThread.start();
		}
	}
	
	protected void init() throws Exception {
		window.init();
		mouseInput.init(window);
		gameLogic.init(window);
		timer.init();
	}
	
	protected void input() {
		mouseInput.input(window);
		gameLogic.input(window, mouseInput);
	}
	
	protected void update(float interval) {
		gameLogic.update(interval, mouseInput);
	}
	
	protected void render() {
		gameLogic.render(window);
		window.update();
	}
	
	private void sync() {
		float loopSlot = 1f / TARGET_FPS;
		double endTime = timer.getLastLoopTime() + loopSlot;
		while (timer.getTime() < endTime) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void gameLoop() {
		float elapsedTime;
		float accumulator = 0f;
		float interval = 1f / TARGET_UPS;
		
		boolean running = true;
		while (running && !window.windowShouldClose()) {
			elapsedTime = timer.getElapsedTime();
			accumulator += elapsedTime;
			
			input();
			
			while (accumulator >= interval) {
				update(interval);
				accumulator -= interval;
			}
			
			render();
			
			if (!window.isvSync()) {
				sync();
			}
		}
	}
	
	protected void cleanup() {
		gameLogic.cleanup();
	}
	
	@Override
	public void run() {
		try {
			init();
			gameLoop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cleanup();
		}
	}
}
