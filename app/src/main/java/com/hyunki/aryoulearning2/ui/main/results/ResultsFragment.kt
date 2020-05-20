package com.hyunki.aryoulearning2.ui.main.results

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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.ui.main.State
import com.hyunki.aryoulearning2.ui.main.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.controller.NavListener
import com.hyunki.aryoulearning2.ui.main.results.rv.ResultsAdapter
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.hyunki.aryoulearning2.model.Model
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory

import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.HashMap
import java.util.Objects

import javax.inject.Inject

//TODO- refactor resultsfragment
class ResultsFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory) : Fragment() {
    private var rainbowRatingBar: RatingBar? = null
    private var categoryTextView: TextView? = null
    private val modelMap = HashMap<String, Model>()
    internal var shareFAB: FloatingActionButton
    internal var backFAB: FloatingActionButton
    private var resultRV: RecyclerView? = null
    private var pronunciationUtil: PronunciationUtil? = null
    private val textToSpeech: TextToSpeech? = null
    private var viewModel: MainViewModel? = null
    private var progressBar: ProgressBar? = null
    private var navListener: NavListener? = null

    override fun onAttach(context: Context) {
        (activity!!.application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)

        if (context is NavListener) {
            navListener = context
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, viewModelProviderFactory).get(MainViewModel::class.java)
        progressBar = activity!!.findViewById(R.id.progress_bar)
        initializeViews(view)
        setViews()
        renderModelList(viewModel!!.modelLiveData.value!!)
    }

    private fun setViews() {
        displayRatingBarAttempts()
        //        categoryTextView.setText(MainActivityX.currentCategory);
        shareFAB.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.share_button_color))
        backFABClick()
        shareFABClick()
    }

    private fun setResultRV() {
        resultRV!!.adapter = ResultsAdapter(viewModel!!.wordHistory, modelMap, pronunciationUtil, textToSpeech)
        resultRV!!.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

    }

    private fun shareFABClick() {
        shareFAB.setOnClickListener { v ->
            v = Objects.requireNonNull<FragmentActivity>(this@ResultsFragment.activity).getWindow().getDecorView().getRootView()
            if (ContextCompat.checkSelfPermission(v.context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(v.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@ResultsFragment.activity!!,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
                this@ResultsFragment.takeScreenshotAndShare(v)
            } else {
                this@ResultsFragment.takeScreenshotAndShare(v)
            }
        }
    }

    private fun backFABClick() {
        backFAB.setOnClickListener { v -> navListener!!.moveToListFragment() }

    }

    private fun allowOnFileUriExposed() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

        rainbowRatingBar!!.numStars = viewModel!!.wordHistory.size
        rainbowRatingBar!!.stepSize = 1f
        rainbowRatingBar!!.rating = getCorrectAnswerCount(viewModel!!.wordHistory).toFloat()
        rainbowRatingBar!!.setIsIndicator(true)
    }

    private fun renderModelList(state: State) {
        Log.d("results", "renderModelList: " + state.javaClass)
        if (state === State.Loading) {
            progressBar!!.bringToFront()
            showProgressBar(true)

        } else if (state === State.Error) {
            showProgressBar(false)

        } else if (state.javaClass == State.Success.OnModelsLoaded::class.java) {
            showProgressBar(false)
            val (models) = state as State.Success.OnModelsLoaded
            for (i in 0 until models.size) {
                modelMap[models[i].name] = models[i]
            }
            Log.d("resultsAdapter", "renderModelList: " + models.size)
            setResultRV()
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            progressBar!!.visibility = View.VISIBLE
        } else {
            progressBar!!.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //        textToSpeech.shutdown();
        pronunciationUtil = null
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
        private val REQUEST_CODE = 1
    }
}
