package science.credo.mobiledetector2.network

import com.google.gson.Gson
import com.google.gson.JsonObject


data class Parameter(
    val name: String,
    val value: String
) {

    constructor(
        name: String,
        any: Any
    ) : this(name, Gson().toJson(any))

    fun toJsonObject(): JsonObject {
        val json = JsonObject()
        json.addProperty(name, value)
        return json
    }

    companion object{
        fun toJsonObject(vararg params :Parameter) : JsonObject {
            val json = JsonObject()
            for(p in params){
                json.addProperty(p.name, p.value)

            }
            return json
        }
    }
}