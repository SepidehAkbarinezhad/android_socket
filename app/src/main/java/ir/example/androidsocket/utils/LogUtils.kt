package ir.example.androidsocket.utils

import android.util.Log

fun serverLog(message : String , tag : String = "serverTag"){
    Log.d(tag , message)
}

fun clientLog(message : String , tag : String = "clientTag"){
    Log.d(tag , message)
}
