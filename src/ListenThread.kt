import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.SourceDataLine
import kotlin.system.exitProcess

//Inner class to play back the data that was
// saved.
internal class ListenThread(
    private val sourceDataLine: SourceDataLine,
    private val audioFormat: AudioFormat,
    private val audioInputStream: AudioInputStream
) : Thread() {
    //This is a working buffer used to transfer
    // the data between the AudioInputStream and
    // the SourceDataLine.  The size is rather
    // arbitrary.
    private val playBuffer: ByteArray = ByteArray(16384)

    override fun run() = try {


        //Open and start the SourceDataLine
        sourceDataLine.open(audioFormat)
        sourceDataLine.start()

        var cnt: Int

        //Transfer the audio data to the speakers
        while (true) {

            cnt = audioInputStream.read(
                playBuffer, 0,
                playBuffer.size)

            if ( cnt == -1)
                break

            //Keep looping until the input read
            // method returns -1 for empty stream.
            if (cnt > 0) {
                //Write data to the internal buffer of
                // the data line where it will be
                // delivered to the speakers in real
                // time
                sourceDataLine.write(
                    playBuffer, 0, cnt
                )
            }//end if
        }//end while

        //Block and wait for internal buffer of the
        // SourceDataLine to become empty.
        sourceDataLine.drain()


        //Finish with the SourceDataLine
        sourceDataLine.stop()
        sourceDataLine.close()


    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(0)
    }
    //end catch
//end run
}//end inner class ListenThread