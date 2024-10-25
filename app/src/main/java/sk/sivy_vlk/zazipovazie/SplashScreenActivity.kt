package sk.sivy_vlk.zazipovazie

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_splash_screen)
//        val r = Runnable {
//            // Start your app main activity
//            val i = Intent(
//                this@SplashScreenActivity,
//                MainActivity::class.java
//            )
//            startActivity(i)
//            // close this activity
//            finish()
//        }
//        Handler(Looper.getMainLooper()).postDelayed(r, 3000)
        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
        finish()
    }
}