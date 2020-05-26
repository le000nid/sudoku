package com.example.sudoku.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.sudoku.OpenCVsudoku.RecognActivity
import com.example.sudoku.R
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

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
        ButtonScan.setOnClickListener{
            recogsud()
        }
    }

    fun ownsud() {
        val intent = Intent(this, OwnActivity::class.java)
        startActivity(intent)
    }
    fun easysud(){
        val intent = Intent(this, EasyActivity::class.java)
        startActivity(intent)
    }
    fun midsud(){
        val intent = Intent(this, MidActivity::class.java)
        startActivity(intent)
    }
    fun hardsud(){
        val intent = Intent(this, HardActivity::class.java)
        startActivity(intent)
    }
    fun recogsud(){
        val intent = Intent(this, RecognActivity::class.java)
        startActivity(intent)
    }
}
