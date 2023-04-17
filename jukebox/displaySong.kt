//Brenan Marenger
//This class displays an activity of the passed in song variables, displaying the image and title of the song
package com.example.jukebox

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class displaySong : AppCompatActivity() {

    companion object {
        fun newIntent(packageContext: Context?): Intent?{
            val i:Intent = Intent(packageContext!!, displaySong::class.java)

            return i
        }
    }

    var webthread = false
    var mIcon11: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)

        var songTitle = intent.getStringExtra("songTitle")
        var imageLink = intent.getStringExtra("imageLink")
        var musicLink = intent.getStringExtra("musicLink")

        var viewSongTitle: TextView = findViewById(R.id.songTitle)
        viewSongTitle.text = "$songTitle"

        var mImageView:ImageView = findViewById(R.id.picture)
        val executor = Executors.newSingleThreadExecutor()

        executor.execute {
            if (imageLink != null) {
                doInBackground(imageLink)
            }
        }
        while (!webthread);

        //Returning to main screen
        var backBtn: Button = findViewById(R.id.backBtn)
        backBtn.setOnClickListener{
            finish()
        }

        while (!webthread);
        onPostExecute (mIcon11!!,mImageView!!)
    }

    fun doInBackground(url: String)  {
        try {
            val infile = java.net.URL(url).openStream()
            mIcon11 = BitmapFactory.decodeStream(infile)
        } catch (e:Exception) {
            e.printStackTrace()
        }
        webthread = true
    }

    fun onPostExecute (result:Bitmap,bmImage:ImageView)
    {
        var width: Int = bmImage.context.resources.displayMetrics.widthPixels
        //val height: Int = bmImage.context.resources.displayMetrics.heightPixels
        var height = width * result.height / result.width
        if (height > bmImage.context.resources.displayMetrics.heightPixels) {
            height = bmImage.context.resources.displayMetrics.heightPixels
            width = height*result.width / result.height
        }
        val bitmap = Bitmap.createScaledBitmap(result, width, height, true)
        bmImage.setImageBitmap(bitmap)
        bmImage.setImageBitmap(result)

    }
}