//package com.luo.learnc01.ui
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.luo.learnc01.databinding.FragmentAddFaceBinding
//
///**
// * @author: Luo-DH
// * @date: 3/5/21
// */
//class AddFaceFragment : Fragment() {
//
//    private var _binding: FragmentAddFaceBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentAddFaceBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//
//
//}