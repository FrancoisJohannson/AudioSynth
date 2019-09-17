import javax.swing.*
import java.awt.*

class Scope internal constructor(
    private val buf: ByteArray,
    private val maxVol: Double,
    private val bytesPerFrame: Int,
    private val channels: Int

) : JPanel() {


    init {
        //        this.setBorder(BorderFactory.createTitledBorder(
        //                BorderFactory.createEtchedBorder(), "Oscilloscope"));
        this.background = Color.ORANGE
        this.preferredSize = Dimension(400, 400)
    }


    fun update() {

        repaint()

    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val halfheight = this.height / 2


        g.drawString("0", 0, halfheight)
        g.drawString("Buffer-size "+buf.size, 20, 20)

        val xbuf  = IntArray(this.width)
        val ybuf  = IntArray(this.width)

        val yFactor = halfheight/maxVol
        // val xFactor = 1600/*0.1 Seconds*//this.width.toDouble()

        var xFactor = 2.0
        if ( channels == 2)
            xFactor = 4.0

        g.drawString("xFactor $xFactor", 20, 40)
        g.drawString("Frames "+buf.size/bytesPerFrame, 20, 60)
        g.drawString("Channels $channels", 20, 80)


        for (x in 0 until this.width) {

            xbuf[x] = x
            ybuf[x] = (buf[soundBufferXPos(x,xFactor)].toInt()*yFactor.toInt()).toInt()+halfheight

        }

        g.drawPolyline(xbuf,ybuf,this.width)
        g.drawLine(0, halfheight, this.width, halfheight)

    }



    private fun soundBufferXPos(x:Int,xFactor:Double) : Int {
        var soundBufferPos = (x*(xFactor)).toInt()
        if ( soundBufferPos>=buf.size ) {
            soundBufferPos %= buf.size
        }

        return soundBufferPos
    }



}
