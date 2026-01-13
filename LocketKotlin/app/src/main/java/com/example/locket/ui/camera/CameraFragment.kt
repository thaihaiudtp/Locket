package com.example.locket.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.locket.R
import com.example.locket.databinding.FragmentCameraBinding
import com.example.locket.databinding.ItemFriendBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class FriendUI(val id: String, val name: String)

@AndroidEntryPoint
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private val viewModel: CameraViewModel by viewModels()
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService? = null
    private var camera: Camera? = null

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var isFlashOn = false

    private var capturedFile: File? = null
    private var currentTimeStr: String = ""
    private var currentLocation: String = "Hanoi, VN"
    private var showTime = false
    private var showLocation = false

    private val friendsAdapter = FriendsAdapter()

    // Gesture Detector để xử lý vuốt
    private lateinit var gestureDetector: GestureDetectorCompat

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startCamera() else Toast.makeText(context, "Cần quyền Camera để sử dụng", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCameraBinding.bind(view)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupGestures()
        checkPermissions()
        setupUI()
        setupObservers()
    }

    private fun setupGestures() {
        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val distanceY = e2.y - e1.y
                val distanceX = e2.x - e1.x

                // Logic: Vuốt từ dưới lên (Y giảm, distanceY âm) và vuốt dọc nhiều hơn vuốt ngang
                if (abs(distanceY) > abs(distanceX) && distanceY < -50) {
                    openHistory()
                    return true
                }
                return false
            }
        })
    }

    private fun openHistory() {
        try {
            findNavController().navigate(R.id.action_cameraFragment_to_historyFragment)
        } catch (e: Exception) {
            Log.e("CameraFragment", "Lỗi điều hướng History: ${e.message}")
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI() {
        // --- 1. XỬ LÝ CLICK NÚT ---
        // Nút Chụp
        binding.btnCapture.setOnClickListener { takePhoto() }

        // Nút Add Friend
        binding.btnAddFriend.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_cameraFragment_to_friendFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi mở Friend: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Nút Profile
        binding.btnProfile.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_cameraFragment_to_profileFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi mở Profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Nút History (Click icon)
        binding.btnHistory.setOnClickListener { openHistory() }

        // Các nút chức năng khác
        binding.btnFlipCamera.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        binding.btnFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            imageCapture?.flashMode = if(isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
            binding.btnFlash.imageAlpha = if(isFlashOn) 255 else 128 // Hiệu ứng mờ khi tắt
        }

        // --- 2. XỬ LÝ CẢM ỨNG & VUỐT ---

        // A. Gắn sự kiện vuốt cho TOÀN BỘ màn hình (bao gồm vùng đen)
        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            // Trả về false để sự kiện có thể đi tiếp xuống các view con (như nút bấm)
            // Tuy nhiên, nếu view con không xử lý click, root sẽ tiêu thụ event.
            // Để an toàn cho các nút bấm, ta nên cẩn thận.
            // Cách tốt nhất: gestureDetector.onTouchEvent trả về true nếu xử lý vuốt, false nếu không.
            return@setOnTouchListener gestureDetector.onTouchEvent(event)
        }

        // B. Gắn sự kiện Lấy Nét (Focus) chỉ cho vùng ViewFinder
        binding.viewFinder.setOnTouchListener { _, event ->
            // Ưu tiên kiểm tra vuốt trước (để vuốt trên camera vẫn mở History)
            if (gestureDetector.onTouchEvent(event)) return@setOnTouchListener true

            // Nếu không phải vuốt mà là chạm nhả (Tap) -> Lấy nét
            if (event.action == MotionEvent.ACTION_UP) {
                val factory = binding.viewFinder.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build()
                camera?.cameraControl?.startFocusAndMetering(action)
                return@setOnTouchListener true
            }
            return@setOnTouchListener true
        }

        // --- UI REVIEW MODE ---
        binding.btnCloseReview.setOnClickListener { resetToCameraMode() }
        binding.btnToggleTime.setOnClickListener {
            showTime = !showTime
            binding.tvTimeOverlay.isVisible = showTime
        }
        binding.btnToggleLocation.setOnClickListener {
            showLocation = !showLocation
            binding.tvLocationOverlay.isVisible = showLocation
        }
        binding.btnSend.setOnClickListener {
            capturedFile?.let { file ->
                val msg = binding.etMessage.text.toString()
                viewModel.uploadImage(file, if(msg.isNotBlank()) msg else null, if (showTime) currentTimeStr else null, if (showLocation) currentLocation else null)
            }
        }

        binding.rvFriends.adapter = friendsAdapter
        friendsAdapter.submitList(listOf(FriendUI("all", "All"), FriendUI("1", "Alice"), FriendUI("2", "Bob")))
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }
            imageCapture = ImageCapture.Builder().setFlashMode(if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF).build()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) { Log.e("CameraFragment", "Binding failed", exc) }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(requireContext().cacheDir, "Locket_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) { Toast.makeText(context, "Error: ${exc.message}", Toast.LENGTH_SHORT).show() }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                capturedFile = photoFile
                showReviewUI(Uri.fromFile(photoFile))
            }
        })
    }

    private fun showReviewUI(uri: Uri) {
        binding.groupCameraUI.isVisible = false
        binding.groupReviewUI.isVisible = true
        binding.imgCaptured.isVisible = true
        binding.imgCaptured.load(uri)
        currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        binding.tvTimeOverlay.text = currentTimeStr
        binding.tvLocationOverlay.text = currentLocation
        binding.etMessage.setText("")
        showTime = false; showLocation = false
        binding.tvTimeOverlay.isVisible = false; binding.tvLocationOverlay.isVisible = false
    }

    private fun resetToCameraMode() {
        binding.groupCameraUI.isVisible = true
        binding.groupReviewUI.isVisible = false
        binding.imgCaptured.isVisible = false
        capturedFile = null
        binding.imgCaptured.setImageDrawable(null)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadState.collect { state ->
                when(state) {
                    is UploadState.Loading -> { binding.progressSend.isVisible = true; binding.btnSend.isEnabled = false }
                    is UploadState.Success -> { binding.progressSend.isVisible = false; binding.btnSend.isEnabled = true; Toast.makeText(context, "Sent!", Toast.LENGTH_SHORT).show(); resetToCameraMode(); viewModel.resetState() }
                    is UploadState.Error -> { binding.progressSend.isVisible = false; binding.btnSend.isEnabled = true; Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show() }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
        _binding = null
    }
}

class FriendsAdapter : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {
    private var items = listOf<FriendUI>()
    private var selectedPosition = 0
    fun submitList(list: List<FriendUI>) { items = list; notifyDataSetChanged() }
    inner class ViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvAvatar.text = item.name.first().toString()
        val isSelected = position == selectedPosition
        holder.binding.cardAvatar.setCardBackgroundColor(if(isSelected) android.graphics.Color.parseColor("#FFD700") else android.graphics.Color.parseColor("#2C2C2C"))
        holder.itemView.setOnClickListener {
            val prev = selectedPosition; selectedPosition = holder.adapterPosition; notifyItemChanged(prev); notifyItemChanged(selectedPosition)
        }
    }
    override fun getItemCount() = items.size
}