package science.credo.credomobiledetektor.database

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.PrimaryKey

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)

class CachedMessage(endpoint: String, message: String, token: String?) {
    @PrimaryKey
    @AutoIncrement
    @JsonProperty("id")
    var id: Int = 0
    @JsonProperty("endpoint")
    val mEndpoint: String = endpoint
    @JsonProperty("message")
    val mMessage: String = message
    @JsonProperty("token")
    val mToken: String? = token

    constructor() : this("", "", "")
}