package io.github.guideconstraintlayout

import android.graphics.Path
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import io.github.guideconstraintlayout.databinding.ActivityMainBinding
import io.github.guideconstraintlayout.databinding.ComponentGuideLayoutBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val componentGuideBinding by lazy { ComponentGuideLayoutBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 显示第一个高亮布局
        componentGuideBinding.guide1.isVisible = true
        componentGuideBinding.guide2.isVisible = false

        // 目标布局后绑定高亮布局
        binding.text1.setOnClickListener { Toast.makeText(applicationContext, "文本被点击!", Toast.LENGTH_SHORT).show() }
        binding.text1.post {
            componentGuideBinding.guide1.bindTarget(binding.text1, componentGuideBinding.highlightView11)
        }

        // 目标布局后绑定高亮布局
        binding.button1.setOnClickListener { Toast.makeText(applicationContext, "按钮被点击!", Toast.LENGTH_SHORT).show() }
        binding.button1.post {
            componentGuideBinding.guide1.bindTarget(binding.button1, componentGuideBinding.highlightView12)
            componentGuideBinding.guide1.setPreventTouchPenetrateToTarget(true) // 阻止触摸穿透到目标View
        }

        // 下一个高亮布局
        componentGuideBinding.guide1NextButton.setOnClickListener {
            componentGuideBinding.guide1.isVisible = false
            componentGuideBinding.guide2.isVisible = true
            showCustom()
        }

        // 完成高亮布局展示
        componentGuideBinding.guide1FinishButton.setOnClickListener {
            componentGuideBinding.guide1.isVisible = false
            componentGuideBinding.guide2.isVisible = false
        }

        // 添加高亮布局到当前视图
        findViewById<ViewGroup>(android.R.id.content)
            .addView(componentGuideBinding.root)
    }

    // 自定义路径高亮
    private fun showCustom() {
        componentGuideBinding.guide1FinishButton.isEnabled = false // 禁用完成按钮

        // 绑定第二个高亮布局
        componentGuideBinding.guide2.bindTarget(binding.text1, componentGuideBinding.highlightView21)

        // 5秒后显示自定义高亮布局
        lifecycleScope.launch {
            componentGuideBinding.guideText21.text = getString(R.string.guide_text_2_1, "5")

            repeat(5) { index ->
                componentGuideBinding.guideText21.text = getString(R.string.guide_text_2_1, "${5 - index}")
                componentGuideBinding.guide1FinishButton.text = "请稍后(${5 - index}s)"

                if (index >= 4) {
                    componentGuideBinding.guide1FinishButton.isEnabled = true // 启用完成按钮
                    componentGuideBinding.guide1FinishButton.text = "完成"
                    componentGuideBinding.guideText21.text = "自定义路径高亮"

                    val width = binding.text1.width.toFloat()
                    val height = binding.text1.height.toFloat()
                    val radius = sqrt(width * width + height * height) / 2f

                    componentGuideBinding.highlightView21.setHighlightSpace(radius / 4)// 更新space
                    componentGuideBinding.guide2.bindTarget(binding.text1, componentGuideBinding.highlightView21, Path().apply {
                        reset()
                        addCircle(
                            binding.text1.left + width / 2f,
                            binding.text1.top + height / 2f,
                            radius,
                            Path.Direction.CW,
                        )
                        close()
                    }) // 重新绑定目标布局和自定义路径
                }
                delay(1000)
            }
        }
    }
}