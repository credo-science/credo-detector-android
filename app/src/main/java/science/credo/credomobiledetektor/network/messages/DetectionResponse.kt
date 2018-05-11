package science.credo.credomobiledetektor.network.messages

/**
 * Response object created after successful detections submit.
 *
 * @property detections Returned detection ids.
 */
class DetectionResponse (
    val detections : MutableList<StoredDetectionEntity>
)
