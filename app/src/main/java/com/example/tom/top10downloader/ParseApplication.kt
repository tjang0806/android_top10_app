package com.example.tom.top10downloader

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class ParseApplication {
    private val TAG = "ParseApplication"
    // create arraylist containing FeedEntry(class is defined in MainActivity.kt)
    val applications = ArrayList<FeedEntry>()

    fun parse(xmlData : String):Boolean{
        Log.d(TAG, "parse called with $xmlData")

        // initialize local variables
        var status = true;
        var inEntry = false
        var textValue = ""

        try{
            // XmlPullParserFactory.newInstance() :
                //Creates a new instance of a PullParserFactory that can be used to create XML pull parsers.
                // The factory will always return instances of Android's built-in XmlPullParser and XmlSerializer.
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true

            //newPullParser
                // create new instance of a XML Pull Parser using the currently configured factory features
            val xpp = factory.newPullParser()

            // push text file into parser
            xpp.setInput(xmlData.reader())
            //xml event declaration
            var eventType = xpp.eventType

            var currentRecord = FeedEntry()

            //looping until the end of xml file
            while (eventType != XmlPullParser.END_DOCUMENT){

                //convert tag name to lowercase
                val tagName = xpp.name?.toLowerCase()

                //if start tag starts with entry stop and dig in to get further tag info
                when (eventType){
                    XmlPullParser.START_TAG -> {
                        Log.d(TAG, "parse: STarting tag for " + tagName)
                        if(tagName == "entry"){
                            inEntry = true
                        }
                    }

                    XmlPullParser.TEXT -> textValue = xpp.text

                    XmlPullParser.END_TAG -> {
                        Log.d(TAG, "parse: Ending tag for " + tagName)
                        if(inEntry){
                            when (tagName){
                                "entry" -> {
                                    applications.add(currentRecord)
                                    inEntry = false
                                    currentRecord = FeedEntry()
                                }

                                "name" -> currentRecord.name = textValue
                                "artist"-> currentRecord.artist = textValue
                                "releaseDate" -> currentRecord.releaseDate = textValue
                                "summary" -> currentRecord.summary = textValue
                                "image" -> currentRecord.imageURL = textValue
                            }
                        }
                    }
                }

                eventType = xpp.next()
            }

            for(app in applications){
                Log.d(TAG, "********************")
                Log.d(TAG, app.toString())
            }

        }catch (e: Exception){
            e.printStackTrace()
            status = false
        }
        return status
    }
}

