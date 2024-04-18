package kr.ac.hansung.mahjongstarter

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var last_x: Float = 0.0f
    private var last_y: Float = 0.0f
    private var last_z: Float = 0.0f
    private val shakeThreshold: Float = 800.0f // 흔들림 감지 임계값

    private var buttonPressTimes = mutableMapOf<ImageView, Long>()
    private var isButtonPressed = mutableMapOf<ImageView, Boolean>()
    private lateinit var blocks: List<ImageView>
    private lateinit var backs: List<ImageView>
    private var imageSet = false
    private lateinit var vibrator: Vibrator

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val block1 = findViewById<ImageView>(R.id.block1)
        val block2 = findViewById<ImageView>(R.id.block2)
        val block3 = findViewById<ImageView>(R.id.block3)
        val block4 = findViewById<ImageView>(R.id.block4)
        val back1 = findViewById<ImageView>(R.id.back1)
        val back2 = findViewById<ImageView>(R.id.back2)
        val back3 = findViewById<ImageView>(R.id.back3)
        val back4 = findViewById<ImageView>(R.id.back4)

        // val testButton = findViewById<Button>(R.id.testButton);

        blocks = listOf(block1, block2, block3, block4)
        backs = listOf(back1, back2, back3, back4)
        backs.forEach { it.visibility = ImageView.INVISIBLE }

        blocks.forEach { block ->
            buttonPressTimes[block] = 0L
            isButtonPressed[block] = false

            block.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        buttonPressTimes[v as ImageView] = System.currentTimeMillis()
                        isButtonPressed[v] = true
                    }
                    MotionEvent.ACTION_UP -> {
                        isButtonPressed[v as ImageView] = false
                    }
                }

                if (!imageSet && isButtonPressed.all { it.value }) {
                    val allPressedFor2Seconds = buttonPressTimes.all {
                        System.currentTimeMillis() - it.value >= 2000
                    }
                    if (allPressedFor2Seconds) {
                        setRandomImages()
                    }
                }
                true
            }
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

//        testButton.setOnClickListener {
//            if (!imageSet) {
//                setRandomImages()
//                imageSet = true
//            }
//        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val curTime = System.currentTimeMillis()
        if ((curTime - lastUpdate) > 100) {
            val diffTime = (curTime - lastUpdate)
            lastUpdate = curTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

            if (speed > shakeThreshold) {
                resetBlocks()
            }

            last_x = x
            last_y = y
            last_z = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun resetBlocks() {
        blocks.forEach { block ->
            block.setImageResource(R.drawable.block_back)
        }
        backs.forEach { it.visibility = ImageView.INVISIBLE }
        imageSet = false
    }

    private fun setRandomImages() {
        val images = listOf(
            R.drawable.block_east,
            R.drawable.block_west,
            R.drawable.block_south,
            R.drawable.block_north
        )

        val shuffledImages = images.shuffled()

        blocks.forEachIndexed { index, block ->
            block.setImageResource(shuffledImages[index])
        }

        val randomBack = backs[Random.nextInt(backs.size)]
        randomBack.visibility = ImageView.VISIBLE

        imageSet = true

        if (vibrator.hasVibrator()) {
            vibrator.vibrate(100)
        }
    }
}