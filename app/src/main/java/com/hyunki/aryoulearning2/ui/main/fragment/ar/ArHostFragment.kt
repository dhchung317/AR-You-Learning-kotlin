package com.hyunki.aryoulearning2.ui.main.fragment.ar

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
//import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.animation.Animations
import com.hyunki.aryoulearning2.animation.LottieHelper
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.ui.main.fragment.ar.controller.GameCommandListener
import com.hyunki.aryoulearning2.ui.main.fragment.ar.controller.GameManager
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt
import com.hyunki.aryoulearning2.databinding.ActivityArfragmentHostBinding
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.ui.main.MainActivity
import androidx.core.view.isNotEmpty

class ArHostFragment @Inject
constructor(private var pronunciationUtil: PronunciationUtil?) : Fragment(), GameCommandListener {
    private var _binding: ActivityArfragmentHostBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProviderFactory

    @Inject
    lateinit var lottieHelper: LottieHelper

    @Inject
    lateinit var application: Application
    private lateinit var modelUtil: ModelUtil
    private lateinit var modelList: List<Model>
    private lateinit var arViewModel: ArViewModel
    private lateinit var arFragment: ArGameFragment
    private lateinit var gameManager: GameManager
    private lateinit var listener: NavListener
    //TODO: media player
    //    private lateinit var playBalloonPop: MediaPlayer
    private lateinit var frameLayout: FrameLayout
    private lateinit var wordContainer: LinearLayout
    private lateinit var wordValidatorLayout: View
    private lateinit var wordCardView: CardView
    private lateinit var wordValidatorCv: CardView
    private lateinit var wordValidator: TextView
    private lateinit var validatorWord: TextView
    private lateinit var validatorWrongWord: TextView
    private lateinit var validatorWrongPrompt: TextView
    private lateinit var validatorImage: ImageView
    private lateinit var validatorBackgroundImage: ImageView
    private lateinit var validatorOkButton: Button
    private lateinit var undo: ImageButton
    private lateinit var exitMenu: View
    private lateinit var exit: ImageButton
    private lateinit var exitYes: Button
    private lateinit var exitNo: Button
    private lateinit var tapAnimation: LottieAnimationView
    private lateinit var fadeIn: ObjectAnimator
    private lateinit var fadeOut: ObjectAnimator
    private lateinit var gestureDetector: GestureDetector
    private lateinit var base: Node
    private var mainAnchor: Anchor? = null
    private var mainAnchorNode: AnchorNode? = null
    private lateinit var mainHit: HitResult
    private lateinit var category: String
    private var hasPlacedGame = false
    private var placedAnimation = false
    private var modelMapList: List<MutableMap<String, ModelRenderable>> = ArrayList()
    private var letterMap = mutableMapOf<String, ModelRenderable>()

    //TODO implement text to speech
//    private val textToSpeech: TextToSpeech
//    init {
//        this.textToSpeech = pronunciationUtil.textToSpeech
//    }

    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        if (context is NavListener) {
            listener = context
        }
        //        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    //TODO implement audio effects
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpResultListener()
        //        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        //        playBalloonPop = MediaPlayer.create(getContext(), R.raw.pop_effect);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityArfragmentHostBinding.inflate(inflater, container, false)
        arFragment =
            childFragmentManager.findFragmentById(R.id.ux_fragment).let { it as ArGameFragment }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        frameLayout = binding.frameLayout


        setUpViews(view)
        setAnimations()

        gestureDetector = getGestureDetector()
        arViewModel = ViewModelProvider(this, viewModelProviderFactory).get(ArViewModel::class.java)
        setUpARScene(arFragment)
        runViewModel(arViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasPlacedGame = false
        placedAnimation = false
    }

    //TODO implement audio effects shutdown
    override fun onDestroy() {
        super.onDestroy()
//        textToSpeech.shutdown()
        pronunciationUtil = null
        //        playBalloonPop.reset();
        //        playBalloonPop.release();
    }

    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startDefaultCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Go to settings and enable camera permission to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun checkPermission() {

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startDefaultCamera()
            }

            else -> {
                cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
            }
        }
//        return ContextCompat.checkSelfPermission(
//            requireContext(), Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startDefaultCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)
        }
    }

    override fun startNextGame(modelKey: String) {
        refreshModelResources()

        mainAnchor = mainHit.createAnchor()
        mainAnchorNode = AnchorNode(mainAnchor)
        mainAnchorNode!!.setParent(arFragment.arSceneView.scene)

        for (i in modelMapList.indices) {
            for ((key, value) in modelMapList[i]) {
                if (key == modelKey) {
                    createSingleGame(value, key)
                }
            }
        }
        wordContainer.removeAllViews()
    }

    //TODO("show validator card with data from game manager")
    override fun showCard(isCorrect: Boolean) {
        validatorWord.text = gameManager.currentWord.answer
        validatorWrongWord.visibility = View.INVISIBLE
        validatorWrongPrompt.visibility = View.INVISIBLE
        Picasso.get().load(gameManager.currentWord.image).into(validatorImage)
        when (isCorrect) {
            true -> setUpCardWithCorrectValidators()
            else -> setUpCardWithInorrectValidators()
        }
        validatorOkButton.setOnClickListener {
            onHidingCard(isCorrect)
            fadeOut.startDelay = 500
            fadeOut.start()
        }
        fadeIn.start()
    }

    override fun onHidingCard(wasCorrect: Boolean) {
        gameManager.onHidingCard(wasCorrect)
    }

    private fun setUpCardWithCorrectValidators() {
        //setup views to validate that the user is correct
        validatorBackgroundImage.setImageResource(R.drawable.star)
    }

    private fun setUpCardWithInorrectValidators() {
        //setup views to inform the user they were incorrect
        validatorBackgroundImage.setImageResource(R.drawable.error)
        validatorWrongWord.visibility = View.VISIBLE
        validatorWrongPrompt.visibility = View.VISIBLE
    }

    private fun setUpViews(view: View) {
        initViews(view)
        setListeners()
        //        setAnimations();
    }

    //TODO fix/refactor validator cards that show results for every round
    private fun initViews(view: View) {
        exit = binding.exitImageButton
        undo = binding.buttonUndo
        wordCardView = binding.cardWordContainer
        wordContainer = binding.wordContainer
//        wordValidatorLayout = layoutInflater.inflate(R.layout.validator_card, frameLayout, false)
        wordValidatorCv = binding.validatorCard.wordValidatorCv
        wordValidator = binding.validatorCard.wordValidator
        validatorImage = binding.validatorCard.validatorImageView
        validatorBackgroundImage = binding.validatorCard.correctStarImageView
        validatorWord = binding.validatorCard.validatorWord
        validatorWrongPrompt = binding.validatorCard.validatorIncorrectPrompt
        validatorWrongWord = binding.validatorCard.validatorWrongWord
        validatorOkButton = binding.validatorCard.buttonValidatorOk

        exitMenu = binding.exitLayout.exitCard
        exitYes = binding.exitLayout.exitButtonYes
        exitNo = binding.exitLayout.exitButtonNo

        wordValidatorCv.visibility = View.INVISIBLE;
    }

    private fun setListeners() {
        exit.setOnClickListener { frameLayout.addView(exitMenu) }
        exitYes.setOnClickListener { listener.moveToListFragment() }
        exitNo.setOnClickListener { frameLayout.removeView(exitMenu) }
        undo.setOnClickListener { undoLastLetter() }
    }

    private fun getGestureDetector(): GestureDetector {
        return GestureDetector(
            activity,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    onSingleTap(e)
                    return true
                }

                override fun onDown(e: MotionEvent): Boolean {
                    onSingleTap(e)
                    return true
                }
            })
    }

    private fun setUpARScene(arFragment: ArFragment) {
        setOnTouchListener(arFragment)
        setAddOnUpdateListener(arFragment)
    }

    private fun setOnTouchListener(arFragment: ArFragment) {
        val scene = arFragment.arSceneView.scene
        scene.setOnTouchListener { _: HitTestResult, event: MotionEvent ->
            if (!hasPlacedGame) {
                gestureDetector.onTouchEvent(event)
            }
            false
        }
    }

    private fun setAddOnUpdateListener(arFragment: ArFragment) {
        val scene = arFragment.arSceneView.scene
        scene.addOnUpdateListener { _ ->
            val frame = arFragment.arSceneView.arFrame ?: return@addOnUpdateListener

            if (frame.camera.trackingState != TrackingState.TRACKING) {
                return@addOnUpdateListener
            }
            if (!hasPlacedGame) {
                for (plane in frame.getUpdatedTrackables(Plane::class.java)) {
                    if (!placedAnimation && plane.trackingState == TrackingState.TRACKING) {
                        placedAnimation = true
                        tapAnimation = lottieHelper.getAnimationView(
                            application,
                            LottieHelper.AnimationType.TAP
                        )
                        val lav = lottieHelper.getTapAnimationToScreen(
                            tapAnimation,
                            requireActivity().window.decorView.width,
                            requireActivity().window.decorView.height
                        )
                        frameLayout.addView(lav, 500, 500)
                    }
                }
            }
        }
    }


    private fun onSingleTap(tap: MotionEvent) {

        if (!arViewModel.isLettersLoaded() || !arViewModel.isModelsLoaded()) {
            // We can't do anything yet.
            return
        }
        val frame = arFragment.arSceneView.arFrame

        if (frame != null) {
            if (!hasPlacedGame && tryPlaceGame(tap, frame)) {
                hasPlacedGame = true
                frameLayout.removeView(tapAnimation)
            }
        }
    }

    private fun tryPlaceGame(tap: MotionEvent?, frame: Frame): Boolean {
        if (tap != null && frame.camera.trackingState == TrackingState.TRACKING) {
            mainHit = frame.hitTest(tap)[0]
            val trackable = mainHit.trackable

            if (trackable is Plane && trackable.isPoseInPolygon(mainHit.hitPose)) {
                gameManager = GameManager(modelList, this, listener)
                modelUtil = gameManager.modelUtil
                // Create the Anchor.
                if (trackable.getTrackingState() == TrackingState.TRACKING) {
                    mainAnchor = mainHit.createAnchor()
                }

                mainAnchorNode = AnchorNode(mainAnchor)
                mainAnchorNode!!.setParent(arFragment.arSceneView.scene)

                val modelKey = gameManager.getCurrentWordAnswer()

                wordCardView.visibility = View.VISIBLE

                for (i in modelMapList.indices) {
                    for ((key, value) in modelMapList[i]) {
                        if (key == modelKey) {
                            createSingleGame(value, key)
                        }
                    }
                }

                return true
            }
        }
        return false
    }

    private fun runViewModel(arViewModel: ArViewModel) {
        arViewModel.getModelLiveData().observe(
            viewLifecycleOwner,
            Observer { models -> processModelData(models) })
        arViewModel.getFutureModelMapListLiveData().observe(
            viewLifecycleOwner,
            Observer { mapList -> processFutureModelMapList(mapList) })
        arViewModel.getFutureLetterMapLiveData().observe(
            viewLifecycleOwner,
            Observer { map -> processFutureLetterMap(map) })
        arViewModel.getModelMapListLiveData().observe(
            viewLifecycleOwner,
            Observer { mapList -> processModelMapList(mapList) })
        arViewModel.getLetterMapLiveData().observe(
            viewLifecycleOwner,
            Observer { map -> processLetterMap(map) })
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            (requireActivity() as MainActivity).showProgressBar(true)
        } else {
            (requireActivity() as MainActivity).showProgressBar(false)
        }
    }

    private fun processModelData(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Success.OnModelsLoaded -> {
                showProgressBar(false)
                modelList = state.models
                arViewModel.loadListofMapsOfFutureModels(Single.just(state.models))
            }

            else -> showProgressBar(false)
        }
    }

    private fun processFutureModelMapList(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Success.OnFutureModelMapListLoaded -> {
                showProgressBar(false)
                arViewModel.loadMapOfFutureLetters(Observable.just(state.futureModelMapList))
                arViewModel.loadModelRenderables(Observable.just(state.futureModelMapList))
            }

            else -> showProgressBar(false)
        }
    }

    private fun processFutureLetterMap(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Success.OnFutureLetterMapLoaded -> {
                showProgressBar(false)
                arViewModel.loadLetterRenderables(Observable.just(state.futureLetterMap))
            }

            else -> showProgressBar(false)
        }
    }

    private fun processModelMapList(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Success.OnModelMapListLoaded -> {
                showProgressBar(false)
                modelMapList = state.modelMap
            }

            else -> showProgressBar(false)
        }
    }

    private fun processLetterMap(state: ArState) {
        when (state) {
            is ArState.Loading -> showProgressBar(true)
            is ArState.Success.OnLetterMapLoaded -> {
                showProgressBar(false)
                letterMap = state.letterMap
            }

            else -> showProgressBar(false)
        }
    }

//TODO - refactor animations to separate class
    private fun setAnimations() {
        fadeIn = Animations.Normal().setCardFadeInAnimator(wordValidatorCv)

        fadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                frameLayout.addView(wordValidatorLayout)
                validatorOkButton.isClickable = false
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                validatorOkButton.isClickable = true
            }
        })

        fadeOut = Animations.Normal().setCardFadeOutAnimator(wordValidatorCv)
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                frameLayout.removeView(wordValidatorLayout)

//                if (roundCounter < roundLimit && roundCounter < modelMapListLiveData.size()) {
//                    createNextGame(modelMapListLiveData.get(roundCounter));
//                } else {
//                    moveToReplayFragment();
//                }
            }
        })
    }

    private fun createSingleGame(mainModel: ModelRenderable, name: String) {
        base = modelUtil.getGameAnchor(mainModel)
        mainAnchorNode?.addChild(base)
        placeLetters(name)
    }

    private fun connectAnchorToBase(anchorNode: AnchorNode) {
        arFragment.arSceneView.scene.addChild(anchorNode)
        base.addChild(anchorNode)
    }

    private fun addLetterToWordContainer(letter: String) {
        val ballonTF = ResourcesCompat.getFont(requireActivity(), R.font.balloon)
        val t = TextView(activity)
        t.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        t.typeface = ballonTF
        t.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
        t.textSize = 100f
        t.text = letter
        t.textAlignment = View.TEXT_ALIGNMENT_CENTER
        wordContainer.addView(t)
    }

    private fun placeSingleLetter(letter: String) {
        val letterAnchorNode = modelUtil.getLetter(base, letterMap[letter], arFragment)
        letterAnchorNode.children[0].setOnTapListener(
            getNodeOnTapListener(
                letter,
                letterAnchorNode
            )
        )
        connectAnchorToBase(letterAnchorNode)
    }

    private fun placeLetters(word: String) {
        for (letter in word) {
            placeSingleLetter(
                letter.toString()
            )
        }
    }

    private fun addLetterToWordBox(letter: String) {
        addLetterToWordContainer(letter)
    }

    private fun getLetterTapAnimation(isCorrect: Boolean): LottieAnimationView {
        return when (isCorrect) {
            true -> {
                lottieHelper.getAnimationView(
                    activity,
                    LottieHelper.AnimationType.SPARKLES
                )
            }

            else -> {
                lottieHelper.getAnimationView(
                    activity,
                    LottieHelper.AnimationType.ERROR
                )
            }
        }
    }

    private fun undoLastLetter() {
        val erasedLetter = gameManager.subtractLetterFromAttempt()
        recreateErasedLetter(erasedLetter)
        if (wordContainer.isNotEmpty()) {
            wordContainer.removeViewAt(wordContainer.childCount - 1)
        }
    }

    private fun recreateErasedLetter(letterToRecreate: String) {
        if (letterToRecreate != "") {
            placeSingleLetter(letterToRecreate)
        }
    }

    private fun getNodeOnTapListener(
        letterString: String,
        letterAnchorNode: AnchorNode
    ): Node.OnTapListener {
        return Node.OnTapListener { _, motionEvent ->
            addLetterToWordBox(letterString)
            letterAnchorNode.anchor?.detach()

            val isCorrect = gameManager.addTappedLetterToCurrentWordAttempt(letterString)

            val lav =
                lottieHelper.getAnimationViewOnTopOfLetter(
                    getLetterTapAnimation(isCorrect),
                    (motionEvent.x - 7).roundToInt(),
                    (motionEvent.y + 7).roundToInt()
                )

            frameLayout.addView(lav, 300, 300)
            lav.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    frameLayout.removeView(lav)
                }
            })
            gameManager.onWordAnswered()
        }
    }

    private fun refreshModelResources() {
        mainAnchorNode?.anchor?.detach()
        mainAnchor = null
        mainAnchorNode = null
    }

    private fun setUpResultListener() {
        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            this,
            FragmentResultListener { requestKey, result ->
                onFragmentResult(requestKey, result)
            })
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        if (REQUEST_KEY == requestKey) {
            category = result.getString(KEY_ID) ?: "Animals"
            arViewModel.fetchModelsFromRepository(category)
        }
    }

    companion object {
        const val REQUEST_KEY = "get-current-category"
        const val KEY_ID = "current-category"
    }
}