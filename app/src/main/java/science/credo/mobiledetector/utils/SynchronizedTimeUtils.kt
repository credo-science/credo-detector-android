package science.credo.mobiledetector.utils

import android.os.SystemClock
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object SynchronizedTimeUtils{

    fun getTimestamp(): Long {
        return if(SntpClient.ntpTime != 0L){
            val d = SystemClock.elapsedRealtime()  - SntpClient.ntpTimeReference
            SntpClient.ntpTime + d
        }else{
            System.currentTimeMillis()
        }
    }
    fun getTimestamp(elapsedRealtime : Long): Long {
        return if(SntpClient.ntpTime != 0L){
            val d = elapsedRealtime  - SntpClient.ntpTimeReference
            SntpClient.ntpTime + d
        }else{
            System.currentTimeMillis()
        }
    }

    object SntpClient {

        // system time computed from NTP server response
        /**
         * Returns the time computed from the NTP transaction.
         *
         * @return time value computed from NTP server response.
         */
        var ntpTime: Long = 0
            private set

        // value of SystemClock.elapsedRealtime() corresponding to mNtpTime
        /**
         * Returns the reference clock value (value of SystemClock.elapsedRealtime())
         * corresponding to the NTP time.
         *
         * @return reference clock corresponding to the NTP time.
         */
        var ntpTimeReference: Long = 0
            private set

        // round trip time in milliseconds
        /**
         * Returns the round trip time of the NTP transaction
         *
         * @return round trip time in milliseconds.
         */
        var roundTripTime: Long = 0
            private set

        /**
         * Sends an SNTP request to the given host and processes the response.
         *
         * @param host host name of the server.
         * @param timeout network timeout in milliseconds.
         * @return true if the transaction was successful.
         */
        fun requestTime(host: String, timeout: Int): Boolean {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                socket.soTimeout = timeout
                val address = InetAddress.getByName(host)
                val buffer = ByteArray(NTP_PACKET_SIZE)
                val request = DatagramPacket(buffer, buffer.size, address, NTP_PORT)

                // set mode = 3 (client) and version = 3
                // mode is in low 3 bits of first byte
                // version is in bits 3-5 of first byte
                buffer[0] = (NTP_MODE_CLIENT or (NTP_VERSION shl 3)).toByte()

                // get current time and write it to the request packet
                val requestTime = System.currentTimeMillis()
                val requestTicks = SystemClock.elapsedRealtime()
                writeTimeStamp(buffer, TRANSMIT_TIME_OFFSET, requestTime)

                socket.send(request)

                // read the response
                val response = DatagramPacket(buffer, buffer.size)
                socket.receive(response)
                val responseTicks = SystemClock.elapsedRealtime()
                val responseTime = requestTime + (responseTicks - requestTicks)

                // extract the results
                val originateTime = readTimeStamp(buffer, ORIGINATE_TIME_OFFSET)
                val receiveTime = readTimeStamp(buffer, RECEIVE_TIME_OFFSET)
                val transmitTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET)
                val roundTripTime = responseTicks - requestTicks - (transmitTime - receiveTime)
                // receiveTime = originateTime + transit + skew
                // responseTime = transmitTime + transit - skew
                // clockOffset = ((receiveTime - originateTime) + (transmitTime - responseTime))/2
                //             = ((originateTime + transit + skew - originateTime) +
                //                (transmitTime - (transmitTime + transit - skew)))/2
                //             = ((transit + skew) + (transmitTime - transmitTime - transit + skew))/2
                //             = (transit + skew - transit + skew)/2
                //             = (2 * skew)/2 = skew
                val clockOffset = (receiveTime - originateTime + (transmitTime - responseTime)) / 2
                // if (false) Log.d(TAG, "round trip: " + roundTripTime + " ms");
                // if (false) Log.d(TAG, "clock offset: " + clockOffset + " ms");

                // save our results - use the times on this side of the network latency
                // (response rather than request time)
                ntpTime = responseTime + clockOffset
                ntpTimeReference = responseTicks
                this.roundTripTime = roundTripTime
            } catch (e: Exception) {
                if (false) Log.d(TAG, "request time failed: $e")
                return false
            } finally {
                socket?.close()
            }

            return true
        }

        /**
         * Reads an unsigned 32 bit big endian number from the given offset in the buffer.
         */
        private fun read32(buffer: ByteArray, offset: Int): Long {
            val b0 = buffer[offset]
            val b1 = buffer[offset + 1]
            val b2 = buffer[offset + 2]
            val b3 = buffer[offset + 3]

            // convert signed bytes to unsigned values
            val i0 = if (b0.toLong() and 0x80L == 0x80L) (b0.toLong() and 0x7FL) + 0x80L else b0.toLong()
            val i1 = if (b1.toLong() and 0x80L == 0x80L) (b1.toLong() and 0x7FL) + 0x80L else b1.toLong()
            val i2 = if (b2.toLong() and 0x80L == 0x80L) (b2.toLong() and 0x7FL) + 0x80L else b2.toLong()
            val i3 = if (b3.toLong() and 0x80L == 0x80L) (b3.toLong() and 0x7FL) + 0x80L else b3.toLong()

            return (i0 shl 24) + (i1 shl 16) + (i2 shl 8) + i3
        }

        /**
         * Reads the NTP time stamp at the given offset in the buffer and returns
         * it as a system time (milliseconds since January 1, 1970).
         */
        private fun readTimeStamp(buffer: ByteArray, offset: Int): Long {
            val seconds = read32(buffer, offset)
            val fraction = read32(buffer, offset + 4)
            return (seconds - OFFSET_1900_TO_1970) * 1000 + fraction * 1000L / 0x100000000L
        }

        /**
         * Writes system time (milliseconds since January 1, 1970) as an NTP time stamp
         * at the given offset in the buffer.
         */
        private fun writeTimeStamp(buffer: ByteArray, offset: Int, time: Long) {
            var offset = offset
            var seconds = time / 1000L
            val milliseconds = time - seconds * 1000L
            seconds += OFFSET_1900_TO_1970

            // write seconds in big endian format
            buffer[offset++] = (seconds shr 24).toByte()
            buffer[offset++] = (seconds shr 16).toByte()
            buffer[offset++] = (seconds shr 8).toByte()
            buffer[offset++] = (seconds shr 0).toByte()

            val fraction = milliseconds * 0x100000000L / 1000L
            // write fraction in big endian format
            buffer[offset++] = (fraction shr 24).toByte()
            buffer[offset++] = (fraction shr 16).toByte()
            buffer[offset++] = (fraction shr 8).toByte()
            // low order bits should be random data
            buffer[offset++] = (Math.random() * 255.0).toByte()
        }

        private val TAG = "SntpClient"

        private val REFERENCE_TIME_OFFSET = 16
        private val ORIGINATE_TIME_OFFSET = 24
        private val RECEIVE_TIME_OFFSET = 32
        private val TRANSMIT_TIME_OFFSET = 40
        private val NTP_PACKET_SIZE = 48

        private val NTP_PORT = 123
        private val NTP_MODE_CLIENT = 3
        private val NTP_VERSION = 3

        // Number of seconds between Jan 1, 1900 and Jan 1, 1970
        // 70 years plus 17 leap days
        private val OFFSET_1900_TO_1970 = (365L * 70L + 17L) * 24L * 60L * 60L
    }

}