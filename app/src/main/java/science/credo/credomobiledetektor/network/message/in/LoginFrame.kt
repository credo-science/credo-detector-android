package science.credo.credomobiledetektor.network.message.`in`

import science.credo.credomobiledetektor.network.message.FrameInHeader

/**
 * Created by poznan on 28/09/2017.
 */

/*{"header":{"server":"0.90","frame_type":"login","protocol":"1.0","time_stamp":1506606884264},
    "body":{"user_info":{"email":"piotr_poznanski@o2.pl ","name":"moi","key":"FKPUJ5EDZ","team":"no team"}}}*/

class LoginFrame {
    class UserInfo {var name: String="" ; var email: String=""; var key: String=""; var team: String=""}
    class Body {var user_info: UserInfo = UserInfo() }
    var header: FrameInHeader = FrameInHeader()
    var body: Body = Body()
}