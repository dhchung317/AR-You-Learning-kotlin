package com.hyunki.aryoulearning2.ui.main.fragment.results

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.databinding.FragmentResultsBinding
import com.hyunki.aryoulearning2.ui.main.fragment.ar.GameViewModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.ui.main.fragment.results.rv.ResultsAdapter
import com.hyunki.aryoulearning2.ui.main.fragment.results.util.captureLayout
import com.hyunki.aryoulearning2.ui.main.fragment.results.util.combineBitmaps
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import androidx.core.graphics.createBitmap
import com.hyunki.aryoulearning2.ui.main.fragment.results.util.captureRv

// TODO: fix results: utilize wordhistory from gameviewmodel
class ResultsFragment @Inject
constructor() : Fragment() {
    @Inject
    lateinit var viewModelProviderFactory: ViewModelProviderFactory
    private val gameViewModel: GameViewModel by activityViewModels { viewModelProviderFactory }
    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private lateinit var rainbowRatingBar: RatingBar
    private lateinit var categoryTextView: TextView
    private lateinit var shareFAB: FloatingActionButton
    private lateinit var backFAB: FloatingActionButton
    private lateinit var resultRV: RecyclerView
    private lateinit var navListener: NavListener

    // TODO: implement pronunciation
    // private lateinit var pronunciationUtil: PronunciationUtil
    // private lateinit var textToSpeech: TextToSpeech

    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)

        if (context is NavListener) {
            navListener = context
        }
    }

// TODO: pronunciation
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        pronunciationUtil = new PronunciationUtil ();
//        textToSpeech = pronunciationUtil.getTTS(getContext());
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        setViews()
        setResultRV()
    }

    private fun initializeViews() {
        rainbowRatingBar = binding.rainbowCorrectwordRatingbar
        shareFAB = binding.shareInfo
        backFAB = binding.backBtn
        resultRV = binding.resultRecyclerview
        categoryTextView = binding.resultsCategory
    }

    private fun setViews() {
        displayRatingBarAttempts()
        shareFAB.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.share_button_color
                )
            )
        backFABClick()
        shareFABClick()
    }

    private fun setResultRV() {
        val adapter = ResultsAdapter(
            // TODO: pronunciation
            // pronunciationUtil, textToSpeech
        )
        resultRV.adapter = adapter
        resultRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter.submitList(gameViewModel.wordHistory)
    }

    private fun shareFABClick() {
        Log.d("share fab click", "ran")
        shareFAB.setOnClickListener { v ->
            if (ContextCompat.checkSelfPermission(
                    v.context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    v.context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("share fab click permissions", "no no no")
                ActivityCompat.requestPermissions(
                    this@ResultsFragment.requireActivity(),
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CODE
                )
                this@ResultsFragment.takeScreenshotAndShare(v.rootView)
            } else {
                Log.d("share fab click permissions", "yea")
                this@ResultsFragment.takeScreenshotAndShare(v.rootView)
            }
        }
    }

    private fun backFABClick() {
        backFAB.setOnClickListener { navListener.moveToListFragment() }
    }

    private fun takeFullScreenshotAndShare() {
        val header = binding.header
        val list = binding.resultRecyclerview
        if (header?.width == 0 || list.width == 0) {
            header?.post { takeFullScreenshotAndShare() }
            return
        }

        val headerBmp = captureLayout(header)
        val listBmp = captureRv(list)

        val finalBmp = combineBitmaps(headerBmp, listBmp)

        saveBitmap(finalBmp) // your save/share function
    }

    private fun takeScreenshotAndShare(view: View) {
        // Ensure view has been laid out
        if (view.width == 0 || view.height == 0) {
            view.post { takeScreenshotAndShare(view) }
            return
        }

        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)

        // Optional: fill background white so JPEG doesn't turn transparency black
        canvas.drawColor(Color.WHITE)

        view.draw(canvas)

        saveBitmap(bitmap)
    }

    private fun shareIt(imageFile: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Share screenshot"))
    }

//    private fun shareIt(imagePath: File) {
//        Log.d("share it", "ran")
//        val uri = Uri.fromFile(imagePath)
//        val intent = Intent()
//        intent.action = Intent.ACTION_SEND
//        intent.type = "image/*"
//        intent.putExtra(Intent.EXTRA_SUBJECT, "")
//        intent.putExtra(Intent.EXTRA_TEXT, "")
//        intent.putExtra(Intent.EXTRA_STREAM, uri)
//        try {
//            Log.d("share it", "share screen")
//            startActivity(Intent.createChooser(intent, "Share Screenshot"))
//        } catch (e: ActivityNotFoundException) {
//            Log.e("error in results fragment:", e.message.toString())
//            Toast.makeText(context, "No App Available", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun saveBitmap(bitmap: Bitmap) {
        val fileName = "screenshot_${System.currentTimeMillis()}.jpg"
        val imageFile =
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)

        runCatching {
            FileOutputStream(imageFile).use { out ->
                val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                if (!ok) error("Bitmap.compress() failed")
            }
        }.onSuccess {
            shareIt(imageFile)
        }.onFailure {
            Log.e("saveBitmap", "Failed saving screenshot", it)
        }
    }

    private fun displayRatingBarAttempts() {
        rainbowRatingBar.numStars = gameViewModel.wordHistory.size
        rainbowRatingBar.stepSize = 1f
        rainbowRatingBar.rating = getCorrectAnswerCount(gameViewModel.wordHistory).toFloat()
        rainbowRatingBar.setIsIndicator(true)
    }

//    private fun showProgressBar(isVisible: Boolean) {
//        if (isVisible) {
//            (requireActivity() as MainActivity).showProgressBar(true)
//        } else {
//            (requireActivity() as MainActivity).showProgressBar(false)
//        }
//    }

    private fun getCorrectAnswerCount(wordHistory: List<CurrentWord>): Int {
        var count = 0

        for (i in wordHistory.indices) {
            if (wordHistory[i].attempts.isEmpty()) {
                count++
            }
        }
        return count
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE = 1
    }
}
