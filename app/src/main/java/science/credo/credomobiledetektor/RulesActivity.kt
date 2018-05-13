package science.credo.credomobiledetektor

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_rules.*
import science.credo.credomobiledetektor.R.string.rules_content_file

class RulesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rules)
        val rules = getString(rules_content_file)
        web_view.loadUrl("file:///android_asset/$rules.html")
    }
}
