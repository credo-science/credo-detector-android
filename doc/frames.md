# Fields of frames documentation

## The `ping` frame:
```json
{
  "device_id": "********",
  "device_type": "OnePlus3T",
  "device_model": "ONEPLUS A3010",
  "system_version": "26-8.0.0",
  "app_version": "6",
  "delta_time": 1025,
  "timestamp": 1526290053096,
  "frame_width": 720,
  "frame_height": 480,
  "start_detection_timestamp": 1526290045757,
  "last_flush_timestamp": 1526290052067,
  "last_frame_achieved_timestamp": 1526290053081,
  "last_frame_performed_timestamp": 1526290053088,
  "last_hit_timestamp": 0,
  "all_frames": 29,
  "performed_frames": 29,
  "on_time": 1025,
  "blacks_stats_min": 999.9913194444445,
  "blacks_stats_max": 1000.0,
  "blacks_stats_average": 999.9996008939975,
  "blacks_stats_samples": 29,
  "average_stats_min": 8.397387152777778,
  "average_stats_max": 8.526255787037037,
  "average_stats_average": 8.449381585249043,
  "average_stats_samples": 29,
  "max_stats_min": 17.0,
  "max_stats_max": 23.0,
  "max_stats_average": 18.724137931034484,
  "max_stats_samples": 29
}
```
* `device_id:string` - unique ID of hardware
* `device_type:string` - device type (TODO: what for non Android smartphones),
* `device_model:string` - device model (TODO: what for non Android smartphones),
* `system_version:string` - version of OS (TODO: what for non Android smartphones),
* `app_version:int` - version of app (TODO: what for non Android smartphones uses other app),
* `delta_time:long` - time in \[ms\] from start of detection of sent previous `ping` frame,
* `timestamp:long` - timestamp of this frame creation,
* `frame_width:int` - width of camera frame in \[pixels\],
* `frame_height:int` - height of camera frame in \[pixels\],
* `start_detection_timestamp:long` - timestamp of start detection,
* `last_flush_timestamp:long` - timestamp of creation this frame (FIXME: duplicated with `timestamp`),
* `last_frame_achieved_timestamp:long` - timestamp of latest frame achieved from camera,
* `last_frame_performed_timestamp:long` - timestamp of latest frame with good covered camera,
* `last_hit_timestamp:long` - timestamp of latest hit detection,
* `all_frames:int` - count of all frames achieved from camera since start detection or previous `ping`,
* `performed_frames:int` - count of frames form good covered camera since start detection or previous `ping`,
* `on_time:long` - time in \[ms\] of detection working with good camera covering since start detection or previous `ping`,
* `blacks_stats_min:float` - min value of promiles of pixels darkness than `black` threshold from frames from good camera covering since start detection or previous `ping`,
* `blacks_stats_max:float` - max value of promiles of pixels darkness than `black` threshold from frames from good camera covering since start detection or previous `ping`,
* `blacks_stats_average:float` - average value of promiles of pixels darkness than `black` threshold from frames from good camera covering since start detection or previous `ping`,
* `blacks_stats_samples:int` - count of samles using in abowe stats,
* `average_stats_min:float` - min value of average bright of pixels from frames from good camera covering since start detection or previous `ping`,
* `average_stats_max:float` - max value of average bright of pixels from frames from good camera covering since start detection or previous `ping`,
* `average_stats_average:float` - average value of average bright of pixels from frames from good camera covering since start detection or previous `ping`,
* `average_stats_samples:int` - count of samles using in abowe stats (FIXME: duplicated with `blacks_stats_samples`),
* `max_stats_min:float` - min value of max bright of pixel from frames from good camera covering since start detection or previous `ping`,
* `max_stats_max:float` - max value of max bright of pixel from frames from good camera covering since start detection or previous `ping`,
* `max_stats_average:float` - average value of max bright of pixel from frames from good camera covering since start detection or previous `ping`,
* `max_stats_samples:int` - count of samles using in abowe stats (FIXME: duplicated with `blacks_stats_samples`)

TODO: `blacks_stats`, `average_stats` and `max_stats` from all frames


## The `detection` frame:
```json
{
  "device_id": "********",
  "device_type": "OnePlus3T",
  "device_model": "ONEPLUS A3010",
  "system_version": "26-8.0.0",
  "app_version": "6",
  "detections": [{
    "timestamp": 1526290398175,
    "latitude": 49.80294781,
    "longitude": 19.04453905,
    "altitude": 383.0,
    "accuracy": 13.936001,
    "provider": "gps",
    "width": 720,
    "height": 480,
    "x": 645,
    "y": 477,
    "average": 18.14277488425926,
    "blacks": 825.2054398148148,
    "ax": -1.9684753,
    "ay": 7.31308,
    "az": 5.998352,
    "orientation": 286.87753,
    "temperature": 0,
    "id": 9,
    "black_threshold": 20,
    "frame_content": "...",
    "max": 135
  }]
}
```

* `device_id`, `device_type`, `device_model`,`system_version` and `app_version` - see: `ping` frame,
* `detections` - array of
   * `timestamp:long` - timestamp of detection,
   * `latitude:float` - latitude from GPS,
   * `longitude:float` - longitude from GPS,
   * `altitude:float` - altitude from GPS,
   * `accuracy:float` - accuracy from GPS,
   * `provider:string` - name of localization provider,
   * `width:int` - 720,
   * `height:int` - 480,
   * `x:int` - X of brightest pixel,
   * `y:int` - Y of brightest pixel,
   * `average:float` - average bright of pixels in frame with hit,
   * `blacks:float` - 825.2054398148148,
   * `ax:float` - -1.9684753,
   * `ay:float` - 7.31308,
   * `az:float` - 5.998352,
   * `orientation:float` - 286.87753,
   * `temperature:float` - 0,
   * `id:int` - 9,
   * `black_threshold:int` - 20,
   * `frame_content:byte` - "...",
   * `max:int` - 135
