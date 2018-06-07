package com.example.tom.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.properties.Delegates


class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary : String =""
    var imageURL : String = ""

    /*
    override fun toString(): String {
        return """
            name = $name
            artist = $artist
            releaseDate = $releaseDate
            imageURL = $imageURL

            """.trimIndent()
    }
    */
}

private const val TEXT_CONTENTS = "TextContent"

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var downloadData: DownloadData? = null

    private var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit = 10

    private var feedCashedUrl = "INVALIDATED"
    private val STATE_URL = "feedURL"
    private val STATE_LIMIT = "feedLimite"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState != null){
            feedUrl = savedInstanceState.getString(STATE_URL)
            feedLimit = savedInstanceState.getInt(STATE_LIMIT)
        }

        Log.d(TAG, "onCreate called")
        downloadUrl(feedUrl.format(feedLimit))

    }

    private fun downloadUrl(feedUrl: String){

        if(feedUrl != feedCashedUrl){
            //async function calling with URL(xml)
            downloadData = DownloadData(this,xmlListView)
            downloadData?.execute(feedUrl)
            feedCashedUrl = feedUrl
            Log.d(TAG, "onCreate: done")
        }
        else{
            Log.d(TAG, "URL not changed")
        }
    }

    //menu generator
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.feeds_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            R.id.mnuFree ->
                    feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.mnuPaid ->
                    feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.mnuSongs ->
                    feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"

            R.id.mnu10, R.id.mnu25-> {
                if(!item.isChecked){
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit to $feedLimit")
                }
                else{
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit unchanged")
                }
            }

            R.id.mnuRefresh -> feedCashedUrl = "INVALIDATE"

            else ->
                    return super.onOptionsItemSelected(item)
        }

        downloadUrl(feedUrl.format(feedLimit))
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_URL, feedUrl)
        outState.putInt(STATE_LIMIT, feedLimit)
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    companion object{
        //   param,progress,result
        private  class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {
            private val TAG= "DownloadData"

            var propContext : Context by Delegates.notNull()
            var propListView : ListView by Delegates.notNull()

            init {
                propContext = context
                propListView = listView
            }

            // after onPreExecute()
            // used to perform background computation
            override fun doInBackground(vararg url: String?): String {

                Log.d(TAG, "doInBackground: start with ${url[0]}")

                //download xml
                val rssFeed = downloadXML(url[0])

                if(rssFeed.isEmpty()){
                    Log.e(TAG, "doInBackground: Error downloading")
                }
                return rssFeed
            }

            // after doInBckGround(Params..)
            // This method is used to display any form of progress in the user interface while the back-
            // groundcomputation is still running
            override fun onPostExecute(result: String) {
                // result variable is generated by 'doInBackGround'
                // it is rssFeed
                super.onPostExecute(result)

                //instantiate ParseApplication to part xml data
                val parseApplications = ParseApplication()
                // parsing
                parseApplications.parse(result)
                Log.d(TAG, "onPostExecute: parameter is $result")


                //val arrayAdaptor = ArrayAdapter<FeedEntry>(propContext, R.layout.list_item, parseApplications.applications)
                //propListView.adapter = arrayAdaptor

                val feedAdopter = FeedAdapter(propContext, R.layout.list_record, parseApplications.applications)
                propListView.adapter = feedAdopter

            }

            // downloading xml data
            private fun downloadXML(urlPath: String?): String{

                // instead of long nasty below code, you can do it with single line
                return URL(urlPath).readText()
                /*
                val xmlResult = StringBuilder()

                try{
                    val url = URL(urlPath)
                    val connection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
                    val response = connection.responseCode
                    Log.d(TAG, "downloadXML: The response code was $response")

                    //java way
                    /*
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))

                    val inputBuffer = CharArray(500)
                    var charsRead = 0
                    while (charsRead >=0){
                        charsRead = reader.read(inputBuffer)
                        if (charsRead > 0){
                            xmlResult.append(String(inputBuffer,0,charsRead))
                        }
                    }
                    reader.close()*/
                    //better way
                    val stream = connection.inputStream
                    stream.buffered().reader().use{reader->
                        xmlResult.append(reader.readText())
                    }

                    Log.d(TAG, "Received ${xmlResult.length} bytes")
                    return xmlResult.toString()
                }

                //java way
                /*
                catch (e: MalformedURLException){
                    Log.e(TAG, "donwloadXML: Invalid URL ${e.message}")
                }
                catch (e: IOException){
                    Log.e(TAG, "donwloadXML: IO Exception reading data :  ${e.message}")
                }
                catch (e: Exception){
                    Log.e(TAG, "Unknown error:  ${e.message}")
                }
                */
                //better way
                catch (e: Exception){
                    val errorMessage: String = when (e){
                        is MalformedURLException -> "donwloadXML: Invalid URL ${e.message}"
                        is IOException -> "donwloadXML: IO Exception reading data :  ${e.message}"
                        is SecurityException -> {e.printStackTrace()
                            "downloadXML : Security Exception. Needs permisssion? ${e.message}"
                        }
                        else -> "Unknown error: ${e.message}"
                    }
                }
                return ""
                */
            }
        }



    }


}
