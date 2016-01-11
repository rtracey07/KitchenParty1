package sample;

/** Labrador Creative Arts Festival 2015 - Kitchen Party Demo Pt 1 - Robert Tracey
 *  Modified Jan 7th, 2016  */

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.effect.*;
import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import java.util.*;

/** Kitchen Party Demo Pt 1 - A visualization project using midi network communication
 *  and Capacitive touch sensors (mapped to keyboard) to trigger lighting, audio,
 *  and projection effects when different instruments (made out of kitchen appliances)
 *  were played.
 *
 *  Original project interfaced with QLab v3 and Mainstage 3 and QLC+ using midi triggering.
 *  Images were forced to a secondary 1024x768 wide-throw projector.
 *
 *  Piano keys: Q,W,E,R,T,Y
 *  Horn keys:  ARROW.UP, ARROW.DOWN, ARROW.LEFT, ARROW.RIGHT
 *  Drum keys: U,I,O,P,[
 *
 *  NOTE: Corresponding QLab, Mainstage, and QLC+ files must be open for midi triggering to work.
 */

public class Main extends Application {

    //Opacity of all shapes.
	final double opacityValue = 0.7;

    //Spoon Piano Keyboard Mapping and Note Values.
	private KeyCode[] keys = {KeyCode.Q, KeyCode.W, KeyCode.E, KeyCode.R, KeyCode.T, KeyCode.Y};
	private int[] keyNotes = {87, 90, 92, 94, 97, 99};

    //Spatula Horn Keyboard Mapping and Note Values.
    private KeyCode[] horns = {KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT};
    private int[] hornNotes = {51,56, 61, 63};

    //Pot Drum Kit Keyboard Mapping and Note Values.
    private KeyCode[] drums = {KeyCode.U, KeyCode.I, KeyCode.O, KeyCode.P, KeyCode.OPEN_BRACKET};
	private int[] beats = {53, 40, 50, 48, 34, -1, 33, 94, 90, -1};

    //Colours for Keyboard Geometry.
	private Color[] keyColors = {new Color(0,0.12,1,opacityValue) ,new Color(0,0.24,1,opacityValue),
								 new Color(0,0.36,1,opacityValue), new Color(0,0.48,1,opacityValue),
								 new Color(0,0.6,1,opacityValue), new Color(0,0.72,1,opacityValue)};

    //Colours for Horn Geometry.
    private Color[] hornColors = {new Color(1,0.12,0,opacityValue) ,new Color(1,0.24,0,opacityValue),
                                 new Color(1,0.36,0,opacityValue), new Color(1,0.48,0,opacityValue)};

    //Midi Routing.
	MidiPlayer2 drum, horn, key;
    MidiPlayer2 lighting, qlab;

    //Geometry Grouping.
	private Group keyRects, hornRects;

	public static void main(String[] args) { launch(args); }

	@Override
	public void start(Stage primaryStage) {

        //Initialize Lighting State.
        lighting = new MidiPlayer2("Lighting");
        lighting.noteOn(98, 100);

        //Initialize QLab App State.
        qlab = new MidiPlayer2("QLAB");
        qlab.noteOn(2,1);

        //Initialize Instrument Midi Outputs.
		drum = new MidiPlayer2("Drums");
		key = new MidiPlayer2("Keys");
		horn = new MidiPlayer2("Horn");

		Group root = new Group();

        //Set screen size to primary window size and set initial state (black).
        javafx.geometry.Rectangle2D boundary = Screen.getScreens().get(0).getBounds();
		final Scene scene = new Scene(root, boundary.getWidth(),boundary.getHeight(), Color.BLACK);
		primaryStage.setScene(scene);
        primaryStage.setX(boundary.getMinX());
        primaryStage.setY(boundary.getMinY());

        //Create Geometry for Keyboard.
		keyRects = new Group();
		for (int i = 0; i <keys.length; i++) {
			Rectangle rectangle = new Rectangle(boundary.getWidth()/keys.length, boundary.getHeight(), keyColors[i]);
			rectangle.setLayoutX(rectangle.getWidth() * i);
	        rectangle.setLayoutY(0);
			rectangle.setTranslateZ(0.2);
			rectangle.setStrokeType(StrokeType.INSIDE);
			rectangle.setStrokeWidth(rectangle.getWidth() / 10);
			rectangle.setOpacity(0.8);
			rectangle.setVisible(false);
			keyRects.getChildren().add(rectangle);
		}

		keyRects.setEffect(new BoxBlur());

        //Create Geometry for Horn.
		hornRects = new Group();
		for (int i = 0; i <horns.length; i++) {
			Rectangle rectangle = new Rectangle(boundary.getWidth(), boundary.getHeight()/horns.length, hornColors[i]);
			rectangle.setLayoutX(0);
			rectangle.setLayoutY(rectangle.getHeight()*i);
			rectangle.setTranslateZ(0.1);
			rectangle.setStrokeType(StrokeType.INSIDE);
			rectangle.setOpacity(0.6);
			rectangle.setVisible(false);
			hornRects.getChildren().add(rectangle);
		}

		hornRects.setEffect(new BoxBlur());

        //Add Geometry.
		root.getChildren().add(keyRects);
		root.getChildren().add(hornRects);

        //Hash Set to hold currently triggered instruments.
		final Set<KeyCode> pressedKeys = new HashSet<KeyCode>();

        //Instrument Trigger Handler.
		scene.setOnKeyPressed(new EventHandler<javafx.scene.input.KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
                boolean found = false;

                //Check for initial press, and not already triggered.
                if (!pressedKeys.contains(event.getCode())) {

                    //Check Piano Triggers for corresponding press.
                    for (int i = 0; i < keys.length && !found; i++) {
                        //Trigger found.
                        if (event.getCode() == keys[i]) {
                            //Add instrument key.
                            pressedKeys.add(event.getCode());
                            //Trigger instrument lighting effect.
                            lighting.noteOn(22 + i, 100);
                            //Display geometry.
                            keyRects.getChildren().get(i).setVisible(true);
                            //Trigger keyboard note.
                            key.noteOn(keyNotes[i], 100);
                            found = true;
                        }
                    }

                    //Check Drum Triggers for corresponding press.
                    for (int i = 0; i < drums.length && !found; i++) {
                        //Trigger found.
                        if (event.getCode() == drums[i]) {
                            //Add instrument key.
                            pressedKeys.add(event.getCode());
                            //Trigger instrument lighting effect.
                            lighting.noteOn(21 - 6 + i, 100);
                            //Strobe Screen.
                            scene.setFill(Color.DARKGRAY);
                            //Trigger drum notes.
                            drum.noteOn(beats[i], 100);
                            drum.noteOn(beats[i+5], 100);
                            found = true;
                        }
                    }

                    //Check Horn Trigger for corresponding press.
                    for (int i = 0; i < horns.length && !found; i++) {
                        //Trigger found.
                        if (event.getCode() == horns[i]) {
                            //Add instrument key.
                            pressedKeys.add(event.getCode());
                            //Trigger horn note.
                            horn.noteOn(hornNotes[i], 100);
                            //Trigger instrument lighting effect.
                            lighting.noteOn(21-11+i, 100);
                            //Display geometry.
                            hornRects.getChildren().get(i).setVisible(true);
                            found = true;
                        }
                    }
                }
            }
        } );

        //Instrument Trigger Release Handler.
        scene.setOnKeyReleased(new EventHandler<javafx.scene.input.KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                boolean found = false;

                //Check that trigger is not pressed.
                if (pressedKeys.contains(event.getCode())) {
                    //Search Keyboard keys for corresponding trigger.
                    for (int i = 0; i < keys.length && !found; i++) {
                        //If found, reset audio, lighting, and geometry state.
                        if (event.getCode() == keys[i]) {
                            pressedKeys.remove(event.getCode());
                            lighting.noteOff(22 + i, 100);
                            keyRects.getChildren().get(i).setVisible(false);
                            key.noteOff(keyNotes[i], 100);
                            found = true;
                        }
                    }

                    //Search Drum keys for corresponding trigger.
                    for (int i = 0; i < drums.length && !found; i++) {
                        //If found, reset audio, lighting, and geometry state.
                        if (event.getCode() == drums[i]) {
                            pressedKeys.remove(event.getCode());
                            lighting.noteOff(21 - 6 + i, 100);
                            scene.setFill(Color.BLACK);
                            drum.noteOff(beats[i], 100);
                            drum.noteOff(beats[i+5], 100);
                            found = true;
                        }
                    }

                    //Search Horn keys for corresponding trigger.
                    for (int i = 0; i < horns.length && !found; i++) {
                        if (event.getCode() == horns[i]) {
                            pressedKeys.remove(event.getCode());
                            horn.noteOff(hornNotes[i], 100);
                            lighting.noteOn(21 - 11 + i, 100);
                            hornRects.getChildren().get(i).setVisible(false);
                            found = true;
                        }
                    }

                    //Hash Set is empty, restore to initial state.
                    if (pressedKeys.isEmpty()) {
                        lighting.noteOn(101, 100);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                        lighting.noteOn(98, 100);
                    }

                }
            }
        });

        //Set to Full Screen.
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreen(true);

        //On close, exit lighting and qlab sessions.
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                lighting.noteOn(98, 100);
                qlab.noteOn(2,2);
            }
        });

        //Display.
        primaryStage.show();
		}
	}