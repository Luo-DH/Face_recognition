package com.luo.blue

import android.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.luo.base.BlueSocket
import com.luo.blue.databinding.BlueFragmentBinding
import java.util.*
import kotlin.collections.ArrayList


class BlueFragment : Fragment() {

    companion object {
        fun newInstance() = BlueFragment()
    }

    private var _binding: BlueFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<BlueViewModel>()

    //定义一个列表，存蓝牙设备的地址。
    private val arrayList: ArrayList<String> = ArrayList()

    //定义一个列表，存蓝牙设备地址，用于显示。
    private val deviceName: ArrayList<String> = ArrayList()

    private lateinit var arrayAdapter: ArrayAdapter<String>


    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                deviceName.add(
                    """
                    设备名：${device!!.name}
                    设备地址：${device.address}
                    
                    """.trimIndent()
                ) //将搜索到的蓝牙名称和地址添加到列表。
                arrayList.add(device.address) //将搜索到的蓝牙地址添加到列表。
                arrayAdapter.notifyDataSetChanged() //更新
            }
        }
    }
    val blueAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BlueFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        activity?.unregisterReceiver(bluetoothReceiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        if (!blueAdapter.isEnabled) {
            blueAdapter.enable()
        }

        //不在可被搜索的范围
        if (blueAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(
                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                300
            ) //设置本机蓝牙在300秒内可见
            startActivity(discoverableIntent)
        }

        if (blueAdapter.isDiscovering) {
            //判断蓝牙是否正在扫描，如果是调用取消扫描方法；如果不是，则开始扫描
            blueAdapter.cancelDiscovery()
        } else
            blueAdapter.startDiscovery()

        arrayAdapter = ArrayAdapter(this.requireContext(), R.layout.simple_expandable_list_item_1, deviceName)

        binding.blueRv.adapter = arrayAdapter



        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND) //注册广播接收信号

        activity?.registerReceiver(bluetoothReceiver, intentFilter) //用BroadcastReceiver 来取得结果


        //定义列表Item的点击事件
        binding.blueRv.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            val adapter = BluetoothAdapter.getDefaultAdapter()
            val device = adapter.getRemoteDevice(arrayList[i])
            val clientThread = ClientThread(device)
            clientThread.start()
        }

        binding.blueBtn.setOnClickListener {
            val os = socket.outputStream
            os.write("1".toByteArray())
            os.flush()
//            os.close()
        }
    }
    private lateinit var socket: BluetoothSocket
    inner class ClientThread(private val device: BluetoothDevice) : Thread() {

        override fun run() {
            socket = device.javaClass.getDeclaredMethod(
                "createRfcommSocket", *arrayOf<Class<*>?>(
                    Int::class.javaPrimitiveType
                )
            ).invoke(device, 1) as BluetoothSocket
            BlueSocket.socket = socket
            blueAdapter.cancelDiscovery();//adapter为获取到的蓝牙适配器
            socket.connect()

            val os = socket.outputStream;//获取输出流
            os?.write(1)
            os.flush();//将输出流的数据强制提交
//            os.close();//关闭输出流
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.requireContext(), "已授权", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this.requireContext(), "拒绝授权", Toast.LENGTH_SHORT).show()
        }
    }
}