//package com.luo.learnc01.ui
//
//import android.graphics.Bitmap
//import android.graphics.Color
//import android.graphics.RectF
//import android.os.Bundle
//import android.os.Handler
//import android.util.Size
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.lifecycle.observe
//import androidx.navigation.fragment.findNavController
//import com.apkfuns.logutils.LogUtils
//import com.google.common.util.concurrent.ListenableFuture
//import com.luo.learnc01.R
//import com.luo.learnc01.databinding.FragmentMainBinding
//import com.luo.recognize.modules.Bbox
//import com.luo.recognize.modules.Box
//import com.luo.recognize.modules.DBMsg
//import com.luo.recognize.others.toCropBitmap
//import com.luo.recognize.others.toRotaBitmap
//import com.luo.recognize.repository.MainRepository
//import com.luo.recognize.ui.viewmodels.MainViewModel
//import com.luo.recognize.ui.viewmodels.MainViewModelFactory
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.util.concurrent.Executors
//import kotlin.math.min
//
///**
// * @author: Luo-DH
// * @date: 3/5/21
// */
//class MainFragment : Fragment() {
//
//    private var _binding: FragmentMainBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
//
//    private var executor = Executors.newSingleThreadExecutor()
//
//    private lateinit var preview: Preview
//    private lateinit var cameraSelector: CameraSelector
//    private lateinit var imageAnalysis: ImageAnalysis
//
//    private lateinit var rotaBitmap: Bitmap
//
//
//    private lateinit var dbFeatures: HashMap<String, FloatArray>
//    private lateinit var dbFeaturesWithBitmap: HashMap<String, DBMsg>
//
//    // ????????????????????????
//    private lateinit var name: String
//
//    // ?????????????????????????????????????????????????????????
//    private var lastBox: Bbox? = null
//    private var lastBox2: Box? = null
//
//    // ??????????????????
//    private var threshold: Double = 0.7
//
//    // ??????????????????
//    private var thresholdHigh: Double = 0.8
//
//    // ??????????????????
//    private var thresholdLow: Double = 0.4
//
//    // ??????????????????????????????
//    private var canDetect: Boolean = true
//
//    private val voteMap = HashMap<String, Int>()
//
//    // ????????????
//    private var lensFacing = CameraSelector.LENS_FACING_FRONT
//
//    private val activityViewModel by activityViewModels<MainViewModel> {
//        val repository = MainRepository()
//        val viewModelFactory = MainViewModelFactory(repository, requireContext())
//        viewModelFactory
//    }
//    private val viewModel get() = activityViewModel
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentMainBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // ????????????
//        setupCameraProviderFuture()
//
//        // ??????????????????
//        setupObserver()
//
//        // ??????????????????
//        setupClickListener()
//    }
//
//    private fun setupClickListener() {
//        binding.btnAdd.setOnClickListener {
//            findNavController().navigate(R.id.action_mainFragment_to_addFaceFragment)
//        }
//    }
//
//    /**
//     * ??????????????????
//     */
//    private fun setupCameraProviderFuture() {
//        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext()).apply {
//            addListener(Runnable {
//                val cameraProvider = this.get()
//
//                // ????????????
//                preview = setupPreview()
//
//                // ????????????
//                cameraSelector = setupCameraSelector()
//
//                // ????????????
//                imageAnalysis = setupImageAnalysis()
//                cameraProvider.unbindAll()
//
//                cameraProvider.bindToLifecycle(
//                    requireActivity(),
//                    cameraSelector,
//                    preview,
//                    imageAnalysis
//                )
//
//            }, ContextCompat.getMainExecutor(requireContext()))
//        }
//    }
//
//    /**
//     * ??????????????????
//     */
//    private fun setupPreview() = Preview.Builder()
//        .setTargetResolution(Size(960, 1280))  // ?????????
////        .setTargetRotation(binding.pvFinder.display.rotation)
//        .build().also {
//            it.setSurfaceProvider(binding.pvFinder.createSurfaceProvider())
//        }
//
//    /**
//     * ????????????
//     */
//    private fun setupCameraSelector() = CameraSelector.Builder()
//        .requireLensFacing(lensFacing).build()
//
//    /**
//     * ??????????????????
//     */
//    private fun setupImageAnalysis() = ImageAnalysis.Builder()
//        .setTargetResolution(Size(960, 1280))
////        .setTargetRotation(binding.pvFinder.display.rotation)
//        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//        .build().apply {
//
//            // ?????????????????????
//            setAnalyzer(executor, ImageAnalysis.Analyzer { image ->
//
//                var bitmap = Bitmap.createBitmap(
//                    image.width, image.height, Bitmap.Config.ARGB_8888
//                )
//                image.use {
//                    bitmap = activityViewModel.imageToBitmap2(image)
//                }
//
//                // ????????????
//                rotaBitmap = bitmap.toRotaBitmap()
//
//                // ????????????
//                activityViewModel.detectFace2(rotaBitmap, lastBox2)
//
//            })
//        }
//
//
//    /**
//     * ??????????????????????????????
//     *      ???????????????????????????????????????
//     */
//    private fun setupObserver() {
//
//        binding.imageViewTrans.setOnClickListener {
//            lensFacing = CameraSelector.LENS_FACING_FRONT
//        }
//
//        /**
//         * ????????????????????????
//         *      ??????????????????????????????????????????
//         */
//        viewModel.feature.observe(viewLifecycleOwner) {
//            // ??????????????????
//            viewModel.calCosineDistance(it, dbFeaturesWithBitmap)
//        }
//
//        /**
//         * ??????????????????
//         *      ?????????????????????
//         */
//        viewModel.cosDist.observe(this.viewLifecycleOwner) { results ->
//            val result = results.maxBy { it.value }!!
//
//            /**
//             * ??????????????????????????????
//             */
//            val tmpName = if (result.value > threshold) result.key else "Unknow"
//            var highScore = false
//            var lowScore = false
//            if (result.value > thresholdHigh)
//                highScore = true
//            if (result.value < thresholdLow)
//                lowScore = true
//
//            // ??????
////            viewModel.toVote(tmpName, voteMap)
//            viewModel.toVote2(tmpName, voteMap, highScore = highScore, lowScore = lowScore)
//
//            name = ""
//            // ??????UI??????
//            GlobalScope.launch(Dispatchers.Main) {
//
//                binding.tvThr.text = "????????????${(result.value * 100).toInt()}%"
//            }
//        }
//
//        // ????????????????????????
//        viewModel.faceCast.observe(this.viewLifecycleOwner) {
//            binding.textView.text = "??????????????????: ${it} ms"
//        }
//
////        // ????????????????????????
////        viewModel.detectBox.observe(this) {
////            val mutableBitmap = rotaBitmap.copy(Bitmap.Config.ARGB_8888, true)
////            analysisBitmap(mutableBitmap, it)
////        }
//        // ????????????????????????
//        viewModel.detectBox2.observe(this.viewLifecycleOwner) {
//            val mutableBitmap = rotaBitmap.copy(Bitmap.Config.ARGB_8888, true)
//            analysisBitmap(mutableBitmap, it)
//        }
//
//        // ????????????????????????
//        viewModel.detectCast.observe(this.viewLifecycleOwner) {
//            binding.valTxtView.text = "??????????????????: ${it} ms"
//        }
//
//        // ?????????????????????
//        viewModel.bitmapRes.observe(this.viewLifecycleOwner) {
//            requireActivity().runOnUiThread {
////                binding.imageView.visibility = View.VISIBLE
//                binding.imageView.setImageBitmap(it)
//
//            }
//        }
//
//        // ????????????????????????
//        viewModel.bitmapMeta.observe(this.viewLifecycleOwner) {
//
//            // ????????????
//            rotaBitmap = it.toRotaBitmap()
//
//            // ????????????
//            viewModel.detectFace(rotaBitmap)
//        }
//
//        // ????????????????????????
//        viewModel.loading.observe(this.viewLifecycleOwner) {
//            if (it) {
//                binding.progressBar.visibility = View.VISIBLE
//                binding.imageView.visibility = View.GONE
//                binding.pvFinder.visibility = View.GONE
//            } else {
//                binding.progressBar.visibility = View.GONE
//                binding.imageView.visibility = View.VISIBLE
//                binding.pvFinder.visibility = View.VISIBLE
//            }
//        }
//
//        // ????????????????????????
//        viewModel.features.observe(this.viewLifecycleOwner) {
//            dbFeatures = it
//            val num = it.size
//            binding.dbNms.text = "????????????????????????${num}"
//        }
//
//        // ???????????????????????????
//        viewModel.featuresWithBitmap.observe(this.viewLifecycleOwner) {
//            dbFeaturesWithBitmap = it
//            binding.dbNms.text = "????????????????????????${it.size}"
//        }
//
//        // ????????????
//        viewModel.mapVote.observe(this.viewLifecycleOwner) { voteMap ->
//            LogUtils.d("vote: ${voteMap}")
//
//            // ??????
//            viewModel.voting(voteMap)
//
//        }
//
//        // ???????????????
//        viewModel.isVoting.observe(this.viewLifecycleOwner) {
//            if (it) { // ????????????
//
//            } else {
//                // ??????UI??????
//                GlobalScope.launch(Dispatchers.Main) {
//                    binding.boxPrediction.visibility = View.GONE
//                    binding.imageView2.visibility = View.VISIBLE
//                    binding.imageViewFinalBitmap.visibility = View.VISIBLE
//                    binding.finalName.visibility = View.VISIBLE
//                    binding.imageView2.setImageBitmap(dbFeaturesWithBitmap[name]?.bitmap)
//                    binding.imageViewFinalBitmap.setImageBitmap(dbFeaturesWithBitmap[name]?.bitmap)
//                    binding.tvName.text = "???????????????$name"
//                    binding.tvNow.text = "???????????????$name"
//                    if (dbFeaturesWithBitmap[name] != null) {
//                        binding.finalName.setTextColor(Color.GREEN)
//                        binding.finalName.text = "???????????????$name"
//                    } else {
//                        binding.finalName.setTextColor(Color.RED)
//                        binding.finalName.text = "???????????????????????? ${name}"
//                    }
//                    Handler().postDelayed({
//                        canDetect = true
//                        binding.imageView2.visibility = View.GONE
//                        binding.imageViewFinalBitmap.visibility = View.GONE
//                        binding.finalName.visibility = View.GONE
//                    }, 2_000)
//                }
//            }
//        }
//
//        viewModel.finalName.observe(this.viewLifecycleOwner) {
//            // ????????????????????????
//            name = it
//
//            // ????????????????????????????????????
//            canDetect = false
//
//            // ???????????????
//            voteMap.clear()
//        }
//
//        /**
//         * ?????????????????????????????????10?????????????????????????????????
//         */
//        viewModel.emptyBox.observe(this.viewLifecycleOwner) {
//            if (it >= 10) {
//                voteMap.clear()
//            }
//        }
//    }
//
//    /**
//     * Helper function used to map the coordinates for objects coming out of
//     * the model into the coordinates that the user sees on the screen.
//     */
//    private fun mapOutputCoordinates(location: RectF): RectF {
//        val view_finder = binding.pvFinder
//
//        // Step 1: map location to the preview coordinates
//        val previewLocation = RectF(
//            location.left * view_finder.width,
//            location.top * view_finder.height,
//            location.right * view_finder.width,
//            location.bottom * view_finder.height
//        )
//
////        val lensFacing = CameraSelector.LENS_FACING_BACK
//
//        // Step 2: compensate for camera sensor orientation and mirroring
//        val isFrontFacing = lensFacing == CameraSelector.LENS_FACING_FRONT
//        val correctedLocation = if (isFrontFacing) {
//            RectF(
//                view_finder.width - previewLocation.right,
//                previewLocation.top,
//                view_finder.width - previewLocation.left,
//                previewLocation.bottom
//            )
//        } else {
//            previewLocation
//        }
//
////        return correctedLocation
//
//        // Step 3: compensate for 1:1 to 4:3 aspect ratio conversion + small margin
//        val margin = 0.1f
//        val requestedRatio = 18.7f / 15f
//        val midX = (correctedLocation.left + correctedLocation.right) / 2f
//        val midY = (correctedLocation.top + correctedLocation.bottom) / 2f
//        return if (view_finder.width < view_finder.height) {
//            RectF(
//                midX - (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
//                midY - (1f - margin) * correctedLocation.height() / 2f,
//                midX + (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
//                midY + (1f - margin) * correctedLocation.height() / 2f
//            )
//        } else {
//            RectF(
//                midX - (1f - margin) * correctedLocation.width() / 2f,
//                midY - (1f + margin) * requestedRatio * correctedLocation.height() / 2f,
//                midX + (1f - margin) * correctedLocation.width() / 2f,
//                midY + (1f + margin) * requestedRatio * correctedLocation.height() / 2f
//            )
//        }
//    }
//
//    /**
//     * ???????????????????????????????????????
//     */
//    private fun analysisBitmap(bitmap: Bitmap, result: List<Box>) =
//        binding.pvFinder.post {
//            if (!canDetect) {
//                voteMap.clear()
//                return@post
//            }
//
//            if (result.isEmpty()) {
//                binding.boxPrediction.visibility = View.GONE
//                viewModel.drawBoxRects(bitmap, null)
//                lastBox = null
//                lastBox2 = null
//                viewModel.isEmptyBox(true)
//                return@post
//            }
//            viewModel.isEmptyBox(false)
//
////            val maxResult = result.maxBy { (it.x2 - it.x1) * (it.y2 - it.y1) }!!
//
//            val box = result.maxBy { (it.x2 - it.x1) * (it.y2 - it.y1) }!!
////            val box = maxResult
//
//
//            // ??????????????????????????????
////            lastBox = box
//            lastBox2 = box
//
//            val location = mapOutputCoordinates(
//                RectF().also {
//                    it.left = box.x1.toFloat() / bitmap.width
//                    it.right = box.x2.toFloat() / bitmap.width
//                    it.top = box.y1.toFloat() / bitmap.height
//                    it.bottom = box.y2.toFloat() / bitmap.height
//                }
//            )
//
//            (binding.boxPrediction.layoutParams as ViewGroup.MarginLayoutParams).apply {
//                topMargin = location.top.toInt()
//                leftMargin = location.left.toInt()
//                width = min(binding.pvFinder.width, location.right.toInt() - location.left.toInt())
//                height =
//                    min(binding.pvFinder.height, location.bottom.toInt() - location.top.toInt())
//            }
//
//            // Make sure all UI elements are visible
//            binding.boxPrediction.visibility = View.VISIBLE
//
//            val cropBitmap =
////                maxResult.toCropBitmap(bitmap)
//                result.maxBy { (it.x2 - it.x1) * (it.y2 - it.y1) }!!.toCropBitmap(bitmap)
//
//
//            val landmarks = IntArray(10)
//            var i = 0
//            box.landmarks.forEach {
//                landmarks[i] = it.x.toInt() - box.x1
//                landmarks[i + 5] = it.y.toInt() - box.y1
//                i += 1
//            }
//
//            viewModel.getFeature(cropBitmap, landmarks)
//
//            // ???????????????
//            viewModel.drawBoxRects(bitmap, result.maxBy { (it.x2 - it.x1) * (it.y2 - it.y1) }!!)
////            viewModel.drawBoxRects(bitmap, maxResult)
//        }
//
//
//}