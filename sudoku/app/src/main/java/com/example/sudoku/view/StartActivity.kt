package com.example.sudoku.view

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sudoku.OpenCVsudoku.RecognActivity
import com.example.sudoku.R
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : AppCompatActivity() {
    private val PERMISSION_CODE = 100
    var cellSt: String = ""
    val cret = arrayOf("✎ Create puzzle","\uD83C\uDFB2 Generate", "\uD83D\uDCF7 Recognize")
    val gen = arrayOf("Super easy","Easy", "Normal", "Hard")
    val load = arrayOf("✎ Last created sudoku", "\uD83C\uDFB2 Last generated sudoku")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        buttonOwn.alpha= 0.8F
        loadGame.alpha=0.8F
        ButtonStat.alpha=0.8F

        buttonOwn.setOnClickListener{
            val buildercr = AlertDialog.Builder(this)
            buildercr.setTitle("New sudoku")
                .setItems(cret) { dialogInterface: DialogInterface, i: Int ->
                    if(i == 0){
                        ownsud()
                    } else if (i == 1) {
                        val buildergen = AlertDialog.Builder(this)
                        buildergen.setTitle("Generate")
                            .setItems(gen) { dialogInterface: DialogInterface, i: Int ->
                                if(i == 0){
                                    seasysud()
                                } else if(i == 1) {
                                    easysud()
                                } else if(i == 2){
                                    midsud()
                                } else {
                                    hardsud()
                                }
                            }
                        buildergen.create().show()
                    } else {
                        recogsud()
                    }
                }
            buildercr.create().show()
        }

        loadGame.setOnClickListener {
            val builderl = AlertDialog.Builder(this)
            builderl.setTitle("Load sudoku")
                .setItems(load) { dialogInterface: DialogInterface, i: Int ->
                    if(i == 0){
                        ownsud()
                    } else if(i == 1){
                        var sPref: SharedPreferences = getSharedPreferences("genPrefs", MODE_PRIVATE)
                        cellSt = sPref.getString("genCells", "").toString()
                        if (cellSt == ""){
                            Toast.makeText(this, "There are no saved generated sudoku", Toast.LENGTH_SHORT).show()
                        } else {
                            lastsud()
                        }
                    }
                }
            builderl.create().show()
        }

        ButtonStat.setOnClickListener{
            val alert = AlertDialog.Builder(this)
            alert.setTitle("Statistics")
            val sPref: SharedPreferences = getSharedPreferences("genPrefs", MODE_PRIVATE)
            val rec1 = sPref.getInt("rectime1", 0)
            val rec2 = sPref.getInt("rectime2", 0)
            val rec3 = sPref.getInt("rectime3", 0)
            val rec5 = sPref.getInt("rectime5", 0)
            val wins1 = sPref.getInt("wins1", 0)
            val wins2 = sPref.getInt("wins2", 0)
            val wins3 = sPref.getInt("wins3", 0)
            val wins5 = sPref.getInt("wins5", 0)
            var rec1msg = "--.--"
            if (rec1 != 0) {
                val min = rec1 / 60
                val sec = rec1 % 60
                rec1msg = "$min min $sec sec"
            }
            var rec2msg = "--.--"
            if (rec2 != 0) {
                val min = rec2 / 60
                val sec = rec2 % 60
                rec2msg = "$min min $sec sec"
            }
            var rec3msg = "--.--"
            if (rec3 != 0) {
                val min = rec3 / 60
                val sec = rec3 % 60
                rec3msg = "$min min $sec sec"
            }
            var rec5msg = "--.--"
            if (rec5 != 0) {
                val min = rec5 / 60
                val sec = rec5 % 60
                rec5msg = "$min min $sec sec"
            }
            alert.setMessage("Easy: \nRecord time: $rec1msg\nWins: $wins1\n\nMedium: \nRecord time: $rec2msg\nWins: $wins2\n\nHard: \nRecord time: $rec3msg\nWins: $wins3\n\nSuper easy: \nRecord time: $rec5msg\nWins: $wins5")
            alert.setPositiveButton("Ok") { dialog, id -> }
            alert.create().show()
        }
        checkPerm(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            PERMISSION_CODE
        )
    }

    private fun checkPerm(permission: String, permission2: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(
                this,
                permission2
            ) == PackageManager.PERMISSION_DENIED) {
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
    private fun easysud(){
        val intent = Intent(this, GenActivity::class.java)
        intent.putExtra("dif", "1")
        startActivity(intent)
    }
    private fun midsud(){
        val intent = Intent(this, GenActivity::class.java)
        intent.putExtra("dif", "2")
        startActivity(intent)
    }
    private fun hardsud(){
        val intent = Intent(this, GenActivity::class.java)
        intent.putExtra("dif", "3")
        startActivity(intent)
    }
    private fun seasysud(){
        val intent = Intent(this, GenActivity::class.java)
        intent.putExtra("dif", "5")
        startActivity(intent)
    }
    private fun lastsud(){
        val intent = Intent(this, GenActivity::class.java)
        intent.putExtra("dif", "4")
        startActivity(intent)
    }
    private fun recogsud(){
        val intent = Intent(this, RecognActivity::class.java)
        startActivity(intent)
    }
}
