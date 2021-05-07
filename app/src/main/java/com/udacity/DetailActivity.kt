package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.transition.Transition
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val fileName = intent.getStringExtra(DOWNLOAD_URL_KEY).toString()
        val status = intent.getStringExtra(DOWNLOAD_STATUS_KEY).toString()

        fileNameValue.text = fileName
        statusValue.text = status

        val motionLayout: MotionLayout = findViewById(R.id.detail_motion_layout)

        // Not sure how to get the failed state from user testing
        // but I swapped the transitions being used here and confirmed they work
        // so in a fail state the failed transition will in fact execute
        if (status == "Success") {
            motionLayout.setTransition(R.id.success_transition)
        } else {
            motionLayout.setTransition(R.id.failed_transition)
        }

        okButton.setOnClickListener {
            val int = Intent(applicationContext, MainActivity::class.java)
            startActivity(int)
        }
    }

}
