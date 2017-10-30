package com.lottieapp.activities


import android.content.Context
import android.graphics.Point
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import com.airbnb.lottie.LottieComposition
import com.lottieapp.utils.AnimatorListenerAdapter
import com.lottieapp.R
import com.lottieapp.interfaces.ILottieApplication
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val handler = Handler()
    private val systemAnimationsAreDisabled by lazy { getAnimationScale(this) == 0f }

    private val application: ILottieApplication
        get() = getApplication() as ILottieApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        animationView.addAnimatorListener(AnimatorListenerAdapter(
                onStart = { startRecordingDroppedFrames() },
                onEnd = {
                    recordDroppedFrames()
                    postUpdatePlayButtonText()
                    animationView.performanceTracker?.logRenderTimes()
                },
                onCancel = { postUpdatePlayButtonText() },
                onRepeat = {
                    animationView.performanceTracker?.logRenderTimes()
                    animationView.performanceTracker?.clearRenderTimes()
                    recordDroppedFrames()
                    startRecordingDroppedFrames()
                }
        ))

        playButton.setOnClickListener {

            if (animationView.isAnimating) {
                animationView.pauseAnimation()
                postUpdatePlayButtonText()
            } else {
                if (animationView.progress == 1f && !systemAnimationsAreDisabled) {
                    animationView.progress = 0f
                }
                animationView.resumeAnimation()
                postUpdatePlayButtonText()
            }
        }

        val assetName = "cloud_disconnection.json"
        LottieComposition.Factory.fromAssetFileName(this, assetName, { composition ->
            if (composition == null) {
                onLoadError()
            } else {
                setComposition(composition, assetName)
            }
        })
    }


    private fun startRecordingDroppedFrames() = application.startRecordingDroppedFrames()

    private fun recordDroppedFrames() {
        val droppedFrames = application.stopRecordingDroppedFrames()
        Log.d(TAG, "Dropped frames: " + droppedFrames.first)
    }


    private fun postUpdatePlayButtonText() = handler.post {
        if (playButton != null) {
            updatePlayButtonText()
        }
    }

    private fun updatePlayButtonText() {
        playButton.isActivated = animationView.isAnimating
    }

    private fun getAnimationScale(context: Context): Float {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.Global.getFloat(context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        } else {

            @Suppress("DEPRECATION")
            Settings.System.getFloat(context.contentResolver,
                    Settings.System.ANIMATOR_DURATION_SCALE, 1.0f)
        }
    }

    private fun onLoadError() = Log.e(TAG, "Failed to load animation")

    private fun setComposition(composition: LottieComposition, name: String) {
        if (composition.hasImages() && TextUtils.isEmpty(animationView.imageAssetsFolder)) {
            Log.e(TAG, "This animation has images and no image folder was set")
            return
        }
        animationView.setComposition(composition)
        // make sure the animation doesn't start larger than the screen
        val screenSize = Point()
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getSize(screenSize)
        val scale = screenSize.x / composition.bounds.width().toFloat()
        animationView.scale = minOf(scale, 1f)
    }
}
