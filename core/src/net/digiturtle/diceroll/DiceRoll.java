package net.digiturtle.diceroll;

import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class DiceRoll extends ApplicationAdapter {
	
	public static final int WIDTH = 400, HEIGHT = 640;
	public static final int TOTAL_THEORETICAL = 36;
	
	private Stage stage;
	private Skin skin;
	
	private TextButton undo, redo, reset;
	private Label lastValue;
	
	private ShapeRenderer renderer;
	private OrthographicCamera camera;
	
	private int[] experimental, theoretical;
	
	private ArrayList<Integer> rolls = new ArrayList<>();
	private int position = 0;
	
	private void logRoll (int roll) {
		rolls.add(position, roll);
		position++;
		recomputeState();
	}
	
	private void undoRoll () {
		position = Math.max(0, position - 1);
		recomputeState();
	}
	private void redoRoll () {
		position = Math.min(rolls.size(), position + 1);
		recomputeState();
	}
	private void reset () {
		rolls.clear();
		position = 0;
		recomputeState();
	}
	
	private void recomputeState () {
		lastValue.setText("LAST:\n" + (position > 0 ? rolls.get(position - 1) : "?"));
		Arrays.fill(experimental, 0);
		for (int i = 0; i < position; i++) {
			experimental[rolls.get(i) - 2]++;
		}
	}

	@Override
	public void create () {
		theoretical = new int[] {
			1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1
		};
		experimental = new int[11];
		
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		stage = new Stage(new StretchViewport(WIDTH, HEIGHT));
		renderer = new ShapeRenderer();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, WIDTH, HEIGHT);
		
		int slot = 0;
		for (int roll = 2; roll <= 12; roll++) {
			int x = (slot % 4) * 90 + 20 + 10, y = ((slot - (slot % 4)) / 4) * 90 + 20;
			TextButton button = new TextButton(Integer.toString(roll), skin);
			button.setBounds(x, HEIGHT - y - 80, 65, 65);
			final int number = roll;
			button.addListener(new ClickListener () {
				public void clicked (InputEvent evt, float x, float y) {
					logRoll(number);
				}
			});
			stage.addActor(button);
			slot++;
		}
		
		int widths = 100, padding = 15;
		int offset = (WIDTH - (widths * 3 + padding * 2)) / 2;
		undo = new TextButton("UNDO", skin);
		undo.setBounds(offset, HEIGHT - 360, widths, 65);
		undo.addListener(new ClickListener () {
			public void clicked (InputEvent evt, float x, float y) {
				undoRoll();
			}
		});
		stage.addActor(undo);
		
		redo = new TextButton("REDO", skin);
		redo.setBounds(offset + widths + padding, HEIGHT - 360, widths, 65);
		redo.addListener(new ClickListener () {
			public void clicked (InputEvent evt, float x, float y) {
				redoRoll();
			}
		});
		stage.addActor(redo);
		
		reset = new TextButton("RESET", skin);
		reset.setBounds(offset + 2 * (widths + padding), HEIGHT - 360, widths, 65);
		reset.addListener(new ClickListener () {
			public void clicked (InputEvent evt, float x, float y) {
				reset();
			}
		});
		stage.addActor(reset);

		int x = 3 * 90 + 20 + 10, y = 3 * 90 + 20;
		lastValue = new Label("LAST:\n?", skin);
		lastValue.setPosition(x, HEIGHT - y + 20);
		lastValue.setColor(Color.GREEN);
		stage.addActor(lastValue);
		
		Gdx.input.setInputProcessor(stage);
	}
	
	private double[] computeNormalizedProbabilities(int[] values) {
		int total = Arrays.stream(values).sum();
		double[] result = new double[values.length];
		if (total == 0) {
			return result;
		}
		for (int i = 0; i < result.length; i++) {
			result[i] = (double)values[i] / total;
		}
		double max = Arrays.stream(result).max().getAsDouble();
		for (int i = 0; i < result.length; i++) {
			result[i] /= max;
		}
		return result;
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
		renderer.begin(ShapeType.Filled);
		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(.8f, .8f, .8f, 1);
		renderer.rect(10, 10, WIDTH - 20, 250);
		
		// 380 = 5 + 5 + (5 + X) * 22
		double[] theoreticalNorm = computeNormalizedProbabilities(theoretical);
		renderer.setColor(.2f, .2f, .2f, .4f);
		for (int i = 0; i < theoretical.length; i++) {
			renderer.rect(10 + 15 + 16 * i * 2, 10, 12, (int) (theoreticalNorm[i] * 240));
		}

		renderer.setColor(0, .8f, 0, 1);
		double[] experimentalNorm = computeNormalizedProbabilities(experimental);
		for (int i = 0; i < theoretical.length; i++) {
			renderer.rect(10 + 15 + 16 * i * 2 + 13, 10, 12, (int) (experimentalNorm[i] * 240));
		}
		
		renderer.end();
	}
	
	@Override
	public void dispose () {
		stage.dispose();
	}
}
