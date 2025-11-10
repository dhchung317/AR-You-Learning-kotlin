package com.hyunki.aryoulearning2.ui.main.fragment.results

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.ui.main.fragment.results.rv.ResultsAdapter
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

//TODO- refactor resultsfragment
class ResultsFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory) : Fragment() {
    private lateinit var rainbowRatingBar: RatingBar
    private lateinit var categoryTextView: TextView
    private val modelMap = HashMap<String, Model>()
    private lateinit var shareFAB: FloatingActionButton
    private lateinit var backFAB: FloatingActionButton
    private lateinit var resultRV: RecyclerView
    private lateinit var pronunciationUtil: PronunciationUtil
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var viewModel: MainViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var navListener: NavListener

    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)

        if (context is NavListener) {
            navListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        pronunciationUtil = new PronunciationUtil();
        //        textToSpeech = pronunciationUtil.getTTS(getContext());
    }

    private fun initializeViews(view: View) {
        rainbowRatingBar = view.findViewById(R.id.rainbow_correctword_ratingbar)
        shareFAB = view.findViewById(R.id.share_info)
        backFAB = view.findViewById(R.id.back_btn)
        resultRV = view.findViewById(R.id.result_recyclerview)
        categoryTextView = view.findViewById(R.id.results_category)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity(), viewModelProviderFactory)
            .get(MainViewModel::class.java)
        progressBar = requireActivity().findViewById(R.id.progress_bar)
        initializeViews(view)
        setViews()
        renderModelList(viewModel.getModelLiveData().value!!)
    }

    private fun setViews() {
        displayRatingBarAttempts()
        //        categoryTextView.setText(MainActivityX.currentCategory);
        shareFAB.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.share_button_color))
        backFABClick()
        shareFABClick()
    }

    private fun setResultRV() {
        resultRV.adapter =
            ResultsAdapter(
                viewModel.getWordHistory(), modelMap,
//                pronunciationUtil, textToSpeech
            )
        resultRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

    }

    private fun shareFABClick() {
        shareFAB.setOnClickListener { v ->
            if (ContextCompat.checkSelfPermission(
                    v.context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    v.context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@ResultsFragment.requireActivity(),
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CODE
                )
                this@ResultsFragment.takeScreenshotAndShare(v)
            } else {
                this@ResultsFragment.takeScreenshotAndShare(v)
            }
        }
    }

    private fun backFABClick() {
        backFAB.setOnClickListener { v -> navListener.moveToListFragment() }

    }

    private fun allowOnFileUriExposed() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
    }

    private fun takeScreenshotAndShare(view: View) {
        allowOnFileUriExposed()
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache(true)
        val b = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = true
        saveBitmap(b)
    }

    private fun shareIt(imagePath: File) {
        val uri = Uri.fromFile(imagePath)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "")
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No App Available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val now = Date()
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)

        try {
            val mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"

            val imageFile = File(mPath)

            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            shareIt(imageFile)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun displayRatingBarAttempts() {
        rainbowRatingBar.numStars = viewModel.getWordHistory().size
        rainbowRatingBar.stepSize = 1f
        rainbowRatingBar.rating = getCorrectAnswerCount(viewModel.getWordHistory()).toFloat()
        rainbowRatingBar.setIsIndicator(true)
    }

    private fun renderModelList(state: MainState) {
        when (state) {

            is MainState.Loading -> showProgressBar(true)

            is MainState.Success.OnModelsLoaded -> {
                showProgressBar(false)
                val (models) = state
                for (i in models.indices) {
                    modelMap[models[i].name] = models[i]
                }
                setResultRV()
            }

            else -> showProgressBar(false)
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown();
    }

    private fun getCorrectAnswerCount(wordHistory: List<CurrentWord>): Int {
        var count = 0

        for (i in wordHistory.indices) {
            if (wordHistory[i].attempts.size < 1) {
                count++
            }
        }
        return count
    }

    companion object {
        private const val REQUEST_CODE = 1
    }
}
