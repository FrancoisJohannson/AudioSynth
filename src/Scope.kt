import javax.swing.*
import java.awt.*
import java.nio.ByteBuffer
import java.nio.ShortBuffer
import java.awt.BorderLayout



class Scope internal constructor(
    private val buf: ByteArray,
    private val maxVol: Double,
    private val bytesPerFrame: Int,
    private val channels: Int,
    private val displayedChannel: Int

) : JPanel() {

    private var shortBuffer: ShortBuffer? = null

    private var visiblePartOfBuffer: Float = 0.01f
    private var offset: Int = 0

    private val volume = JSlider(JSlider.VERTICAL, 1/*minimum*/, 10000/*maximum*/, 10/*value*/)
    private val sliderOffset = JSlider(JSlider.HORIZONTAL, 1/*minimum*/, 10000/*maximum*/, 10/*value*/)

    private val view = View()

    init {
        //        this.setBorder(BorderFactory.createTitledBorder(
        //                BorderFactory.createEtchedBorder(), "Oscilloscope"));


        this.layout = BorderLayout()

        val byteBuffer = ByteBuffer.wrap(buf)
        shortBuffer = byteBuffer.asShortBuffer()



        view.background = Color.ORANGE
        view.preferredSize = Dimension(400, 400)
        this.add(view, BorderLayout.WEST)



        volume.toolTipText = "Volume of beep"
        volume.addChangeListener {
            this.visiblePartOfBuffer = volume.value.toFloat()/10000.0f
            view.update()
        }
        this.add(volume, BorderLayout.EAST)

        sliderOffset.toolTipText = "Offset"
        sliderOffset.addChangeListener {
            this.offset = sliderOffset.value
            view.update()
        }
        this.add(sliderOffset, BorderLayout.SOUTH)


    }

    public override fun paintComponent(g: Graphics) {

        view.preferredSize = Dimension(this.width-(volume.width*2), this.height)

    }


    inner class View : JPanel() {


        fun update() {

            repaint()

        }

        public override fun paintComponent(g: Graphics) {
            super.paintComponent(g)

            val halfheight = this.height / 2


            g.drawString("0", 0, halfheight)
            g.drawString("Buffer-size "+shortBuffer!!.capacity(), 20, 20)

            val xbuf  = IntArray(this.width)
            val ybuf  = IntArray(this.width)

            val yFactor = halfheight/maxVol
            val xFactor = (shortBuffer!!.capacity()*visiblePartOfBuffer)/this.width.toDouble()

            //var xFactor = 1.0
            //if ( channels == 2)
            //     xFactor = 2.0

            g.drawString("xFactor $xFactor", 20, 40)
            g.drawString("Frames "+shortBuffer!!.capacity()/channels, 20, 60)
            g.drawString("Channels $channels", 20, 80)
            g.drawString("yFactor $yFactor", 20, 100)

            println("******************************")

            var refactoredValue = 0

            for (x in 0 until this.width) {

                println("Original Value: " + shortBuffer!![x])

                xbuf[x] = x

                if ( channels == 1)
                    refactoredValue = ((shortBuffer!![(soundBufferXPos(x,xFactor))+offset]*yFactor).toInt())+halfheight

                if ( channels == 2) {
                    if ( displayedChannel== 1)
                        refactoredValue = ((shortBuffer!![(soundBufferXPos(x, xFactor) * 2)+(offset*2)] * yFactor).toInt()) + halfheight

                    if ( displayedChannel== 2)
                        refactoredValue = ((shortBuffer!![(soundBufferXPos(x, xFactor) * 2)+1+(offset*2)] * yFactor).toInt()) + halfheight

                }

                println("Refactored Value: " + refactoredValue)
                ybuf[x] = refactoredValue

            }

            g.drawPolyline(xbuf,ybuf,this.width)
            g.drawLine(0, halfheight, this.width, halfheight)

        }

        private fun soundBufferXPos(x:Int,xFactor:Double) : Int {
            var soundBufferPos = (x*(xFactor)).toInt()
            if ( soundBufferPos>=shortBuffer!!.capacity() ) {
                soundBufferPos %= shortBuffer!!.capacity()//
            }

            return soundBufferPos
        }

    }









}
