package science.credo.mobiledetector.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import science.credo.mobiledetector.R
import science.credo.mobiledetector.database.DataManager
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    private var temperatureChart: LineChart? = null
    private var hitsChart: LineChart? = null
    private var workingTimeChart: LineChart? = null
    private var loadingProgress: ProgressBar? = null
    private var statisticsContent: View? = null
    private var noDataText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        temperatureChart = view.findViewById(R.id.temperature_chart)
        hitsChart = view.findViewById(R.id.hits_chart)
        workingTimeChart = view.findViewById(R.id.working_time_chart)
        loadingProgress = view.findViewById(R.id.loading_progress)
        statisticsContent = view.findViewById(R.id.statistics_content)
        noDataText = view.findViewById(R.id.no_data_text)

        setupCharts()
        loadData()
    }

    private fun setupCharts() {
        temperatureChart?.let { configureChart(it) }
        hitsChart?.let { configureChart(it) }
        workingTimeChart?.let { configureChart(it) }
    }

    private fun configureChart(chart: LineChart) {
        chart.description.isEnabled = false
        chart.setNoDataText(getString(R.string.no_data_available))
        chart.setNoDataTextColor(Color.WHITE)
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.legend.textColor = Color.WHITE

        val xAxis = chart.xAxis
        xAxis.isEnabled = true
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.gridColor = Color.parseColor("#33FFFFFF")
        xAxis.textColor = Color.WHITE
        xAxis.granularity = 60f
        xAxis.labelCount = 4
        xAxis.valueFormatter = object : IAxisValueFormatter {
            private val mFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return mFormat.format(Date(value.toLong() * 1000L))
            }
        }

        chart.axisLeft.textColor = Color.WHITE
        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.gridColor = Color.parseColor("#33FFFFFF")
        chart.axisRight.isEnabled = false
    }

    private fun loadData() {
        val currentContext = context ?: return
        loadingProgress?.visibility = View.VISIBLE
        statisticsContent?.visibility = View.GONE
        noDataText?.visibility = View.GONE

        doAsync {
            try {
                val dm = DataManager.getDefault(currentContext)
                val pings = dm.getPings().sortedBy { it.timestamp }
                val hits = dm.getHits()

                val tempEntries = mutableListOf<Entry>()
                val workingEntries = mutableListOf<Entry>()
                var totalOnTimeMinutes = 0f

                for (ping in pings) {
                    if (ping.timestamp == 0L) continue
                    val timestampSeconds = ping.timestamp / 1000f
                    tempEntries.add(Entry(timestampSeconds, ping.temperature.toFloat()))
                    
                    totalOnTimeMinutes += ping.on_time.toFloat() / 60000f
                    workingEntries.add(Entry(timestampSeconds, totalOnTimeMinutes))
                }

                // Group hits by hour
                val hitsEntries = mutableListOf<Entry>()
                if (hits.isNotEmpty()) {
                    val calendar = Calendar.getInstance()
                    val hitsByHour = hits.groupBy {
                        calendar.timeInMillis = it.mTimestamp
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.timeInMillis
                    }.mapValues { it.value.size }

                    hitsByHour.keys.sorted().forEach { timestamp ->
                        hitsEntries.add(Entry(timestamp / 1000f, hitsByHour[timestamp]!!.toFloat()))
                    }
                }

                uiThread {
                    if (!isAdded) return@uiThread
                    loadingProgress?.visibility = View.GONE

                    if (tempEntries.isEmpty() && hitsEntries.isEmpty() && workingEntries.isEmpty()) {
                        noDataText?.visibility = View.VISIBLE
                    } else {
                        statisticsContent?.visibility = View.VISIBLE
                        temperatureChart?.let { updateChartData(it, tempEntries, getString(R.string.title_charts_temperature), Color.RED) }
                        hitsChart?.let { updateChartData(it, hitsEntries, getString(R.string.title_charts_detections), Color.BLUE) }
                        workingTimeChart?.let { updateChartData(it, workingEntries, getString(R.string.title_charts_working_time_min), Color.parseColor("#388E3C")) }
                    }
                }
            } catch (e: Exception) {
                uiThread {
                    if (!isAdded) return@uiThread
                    loadingProgress?.visibility = View.GONE
                    noDataText?.text = getString(R.string.error_prefix) + " ${e.message}"
                    noDataText?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateChartData(chart: LineChart, entries: List<Entry>, label: String, color: Int) {
        if (entries.isEmpty()) {
            chart.data = null
            chart.invalidate()
            return
        }

        val dataSet = LineDataSet(entries, label)
        dataSet.color = color
        dataSet.setCircleColor(color)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 3f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 9f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = color
        dataSet.fillAlpha = 50
        dataSet.setDrawValues(false)

        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    companion object {
        fun newInstance(): StatisticsFragment = StatisticsFragment()
    }
}
