/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import org.tensorflow.lite.task.gms.vision.detector.Detection
import java.util.LinkedList
import kotlin.math.max


class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<Detection> = LinkedList<Detection>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var buttonPaint = Paint()
    private var buttonTextPaint = Paint()

    //the size of this OverlayView
    private var highestScore = 0f
    private lateinit var highestResult: Detection



    private var scaleFactor: Float = 1f

    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        buttonTextPaint.color = Color.WHITE
        buttonTextPaint.style = Paint.Style.FILL
        buttonTextPaint.textSize = 100f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE

        buttonPaint.color = ContextCompat.getColor(context!!, R.color.button_color)
        buttonPaint.strokeWidth = 8F
        buttonPaint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                val measuredY = this.measuredHeight.toFloat()
                val buttonHeight =  measuredY * 0.2
                val inBounds = event.y >  (measuredY - buttonHeight)
                if (inBounds){
                    Log.d("TOUCH", "BUTTON TOUCHED: ${highestResult.categories[0].label}")
                }

                return true
            }

//            MotionEvent.ACTION_UP -> {
//               // Log.d("TOUCH", "UP: $event.x : $event.y")
//                val xMe = this.measuredWidth
//                val yMe = this.measuredHeight
//                Log.d("TOUCH", "UP: $xMe : $yMe.y")
//
//                // For this particular app we want the main work to happen
//                // on ACTION_UP rather than ACTION_DOWN. So this is where
//                // we will call performClick().
//               // performClick()
//                return true
//            }
        }
        return false
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (results.size < 1) return
        //assume the first one is the highestScore
         highestScore = results[0].categories[0].score
         highestResult = results[0]
        for (nC in 0 until results.size) {

            //find the highestResult
            if (nC > 0 && results[nC].categories[0].score > highestScore) {
                highestScore = results[nC].categories[0].score
                highestResult = results[nC]
            }


            val result = results[nC]
            val boundingBox = result.boundingBox

            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            // Draw bounding box around detected objects
            val drawableRect = RectF(left, top, right, bottom)
            canvas.drawRect(drawableRect, boxPaint)

            // Create text to display alongside detected objects
            val drawableText =
                result.categories[0].label + " " +
                        String.format("%.2f", result.categories[0].score)

            // Draw rect behind display text
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                left,
                top,
                left + textWidth + Companion.BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + Companion.BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )

            // Draw text for detected object
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
        }//end for loop


            val measuredX = this.measuredWidth.toFloat()
            val measuredY = this.measuredHeight.toFloat()
            val buttonHeight =  measuredY * 0.2
            val buttonRect = RectF(0f,   (measuredY - buttonHeight).toFloat(),  measuredX, measuredY)
            canvas.drawRect(buttonRect, buttonPaint)
            val label = highestResult.categories[0].label
            canvas.drawText(label.toUpperCase(), (measuredX / 2), measuredY - (buttonHeight / 2).toFloat(), buttonTextPaint)


    }

    fun setResults(
      detectionResults: MutableList<Detection>,
      imageHeight: Int,
      imageWidth: Int,
    ) {
        results = detectionResults

        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.
        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}
