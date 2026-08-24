package science.credo.mobiledetector

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import science.credo.mobiledetector.databinding.ActivityRulesBinding
import science.credo.mobiledetector.R.string.rules_content_file

class RulesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRulesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val rules = getString(rules_content_file)
        binding.webView.loadUrl("file:///android_asset/$rules.html")
    }
}
