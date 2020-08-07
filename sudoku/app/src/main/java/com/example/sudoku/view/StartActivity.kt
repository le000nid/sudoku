package com.example.sudoku.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sudoku.OpenCVsudoku.RecognActivity
import com.example.sudoku.R
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : AppCompatActivity() {
    private val PERMISSION_CODE = 100
    var cellSt: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        var sPref: SharedPreferences = getSharedPreferences("genPrefs", MODE_PRIVATE)
        cellSt = sPref.getString("genCells", "").toString()
        if (cellSt == ""){
            lastGame.alpha = 0.3F
            lastGame.isEnabled = false
        }

        buttonOwn.setOnClickListener{
            ownsud()
        }
        buttonGen1.setOnClickListener{
            easysud()
        }
        buttonGen2.setOnClickListener{
            midsud()
        }
        buttonGen3.setOnClickListener{
            hardsud()
        }
        lastGame.setOnClickListener{
            lastsud()
        }
        ButtonScan.setOnClickListener{
            recogsud()
        }
        checkPerm(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, PERMISSION_CODE)
    }

    override fun onRestart() {
        super.onRestart()
        finish()
        startActivity(intent)
    }

    private fun checkPerm(permission: String, permission2: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(this, permission2) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission, permission2), requestCode)
        } else if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else if (ContextCompat.checkSelfPermission(this, permission2) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission2), requestCode)
        }
    }

    private fun ownsud() {
        val intent = Intent(this, OwnActivity::class.java)
        startActivity(intent)
    }
    fun easysud(){
        val intent = Intent(this, GenActivity::class.java)
        intent.putExtra("dif", "1")
        startActivity(intent)
    }
    fun midsud(){
        val intent = Intent(this, GenActivity::class.java)
        intent.putExtra("dif", "2")
        startActivity(intent)
    }
    fun hardsud(){
        val intent = Intent(this, GenActivity::class.java)
        intent.putExtra("dif", "3")
        startActivity(intent)
    }
    fun lastsud(){
        val intent = Intent(this, GenActivity::class.java)
        intent.putExtra("dif", "4")
        startActivity(intent)
    }
    fun recogsud(){
        val intent = Intent(this, RecognActivity::class.java)
        startActivity(intent)
    }
}
