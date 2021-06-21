package com.luo.facerecognition

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.luo.base.BlueSocket
import com.luo.facerecognition.databinding.FragmentMainBinding
import com.luo.learnc01.CameraActivity

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel by viewModels<MainFragmentViewModel>()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置点击监听
        setClickListener()


    }

    private fun setClickListener() {
        binding.mainBtnBlue.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_blueFragment)
        }
        binding.mainBtnRecognize.setOnClickListener {
//            findNavController().navigate(R.id.action_mainFragment_to_recognizeFragment)
            startActivity(Intent(this.requireContext(), CameraActivity::class.java))
        }
        binding.mainBtnSetting.setOnClickListener {
            val os = BlueSocket.socket?.outputStream
            os?.write("2".toByteArray())
            os?.flush()
        }
    }

}