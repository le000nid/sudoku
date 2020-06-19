package com.example.sudoku.OpenCVsudoku

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.sudoku.R
import com.example.sudoku.view.PlayRecognActivity
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*
import kotlin.collections.ArrayList


class RecognActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private var mOpenCvCameraView: PortraitCameraView ? = null

    private lateinit var play: PlayRecognActivity

    var cropped: Mat? = null

    var tessBaseApi: TessBaseAPI? = null

    val DATA_PATH: String = "/mnt/sdcard/tesseract"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_recogn)
        mOpenCvCameraView = findViewById<PortraitCameraView>(R.id.view)
        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView!!.setCvCameraViewListener(this)
    }

    override fun onResume() {
        super.onResume()
        OpenCVLoader.initDebug()
        mOpenCvCameraView!!.enableView()
    }

    override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        val grayMat = inputFrame.gray()
        val blurMat = Mat()
        Imgproc.GaussianBlur(grayMat, blurMat, Size(5.0, 5.0), 0.0)
        val thresh = Mat()
        Imgproc.adaptiveThreshold(blurMat, thresh, 255.0,1,1,11,2.0)

        val contours: List<MatOfPoint> = ArrayList()
        val hier = Mat()
        Imgproc.findContours(thresh, contours, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        hier.release()

        var biggest = MatOfPoint2f()
        var max_area = 0.0
        for (i in contours) {
            val area = Imgproc.contourArea(i)
            if (area > 100) {
                val m = MatOfPoint2f(*i.toArray())
                val peri = Imgproc.arcLength(m, true)
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(m, approx, 0.02 * peri, true)
                if (area > max_area && approx.total() == 4L) {
                    biggest = approx
                    max_area = area
                }
            }
        }
        val displayMat = inputFrame.rgba()
        val points = biggest.toArray()
        cropped = Mat()
        if (points.size >= 4) {
            Imgproc.line(
                displayMat,
                Point(points[0].x, points[0].y),
                Point(points[1].x, points[1].y),
                Scalar(0.0, 255.0, 0.0),
                2
            )
            Imgproc.line(
                displayMat,
                Point(points[1].x, points[1].y),
                Point(points[2].x, points[2].y),
                Scalar(0.0, 255.0, 0.0),
                2
            )
            Imgproc.line(
                displayMat,
                Point(points[2].x, points[2].y),
                Point(points[3].x, points[3].y),
                Scalar(0.0, 255.0, 0.0),
                2
            )
            Imgproc.line(
                displayMat,
                Point(points[3].x, points[3].y),
                Point(points[0].x, points[0].y),
                Scalar(0.0, 255.0, 0.0),
                2
            )
            // crop the image
            val moment = Imgproc.moments(biggest)
            val x = (moment._m10 / moment._m00).toInt()
            val y = (moment._m01 / moment._m00).toInt()

            val sortedPoints = arrayOfNulls<Point>(4)

            for (i in 0..3) {
                if (points[i].x < x && points[i].y < y) {
                    sortedPoints[0] = Point(points[i].x, points[i].y)
                } else if (points[i].x > x && points[i].y < y) {
                    sortedPoints[1] = Point(points[i].x, points[i].y)
                } else if (points[i].x < x && points[i].y > y) {
                    sortedPoints[2] = Point(points[i].x, points[i].y)
                } else if (points[i].x > x && points[i].y > y) {
                    sortedPoints[3] = Point(points[i].x, points[i].y)
                }
            }

            val src = MatOfPoint2f(
                points[1],
                points[0],
                points[2],
                points[3]
            )
            val dst = MatOfPoint2f(
                Point(0.0, 0.0),
                Point(500.0-1, 0.0),
                Point(0.0, 500.0-1),
                Point(500.0-1, 500.0-1)
            )
            val warpMat = Imgproc.getPerspectiveTransform(src, dst)
            Imgproc.warpPerspective(displayMat, cropped, warpMat, Size(500.0, 500.0))
        }
        return displayMat
    }
    fun capture(v: View?) {

        if (cropped!!.width() < 1 || cropped!!.height() < 1) {
            finish()
        }
        mOpenCvCameraView?.visibility = View.GONE

        // initialize the TessBase
        tessBaseApi = TessBaseAPI()
        tessBaseApi!!.init(DATA_PATH,"eng")
        tessBaseApi!!.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK)
        tessBaseApi!!.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "123456789")
        tessBaseApi!!.setVariable("classify_bin_numeric_mode", "1")

        val output: Mat = cropped!!.clone()

        val SUDOKU_SIZE = 9
        val IMAGE_WIDTH: Int = output.width()
        val IMAGE_HEIGHT: Int = output.height()
        val PADDING = IMAGE_WIDTH / 25.toDouble()
        val HSIZE = IMAGE_HEIGHT / SUDOKU_SIZE
        val WSIZE = IMAGE_WIDTH / SUDOKU_SIZE

        val sudosa = Array(SUDOKU_SIZE) { IntArray(SUDOKU_SIZE) }

        var y: Int = 0
        var iy: Int = 0
        while (y < IMAGE_HEIGHT - HSIZE) {
            var x: Int = 0
            var ix: Int = 0
            while (x < IMAGE_WIDTH - WSIZE) {
                sudosa[iy][ix] = 0
                val cx: Int = x + WSIZE / 2
                val cy: Int = y + HSIZE / 2
                val p1: Point = Point(cx + PADDING, cy + PADDING)
                val p2: Point = Point(cx - PADDING, cy - PADDING)
                val R: Rect = Rect(p1, p2)
                val digit_cropped: Mat = Mat(output, R)
                val grayMat = Mat()
                Imgproc.cvtColor(digit_cropped,grayMat,Imgproc.COLOR_BGR2GRAY)
                val blurMat = Mat()
                Imgproc.GaussianBlur(grayMat, blurMat, Size(3.0, 3.0), 0.0)
                val thresh = Mat()
                Imgproc.adaptiveThreshold(blurMat, thresh, 255.0,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,75,10.0)
                Imgproc.rectangle(output, p1, p2, Scalar(0.0, 0.0, 0.0))
                val digit_bitmap: Bitmap? = Bitmap.createBitmap(
                    digit_cropped.cols(),
                    digit_cropped.rows(),
                    Bitmap.Config.ARGB_8888
                )
                Utils.matToBitmap(thresh, digit_bitmap)
                tessBaseApi!!.setImage(digit_bitmap)
                val recognizedText:String = tessBaseApi!!.utF8Text
                if (recognizedText.length == 1) {
                    sudosa[iy][ix] = Integer.valueOf(recognizedText)
                }
                tessBaseApi!!.clear()
                x += WSIZE
                ix++
            }
            Log.i("testing", "" + Arrays.toString(sudosa.get(iy)))
            y += HSIZE
            iy++
        }

        tessBaseApi!!.end()

        val temp = IntArray(81)

        for (i in 0..80) {
            temp[i] = sudosa[i/9][i%9]
        }
        val intent = Intent(this, PlayRecognActivity::class.java)
        intent.putExtra("ar", temp)
        startActivity(intent)
    }
}