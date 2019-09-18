//Inner signal generator class.

import java.nio.ByteBuffer
import java.nio.ShortBuffer
import kotlin.math.sin

//An object of this class can be used to
// generate a variety of different synthetic
// audio signals.  Each time the getSyntheticData
// method is called on an object of this class,
// the method will fill the incoming array with
// the samples for a synthetic signal.
internal class SynGen {
    private var shortBuffer: ShortBuffer? = null
    private var byteLength: Int = 0


    fun getSyntheticData(synDataBuffer: ByteArray) {
        //Prepare the ByteBuffer and the shortBuffer
        // for use
        //Note:  Because this class uses a ByteBuffer
        // asShortBuffer to handle the data, it can
        // only be used to generate signed 16-bit
        // data.
        val byteBuffer = ByteBuffer.wrap(synDataBuffer)
        shortBuffer = byteBuffer.asShortBuffer()

        byteLength = synDataBuffer.size




    }//end getSyntheticData method
    //-------------------------------------------//

    //This method generates a monaural tone
    // consisting of the sum of three sinusoids.
    fun tones(sampleRate: Float, freq:Float): Int {
        //var sampleRate = sampleRate
        //Each channel requires two 8-bit bytes per
        // 16-bit sample.
        val bytesPerSamp = 2
        //sampleRate = 16000.0f
        // Allowable 8000,11025,16000,22050,44100
        val sampLength = byteLength / bytesPerSamp
        for (cnt in 0 until sampLength) {
            val time = (cnt / sampleRate).toDouble()
            val sinValue = (sin(2.0 * Math.PI * freq * time) +
                    sin(2.0 * Math.PI * (freq / 1.8) * time) +
                    sin(2.0 * Math.PI * (freq / 1.5) * time)) / 3.0
            shortBuffer!!.put((16000 * sinValue).toShort())
        }//end for loop

        return 1
    }//end method tones
    //-------------------------------------------//

    fun sineWave(sampleRate: Float, frequency:Float): Int {
        //var sampleRate = sampleRate
        //Each channel requires two 8-bit bytes per
        // 16-bit sample.
        val bytesPerSamp = 2
        //sampleRate = 16000.0f
        // Allowable 8000,11025,16000,22050,44100
        val sampLength = byteLength / bytesPerSamp

//        println("=======================================")
//        println("sampLength:" + sampLength)
//        println("=======================================")

        for (cnt in 0 until sampLength) {
            val time = (cnt / sampleRate).toDouble()
            val freq = frequency//arbitrary frequency
            val sinValue = sin(2.0 * Math.PI * freq * time)

//            if ( cnt < 200)
//                println(sinValue)

            shortBuffer!!.put((16000 * sinValue).toShort())
        }//end for loop

        return 1
    }//end method mySound
    //-------------------------------------------//

    //This method generates a stereo speaker sweep,
    // starting with a relatively high frequency
    // tone on the left speaker and moving across
    // to a lower frequency tone on the right
    // speaker.
    fun stereoPanning(sampleRate: Float, frequency:Float): Int {
        val bytesPerSamp = 4//Based on channels
        // Allowable 8000,11025,16000,22050,44100
        val sampLength = byteLength / bytesPerSamp
        for (cnt in 0 until sampLength) {
            //Calculate time-varying gain for each
            // speaker
            val rightGain = 16000.0 * cnt / sampLength
            val leftGain = 16000.0 - rightGain

            setupStereoBuffer(sampleRate, leftGain, rightGain, cnt,frequency)
        }//end for loop

        return 2
    }//end method stereoPanning
    //-------------------------------------------//

    //This method uses stereo to switch a sound
    // back and forth between the left and right
    // speakers at a rate of about eight switches
    // per second.  On my system, this is a much
    // better demonstration of the sound separation
    // between the two speakers than is the
    // demonstration produced by the stereoPanning
    // method.  Note also that because the sounds
    // are at different frequencies, the sound
    // produced is similar to that of U.S.
    // emergency vehicles.

    fun stereoPingpong(sampleRate: Float, frequency:Float): Int {
        val bytesPerSamp = 4//Based on channels
        // Allowable 8000,11025,16000,22050,44100
        val sampLength = byteLength / bytesPerSamp
        var leftGain = 0.0
        var rightGain = 16000.0
        for (cnt in 0 until sampLength) {
            //Calculate time-varying gain for each
            // speaker
            if (cnt % (sampLength / 8) == 0) {
                //swap gain values
                val temp = leftGain
                leftGain = rightGain
                rightGain = temp
            }//end if

            setupStereoBuffer(sampleRate, leftGain, rightGain, cnt, frequency)
        }//end for loop

        return 2
    }//end stereoPingpong method

    private fun setupStereoBuffer(
        sampleRate: Float,
        leftGain: Double,
        rightGain: Double,
        cnt: Int,
        freq: Float ) {
        val time = (cnt / sampleRate).toDouble()
        //val freq = 600.0//An arbitrary frequency
        //Generate data for left speaker
        var sinValue = sin(2.0 * Math.PI * freq * time)
        shortBuffer!!.put(
            (leftGain * sinValue).toShort()
        )
        //Generate data for right speaker
        sinValue = sin(2.0 * Math.PI * (freq * 0.8) * time)
        shortBuffer!!.put(
            (rightGain * sinValue).toShort()
        )
    }
    //-------------------------------------------//

    //This method generates a monaural linear
    // frequency sweep from 100 Hz to 1000Hz.
    fun fmSweep(sampleRate: Float, frequency:Float): Int {
        val bytesPerSamp = 2//Based on channels
        // Allowable 8000,11025,16000,22050,44100
        val sampLength = byteLength / bytesPerSamp
        val highFreq = frequency+1000.0

        for (cnt in 0 until sampLength) {
            val time = (cnt / sampleRate).toDouble()

            val freq = frequency + cnt * (highFreq - frequency) / sampLength
            val sinValue = sin(2.0 * Math.PI * freq * time)
            shortBuffer!!.put((16000 * sinValue).toShort())
        }//end for loop

        return 1
    }//end method fmSweep
    //-------------------------------------------//

    //This method generates a monaural triple-
    // frequency pulse that decays in a linear
    // fashion with time.
    fun decayPulse(sampleRate: Float, freq:Float): Int {
        val bytesPerSamp = 2//Based on channels
        // Allowable 8000,11025,16000,22050,44100
        val sampLength = byteLength / bytesPerSamp
        for (cnt in 0 until sampLength) {
            //The value of scale controls the rate of
            // decay - large scale, fast decay.
            var scale = (2 * cnt).toDouble()
            if (scale > sampLength) scale = sampLength.toDouble()
            val gain = 16000 * (sampLength - scale) / sampLength
            val time = (cnt / sampleRate).toDouble()
            //val freq = 499.0//an arbitrary freq
            val sinValue = (sin(2.0 * Math.PI * freq * time) +
                    sin(2.0 * Math.PI * (freq / 1.8) * time) +
                    sin(2.0 * Math.PI * (freq / 1.5) * time)) / 3.0
            shortBuffer!!.put((gain * sinValue).toShort())
        }//end for loop

        return 1
    }//end method decayPulse
    //-------------------------------------------//

    //This method generates a monaural triple-
    // frequency pulse that decays in a linear
    // fashion with time.  However, three echoes
    // can be heard over time with the amplitude
    // of the echoes also decreasing with time.
    fun echoPulse(sampleRate: Float, frequency:Float): Int {
        val bytesPerSamp = 2//Based on channels
        // Allowable 8000,11025,16000,22050,44100
        val sampLength = byteLength / bytesPerSamp
        var cnt2 = -8000
        var cnt3 = -16000
        var cnt4 = -24000
        var cnt1 = 0
        while (cnt1 < sampLength) {
            var `val` = pulseHelper(
                cnt1, sampLength, sampleRate, frequency
            )
            if (cnt2 > 0) {
                `val` += 0.7 * pulseHelper(
                    cnt2, sampLength, sampleRate, frequency
                )
            }//end if
            if (cnt3 > 0) {
                `val` += 0.49 * pulseHelper(
                    cnt3, sampLength, sampleRate, frequency
                )
            }//end if
            if (cnt4 > 0) {
                `val` += 0.34 * pulseHelper(
                    cnt4, sampLength, sampleRate, frequency
                )
            }//end if

            shortBuffer!!.put(`val`.toShort())
            cnt1++
            cnt2++
            cnt3++
            cnt4++
        }//end for loop

        return 1

    }//end method echoPulse

    //-------------------------------------------//

    //This method generates a monaural triple-
    // frequency pulse that decays in a linear
    // fashion with time.  However, three echoes
    // can be heard over time with the amplitude
    // of the echoes also decreasing with time.
    //Note that this method is identical to the
    // method named echoPulse, except that the
    // algebraic sign was switched on the amplitude
    // of two of the echoes before adding them to
    // the composite synthetic signal.  This
    // resulted in a difference in the
    // sound.
    fun waWaPulse(sampleRate: Float, frequency:Float): Int {
        //var sampleRate = sampleRate
        val bytesPerSamp = 2//Based on channels
        //sampleRate = 16000.0f
        // Allowable 8000,11025,16000,22050,44100
        val sampLength = byteLength / bytesPerSamp
        var cnt2 = -8000
        var cnt3 = -16000
        var cnt4 = -24000
        var cnt1 = 0
        while (cnt1 < sampLength) {
            var `val` = pulseHelper(
                cnt1, sampLength, sampleRate, frequency
            )
            if (cnt2 > 0) {
                `val` += -0.7 * pulseHelper(
                    cnt2, sampLength, sampleRate, frequency
                )
            }//end if
            if (cnt3 > 0) {
                `val` += 0.49 * pulseHelper(
                    cnt3, sampLength, sampleRate, frequency
                )
            }//end if
            if (cnt4 > 0) {
                `val` += -0.34 * pulseHelper(
                    cnt4, sampLength, sampleRate, frequency
                )
            }//end if

            shortBuffer!!.put(`val`.toShort())
            cnt1++
            cnt2++
            cnt3++
            cnt4++
        }//end for loop

        return 1
    }//end method waWaPulse
    //-------------------------------------------//

    private fun pulseHelper(cnt: Int, sampLength: Int, sampleRate: Float, freq:Float): Double {
        //The value of scale controls the rate of
        // decay - large scale, fast decay.
        var scale = (2 * cnt).toDouble()
        if (scale > sampLength) scale = sampLength.toDouble()
        val gain = 16000 * (sampLength - scale) / sampLength
        val time = (cnt / sampleRate).toDouble()
        //val freq = 499.0//an arbitrary freq
        val sinValue = (sin(2.0 * Math.PI * freq * time) +
                sin(2.0 * Math.PI * (freq / 1.8) * time) +
                sin(2.0 * Math.PI * (freq / 1.5) * time)) / 3.0
        return (gain * sinValue).toShort().toDouble()
    }//end waWaPulseHelper

    //-------------------------------------------//
}//end SynGen class
