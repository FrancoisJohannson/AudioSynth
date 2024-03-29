/*File AudioSynth01.java
Copyright 2003, R.G.Baldwin

This program demonstrates the ability to create
synthetic audio data, and to play it back
immediately, or to store it in an AU file for
later playback.

A GUI appears on the screen containing the
following components in the North position:

Generate button
Play/File button
Elapsed time meter (JTextField)

Several radio buttons appear in the Center
position of the GUI.  Each radio button selects
a different format for synthetic audio data.

The South position of the GUI contains the
following components:

Listen radio button
File radio button
File Name text field

Select a radio button from the Center and click
the Generate button.  A short segment of
synthetic audio data will be generated and saved
in memory.  The segment length is two seconds
for monaural data and one second for stereo data,
at 16000 samp/sec and 16 bits per sample.

To listen to the audio data, select the Listen
radio button in the South position and click the
Play/File button.  You can listen to the data
repeatedly if you so choose.  In addition to
listening to the data, you can also save it in
an audio file.

To save the audio data in an audio file of type
AU, enter a file name (without extension) in the
text field in the South position, select the
File radio button in the South position, and
click the Play/File button.

You should be able to play the audio file back
with any standard media player that can handle
the AU file type, or with a program written in
Java, such as the program named AudioPlayer02
that was discussed in an earlier lesson.

Tested using SDK 1.4.0 under Win2000
************************************************/

import javax.swing.*
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.sound.sampled.*
import java.io.*
import java.util.Date
import kotlin.system.exitProcess
import javax.swing.JCheckBox

class AudioSynth01
//-------------------------------------------//

    : JFrame(), KeyListener  {
    //Allowable 8000,11025,16000,22050,44100

    private val sampleRate = 16000.0f

    private var voicesActive : MutableList<Voice> = mutableListOf()

    private var channels: Int = 0
    // Allowable 1,2

    //private var playThread =Thread{}

    private val bigEndian: Boolean
    //Allowable true,false

    //A buffer to hold two seconds monaural and one
    // second stereo data at 16000 samp/sec for
    // 16-bit samples
    //private val audioData: ByteArray


    //Following components appear in the North
    // position of the GUI.
    private val showScopeBtn = JButton("Show Scope")
    private val playOrFileBtn = JButton("Play/File")
    private val playLoop = JButton("Play Loop")
    private val toggleLoop = JCheckBox("loop")
    private val elapsedTimeMeter = JLabel("0000")

    //Following radio buttons select a synthetic
    // data type.  Add more buttons if you add
    // more synthetic data types.  They appear in
    // the center position of the GUI.
    private val tones = JRadioButton("Tones", true)
    private val stereoPanning = JRadioButton("Stereo Panning")
    private val stereoPingpong = JRadioButton("Stereo Pingpong")
    private val fmSweep = JRadioButton("FM Sweep")
    private val decayPulse = JRadioButton("Decay Pulse")
    private val echoPulse = JRadioButton("Echo Pulse")
    private val waWaPulse = JRadioButton("WaWa Pulse")
    private val sineWave = JRadioButton("sineWave")
    private val superSaw = JRadioButton("superSaw")

    private val radioButtons :List<JRadioButton> = listOf( tones, stereoPanning, stereoPingpong, fmSweep, decayPulse, echoPulse, waWaPulse, sineWave, superSaw  )


    //Following components appear in the South
    // position of the GUI.
    private val listen = JRadioButton("Listen", true)
    private val fileName: JTextField
    private val globalVoice = Voice('ü',/*SynGen(),*/false,ByteArray(16000 * 4))


    init {//constructor

        radioButtons.forEach{ it.background = Color.WHITE }

        //A panel for the North position.  Note the
        // etched border.
        val controlButtonPanel = JPanel()
        controlButtonPanel.border = BorderFactory.createEtchedBorder()

        //A panel and button group for the radio
        // buttons in the Center position.
        val synButtonPanel = JPanel()
        val synButtonGroup = ButtonGroup()
        //This panel is used for cosmetic purposes
        // only, to cause the radio buttons to be
        // centered horizontally in the Center
        // position.
        val centerPanel = JPanel()
        centerPanel.background = Color.WHITE

        //A panel for the South position.  Note the
        // etched border.
        val outputButtonPanel = JPanel()
        outputButtonPanel.border = BorderFactory.createEtchedBorder()
        val outputButtonGroup = ButtonGroup()

        //Disable the Play button initially to force
        // the user to generate some data before
        // trying to listen to it or write it to a
        // file.
        playOrFileBtn.isEnabled = false

        //Register anonymous listeners on the
        // Generate button and the Play/File button.
        channels = 1
        //audioData = ByteArray(16000 * 4)



        //end actionPerformed
        showScopeBtn.addActionListener {
            showScope()
        }//end addActionListener()



        //end actionPerformed
        playOrFileBtn.addActionListener {
            /* Play or file the data synthetic data */
            playOrFileData()
        }//end addActionListener()

        playLoop.addActionListener {
            /* Play or file the data synthetic data */
            globalVoice.playDirectly(sampleRate,channels,true)

        }//end addActionListener()


        toggleLoop.addActionListener{
            globalVoice.bLoopContinue = toggleLoop.isSelected
        }

        //Add two buttons and a text field to a
        // physical group in the North of the GUI.
        controlButtonPanel.add(showScopeBtn)
        controlButtonPanel.add(playOrFileBtn)
        controlButtonPanel.add(playLoop)
        controlButtonPanel.add(toggleLoop)
        controlButtonPanel.add(elapsedTimeMeter)

        //Add radio buttons to a mutually exclusive
        // group in the Center of the GUI.  Make
        // additions here if you add new synthetic
        // generator methods.
        synButtonGroup.add(tones)
        synButtonGroup.add(stereoPanning)
        synButtonGroup.add(stereoPingpong)
        synButtonGroup.add(fmSweep)
        synButtonGroup.add(decayPulse)
        synButtonGroup.add(echoPulse)
        synButtonGroup.add(waWaPulse)
        synButtonGroup.add(sineWave)
        synButtonGroup.add(superSaw)

        //Add radio buttons to a physical group and
        // center it in the Center of the GUI. Make
        // additions here if you add new synthetic
        // generator methods.
        synButtonPanel.layout = GridLayout(0, 1)
        synButtonPanel.add(tones)
        synButtonPanel.add(stereoPanning)
        synButtonPanel.add(stereoPingpong)
        synButtonPanel.add(fmSweep)
        synButtonPanel.add(decayPulse)
        synButtonPanel.add(echoPulse)
        synButtonPanel.add(waWaPulse)
        synButtonPanel.add(sineWave)
        synButtonPanel.add(superSaw)

        //Note that the centerPanel has center
        // alignment by default.
        centerPanel.add(synButtonPanel)

        //Add radio buttons to a mutually exclusive
        // group in the South of the GUI.
        outputButtonGroup.add(listen)
        val file = JRadioButton("File")
        outputButtonGroup.add(file)

        //Add radio buttons to a physical group in
        // the South of the GUI.
        outputButtonPanel.add(listen)
        outputButtonPanel.add(file)
        fileName = JTextField("junk", 10)
        outputButtonPanel.add(fileName)

        //Add the panels containing components to the
        // content pane of the GUI in the appropriate
        // positions.
        contentPane.add(
            controlButtonPanel, BorderLayout.NORTH
        )
        contentPane.add(
            centerPanel,
            BorderLayout.CENTER
        )
        contentPane.add(
            outputButtonPanel,
            BorderLayout.SOUTH
        )

        //Finish the GUI.  If you add more radio
        // buttons in the center, you may need to
        // modify the call to setSize to increase
        // the vertical component of the GUI size.
        title = "Copyright 2019, Francois Johannson"
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(400, 400)
        isVisible = true
        bigEndian = true

        radioButtons.forEach{ it.addActionListener { performGenerate()} }

        addKeyListener(this)
        setFocusable(true)
        focusTraversalKeysEnabled = true

        tones.setSelected(true)
        performGenerate()

    }//end constructor
    //-------------------------------------------/

    override fun keyTyped(e: KeyEvent) {

        if ( voicesActive.filter{v -> v.key == e.keyChar}.isNotEmpty() )
            return




        when(e.keyChar) {
            'y' -> playNote(e.keyChar,261.6256f)  // C4
                's' -> playNote(e.keyChar,277.1826f)  // C#4
            'x' -> playNote(e.keyChar,293.6648f)  // D4
                'd' -> playNote(e.keyChar,311.1270f)  // D#4
            'c' -> playNote(e.keyChar,329.6276f)  // E4
            'v' -> playNote(e.keyChar,349.2282f)  // F4
                'g' -> playNote(e.keyChar,369.9944f)  // F#4
            'b' -> playNote(e.keyChar,391.9954f)  // G4
                'h' -> playNote(e.keyChar,415.3047f)  // G#4
            'n' -> playNote(e.keyChar,440.0000f)  // A4
                'j' -> playNote(e.keyChar,466.1638f)  // A#4
            'm' -> playNote(e.keyChar,493.8833f)  // B4
            ',' -> playNote(e.keyChar,523.2511f)  // C5
                'l' -> playNote(e.keyChar,554.3653f)  // C#5
            '.' -> playNote(e.keyChar,587.3295f)  // D5
                'ö' -> playNote(e.keyChar,622.2540f)  // D#5
            '-' -> playNote(e.keyChar,659.2551f)  // E5

            'q' -> playNote(e.keyChar,698.4565f)  // F5
                '2' -> playNote(e.keyChar,739.9888f)  // F#5
            'w' -> playNote(e.keyChar,783.9909f)  // G5
                '3' -> playNote(e.keyChar,830.6094f)  // G#5
            'e' -> playNote(e.keyChar,880.0000f)  // A5
                '4' -> playNote(e.keyChar,932.3275f)  // A#5
            'r' -> playNote(e.keyChar,987.7666f)  // B5
            't' -> playNote(e.keyChar,1046.502f)  // C6
                '6' -> playNote(e.keyChar,1108.731f)  // C#6
            'z' -> playNote(e.keyChar,1174.659f)  // D6
                '7' -> playNote(e.keyChar,1244.508f)  // D#6
            'u' -> playNote(e.keyChar,1318.510f)  // E6
            'i' -> playNote(e.keyChar,1396.913f)  // F6
                '9' -> playNote(e.keyChar,1479.978f)  // F#6
            'o' -> playNote(e.keyChar,1567.982f)  // G6
                '0' -> playNote(e.keyChar,1661.219f)  // G#6
            'p' -> playNote(e.keyChar,1760.000f)  // A6
                'ß' -> playNote(e.keyChar,1864.655f)  // A#6

        }



    }

    override fun keyPressed(e: KeyEvent) {
       //println("keyPressed")
    }

    override fun keyReleased(e: KeyEvent) {

        println("keyReleased: " + e.keyChar)

        if ( voicesActive.filter{v -> v.key == e.keyChar}.isNotEmpty() ) {
            val voice = voicesActive.filter { v -> v.key == e.keyChar }.single()
            voice.bLoopContinue = false
            voicesActive.removeIf { v -> v.key == e.keyChar }
        }

    }


    private fun playNote(key:Char, freq:Float) {
        //if ( !bLoopContinue) {

            println("keyTyped: $key")

            val sg = SynGen()
            val audioData = ByteArray(16000 * 4)
            sg.getSyntheticData(audioData)
            channels = selectedEffect(sg).invoke(sampleRate,freq)

            val voice = Voice(key,/*sg,*/true,audioData)
            voicesActive.add(voice)


            voice.playDirectly(sampleRate,channels,bigEndian)
        //}
    }

    private fun performGenerate() : SynGen {
        //Don't allow Play during generation
        playOrFileBtn.isEnabled = false
        //Generate synthetic data
        val sg = SynGen()
        sg.getSyntheticData(globalVoice.audioData)

        //Decide which synthetic data generator
        // method to invoke based on which radio
        // button the user selected in the Center of
        // the GUI.  If you add more methods for
        // other synthetic data types, you need to
        // add corresponding radio buttons to the
        // GUI and add statements here to test the
        // new radio buttons.  Make additions here
        // if you add new synthetic generator
        // methods.
        channels = selectedEffect(sg).invoke(sampleRate,440.0f)

        //Now it is OK for the user to listen
        // to or file the synthetic audio data.
        playOrFileBtn.isEnabled = true

        this.requestFocus()

        return sg
    }

    private fun selectedEffect(sg:SynGen) : (sampleRate:Float,frequency:Float) -> Int {

        if (tones.isSelected) return sg::tones
        if (stereoPanning.isSelected) return sg::stereoPanning
        if (stereoPingpong.isSelected) return sg::stereoPingpong
        if (fmSweep.isSelected) return sg::fmSweep
        if (decayPulse.isSelected) return sg::decayPulse
        if (echoPulse.isSelected) return sg::echoPulse
        if (waWaPulse.isSelected) return sg::waWaPulse
        if (sineWave.isSelected) return sg::sineWave
        if (superSaw.isSelected) return sg::superSaw

        return sg::sineWave
    }


    private fun showScope() {

        val title = radioButtons.single { it.isSelected }.text

        val frameScope = JFrame("$title channel 1")
        val scope = Scope(globalVoice.audioData, 16000.0, channels, 1)
        frameScope.contentPane = scope
        frameScope.pack()
        frameScope.minimumSize = frameScope.size
        frameScope.isLocationByPlatform = true
        frameScope.isVisible = true

        if ( channels == 2) {

            val frameScope2 = JFrame("$title channel 2")
            val scope2 = Scope(globalVoice.audioData, 16000.0, channels, 2)
            frameScope2.contentPane = scope2
            frameScope2.pack()
            frameScope2.minimumSize = frameScope2.size
            frameScope2.isLocationByPlatform = true
            frameScope2.isVisible = true

        }
    }





    //This method plays or files the synthetic
    // audio data that has been generated and saved
    // in an array in memory.
    private fun playOrFileData() {
        try {

            //Decide whether to play the synthetic
            // data immediately, or to write it into
            // an audio file, based on the user
            // selection of the radio buttons in the
            // South of the GUI..
            if (listen.isSelected) {
                //Create a thread to play back the data and
                // start it running.  It will run until all
                // the data has been played back
                //Disable buttons while data is being
                // played.
                //Get beginning of elapsed time for
                // playback
                val startTime = Date().time

                showScopeBtn.isEnabled = false
                playOrFileBtn.isEnabled = false

                //val voice = Voice('ü',/*SynGen(),*/false,audioData)
                globalVoice.playDirectly(sampleRate,channels,bigEndian)                //Get and display the elapsed time for

                // the previous playback.
                val elapsedTime = (Date().time - startTime).toInt()
                elapsedTimeMeter.text = "elapsed time: " + elapsedTime

                //Re-enable buttons for another operation
                showScopeBtn.isEnabled = true
                playOrFileBtn.isEnabled = true
            } else {



                //Disable buttons until existing data
                // is written to the file.
                showScopeBtn.isEnabled = false
                playOrFileBtn.isEnabled = false

                //Get an input stream on the byte array
                // containing the data
                val byteArrayInputStream = ByteArrayInputStream(
                    globalVoice.audioData
                )

                //Get the required audio format
                val signed = true
                val sampleSizeInBits = 16
                //The following are general instance variables
                // used to create a SourceDataLine object.
                val audioFormat = AudioFormat(
                    sampleRate,
                    sampleSizeInBits,
                    channels,
                    signed,
                    bigEndian
                )
                //Get an audio input stream from the
                // ByteArrayInputStream
                val audioInputStream = AudioInputStream(
                    byteArrayInputStream,
                    audioFormat,
                    (globalVoice.audioData.size / audioFormat.frameSize).toLong()
                )

                //Write the data to an output file with
                // the name provided by the text field
                // in the South of the GUI.
                try {
                    AudioSystem.write(
                        audioInputStream,
                        AudioFileFormat.Type.AU,
                        File(fileName.text + ".au")
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    exitProcess(0)
                }
                //end catch


                //Enable buttons for another operation
                showScopeBtn.isEnabled = true
                playOrFileBtn.isEnabled = true
            }//end else
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(0)
        }
        //end catch
    }//end playOrFileData



}//end outer class AudioSynth01.java
