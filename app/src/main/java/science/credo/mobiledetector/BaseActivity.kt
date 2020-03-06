package science.credo.mobiledetector

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    companion object{

        public fun intent(){

            println("========= $javaClass")
        }

    }

}