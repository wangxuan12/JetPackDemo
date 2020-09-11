package com.mooc.ppjoke.ui.publish

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.mooc.libcommon.utils.showToast
import com.mooc.ppjoke.R
import com.mooc.ppjoke.databinding.ActivityLayoutCaptureBinding
import java.io.File
import java.util.concurrent.Executor

@SuppressLint("RestrictedApi")
class CaptureActivity : AppCompatActivity() {

    private lateinit var videoCapture: VideoCapture
    private lateinit var executor: Executor
    private lateinit var imageCapture: ImageCapture
    private var cameraProvider: ProcessCameraProvider? = null
    private var takingPicture: Boolean = true
    private lateinit var binding: ActivityLayoutCaptureBinding
    private val resolution = Size(1280, 720)
    private var outputFilePath: String = ""
    private val deniedPermission = mutableListOf<String>()

    companion object {
        val RESULT_FILE_PATH = "file_path"
        val RESULT_FILE_WIDTH = "file_width"
        val RESULT_FILE_HEIGHT = "file_height"
        val RESULT_FILE_TYPE = "file_type"

        private val PERMISSION_CODE = 1000
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )

        const val REQ_CAPTURE = 10001
        fun startActivityForResult(activity: Activity) {
            activity.startActivityForResult(Intent(activity, CaptureActivity::class.java), REQ_CAPTURE)
        }
    }

    // TODO: 2020/9/8 存储访问升级
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)
        binding = DataBindingUtil.setContentView<ActivityLayoutCaptureBinding>(this,
            R.layout.activity_layout_capture)
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE)
        binding.recordView.onClick {
            takingPicture = true
            val file: File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "${System.currentTimeMillis()}.jepg")
            binding.captureTips.visibility = View.INVISIBLE
            imageCapture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(),
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        onFileSaved(file)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        showToast(exception.message)
                    }
                })
        }
        binding.recordView.onLongClick {
            takingPicture = false
            val file: File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "${System.currentTimeMillis()}.mp4")
            videoCapture.startRecording(VideoCapture.OutputFileOptions.Builder(file).build(),
                executor,
                object : VideoCapture.OnVideoSavedCallback {
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        onFileSaved(file)
                    }

                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?,
                    ) {
                        showToast(message)
                    }
                })
        }
        binding.recordView.onFinish {
            videoCapture.stopRecording()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PreviewActivity.REQ_PREVIEW && resultCode == RESULT_OK) {
            val intent = Intent()
            intent.putExtra(RESULT_FILE_PATH, outputFilePath)
            //当设备处于竖屏情况时，宽高的值 需要互换，横屏不需要
            intent.putExtra(RESULT_FILE_WIDTH, resolution.getHeight())
            intent.putExtra(RESULT_FILE_HEIGHT, resolution.getWidth())
            intent.putExtra(RESULT_FILE_TYPE, !takingPicture)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun onFileSaved(file: File) {
        outputFilePath = file.absolutePath
        val mimeType = if(takingPicture) "image/jpeg" else "video/mp4"
        MediaScannerConnection.scanFile(this, arrayOf(outputFilePath), arrayOf(mimeType), null)
        PreviewActivity.startActivityForResult(this, outputFilePath, !takingPicture, "完成")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            deniedPermission.clear()
            for (i: Int in permissions.indices) {
                val permission = permissions[i]
                val result = grantResults[i]
                if (result == PackageManager.PERMISSION_DENIED) {
                    deniedPermission.add(permission)
                }
            }
            if (deniedPermission.isEmpty()) {
                bindCameraX()
            } else {
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.capture_permission_message))
                    .setNegativeButton(getString(R.string.capture_permission_no)) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .setPositiveButton(getString(R.string.capture_permission_ok)) { _, _ ->
                        ActivityCompat.requestPermissions(this, deniedPermission.toTypedArray(), PERMISSION_CODE)
                    }.create().show()
            }
        }
    }

    // TODO: 2020/9/11 无法录制视频，api不稳定，等稳定后再改
    private fun bindCameraX() {
        executor = ContextCompat.getMainExecutor(this)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            //查询一下当前要使用的设备摄像头(比如后置摄像头)是否存在
                val hasCamera = cameraProvider?.hasCamera(cameraSelector) ?: false
            if (!hasCamera) {
                showToast("无可用的设备cameraId!,请检查设备的相机是否被占用")
                finish()
                return@addListener
            }

            // Preview
            val preview = Preview.Builder()
                //前后摄像头
                .setCameraSelector(cameraSelector)
                //宽高比
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                //旋转角度
                .setTargetRotation(Surface.ROTATION_0)
                //分辨率
//                .setTargetResolution(resolution)
                .build()

            imageCapture = ImageCapture.Builder()
                .setCameraSelector(cameraSelector)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(Surface.ROTATION_0)
//                .setTargetResolution(resolution)
                .build()

            videoCapture = VideoCapture.Builder()
                .setCameraSelector(cameraSelector)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(Surface.ROTATION_0)
//                .setTargetResolution(resolution)
                //视频帧率
                .setVideoFrameRate(25)
                //bit率
                .setBitRate(3 * 1024 * 1024)
                .build()


            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                cameraProvider?.bindToLifecycle(this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
//                cameraProvider?.bindToLifecycle(this,
//                    cameraSelector,
//                    preview,
//                    imageCapture,
//                    videoCapture)
//                cameraProvider?.bindToLifecycle(
//                    this, cameraSelector, preview)
                preview.setSurfaceProvider(binding.previewView.createSurfaceProvider())

            } catch (exc: Exception) {
                Log.e("CaptureActivity", "bindCameraX: ${exc.message}")
            }
        }, executor)
    }

    override fun onDestroy() {
        cameraProvider?.shutdown()
        super.onDestroy()
    }
}