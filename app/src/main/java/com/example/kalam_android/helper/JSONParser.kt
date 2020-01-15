package com.example.kalam_android.helper

import android.content.Context
import com.example.kalam_android.util.Debugger
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset


object JSONParser {

    fun loadJSONFromAsset(pContext: Context, filename: String): String {
        var json = ""
        try {
            val inputStream = pContext.assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val charset: Charset = Charsets.UTF_8
            json = String(buffer, charset)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return json
    }


    fun getCountriesFromJSON(json: String, pContext: Context): ArrayList<String> {
        val jsonArray = JSONArray(json)
        val countriesList = ArrayList<String>()
        for (i in 0 until jsonArray.length()) {
            val jObj: JSONObject = jsonArray.getJSONObject(i)
            Debugger.e("JSON_FILE_DATA", "JSON: $jObj")
            countriesList.add(jObj.getString("name"))
        }
        return countriesList
    }

    fun getCitiesFromJSON(json: String, pContext: Context, query: String): ArrayList<String> {
        val jsonArray = JSONArray(json)
        val citiesList = ArrayList<String>()
        for (i in 0 until jsonArray.length()) {
            val jObj: JSONObject = jsonArray.getJSONObject(i)
            Debugger.d("JSON_FILE_DATA", "JSON: $jObj")
            if (jObj.getString("country") == query) {
                citiesList.add(jObj.getString("name"))
            }
        }
        return citiesList
    }
}


/*var temp = ArrayList<String>()
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                countries.forEach { obj ->
                    Debugger.d("OH, No!", "obj: $obj")
                    if (obj.contains(s.toString())) {
                        Debugger.d("OH, No!", "str: ${s.toString()}")
                        temp.add(obj)
                    }
                }
                dialog.findViewById<RecyclerView>(R.id.rvList).adapter =
                    LocationAdapter(this@CreateProfileActivity, temp)
            }
        })*/