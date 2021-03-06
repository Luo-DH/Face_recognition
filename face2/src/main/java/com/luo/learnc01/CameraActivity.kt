package com.luo.learnc01

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.apkfuns.logutils.LogUtils
import com.google.common.util.concurrent.ListenableFuture
import com.luo.base.BlueSocket
import com.luo.base.face.Face
import com.luo.learnc01.databinding.ActivityCameraBinding
import com.luo.learnc01.face.ArcFace
import com.luo.learnc01.face.RetinaFace2
import com.luo.learnc01.modules.Bbox
import com.luo.learnc01.modules.Box
import com.luo.learnc01.modules.DBMsg
import com.luo.learnc01.others.toCropBitmap
import com.luo.learnc01.others.toRotaBitmap
//import com.luo.learnc01.others.toCropBitmap
//import com.luo.learnc01.others.toRotaBitmap
import com.luo.learnc01.repository.MainRepository
import com.luo.learnc01.ui.viewmodels.MainViewModel
import com.luo.learnc01.ui.viewmodels.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.math.min


class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var rotaBitmap: Bitmap

    private var executor = Executors.newSingleThreadExecutor()

    // viewModel
    private lateinit var viewModel: MainViewModel

    private lateinit var preview: Preview
    private lateinit var cameraSelector: CameraSelector
    private lateinit var imageAnalysis: ImageAnalysis

    private lateinit var dbFeatures: HashMap<String, FloatArray>
    private lateinit var dbFeaturesWithBitmap: HashMap<String, DBMsg>

    // ????????????????????????
    private lateinit var name: String

    // ?????????????????????????????????????????????????????????
    private var lastBox: Bbox? = null
    private var lastBox2: Box? = null

    // ??????????????????
    private var threshold: Double = 0.7

    // ??????????????????
    private var thresholdHigh: Double = 0.8

    // ??????????????????
    private var thresholdLow: Double = 0.4

    // ??????????????????????????????
    private var canDetect: Boolean = true

    private val voteMap = HashMap<String, Int>()

    // ????????????
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ArcFace().init(assets)
        RetinaFace2().init(assets)

        // ?????????viewModel
        initViewModel()

        // ????????????
        setupCameraProviderFuture()

        // ??????????????????
        setupObserver()

        binding.thresholdSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                threshold = i / 100f.toDouble()
                binding.tvThrName.text = "????????????:${threshold}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }


    private fun getPixelsRGBA(image: Bitmap): ByteArray? {
        // ???????????????????????????????????????
        val bytes = image.byteCount
        val buffer = ByteBuffer.allocate(bytes) // ??????????????????buffer
        image.copyPixelsToBuffer(buffer) // ??????????????????buffer
        return buffer.array()
    }

    /**
     * ?????????viewModel
     */
    private fun initViewModel() {
        val repository = MainRepository()
        val viewModelFactory = MainViewModelFactory(repository, applicationContext)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    /**
     * ??????????????????
     */
    private fun setupCameraProviderFuture() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this).apply {
            addListener(Runnable {
                val cameraProvider = this.get()

                // ????????????
                preview = setupPreview()

                // ????????????
                cameraSelector = setupCameraSelector()

                // ????????????
                imageAnalysis = setupImageAnalysis()

                val camera = cameraProvider.bindToLifecycle(
                    this@CameraActivity,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            }, ContextCompat.getMainExecutor(this@CameraActivity))
        }
    }

    /**
     * ??????????????????
     */
    private fun setupPreview() = Preview.Builder()
        .setTargetResolution(Size(960, 1280))  // ?????????
//        .setTargetRotation(binding.pvFinder.display.rotation)
        .build().also {
            it.setSurfaceProvider(binding.pvFinder.createSurfaceProvider())
        }

    /**
     * ????????????
     */
    private fun setupCameraSelector() = CameraSelector.Builder()
        .requireLensFacing(lensFacing).build()

    /**
     * ??????????????????
     */
    private fun setupImageAnalysis() = ImageAnalysis.Builder()
        .setTargetResolution(Size(960, 1280))
//        .setTargetRotation(binding.pvFinder.display.rotation)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build().apply {

            // ?????????????????????
            setAnalyzer(executor, ImageAnalysis.Analyzer { image ->

                var bitmap = Bitmap.createBitmap(
                    image.width, image.height, Bitmap.Config.ARGB_8888
                )
                image.use {
                    bitmap = viewModel.imageToBitmap2(image)
                }

                // ????????????
                rotaBitmap = bitmap.toRotaBitmap()

                // ????????????
                viewModel.detectFace2(rotaBitmap, lastBox2)

            })
        }


    /**
     * ???????????????????????????????????????
     */
    private fun analysisBitmap(bitmap: Bitmap, result: List<Box>) =
        binding.pvFinder.post {
            if (!canDetect) {
                voteMap.clear()
                return@post
            }

            if (result.isEmpty()) {
                binding.boxPrediction.visibility = View.GONE
                viewModel.drawBoxRects(bitmap, null)
                lastBox = null
                lastBox2 = null
                viewModel.isEmptyBox(true)
                return@post
            }
            viewModel.isEmptyBox(false)

//            val maxResult = result.maxBy { (it.x2 - it.x1) * (it.y2 - it.y1) }!!

            val box = result.maxByOrNull{ (it.x2 - it.x1) * (it.y2 - it.y1) }!!
//            val box = maxResult


            // ??????????????????????????????
//            lastBox = box
            lastBox2 = box

            val location = mapOutputCoordinates(
                RectF().also {
                    it.left = box.x1.toFloat() / bitmap.width
                    it.right = box.x2.toFloat() / bitmap.width
                    it.top = box.y1.toFloat() / bitmap.height
                    it.bottom = box.y2.toFloat() / bitmap.height
                }
            )

            (binding.boxPrediction.layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = location.top.toInt()
                leftMargin = location.left.toInt()
                width = min(binding.pvFinder.width, location.right.toInt() - location.left.toInt())
                height =
                    min(binding.pvFinder.height, location.bottom.toInt() - location.top.toInt())
            }

            // Make sure all UI elements are visible
            binding.boxPrediction.visibility = View.VISIBLE

            val cropBitmap =
//                maxResult.toCropBitmap(bitmap)
                result.maxByOrNull{ (it.x2 - it.x1) * (it.y2 - it.y1) }!!.toCropBitmap(bitmap)


            val landmarks = IntArray(10)
            var i = 0
            box.landmarks.forEach {
                landmarks[i] = it.x.toInt() - box.x1
                landmarks[i + 5] = it.y.toInt() - box.y1
                i += 1
            }

            viewModel.getFeature(cropBitmap, landmarks)

            // ???????????????
            viewModel.drawBoxRects(bitmap, result.maxByOrNull{ (it.x2 - it.x1) * (it.y2 - it.y1) }!!)
//            viewModel.drawBoxRects(bitmap, maxResult)
        }


    /**
     * ??????????????????????????????
     *      ???????????????????????????????????????
     */
    private fun setupObserver() {

        binding.imageViewTrans.setOnClickListener {
            lensFacing = CameraSelector.LENS_FACING_FRONT
        }

        /**
         * ????????????????????????
         *      ??????????????????????????????????????????
         */
        viewModel.feature.observe(this) {
            // ??????????????????
//            viewModel.calCosineDistance(it, dbFeaturesWithBitmap)
            viewModel.calCosineDistance(it, Face.faceDetail)
        }

        /**
         * ??????????????????
         *      ?????????????????????
         */
        viewModel.cosDist.observe(this) { results ->
            val result = results.maxByOrNull{ it.value }!!

            /**
             * ??????????????????????????????
             */
            val tmpName = if (result.value > threshold) result.key else "Unknow"
            var highScore = false
            var lowScore = false
            if (result.value > thresholdHigh)
                highScore = true
            if (result.value < thresholdLow)
                lowScore = true

            // ??????
//            viewModel.toVote(tmpName, voteMap)
            viewModel.toVote2(tmpName, voteMap, highScore = highScore, lowScore = lowScore)

            name = ""
            // ??????UI??????
            GlobalScope.launch(Dispatchers.Main) {

                binding.tvThr.text = "????????????${(result.value * 100).toInt()}%"
            }
        }

        // ????????????????????????
        viewModel.faceCast.observe(this) {
            binding.textView.text = "??????????????????: ${it} ms"
        }

//        // ????????????????????????
//        viewModel.detectBox.observe(this) {
//            val mutableBitmap = rotaBitmap.copy(Bitmap.Config.ARGB_8888, true)
//            analysisBitmap(mutableBitmap, it)
//        }
        // ????????????????????????
        viewModel.detectBox2.observe(this) {
            val mutableBitmap = rotaBitmap.copy(Bitmap.Config.ARGB_8888, true)
            analysisBitmap(mutableBitmap, it)
        }

        // ????????????????????????
        viewModel.detectCast.observe(this) {
            binding.valTxtView.text = "??????????????????: ${it} ms"
        }

        // ?????????????????????
        viewModel.bitmapRes.observe(this) {
            runOnUiThread {
//                binding.imageView.visibility = View.VISIBLE
                binding.imageView.setImageBitmap(it)

            }
        }

        // ????????????????????????
        viewModel.bitmapMeta.observe(this) {

            LogUtils.d("????????????")
            // ????????????
            rotaBitmap = it.toRotaBitmap()

            // ????????????
            viewModel.detectFace(rotaBitmap)
        }

        // ????????????????????????
        viewModel.loading.observe(this) {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE
                binding.pvFinder.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.imageView.visibility = View.VISIBLE
                binding.pvFinder.visibility = View.VISIBLE
            }
        }

        // ????????????????????????
        viewModel.features.observe(this) {
            dbFeatures = it
            val num = it.size
            binding.dbNms.text = "????????????????????????${num}"
        }

        // ???????????????????????????
        viewModel.featuresWithBitmap.observe(this) {
            dbFeaturesWithBitmap = it
            binding.dbNms.text = "????????????????????????${it.size}"
        }

        // ????????????
        viewModel.mapVote.observe(this) { voteMap ->
            LogUtils.d("vote: ${voteMap}")

            // ??????
            viewModel.voting(voteMap)

        }

        // ???????????????
        viewModel.isVoting.observe(this) {
            if (it) { // ????????????

            } else {
                // ??????UI??????
                GlobalScope.launch(Dispatchers.Main) {
                    binding.boxPrediction.visibility = View.GONE
                    binding.imageView2.visibility = View.VISIBLE
                    binding.imageViewFinalBitmap.visibility = View.VISIBLE
                    binding.finalName.visibility = View.VISIBLE
                    binding.imageView2.setImageBitmap(dbFeaturesWithBitmap[name]?.bitmap)
                    binding.imageViewFinalBitmap.setImageBitmap(dbFeaturesWithBitmap[name]?.bitmap)
                    binding.tvName.text = "???????????????$name"
                    binding.tvNow.text = "???????????????$name"
                    if (Face.faceDetail.map { it.name }.indexOf(name) > 0) {
                        binding.finalName.setTextColor(Color.GREEN)
                        binding.finalName.text = "???????????????$name"
                        val os = BlueSocket.socket?.outputStream
                        os?.write("1".toByteArray())
                        os?.flush()

                    } else {
                        binding.finalName.setTextColor(Color.RED)
                        binding.finalName.text = "???????????????????????? ${name}"
                        val os = BlueSocket.socket?.outputStream
                        os?.write("2".toByteArray())
                        os?.flush()
                    }
                    Handler().postDelayed({
                        canDetect = true
                        binding.imageView2.visibility = View.GONE
                        binding.imageViewFinalBitmap.visibility = View.GONE
                        binding.finalName.visibility = View.GONE
                    }, 2_000)
                }
            }
        }

        viewModel.finalName.observe(this) {
            // ????????????????????????
            name = it

            // ????????????????????????????????????
            canDetect = false

            // ???????????????
            voteMap.clear()
        }

        /**
         * ?????????????????????????????????10?????????????????????????????????
         */
        viewModel.emptyBox.observe(this) {
            if (it >= 10) {
                voteMap.clear()
            }
        }
    }

    /**
     * Helper function used to map the coordinates for objects coming out of
     * the model into the coordinates that the user sees on the screen.
     */
    private fun mapOutputCoordinates(location: RectF): RectF {
        val view_finder = binding.pvFinder

        // Step 1: map location to the preview coordinates
        val previewLocation = RectF(
            location.left * view_finder.width,
            location.top * view_finder.height,
            location.right * view_finder.width,
            location.bottom * view_finder.height
        )

//        val lensFacing = CameraSelector.LENS_FACING_BACK

        // Step 2: compensate for camera sensor orientation and mirroring
        val isFrontFacing = lensFacing == CameraSelector.LENS_FACING_FRONT
        val correctedLocation = if (isFrontFacing) {
            RectF(
                view_finder.width - previewLocation.right,
                previewLocation.top,
                view_finder.width - previewLocation.left,
                previewLocation.bottom
            )
        } else {
            previewLocation
        }

//        return correctedLocation

        // Step 3: compensate for 1:1 to 4:3 aspect ratio conversion + small margin
        val margin = 0.1f
        val requestedRatio = 18.7f / 15f
        val midX = (correctedLocation.left + correctedLocation.right) / 2f
        val midY = (correctedLocation.top + correctedLocation.bottom) / 2f
        return if (view_finder.width < view_finder.height) {
            RectF(
                midX - (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
                midY - (1f - margin) * correctedLocation.height() / 2f,
                midX + (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
                midY + (1f - margin) * correctedLocation.height() / 2f
            )
        } else {
            RectF(
                midX - (1f - margin) * correctedLocation.width() / 2f,
                midY - (1f + margin) * requestedRatio * correctedLocation.height() / 2f,
                midX + (1f - margin) * correctedLocation.width() / 2f,
                midY + (1f + margin) * requestedRatio * correctedLocation.height() / 2f
            )
        }
    }


}