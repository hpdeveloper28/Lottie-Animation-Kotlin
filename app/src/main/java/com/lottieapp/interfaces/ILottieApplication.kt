package com.lottieapp.interfaces

/**
 * Created by hirenpatel on 30/10/17.
 */
import android.support.v4.util.Pair

internal interface ILottieApplication {
    fun startRecordingDroppedFrames()

    /**
     * Returns the number of frames dropped since starting
     */
    fun stopRecordingDroppedFrames(): Pair<Int, Long>
}
