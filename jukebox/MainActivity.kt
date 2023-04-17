//Brenan Marenger
//Android Programming PG6
//Date: 4/16/23
//This program will scrape an inputted URL and set up a Jukebox, allowing the user to click through songs to play music and show it's image
package com.example.jukebox

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

//http://philos.nmu.edu/weirdAl
class MainActivity : AppCompatActivity() {
    var context: Context? = null
    var webthread = false
    var tmpdir:String? = null

    var imageLinks: MutableList<String> = ArrayList()
    var musicLinks: MutableList<String> = ArrayList()
    var songTitle: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val executor = Executors.newSingleThreadExecutor()
        context = this@MainActivity
        tmpdir = context!!.cacheDir.toString()

        var mButton:Button = findViewById(R.id.submitBtn)
        var mJukebox:EditText = findViewById(R.id.jukeboxURL)
        mJukebox.setText("http://philos.nmu.edu/weirdAl")
        var mFeedback:TextView = findViewById(R.id.feedback)


        mButton.setOnClickListener{
            //Scrape Data
            if(mJukebox.text != null){
                executor.execute{
                    doInBackground(mJukebox)
                }

                while(!webthread);

                println("CHECK POINT 4")

                //hide buttons
                mButton.setVisibility(View.GONE)
                mJukebox.setVisibility(View.GONE)

                println("CHECK POINT 5")

                //make buttons from data
                createButtonList(songTitle, imageLinks, musicLinks)
                mFeedback.text = "Select a Track:"


                val infile:BufferedReader = BufferedReader(InputStreamReader(FileInputStream("$tmpdir/tempweb.html")))
                while(true){
                    val c:Int = infile.read()
                    if(c==-1) break
                    print(c.toChar())
                }
                infile.close()
            }


        }
    }

    //After getting information, this function will create a button for every song found and set up an activity and plays music on click
    fun createButtonList(songTitle: MutableList<String>, imageLinks: MutableList<String>, musicLinks: MutableList<String>){

        val linearLayout = findViewById(R.id.linearLayout) as LinearLayout

        val mp = MediaPlayer()

        for(i in songTitle){
            val button = Button(this)
            button.setText(i)
            var index = songTitle.indexOf(i)

            button.setOnClickListener{
                if(mp.isPlaying){
                    println("Duration: ${mp.duration}")
                    mp.pause()
                    mp.reset()
                }

                var x = Intent(this, displaySong::class.java)
                x?.putExtra("songTitle", i)
                x?.putExtra("imageLink", imageLinks.elementAt(index))
                x?.putExtra("musicLink", musicLinks.elementAt(index))

                mp.setDataSource (musicLinks.elementAt(index))
                mp.prepare()
                mp.start()

                startActivity(x)
            };
            linearLayout.addView(button);
        }
    }

    //Thread that scrapes and parses through given URL, finding the image and music links
    fun doInBackground(url:EditText){
        val obj = URL(url.text.toString())
        val con:HttpURLConnection = obj.openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        val response = con.responseCode

        var webpage:String = ""

        if(response == HttpURLConnection.HTTP_OK){
            var infile = BufferedReader (InputStreamReader(con.inputStream))
            val outfile = PrintWriter("$tmpdir/tempweb.html")
            while(true){
                val c:Int = infile.read()
                if(c==-1)break
                webpage += c.toChar()
                outfile.print(c.toChar())
            }
            infile.close()
            outfile.close()
        } else {
            println("NOT OKAY")
        }

        var reg = Regex("\"\\/weirdAl\\/.*?.\"")
        var regTitle = Regex("%20(.*?)\\.")
        var hrefMatches = reg.findAll(webpage)
        var counter = 0

        for(link in hrefMatches){
            var cleanedTitle = regTitle.find(link.value)
            var tempTitle = cleanedTitle!!.value
            tempTitle = tempTitle.replace(".", "")
            tempTitle = tempTitle.replace("%20", " ")

            if(counter % 2 == 0){
                songTitle.add(tempTitle)
            }
            counter++

            if(link.value.contains(".mp3")){
                println("MUSIC " + link.value)
                musicLinks.add("http://philos.nmu.edu" + link.value.toString().replace("\"", ""))
            } else if (link.value.contains(".jpg")){
                println("IMAGE " + link.value)
                imageLinks.add("http://philos.nmu.edu" + link.value.toString().replace("\"", ""))
            }
        }

        println("ALL THE IMAGE LINKS")
        imageLinks.forEach {
            link -> println(link)
        }
        println("ALL THE MUSIC LINKS")
        musicLinks.forEach {
                link -> println(link)
        }
        println("ALL TITLES")
        songTitle.forEach {
                title -> println(title)
        }

        webthread = true
    }
}