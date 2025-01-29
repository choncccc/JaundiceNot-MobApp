package com.example.jaundicednot

import ApiService
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private lateinit var switchCameraButton: ImageButton
    private lateinit var resultTextView: TextView
    private lateinit var imageCapture: ImageCapture
    private lateinit var pickImage: ImageButton
    private var isFrontCamera = true

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        previewView = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.captureButton)
        switchCameraButton = findViewById(R.id.switchCameraButton)
        resultTextView = findViewById(R.id.tvRes)
        pickImage = findViewById(R.id.pickImageButton)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        captureButton.setOnClickListener { captureImage() }
        switchCameraButton.setOnClickListener {
            isFrontCamera = !isFrontCamera
            startCamera()
        }

        pickImage.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmap = imageProxy.toBitmap()
                    imageProxy.close()
                    bitmap?.let { processImage(it) }
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            })
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val bitmap = uriToBitmap(uri)
                bitmap?.let { processImage(it) }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun processImage(bitmap: Bitmap) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()

        val image = InputImage.fromBitmap(bitmap, 0)
        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
                    val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)

                    if (leftEye != null && rightEye != null) {
                        val eyesBitmap = cropBothEyes(bitmap, leftEye, rightEye)
                        showEyeballInDialog(eyesBitmap)
                    }
                }
            }
            .addOnFailureListener { it.printStackTrace() }
    }

    private fun cropBothEyes(bitmap: Bitmap, leftEye: FaceLandmark, rightEye: FaceLandmark): Bitmap {
        val leftX = leftEye.position.x.toInt()
        val leftY = leftEye.position.y.toInt()
        val rightX = rightEye.position.x.toInt()
        val rightY = rightEye.position.y.toInt()

        val paddingX = 70
        val paddingY = 60

        val cropLeft = minOf(leftX, rightX) - paddingX
        val cropTop = minOf(leftY, rightY) - paddingY
        val cropRight = maxOf(leftX, rightX) + paddingX
        val cropBottom = maxOf(leftY, rightY) + paddingY

        val cropWidth = cropRight - cropLeft
        val cropHeight = cropBottom - cropTop

        return Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropWidth, cropHeight)
    }

    private fun showEyeballInDialog(eyeballBitmap: Bitmap) {
        val imageView = ImageView(this).apply {
            setImageBitmap(eyeballBitmap)
            layoutParams = LinearLayout.LayoutParams(600, 300).apply {
                setMargins(10, 10, 10, 10)
            }
        }

        AlertDialog.Builder(this)
            .setView(imageView)
            .setPositiveButton("OK") { _, _ -> sendImageToServer(eyeballBitmap) }
            .show()
    }

    private fun sendImageToServer(bitmap: Bitmap) {
        val file = File(cacheDir, "eyeball.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.100.7:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val requestBody = file.asRequestBody("image/png".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

        apiService.uploadImage(multipartBody).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                response.body()?.let {
                    runOnUiThread { resultTextView.text = "Prediction: ${it.prediction}" }
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                runOnUiThread { resultTextView.text = "Error: ${t.message}" }
            }
        })
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
