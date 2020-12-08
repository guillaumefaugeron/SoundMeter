package com.github.guillaumefaugeron.soundmeter
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 * or https://android-developers.googleblog.com/2020/07/decrease-startup-time-with-jetpack-app.html
 */
class SoundMeterContentProvider: ContentProvider() {
    override fun onCreate(): Boolean {
        Log.d("ContentProvider", "I'm running before everything (even Application.onCreate()) !")
        return true
    }

    // soundmeter://locations?last=10
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?) = 0
}
