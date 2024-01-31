package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.VideoView
import android.net.Uri
import android.content.Intent
import android.widget.TextView

class GestureDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_display)

        val videoView: VideoView = findViewById(R.id.video_view)
        val practiceButton: Button = findViewById(R.id.practice_button)
        val gestureNameTextView: TextView = findViewById(R.id.gesture_name_textview)
        val replayButton: Button = findViewById(R.id.replay_button)

        // Get the selected gesture from the intent
        val selectedGesture = intent.getStringExtra("SELECTED_GESTURE")
        gestureNameTextView.text = selectedGesture // Set the gesture name
        val videoResId = getResourceIdForGesture(selectedGesture)
        if (videoResId != null) {
            val videoUri = Uri.parse("android.resource://$packageName/$videoResId")
            videoView.setVideoURI(videoUri)
            videoView.start()
        }

        replayButton.setOnClickListener {
            videoView.seekTo(0)
            videoView.start()
        }

        practiceButton.setOnClickListener {
            // Create an Intent to start GestureRecordActivity
            val intent = Intent(this@GestureDisplayActivity, GestureRecordActivity::class.java).apply {
                putExtra("SELECTED_GESTURE", selectedGesture)
            }
            startActivity(intent)
        }
    }

    private fun getResourceIdForGesture(gestureName: String?): Int? {
        return when (gestureName) {
            "Turn on lights" -> R.raw.turn_on_lights
            "Turn off lights" -> R.raw.turn_off_lights
            "Turn on fan" -> R.raw.turn_on_fan
            "Turn off fan" -> R.raw.turn_off_fan
            "Increase fan speed" -> R.raw.increase_fan_speed
            "Decrease fan speed" -> R.raw.decrease_fan_speed
            "Set thermostat to specified temperature" -> R.raw.set_thermostat
            "Gesture 0" -> R.raw.gesture_0
            "Gesture 1" -> R.raw.gesture_1
            "Gesture 2" -> R.raw.gesture_2
            "Gesture 3" -> R.raw.gesture_3
            "Gesture 4" -> R.raw.gesture_4
            "Gesture 5" -> R.raw.gesture_5
            "Gesture 6" -> R.raw.gesture_6
            "Gesture 7" -> R.raw.gesture_7
            "Gesture 8" -> R.raw.gesture_8
            "Gesture 9" -> R.raw.gesture_9
            else -> null
        }
    }
}
