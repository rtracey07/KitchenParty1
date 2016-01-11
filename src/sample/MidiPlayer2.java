package sample;

/** Labrador Creative Arts Festival 2015 - Kitchen Party Demo Pt 1 - Robert Tracey
 *  Modified Jan 7th, 2016  */

import javax.sound.midi.*;

/** MidiPlayer2 - Route midi messages to corresponding devices.  */
public class MidiPlayer2 {

    private MidiDevice port;
    private Receiver receiver;

    public MidiPlayer2(String deviceName){

        //Open device corresponding to provided device name.
        try {
            MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();

            for (MidiDevice.Info e : devices)
                if(e.getName().contains(deviceName))
                    port= MidiSystem.getMidiDevice(e);

            port.open();
            receiver = port.getReceiver();

        //Catch port failure.
        } catch(MidiUnavailableException e){
            System.out.println("Failed to open port " + deviceName);
        }
    }

    //Play given midi note.
    public void noteOn(int value, int velocity){
        //Catch value error.
        if(value == -1)
            return;
        //Send Note to device.
        try {
            receiver.send(new ShortMessage(ShortMessage.NOTE_ON, value, velocity), -1);
        } catch(InvalidMidiDataException e){
            System.out.println("Failed to send note to " + " " + port.getDeviceInfo());
        }
    }

    //Stop given midi note.
    public void noteOff(int value, int velocity) {
        //Catch value error.
        if (value == -1)
            return;
        //Send note-off to device.
        try {
            receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, value, velocity), -1);
        }   catch(InvalidMidiDataException e){
            System.out.println("Failed to end note at " + " " + port.getDeviceInfo());
        }
    }
}
