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
import javax.sound.sampled.*
import java.io.*
import java.util.Date
import kotlin.system.exitProcess

class AudioSynth01
//-------------------------------------------//

    : JFrame() {
    //Allowable 8000,11025,16000,22050,44100

    private val sampleRate = 16000.0f


    private var channels: Int = 0
    // Allowable 1,2


    private val bigEndian: Boolean
    //Allowable true,false

    //A buffer to hold two seconds monaural and one
    // second stereo data at 16000 samp/sec for
    // 16-bit samples
    private val audioData: ByteArray

    //Following components appear in the North
    // position of the GUI.
    private val generateBtn = JButton("Generate")
    private val playOrFileBtn = JButton("Play/File")
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

    //Following components appear in the South
    // position of the GUI.
    private val listen = JRadioButton("Listen", true)
    private val fileName: JTextField

    init {//constructor
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
        audioData = ByteArray(16000 * 4)
        //end actionPerformed
        generateBtn.addActionListener {
            //Don't allow Play during generation
            playOrFileBtn.isEnabled = false
            //Generate synthetic data
            val sg = SynGen()
            sg.getSyntheticData(audioData)

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
            if (tones.isSelected) channels = sg.tones(sampleRate)
            if (stereoPanning.isSelected)
                channels = sg.stereoPanning(sampleRate)
            if (stereoPingpong.isSelected)
                channels = sg.stereoPingpong(sampleRate)
            if (fmSweep.isSelected) channels = sg.fmSweep(sampleRate)
            if (decayPulse.isSelected) channels = sg.decayPulse(sampleRate)
            if (echoPulse.isSelected) channels = sg.echoPulse(sampleRate)
            if (waWaPulse.isSelected) channels = sg.waWaPulse(sampleRate)
            if (sineWave.isSelected) channels = sg.sineWave(sampleRate)

            Scope(audioData,16000.0,4)

            //Now it is OK for the user to listen
            // to or file the synthetic audio data.
            playOrFileBtn.isEnabled = true
        }//end addActionListener()

        //end actionPerformed
        playOrFileBtn.addActionListener {
            /* Play or file the data synthetic data */
            playOrFileData()
        }//end addActionListener()

        //Add two buttons and a text field to a
        // physical group in the North of the GUI.
        controlButtonPanel.add(generateBtn)
        controlButtonPanel.add(playOrFileBtn)
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
        title = "Copyright 2003, R.G.Baldwin"
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(250, 275)
        isVisible = true
        bigEndian = true
    }//end constructor
    //-------------------------------------------//

    //This method plays or files the synthetic
    // audio data that has been generated and saved
    // in an array in memory.
    private fun playOrFileData() {
        try {
            //Get an input stream on the byte array
            // containing the data
            val byteArrayInputStream = ByteArrayInputStream(
                audioData
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
                (audioData.size / audioFormat.frameSize).toLong()
            )

            //Get info on the required data line
            val dataLineInfo = DataLine.Info(
                SourceDataLine::class.java,
                audioFormat
            )

            //Get a SourceDataLine object
            val sourceDataLine = AudioSystem.getLine(
                dataLineInfo
            ) as SourceDataLine
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

                generateBtn.isEnabled = false
                playOrFileBtn.isEnabled = false
                ListenThread(sourceDataLine, audioFormat, audioInputStream).start()
                //Get and display the elapsed time for
                // the previous playback.
                val elapsedTime = (Date().time - startTime).toInt()
                elapsedTimeMeter.text = "" + elapsedTime

                //Re-enable buttons for another operation
                generateBtn.isEnabled = true
                playOrFileBtn.isEnabled = true
            } else {
                //Disable buttons until existing data
                // is written to the file.
                generateBtn.isEnabled = false
                playOrFileBtn.isEnabled = false

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
                generateBtn.isEnabled = true
                playOrFileBtn.isEnabled = true
            }//end else
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(0)
        }
        //end catch
    }//end playOrFileData



}//end outer class AudioSynth01.java
