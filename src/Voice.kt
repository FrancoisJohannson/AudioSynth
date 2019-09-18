import java.io.ByteArrayInputStream
import javax.sound.sampled.*

class Voice internal constructor(

    val key: Char,
    var bLoopContinue:Boolean,

    //A buffer to hold two seconds monaural and one
    // second stereo data at 16000 samp/sec for
    // 16-bit samples
    val audioData: ByteArray
    )
{


    private var playThread =Thread{}

    fun playDirectly(
        sampleRate:Float,
        channels:Int,
        bigEndian:Boolean
        ) {

        playThread=Thread{ playThread(sampleRate,channels,bigEndian) }
        playThread.start()


    }

    fun playThread(
        sampleRate:Float,
        channels:Int,
        bigEndian:Boolean
    ) {

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

        //Get info on the required data line
        val dataLineInfo = DataLine.Info(
            SourceDataLine::class.java,
            audioFormat
        )

        //Get a SourceDataLine object
        val sourceDataLine = AudioSystem.getLine(
            dataLineInfo
        ) as SourceDataLine

        //Open and start the SourceDataLine
        sourceDataLine.open(audioFormat)
        sourceDataLine.start()


        do {

            //Get an input stream on the byte array containing the data
            val byteArrayInputStream = ByteArrayInputStream(audioData)

            //Get an audio input stream from the ByteArrayInputStream
            val audioInputStream = AudioInputStream(
                byteArrayInputStream,
                audioFormat,
                (audioData.size / audioFormat.frameSize).toLong()
            )

            var cnt: Int
            val playBuffer = ByteArray(2048)

            //Transfer the audio data to the speakers
            while (true) {

                cnt = audioInputStream.read(
                    playBuffer, 0,
                    playBuffer.size
                )

                if (cnt == -1)
                    break

                //Keep looping until the input read
                // method returns -1 for empty stream.
                if (cnt > 0 ) {
                    //Write data to the internal buffer of
                    // the data line where it will be
                    // delivered to the speakers in real
                    // time
                    sourceDataLine.write(
                        playBuffer, 0, cnt
                    )
                }//end if
            }//end while

        } while (bLoopContinue)


        //Block and wait for internal buffer of the
        // SourceDataLine to become empty.
        sourceDataLine.drain()

        //Finish with the SourceDataLine
        sourceDataLine.stop()
        sourceDataLine.close()

    }
}