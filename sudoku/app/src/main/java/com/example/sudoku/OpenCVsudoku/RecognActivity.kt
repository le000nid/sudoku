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
import com.example.sudoku.view.OwnActivity
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class RecognActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private var mOpenCvCameraView: CameraBridgeViewBase ? = null

    var cropped: Mat? = null

    var tessBaseApi: TessBaseAPI? = null

    val DATA_PATH: String = android.os.Environment.getExternalStorageDirectory().toString().toString() +
            "/Image2Text/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_recogn)
        mOpenCvCameraView = findViewById<CameraBridgeViewBase>(R.id.view)
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
                    sortedPoints[1] = Point(points[i].x, points[i].y)
                } else if (points[i].x > x && points[i].y < y) {
                    sortedPoints[3] = Point(points[i].x, points[i].y)
                } else if (points[i].x < x && points[i].y > y) {
                    sortedPoints[0] = Point(points[i].x, points[i].y)
                } else if (points[i].x > x && points[i].y > y) {
                    sortedPoints[2] = Point(points[i].x, points[i].y)
                }
            }
            var nulled: Boolean = false
            for(i in 0..3){
                if (sortedPoints[i]==null){
                    nulled=true
                }
            }
            if(!nulled) {
                val src = MatOfPoint2f(
                    sortedPoints[0],
                    sortedPoints[1],
                    sortedPoints[2],
                    sortedPoints[3]
                )
                val dst = MatOfPoint2f(
                    Point(0.0, 0.0),
                    Point(500.0 - 1, 0.0),
                    Point(0.0, 500.0 - 1),
                    Point(500.0 - 1, 500.0 - 1)
                )
                val warpMat = Imgproc.getPerspectiveTransform(src, dst)
                Imgproc.warpPerspective(displayMat, cropped, warpMat, Size(500.0, 500.0))
            }
        }
        return displayMat
    }
    fun capture(v: View?) {
        val dir = File(DATA_PATH + "tessdata")
        dir.mkdirs()

        if (!File(DATA_PATH + "tessdata/eng.traineddata").exists()) {
            try {
                val assetManager = assets
                val `in`: InputStream = assetManager.open("eng.traineddata")
                val out: OutputStream = FileOutputStream(
                    DATA_PATH
                            + "tessdata/eng.traineddata"
                )
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
            }
            catch (e: IOException) {
            }
        }
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
        val grayMat = Mat()
        Imgproc.cvtColor(output, grayMat, Imgproc.COLOR_BGR2GRAY)
        var thresh = Mat()
        Imgproc.adaptiveThreshold(
            grayMat,
            thresh,
            255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY_INV,
            57,
            5.0
        )

        var cnts: List<MatOfPoint> = ArrayList()
        val hier = Mat()
        Imgproc.findContours(thresh, cnts, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        hier.release()
//        for (i in cnts){
//            val area = Imgproc.contourArea(i)
//            if(area<1000){
//                Imgproc.drawContours(thresh, cnts, -1, Scalar(0.0, 0.0, 0.0), -1)
//            }
//        }
        var cntss: MutableList<MatOfPoint> = ArrayList()
        for (i in cnts) {
            if(Imgproc.contourArea(i)<1000){
                cntss.add(i)
            }
        }
        Imgproc.drawContours(thresh, cntss, -1, Scalar(0.0, 0.0, 0.0), -1)


        val vertical_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(1.0, 5.0))
        Imgproc.morphologyEx(thresh, thresh, Imgproc.MORPH_CLOSE, vertical_kernel, Point(-1.0,-1.0), 9)
        val horizantal_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 1.0))
        Imgproc.morphologyEx(thresh, thresh, Imgproc.MORPH_CLOSE, horizantal_kernel, Point(-1.0,-1.0), 9)

        val invert = Mat(thresh.rows(),thresh.cols(), thresh.type())
        Core.bitwise_not(thresh, invert)
        var cntsinv: List<MatOfPoint> = ArrayList()
        Imgproc.findContours(invert, cntsinv, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        val boundingRects: MutableList<Rect> = ArrayList()
        for(i in cntsinv.indices){
            boundingRects.add(Imgproc.boundingRect(cntsinv[i]))
        }

        boundingRects.sortBy { it.y }
        val rectRow = mutableListOf<Rect>()
        var cells = listOf<Rect>()
        var count = 0
        for(i in boundingRects.indices){
            rectRow.add(boundingRects[i])
            count+=1
            if (count==9){
                rectRow.sortBy { it.x }
                cells += rectRow
                rectRow.clear()
                count = 0
            }
        }

        var sudosa = IntArray(81) { 0 }
        for(i in cells.indices) {
            val digit_cropped = Mat(output, cells[i])
            val grayMat = Mat()
            Imgproc.cvtColor(digit_cropped,grayMat,Imgproc.COLOR_BGR2GRAY)
            val blurMat = Mat()
            Imgproc.GaussianBlur(grayMat, blurMat, Size(3.0, 3.0), 0.0)
            val thresh = Mat()
            Imgproc.adaptiveThreshold(blurMat, thresh, 255.0,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV,75,10.0)
            val digit_bitmap: Bitmap? = Bitmap.createBitmap(
                digit_cropped.cols(),
                digit_cropped.rows(),
                Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(thresh, digit_bitmap)
            tessBaseApi!!.setImage(digit_bitmap)
            val recognizedText:String = tessBaseApi!!.utF8Text
            if (recognizedText.length == 1) {
                sudosa[i] = Integer.valueOf(recognizedText)
            }
            tessBaseApi!!.clear()
        }
        tessBaseApi!!.end()
        val intent = Intent(this, OwnActivity::class.java)
        intent.putExtra("ar", sudosa)
        startActivity(intent)
    }
}